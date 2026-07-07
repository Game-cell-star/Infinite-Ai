package com.example.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import android.util.Log
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.util.concurrent.TimeUnit
import kotlin.random.Random

class ChatRepository(private val chatDao: ChatDao) {

    val allSessions: Flow<List<ChatSession>> = chatDao.getAllSessions()

    fun searchSessions(query: String): Flow<List<ChatSession>> {
        return chatDao.searchSessions(query)
    }

    fun getFavoriteSessions(): Flow<List<ChatSession>> {
        return chatDao.getFavoriteSessions()
    }

    fun getMessagesForSession(sessionId: Long): Flow<List<ChatMessage>> {
        return chatDao.getMessagesForSession(sessionId)
    }

    suspend fun createNewSession(title: String): Long = withContext(Dispatchers.IO) {
        chatDao.insertSession(ChatSession(title = title))
    }

    suspend fun updateSession(session: ChatSession) = withContext(Dispatchers.IO) {
        chatDao.updateSession(session)
    }

    suspend fun deleteSession(session: ChatSession) = withContext(Dispatchers.IO) {
        chatDao.deleteSession(session)
    }

    suspend fun addMessage(
        sessionId: Long, 
        role: String, 
        text: String, 
        type: String = "text",
        localImageUri: String? = null,
        generatedImageUrl: String? = null
    ) = withContext(Dispatchers.IO) {
        val message = ChatMessage(
            sessionId = sessionId,
            role = role,
            text = text,
            type = type,
            localImageUri = localImageUri,
            generatedImageUrl = generatedImageUrl
        )
        chatDao.insertMessage(message)
    }

