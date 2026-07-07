package com.example.ui

import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.ChatMessage
import com.example.data.ChatRepository
import com.example.data.ChatSession
import com.example.util.ImageSaver
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.util.Locale

enum class InputMode {
    CHAT, IMAGE, EDIT
}

enum class FontSize(val scale: Float, val label: String) {
    SMALL(0.85f, "Small"),
    MEDIUM(1.0f, "Medium"),
    LARGE(1.15f, "Large")
}

class ChatViewModel(application: Application) : AndroidViewModel(application), TextToSpeech.OnInitListener {

    private val db = AppDatabase.getDatabase(application)
    private val repository = ChatRepository(db.chatDao())
    private val prefs: SharedPreferences = application.getSharedPreferences("infinite_ai_prefs", Context.MODE_PRIVATE)

    // Search Query
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // Filter by Favorites Only
    private val _showFavoritesOnly = MutableStateFlow(false)
    val showFavoritesOnly: StateFlow<Boolean> = _showFavoritesOnly.asStateFlow()

    // Sessions flow with live filtering for Search and Favorites
    val sessions: StateFlow<List<ChatSession>> = combine(
        repository.allSessions,
        _searchQuery,
        _showFavoritesOnly
    ) { allSessions, query, favOnly ->
        var list = allSessions
        if (favOnly) {
            list = list.filter { it.isFavorite }
        }
        if (query.isNotEmpty()) {
            list = list.filter { it.title.contains(query, ignoreCase = true) }
        }
        list
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private val _currentSession = MutableStateFlow<ChatSession?>(null)
    val currentSession: StateFlow<ChatSession?> = _currentSession.asStateFlow()

    // Dynamically retrieve messages of the currently selected session
    val messages: StateFlow<List<ChatMessage>> = _currentSession
        .flatMapLatest { session ->
            if (session != null) {
                repository.getMessagesForSession(session.id)
            } else {
                flowOf(emptyList())
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _inputText = MutableStateFlow("")
    val inputText: StateFlow<String> = _inputText.asStateFlow()

    private val _inputMode = MutableStateFlow(InputMode.CHAT)
    val inputMode: StateFlow<InputMode> = _inputMode.asStateFlow()

    // Selected image for Image Editing / Multimodal inputs
    private val _selectedImageUri = MutableStateFlow<Uri?>(null)
    val selectedImageUri: StateFlow<Uri?> = _selectedImageUri.asStateFlow()

    private val _isGenerating = MutableStateFlow(false)
    val isGenerating: StateFlow<Boolean> = _isGenerating.asStateFlow()

    private val _isGeneratingImage = MutableStateFlow(false)
    val isGeneratingImage: StateFlow<Boolean> = _isGeneratingImage.asStateFlow()

    private val _toastMessage = MutableSharedFlow<String>()
    val toastMessage: SharedFlow<String> = _toastMessage.asSharedFlow()

    private val _downloadingMessageId = MutableStateFlow<Long?>(null)
    val downloadingMessageId: StateFlow<Long?> = _downloadingMessageId.asStateFlow()

    // --- Voice / TTS and Settings States ---
    private var tts: TextToSpeech? = null
    private val _isTtsReady = MutableStateFlow(false)
    val isTtsReady: StateFlow<Boolean> = _isTtsReady.asStateFlow()

    private val _currentlySpeakingMsgId = MutableStateFlow<Long?>(null)
    val currentlySpeakingMsgId: StateFlow<Long?> = _currentlySpeakingMsgId.asStateFlow()

    private var speechRecognizer: SpeechRecognizer? = null
    private val _isSpeechListening = MutableStateFlow(false)
    val isSpeechListening: StateFlow<Boolean> = _isSpeechListening.asStateFlow()

    // Settings flows
    private val _themeMode = MutableStateFlow(prefs.getString("pref_theme", "Dark") ?: "Dark")
    val themeMode: StateFlow<String> = _themeMode.asStateFlow()

    private val _fontSizeState = MutableStateFlow(
        FontSize.valueOf(prefs.getString("pref_font_size", "MEDIUM") ?: "MEDIUM")
    )
    val fontSizeState: StateFlow<FontSize> = _fontSizeState.asStateFlow()

    private val _appLanguage = MutableStateFlow(prefs.getString("pref_language", "English") ?: "English")
    val appLanguage: StateFlow<String> = _appLanguage.asStateFlow()

    init {
        // Automatically select the latest session on startup, if available
        viewModelScope.launch {
            val existingSessions = repository.allSessions.first()
            if (existingSessions.isNotEmpty()) {
                _currentSession.value = existingSessions.first()
            }
        }
        
        // Initialize real Android TextToSpeech engine
        try {
            tts = TextToSpeech(application, this)
        } catch (e: Exception) {
            Log.e("ChatViewModel", "Failed to initialize TTS", e)
        }

        // Initialize Android SpeechRecognizer if available on device
        if (SpeechRecognizer.isRecognitionAvailable(application)) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(application)
        }
    }

    fun setInputText(text: String) {
        _inputText.value = text
    }

    fun setInputMode(mode: InputMode) {
        _inputMode.value = mode
        if (mode != InputMode.EDIT) {
            _selectedImageUri.value = null
        }
    }

    fun setSelectedImageUri(uri: Uri?) {
        _selectedImageUri.value = uri
        if (uri != null) {
            _inputMode.value = InputMode.EDIT
        }
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun toggleFavoritesFilter() {
        _showFavoritesOnly.value = !_showFavoritesOnly.value
    }

    fun selectSession(session: ChatSession?) {
        _currentSession.value = session
    }

    fun createNewEmptySession() {
        _currentSession.value = null
        _inputText.value = ""
        _selectedImageUri.value = null
        _inputMode.value = InputMode.CHAT
    }

    fun toggleSessionFavorite(session: ChatSession) {
        viewModelScope.launch {
            val updated = session.copy(isFavorite = !session.isFavorite)
            repository.updateSession(updated)
            if (_currentSession.value?.id == session.id) {
                _currentSession.value = updated
            }
            _toastMessage.emit(if (updated.isFavorite) "Added to Favorites" else "Removed from Favorites")
        }
    }

    fun deleteSession(session: ChatSession) {
        viewModelScope.launch {
            repository.deleteSession(session)
            if (_currentSession.value?.id == session.id) {
                // Select another session if available
                val remaining = repository.allSessions.first()
                _currentSession.value = remaining.firstOrNull()
            }
            _toastMessage.emit("Session deleted")
        }
    }

    // Identifies if prompt requests identity and returns the overridden response if true.
    private fun getIdentityOverrideResponse(prompt: String): String? {
        val normalized = prompt.lowercase(Locale.ROOT).trim()
        val matchQueries = listOf(
            "what is your name",
            "what's your name",
            "who are you",
            "who created you",
            "who developed you",
            "who is your developer",
            "who is your creator"
        )
        return if (matchQueries.any { normalized.contains(it) }) {
            "My name is InfiniteAI. I was created and developed by JK QuantumTech."
        } else {
            null
        }
    }

    fun handleSend() {
        val prompt = _inputText.value.trim()
        val currentImageUri = _selectedImageUri.value
        
        if (prompt.isEmpty() && currentImageUri == null) return

        _inputText.value = ""
        _selectedImageUri.value = null // Reset selected image after sending
        val currentMode = _inputMode.value

        viewModelScope.launch {
            var session = _currentSession.value
            
            // 1. Materialize session if none is currently selected
            if (session == null) {
                val sessionTitle = if (prompt.length > 24) prompt.take(22) + "..." else if (prompt.isNotEmpty()) prompt else "AI Art Creation"
                val newId = repository.createNewSession(sessionTitle)
                session = ChatSession(id = newId, title = sessionTitle)
                _currentSession.value = session
            } else if (session.title == "New Conversation" || session.title.contains("My Application")) {
                // Update title to match the first prompt
                val sessionTitle = if (prompt.length > 24) prompt.take(22) + "..." else if (prompt.isNotEmpty()) prompt else "AI Art Creation"
                val updated = session.copy(title = sessionTitle)
                repository.updateSession(updated)
                _currentSession.value = updated
            }

            val sessionId = session.id

            // Identify special identity queries
            val identityResponse = getIdentityOverrideResponse(prompt)

            if (currentMode == InputMode.EDIT && currentImageUri != null) {
                // --- IMAGE EDITING PIPELINE ---
                _isGeneratingImage.value = true
                val promptText = if (prompt.isEmpty()) "Improve image quality" else prompt
                
                // Add user request with original loaded image
                repository.addMessage(
                    sessionId = sessionId,
                    role = "user",
                    text = promptText,
                    type = "edit",
                    localImageUri = currentImageUri.toString()
                )

                _toastMessage.emit("Analyzing image & designing your masterpiece...")

                // 1. Send input image + instruction to Gemini to design a stunning generative prompt matching the edit context
                val enhancedPrompt = repository.generateEditPrompt(getApplication(), currentImageUri, promptText)
                Log.d("ChatViewModel", "Enhanced prompt: $enhancedPrompt")

                // 2. Generate new matching Pollinations image based on enhanced prompt
                val finalImageUrl = repository.generateImageUrl(enhancedPrompt)

                // Add edited result as assistant message
                repository.addMessage(
                    sessionId = sessionId,
                    role = "assistant",
                    text = finalImageUrl,
                    type = "image",
                    generatedImageUrl = finalImageUrl
                )
                
                _isGeneratingImage.value = false
                _inputMode.value = InputMode.CHAT // Reset input mode to chat
            } else if (currentMode == InputMode.IMAGE || prompt.startsWith("Create an image of", ignoreCase = true)) {
                // --- AI ARTIST IMAGE GENERATION ---
                _isGeneratingImage.value = true
                val promptText = if (prompt.startsWith("Create an image of ", ignoreCase = true)) {
                    prompt.substring(19)
                } else {
                    prompt
                }
                
                // Add user prompt to history
                repository.addMessage(sessionId, "user", promptText, "text")

                // Generate Pollinations image URL
                val imageUrl = repository.generateImageUrl(promptText)
                
                // Add assistant response with type "image"
                repository.addMessage(
                    sessionId = sessionId, 
                    role = "assistant", 
                    text = imageUrl, 
                    type = "image",
                    generatedImageUrl = imageUrl
                )
                
                _isGeneratingImage.value = false
            } else {
                // --- CHAT GENERATION ---
                _isGenerating.value = true
                
                // Add user message to history
                repository.addMessage(sessionId, "user", prompt, "text")

                if (identityResponse != null) {
                    // Instantly answer using the local identity declaration!
                    repository.addMessage(sessionId, "assistant", identityResponse, "text")
                } else {
                    // Call Gemini API with strict system instructions asserting InfiniteAI name & JK QuantumTech developers
                    val systemInstruction = """
                        You are InfiniteAI, a highly advanced personal AI assistant. 
                        Your name is InfiniteAI. 
                        You were created and developed by JK QuantumTech. 
                        Whenever asked "What is your name?", "Who are you?", "Who created you?", or "Who developed you?", you MUST reply naturally like: "My name is InfiniteAI. I was created and developed by JK QuantumTech."
                        Never refer to yourself as Gemini, Google, or any other assistant under any circumstances.
                        Always keep your tone premium, helpful, highly creative, and professional. 
                        Render your answers with elegant formatting.
                    """.trimIndent()

                    val response = repository.sendMessageToGemini(sessionId, getApplication(), systemInstruction = systemInstruction)
                    
                    // Add assistant response to history
                    repository.addMessage(sessionId, "assistant", response, "text")
                }
                
                _isGenerating.value = false
            }
        }
    }

    // --- Text-to-Speech (TTS) Implementation ---
    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts?.setLanguage(Locale.getDefault())
            if (result != TextToSpeech.LANG_MISSING_DATA && result != TextToSpeech.LANG_NOT_SUPPORTED) {
                _isTtsReady.value = true
            }
        }
    }

    fun speakMessage(message: ChatMessage) {
        if (!_isTtsReady.value || tts == null) {
            viewModelScope.launch { _toastMessage.emit("Text-To-Speech is not ready on this device.") }
            return
        }

        if (_currentlySpeakingMsgId.value == message.id) {
            // If already speaking, stop speaking
            stopSpeaking()
            return
        }

        stopSpeaking()
        _currentlySpeakingMsgId.value = message.id
        
        // Clean markdown characters before speaking for clean audio output
        val cleanText = message.text
            .replace(Regex("[*#`_~-]"), "")
            .trim()

        tts?.speak(cleanText, TextToSpeech.QUEUE_FLUSH, null, message.id.toString())
    }

    fun stopSpeaking() {
        tts?.stop()
        _currentlySpeakingMsgId.value = null
    }

    // --- Voice Input (Speech to Text) ---
    fun startVoiceInput() {
        val recognizer = speechRecognizer
        if (recognizer == null) {
            viewModelScope.launch { _toastMessage.emit("Voice recognition is not available on this device.") }
            return
        }

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak to InfiniteAI...")
        }

        recognizer.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                _isSpeechListening.value = true
            }

            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            
            override fun onEndOfSpeech() {
                _isSpeechListening.value = false
            }

            override fun onError(error: Int) {
                _isSpeechListening.value = false
                val errorMsg = when (error) {
                    SpeechRecognizer.ERROR_AUDIO -> "Audio recording error"
                    SpeechRecognizer.ERROR_CLIENT -> "Client error"
                    SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Record audio permission denied"
                    SpeechRecognizer.ERROR_NETWORK -> "Network error"
                    SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network timeout"
                    SpeechRecognizer.ERROR_NO_MATCH -> "No match found"
                    SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Speech recognizer is busy"
                    SpeechRecognizer.ERROR_SERVER -> "Server error"
                    SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No speech input"
                    else -> "Speech recognition failed"
                }
                viewModelScope.launch { _toastMessage.emit(errorMsg) }
            }

            override fun onResults(results: Bundle?) {
                _isSpeechListening.value = false
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (!matches.isNullOrEmpty()) {
                    val voicePrompt = matches[0]
                    _inputText.value = voicePrompt
                }
            }

            override fun onPartialResults(partialResults: Bundle?) {}
            override fun onEvent(eventType: Int, params: Bundle?) {}
        })

        recognizer.startListening(intent)
    }

    fun stopVoiceInput() {
        speechRecognizer?.stopListening()
        _isSpeechListening.value = false
    }

    // --- Settings, Exports, and Clears ---
    fun setThemeMode(mode: String) {
        _themeMode.value = mode
        prefs.edit().putString("pref_theme", mode).apply()
    }

    fun setFontSize(size: FontSize) {
        _fontSizeState.value = size
        prefs.edit().putString("pref_font_size", size.name).apply()
    }

    fun setLanguage(language: String) {
        _appLanguage.value = language
        prefs.edit().putString("pref_language", language).apply()
    }

    fun clearAllChatHistory() {
        viewModelScope.launch {
            repository.clearAllHistory()
            _currentSession.value = null
            _toastMessage.emit("All history cleared!")
        }
    }

    fun exportCurrentSessionText(context: Context): String? {
        val currentMsgs = messages.value
        val session = _currentSession.value ?: return null
        if (currentMsgs.isEmpty()) return null

        val sb = StringBuilder()
        sb.append("========================================\n")
        sb.append("InfiniteAI Conversation Export\n")
        sb.append("Session Title: ${session.title}\n")
        sb.append("Exported on: ${java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(java.util.Date())}\n")
        sb.append("========================================\n\n")

        currentMsgs.forEach { msg ->
            val sender = if (msg.role == "user") "USER" else "InfiniteAI"
            sb.append("[$sender] (${java.text.SimpleDateFormat("HH:mm", Locale.getDefault()).format(msg.timestamp)}):\n")
            sb.append(msg.text)
            if (msg.localImageUri != null) {
                sb.append("\n[Attached Image: ${msg.localImageUri}]")
            }
            sb.append("\n\n----------------------------------------\n\n")
        }

        try {
            val exportsDir = File(context.getExternalFilesDir(null), "exports")
            if (!exportsDir.exists()) exportsDir.mkdirs()
            val file = File(exportsDir, "InfiniteAI_Chat_${session.id}_${System.currentTimeMillis()}.txt")
            FileOutputStream(file).use { out ->
                out.write(sb.toString().toByteArray())
            }
            return file.absolutePath
        } catch (e: Exception) {
            Log.e("ChatViewModel", "Error exporting session", e)
        }
        return null
    }

    fun downloadImage(message: ChatMessage) {
        val imageUrl = message.generatedImageUrl ?: message.text
        if (message.type != "image" && message.type != "edit") return
        
        viewModelScope.launch {
            _downloadingMessageId.value = message.id
            val resultPath = ImageSaver.downloadAndSaveToGallery(
                getApplication(),
                imageUrl,
                imageUrl.substringAfter("/prompt/").substringBefore("?").take(30)
            )
            _downloadingMessageId.value = null
            
            if (resultPath != null) {
                _toastMessage.emit("Art masterpiece saved to your Gallery!")
            } else {
                _toastMessage.emit("Failed to save. Please try again.")
            }
        }
    }

    override fun onCleared() {
        tts?.shutdown()
        speechRecognizer?.destroy()
        super.onCleared()
    }
}