    suspend fun clearAllHistory() = withContext(Dispatchers.IO) {
        chatDao.clearAllHistory()
    }

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    /**
     * Sends the conversation history to Gemini API and gets the assistant response.
     * Optionally handles multimodal input (images) if they are in the message history or current message.
     */
    suspend fun sendMessageToGemini(
        sessionId: Long, 
        context: Context,
        systemInstruction: String? = null
    ): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext "Error: Gemini API Key is missing or not configured. Please configure your GEMINI_API_KEY in the Secrets panel in AI Studio."
        }

        // Get chat history
        val messages = chatDao.getMessagesForSessionSync(sessionId)
        if (messages.isEmpty()) {
            return@withContext "No messages in this session."
        }

        try {
            val jsonRequest = JSONObject()
            val contentsArray = JSONArray()

            // Build conversation history
            messages.forEach { msg ->
                val contentObj = JSONObject()
                // Gemini API accepts roles: "user" and "model"
                val apiRole = if (msg.role == "user") "user" else "model"
                contentObj.put("role", apiRole)

                val partsArray = JSONArray()
                
                // If there's an image attachment in the user message, send it as inlineData
                if (msg.localImageUri != null) {
                    val base64Image = getBase64FromUri(context, Uri.parse(msg.localImageUri))
                    if (base64Image != null) {
                        val imagePart = JSONObject()
                        val inlineDataObj = JSONObject()
                        inlineDataObj.put("mimeType", "image/jpeg")
                        inlineDataObj.put("data", base64Image)
                        imagePart.put("inlineData", inlineDataObj)
                        partsArray.put(imagePart)
                    }
                }

                val textPart = JSONObject()
                if (msg.type == "image" || msg.type == "edit") {
                    textPart.put("text", msg.text)
                } else {
                    textPart.put("text", msg.text)
                }
                partsArray.put(textPart)
                
                contentObj.put("parts", partsArray)
                contentsArray.put(contentObj)
            }

            jsonRequest.put("contents", contentsArray)

            // Optional System Instruction
            if (systemInstruction != null) {
                val systemObj = JSONObject()
                val systemParts = JSONArray()
                systemParts.put(JSONObject().put("text", systemInstruction))
                systemObj.put("parts", systemParts)
                jsonRequest.put("systemInstruction", systemObj)
            }

            // Optional configuration
            val generationConfig = JSONObject()
            generationConfig.put("temperature", 0.7)
            jsonRequest.put("generationConfig", generationConfig)

            val requestBodyJson = jsonRequest.toString()
            Log.d("ChatRepository", "Request JSON length: ${requestBodyJson.length}")

            val mediaType = "application/json; charset=utf-8".toMediaType()
            val requestBody = requestBodyJson.toRequestBody(mediaType)

            val modelName = "gemini-3.5-flash"
            val url = "https://generativelanguage.googleapis.com/v1beta/models/$modelName:generateContent?key=$apiKey"

            val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .build()

            client.newCall(request).execute().use { response ->
                val responseBodyStr = response.body?.string() ?: ""
                Log.d("ChatRepository", "Response code: ${response.code}")

                if (!response.isSuccessful) {
                    val errorObj = try { JSONObject(responseBodyStr) } catch(e: Exception) { null }
                    val errorMsg = errorObj?.optJSONObject("error")?.optString("message") ?: "HTTP error code: ${response.code}"
                    return@withContext "Error: $errorMsg"
                }

                val responseJson = JSONObject(responseBodyStr)
                val candidatesArray = responseJson.optJSONArray("candidates")
                if (candidatesArray != null && candidatesArray.length() > 0) {
                    val firstCandidate = candidatesArray.getJSONObject(0)
                    val contentObj = firstCandidate.optJSONObject("content")
                    if (contentObj != null) {
                        val partsArray = contentObj.optJSONArray("parts")
                        if (partsArray != null && partsArray.length() > 0) {
                            return@withContext partsArray.getJSONObject(0).optString("text", "Empty response parts")
                        }
                    }
                }
                return@withContext "No response text found from the AI."
            }
        } catch (e: Exception) {
            Log.e("ChatRepository", "Exception calling Gemini API", e)
            return@withContext "Exception: ${e.localizedMessage ?: "Unknown network error"}"
        }
    }

    /**
     * Specialized multimodal call to generate a prompt for editing an existing image.
     * Takes the input image base64, plus the user edit prompt, and asks Gemini to output an enhanced
     * prompt suitable for feeding into an image generator to produce the edited result.
     */
    suspend fun generateEditPrompt(context: Context, imageUri: Uri, editInstruction: String): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext "Error: Gemini API Key is missing."
        }

        val base64Image = getBase64FromUri(context, imageUri)
            ?: return@withContext "Error: Could not read selected image."

        try {
            val jsonRequest = JSONObject()
            val contentsArray = JSONArray()

            val contentObj = JSONObject()
            contentObj.put("role", "user")

            val partsArray = JSONArray()

            // 1. Image part
            val imagePart = JSONObject()
            val inlineDataObj = JSONObject()
            inlineDataObj.put("mimeType", "image/jpeg")
            inlineDataObj.put("data", base64Image)
            imagePart.put("inlineData", inlineDataObj)
            partsArray.put(imagePart)

            // 2. Editing Instruction text part
            val textPart = JSONObject()
            val systemInstructionsPrompt = """
                You are a master image-to-image prompt engineer. 
                The user has uploaded an image and wants to edit it with this instruction: "$editInstruction".
                
                Your job: Analyze the visual contents of the image (subject, composition, color, lighting). 
                Write a highly detailed, cinematic, visually rich 1024x1024 generation prompt that describes the desired edited outcome. 
                The resulting prompt should keep the original subject, structure, or composition of the image, but modify it exactly as the edit instruction requests (e.g., if "make this image cyberpunk", describe the same subject and scene but styled in high-fidelity cyberpunk with neon signs, rain, cybernetic enhancements).
                
                Respond ONLY with the generated prompt in English. Do NOT write any introduction, description, markdown blocks, quotes, or explanations. Just output the raw prompt.
            """.trimIndent()
            textPart.put("text", systemInstructionsPrompt)
            partsArray.put(textPart)

            contentObj.put("parts", partsArray)
            contentsArray.put(contentObj)
            jsonRequest.put("contents", contentsArray)

            val mediaType = "application/json; charset=utf-8".toMediaType()
            val requestBody = jsonRequest.toString().toRequestBody(mediaType)

            val modelName = "gemini-3.5-flash"
            val url = "https://generativelanguage.googleapis.com/v1beta/models/$modelName:generateContent?key=$apiKey"

            val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .build()

            client.newCall(request).execute().use { response ->
                val responseBodyStr = response.body?.string() ?: ""
                if (!response.isSuccessful) {
                    return@withContext "Stunning cinematic edit: $editInstruction, high resolution, detailed."
                }

                val responseJson = JSONObject(responseBodyStr)
                val candidatesArray = responseJson.optJSONArray("candidates")
                if (candidatesArray != null && candidatesArray.length() > 0) {
                    val firstCandidate = candidatesArray.getJSONObject(0)
                    val contentRes = firstCandidate.optJSONObject("content")
                    val parts = contentRes?.optJSONArray("parts")
                    if (parts != null && parts.length() > 0) {
                        return@withContext parts.getJSONObject(0).optString("text", editInstruction).trim()
                    }
                }
                return@withContext editInstruction
            }
        } catch (e: Exception) {
            Log.e("ChatRepository", "Exception generating edit prompt", e)
            return@withContext "$editInstruction, cinematic, high fidelity, 4k"
        }
    }

    /**
     * Generates a high-quality Pollinations AI Image URL based on the prompt.
     * Pollinations AI is fully free, unlimited, and provides stunning images immediately.
     */
    fun generateImageUrl(prompt: String): String {
        val encodedPrompt = Uri.encode(prompt)
        val seed = Random.nextInt(1, 10000000)
        // Set styling parameters to get cinematic/gorgeous results!
        return "https://image.pollinations.ai/prompt/$encodedPrompt?width=1024&height=1024&nologo=true&seed=$seed&enhance=true"
    }

    /**
     * Reads uri bytes and converts them to a base64 string with compression to fit in API limits.
     */
    private fun getBase64FromUri(context: Context, uri: Uri): String? {
        return try {
            val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
            val bytes = inputStream?.use { it.readBytes() } ?: return null
            
            // Decode with scaling to avoid sending too large images (Gemini works best with ~512px to 1024px)
            val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size) ?: return null
            
            // Resize if too large
            val maxDimension = 1024
            val scaledBitmap = if (bitmap.width > maxDimension || bitmap.height > maxDimension) {
                val ratio = bitmap.width.toFloat() / bitmap.height.toFloat()
                val (newWidth, newHeight) = if (ratio > 1) {
                    maxDimension to (maxDimension / ratio).toInt()
                } else {
                    (maxDimension * ratio).toInt() to maxDimension
                }
                Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
            } else {
                bitmap
            }
            
            val outputStream = ByteArrayOutputStream()
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
            val compressedBytes = outputStream.toByteArray()
            Base64.encodeToString(compressedBytes, Base64.NO_WRAP)
        } catch (e: Exception) {
            Log.e("ChatRepository", "Error base64 encoding Uri", e)
            null
        }
    }
}
