package com.example.ui

import android.Manifest
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.speech.SpeechRecognizer
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.TextSnippet
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.Divider
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.ChatMessage
import com.example.data.ChatSession
import com.example.ui.theme.SlateCard
import com.example.ui.theme.SlateDark
import com.example.ui.theme.SlateSurface
import com.example.ui.theme.IndigoAccent
import com.example.ui.theme.PinkAccent
import com.example.ui.theme.BorderAccent
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

enum class AppTab {
    CHAT, ARTIST, SETTINGS
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(viewModel: ChatViewModel) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    val sessions by viewModel.sessions.collectAsState()
    val currentSession by viewModel.currentSession.collectAsState()
    val messages by viewModel.messages.collectAsState()
    val inputText by viewModel.inputText.collectAsState()
    val inputMode by viewModel.inputMode.collectAsState()
    val isGenerating by viewModel.isGenerating.collectAsState()
    val isGeneratingImage by viewModel.isGeneratingImage.collectAsState()
    val downloadingMessageId by viewModel.downloadingMessageId.collectAsState()
    
    val searchQuery by viewModel.searchQuery.collectAsState()
    val showFavoritesOnly by viewModel.showFavoritesOnly.collectAsState()
    
    val selectedImageUri by viewModel.selectedImageUri.collectAsState()
    val currentSpeakingId by viewModel.currentlySpeakingMsgId.collectAsState()
    val isSpeechListening by viewModel.isSpeechListening.collectAsState()
    
    val themeMode by viewModel.themeMode.collectAsState()
    val fontSizeState by viewModel.fontSizeState.collectAsState()
    val appLanguage by viewModel.appLanguage.collectAsState()

    var activeTab by remember { mutableStateOf(AppTab.CHAT) }
    var fullscreenImageUrl by remember { mutableStateOf<String?>(null) }
    
    val haptic = LocalHapticFeedback.current

    // Toast listener
    LaunchedEffect(Unit) {
        viewModel.toastMessage.collectLatest { msg ->
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
        }
    }

    val configuration = LocalConfiguration.current
    val isTablet = configuration.screenWidthDp >= 600

    // Full screen image preview Dialog
    if (fullscreenImageUrl != null) {
        Dialog(
            onDismissRequest = { fullscreenImageUrl = null },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = Color.Black.copy(alpha = 0.95f)
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(fullscreenImageUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = "Full Artwork Preview",
                        modifier = Modifier
                            .fillMaxSize()
                            .clickable { fullscreenImageUrl = null },
                        contentScale = ContentScale.Fit
                    )
                    Row(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .statusBarsPadding()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        IconButton(
                            onClick = {
                                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_TEXT, "Check out this beautiful AI art created with InfiniteAI: $fullscreenImageUrl")
                                }
                                context.startActivity(Intent.createChooser(shareIntent, "Share Image URL"))
                            },
                            modifier = Modifier.background(Color.Black.copy(alpha = 0.6f), CircleShape)
                        ) {
                            Icon(Icons.Default.Share, contentDescription = "Share URL", tint = Color.White)
                        }
                        IconButton(
                            onClick = { fullscreenImageUrl = null },
                            modifier = Modifier.background(Color.Black.copy(alpha = 0.6f), CircleShape)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                        }
                    }
                }
            }
        }
    }

    if (isTablet) {
        // Dual Pane Layout (Tablet Viewport)
        Row(
            modifier = Modifier
                .fillMaxSize()
                .background(SlateDark)
        ) {
            // Sidebar Panel
            Surface(
                modifier = Modifier
                    .width(300.dp)
                    .fillMaxHeight(),
                color = SlateCard,
                border = BorderStroke(1.dp, BorderAccent)
            ) {
                SidebarContent(
                    sessions = sessions,
                    currentSession = currentSession,
                    searchQuery = searchQuery,
                    showFavoritesOnly = showFavoritesOnly,
                    onSearchQueryChanged = { viewModel.setSearchQuery(it) },
                    onToggleFavorites = { viewModel.toggleFavoritesFilter() },
                    onSessionSelected = { viewModel.selectSession(it) },
                    onToggleSessionFavorite = { viewModel.toggleSessionFavorite(it) },
                    onDeleteSession = { viewModel.deleteSession(it) },
                    onNewConversation = { viewModel.createNewEmptySession() }
                )
            }

            // Right Active Panel
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                color = SlateDark
            ) {
                MainContentArea(
                    activeTab = activeTab,
                    onTabChanged = { activeTab = it },
                    currentSession = currentSession,
                    messages = messages,
                    inputText = inputText,
                    inputMode = inputMode,
                    isGenerating = isGenerating,
                    isGeneratingImage = isGeneratingImage,
                    downloadingMessageId = downloadingMessageId,
                    selectedImageUri = selectedImageUri,
                    currentSpeakingId = currentSpeakingId,
                    isSpeechListening = isSpeechListening,
                    themeMode = themeMode,
                    fontSizeState = fontSizeState,
                    appLanguage = appLanguage,
                    onInputTextChanged = { viewModel.setInputText(it) },
                    onInputModeChanged = { viewModel.setInputMode(it) },
                    setSelectedImageUri = { viewModel.setSelectedImageUri(it) },
                    onSend = { viewModel.handleSend() },
                    onMenuClicked = {},
                    onImageClicked = { fullscreenImageUrl = it },
                    onDownloadClicked = { viewModel.downloadImage(it) },
                    onSpeakClicked = { viewModel.speakMessage(it) },
                    onStartVoice = { viewModel.startVoiceInput() },
                    onStopVoice = { viewModel.stopVoiceInput() },
                    setThemeMode = { viewModel.setThemeMode(it) },
                    setFontSize = { viewModel.setFontSize(it) },
                    setLanguage = { viewModel.setLanguage(it) },
                    clearHistory = { viewModel.clearAllChatHistory() },
                    exportHistory = {
                        val path = viewModel.exportCurrentSessionText(context)
                        if (path != null) {
                            Toast.makeText(context, "Chat exported to: $path", Toast.LENGTH_LONG).show()
                        } else {
                            Toast.makeText(context, "No chat history to export.", Toast.LENGTH_SHORT).show()
                        }
                    },
                    isTablet = true,
                    onNewConversation = { viewModel.createNewEmptySession() }
                )
            }
        }
    } else {
        // Mobile Single Pane Layout (With Drawer)
        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                ModalDrawerSheet(
                    drawerContainerColor = SlateCard,
                    modifier = Modifier.width(300.dp)
                ) {
                    SidebarContent(
                        sessions = sessions,
                        currentSession = currentSession,
                        searchQuery = searchQuery,
                        showFavoritesOnly = showFavoritesOnly,
                        onSearchQueryChanged = { viewModel.setSearchQuery(it) },
                        onToggleFavorites = { viewModel.toggleFavoritesFilter() },
                        onSessionSelected = {
                            viewModel.selectSession(it)
                            coroutineScope.launch { drawerState.close() }
                        },
                        onToggleSessionFavorite = { viewModel.toggleSessionFavorite(it) },
                        onDeleteSession = { viewModel.deleteSession(it) },
                        onNewConversation = {
                            viewModel.createNewEmptySession()
                            coroutineScope.launch { drawerState.close() }
                        }
                    )
                }
            }
        ) {
            MainContentArea(
                activeTab = activeTab,
                onTabChanged = { activeTab = it },
                currentSession = currentSession,
                messages = messages,
                inputText = inputText,
                inputMode = inputMode,
                isGenerating = isGenerating,
                isGeneratingImage = isGeneratingImage,
                downloadingMessageId = downloadingMessageId,
                selectedImageUri = selectedImageUri,
                currentSpeakingId = currentSpeakingId,
                isSpeechListening = isSpeechListening,
                themeMode = themeMode,
                fontSizeState = fontSizeState,
                appLanguage = appLanguage,
                onInputTextChanged = { viewModel.setInputText(it) },
                onInputModeChanged = { viewModel.setInputMode(it) },
                setSelectedImageUri = { viewModel.setSelectedImageUri(it) },
                onSend = { viewModel.handleSend() },
                onMenuClicked = { coroutineScope.launch { drawerState.open() } },
                onImageClicked = { fullscreenImageUrl = it },
                onDownloadClicked = { viewModel.downloadImage(it) },
                onSpeakClicked = { viewModel.speakMessage(it) },
                onStartVoice = { viewModel.startVoiceInput() },
                onStopVoice = { viewModel.stopVoiceInput() },
                setThemeMode = { viewModel.setThemeMode(it) },
                setFontSize = { viewModel.setFontSize(it) },
                setLanguage = { viewModel.setLanguage(it) },
                clearHistory = { viewModel.clearAllChatHistory() },
                exportHistory = {
                    val path = viewModel.exportCurrentSessionText(context)
                    if (path != null) {
                        Toast.makeText(context, "Chat exported to: $path", Toast.LENGTH_LONG).show()
                    } else {
                        Toast.makeText(context, "No active chat to export.", Toast.LENGTH_SHORT).show()
                    }
                },
                isTablet = false,
                onNewConversation = { viewModel.createNewEmptySession() }
            )
        }
    }
}

/**
 * Sidebar Drawer Content
 */
@Composable
fun SidebarContent(
    sessions: List<ChatSession>,
    currentSession: ChatSession?,
    searchQuery: String,
    showFavoritesOnly: Boolean,
    onSearchQueryChanged: (String) -> Unit,
    onToggleFavorites: () -> Unit,
    onSessionSelected: (ChatSession) -> Unit,
    onToggleSessionFavorite: (ChatSession) -> Unit,
    onDeleteSession: (ChatSession) -> Unit,
    onNewConversation: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        // Brand Header with Sophisticated Theme styling
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(vertical = 12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(
                        Brush.radialGradient(
                            listOf(PinkAccent, IndigoAccent)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = Color(0xFF381E72),
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "InfiniteAI",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.5.sp
                    ),
                    color = TextPrimary
                )
                Text(
                    text = "Unlimited • JK QuantumTech",
                    style = MaterialTheme.typography.bodySmall,
                    color = PinkAccent,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // New Chat Button
        Button(
            onClick = onNewConversation,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("new_chat_button"),
            colors = ButtonDefaults.buttonColors(containerColor = SlateSurface),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, BorderAccent)
        ) {
            Icon(Icons.Default.Add, contentDescription = null, tint = TextPrimary)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "New Conversation",
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChanged,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Search conversations...", fontSize = 13.sp, color = TextSecondary) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(16.dp), tint = TextSecondary) },
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = SlateDark,
                unfocusedContainerColor = SlateDark,
                focusedBorderColor = IndigoAccent,
                unfocusedBorderColor = BorderAccent
            ),
            shape = RoundedCornerShape(8.dp)
        )

        Spacer(modifier = Modifier.height(14.dp))

        // All vs Starred Tabs
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(SlateDark)
                .padding(4.dp)
        ) {
            Row(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(6.dp))
                    .background(if (!showFavoritesOnly) SlateCard else Color.Transparent)
                    .clickable { if (showFavoritesOnly) onToggleFavorites() }
                    .padding(vertical = 6.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.History, contentDescription = null, sizeFilter = 14, tint = if (!showFavoritesOnly) TextPrimary else TextSecondary)
                Spacer(modifier = Modifier.width(4.dp))
                Text("All", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (!showFavoritesOnly) TextPrimary else TextSecondary)
            }
            Row(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(6.dp))
                    .background(if (showFavoritesOnly) SlateCard else Color.Transparent)
                    .clickable { if (!showFavoritesOnly) onToggleFavorites() }
                    .padding(vertical = 6.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Star, contentDescription = null, sizeFilter = 14, tint = if (showFavoritesOnly) PinkAccent else TextSecondary)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Starred", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (showFavoritesOnly) TextPrimary else TextSecondary)
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Conversations List
        if (sessions.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (showFavoritesOnly) "No starred chats" else "No saved chats",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(sessions) { session ->
                    val isSelected = currentSession?.id == session.id
                    val backgroundColor by animateColorAsState(
                        targetValue = if (isSelected) SlateSurface else Color.Transparent,
                        animationSpec = tween(150)
                    )
                    val borderColor = if (isSelected) IndigoAccent else Color.Transparent

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(backgroundColor)
                            .border(1.dp, borderColor, RoundedCornerShape(10.dp))
                            .clickable { onSessionSelected(session) }
                            .padding(start = 10.dp, end = 4.dp, top = 8.dp, bottom = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Star,
                            contentDescription = "Star status",
                            tint = if (session.isFavorite) PinkAccent else TextSecondary.copy(alpha = 0.4f),
                            modifier = Modifier
                                .size(20.dp)
                                .clickable { onToggleSessionFavorite(session) }
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = session.title,
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (isSelected) TextPrimary else TextSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        
                        // Delete Button
                        IconButton(
                            onClick = { onDeleteSession(session) },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Delete chat",
                                tint = Color(0xFFE54646).copy(alpha = 0.7f),
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Specs Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = SlateDark),
            border = BorderStroke(1.dp, BorderAccent)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = "System Core v1.0",
                    style = MaterialTheme.typography.titleSmall,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Chat", color = TextSecondary, style = MaterialTheme.typography.bodySmall)
                    Text("InfiniteAI 3.5", color = IndigoAccent, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Vision", color = TextSecondary, style = MaterialTheme.typography.bodySmall)
                    Text("Multimodal", color = PinkAccent, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun Icon(icon: ImageVector, contentDescription: String?, sizeFilter: Int, tint: Color) {
    Icon(
        imageVector = icon,
        contentDescription = contentDescription,
        tint = tint,
        modifier = Modifier.size(sizeFilter.dp)
    )
}

/**
 * Main active canvas containing the navigation tabs and corresponding views
 */
@Composable
fun MainContentArea(
    activeTab: AppTab,
    onTabChanged: (AppTab) -> Unit,
    currentSession: ChatSession?,
    messages: List<ChatMessage>,
    inputText: String,
    inputMode: InputMode,
    isGenerating: Boolean,
    isGeneratingImage: Boolean,
    downloadingMessageId: Long?,
    selectedImageUri: Uri?,
    currentSpeakingId: Long?,
    isSpeechListening: Boolean,
    themeMode: String,
    fontSizeState: FontSize,
    appLanguage: String,
    onInputTextChanged: (String) -> Unit,
    onInputModeChanged: (InputMode) -> Unit,
    setSelectedImageUri: (Uri?) -> Unit,
    onSend: () -> Unit,
    onMenuClicked: () -> Unit,
    onImageClicked: (String) -> Unit,
    onDownloadClicked: (ChatMessage) -> Unit,
    onSpeakClicked: (ChatMessage) -> Unit,
    onStartVoice: () -> Unit,
    onStopVoice: () -> Unit,
    setThemeMode: (String) -> Unit,
    setFontSize: (FontSize) -> Unit,
    setLanguage: (String) -> Unit,
    clearHistory: () -> Unit,
    exportHistory: () -> Unit,
    isTablet: Boolean,
    onNewConversation: () -> Unit
) {
    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = SlateCard,
                tonalElevation = 8.dp,
                modifier = Modifier.border(BorderStroke(1.dp, BorderAccent), RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
            ) {
                NavigationBarItem(
                    selected = activeTab == AppTab.CHAT,
                    onClick = { onTabChanged(AppTab.CHAT) },
                    icon = { Icon(Icons.Default.Palette, contentDescription = "Infinite Chat") },
                    label = { Text("Infinite Chat") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = IndigoAccent,
                        selectedTextColor = IndigoAccent,
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextSecondary,
                        indicatorColor = IndigoAccent.copy(alpha = 0.15f)
                    )
                )
                NavigationBarItem(
                    selected = activeTab == AppTab.ARTIST,
                    onClick = { onTabChanged(AppTab.ARTIST) },
                    icon = { Icon(Icons.Default.Palette, contentDescription = "AI Artist") },
                    label = { Text("AI Artist") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = PinkAccent,
                        selectedTextColor = PinkAccent,
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextSecondary,
                        indicatorColor = PinkAccent.copy(alpha = 0.15f)
                    )
                )
                NavigationBarItem(
                    selected = activeTab == AppTab.SETTINGS,
                    onClick = { onTabChanged(AppTab.SETTINGS) },
                    icon = { Icon(Icons.Default.Palette, contentDescription = "Settings") },
                    label = { Text("Settings") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = IndigoAccent,
                        selectedTextColor = IndigoAccent,
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextSecondary,
                        indicatorColor = IndigoAccent.copy(alpha = 0.15f)
                    )
                )
            }
        },
        containerColor = SlateDark
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (activeTab) {
                AppTab.CHAT -> {
                    ChatTabContent(
                        currentSession = currentSession,
                        messages = messages,
                        inputText = inputText,
                        inputMode = inputMode,
                        isGenerating = isGenerating,
                        isGeneratingImage = isGeneratingImage,
                        downloadingMessageId = downloadingMessageId,
                        selectedImageUri = selectedImageUri,
                        currentSpeakingId = currentSpeakingId,
                        isSpeechListening = isSpeechListening,
                        onInputTextChanged = onInputTextChanged,
                        onInputModeChanged = onInputModeChanged,
                        setSelectedImageUri = setSelectedImageUri,
                        onSend = onSend,
                        onMenuClicked = onMenuClicked,
                        onImageClicked = onImageClicked,
                        onDownloadClicked = onDownloadClicked,
                        onSpeakClicked = onSpeakClicked,
                        onStartVoice = onStartVoice,
                        onStopVoice = onStopVoice,
                        isTablet = isTablet,
                        onNewConversation = onNewConversation,
                        fontSizeState = fontSizeState
                    )
                }
                AppTab.ARTIST -> {
                    ArtistTabContent(
                        isGeneratingImage = isGeneratingImage,
                        onImageClicked = onImageClicked,
                        onGenerateCustomImage = { prompt ->
                            onInputTextChanged(prompt)
                            onInputModeChanged(InputMode.IMAGE)
                            onTabChanged(AppTab.CHAT)
                            onSend()
                        }
                    )
                }
                AppTab.SETTINGS -> {
                    SettingsTabContent(
                        themeMode = themeMode,
                        fontSizeState = fontSizeState,
                        appLanguage = appLanguage,
                        setThemeMode = setThemeMode,
                        setFontSize = setFontSize,
                        setLanguage = setLanguage,
                        clearHistory = clearHistory,
                        exportHistory = exportHistory
                    )
                }
            }
        }
    }
}

/**
 * INFINITE CHAT TAB
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatTabContent(
    currentSession: ChatSession?,
    messages: List<ChatMessage>,
    inputText: String,
    inputMode: InputMode,
    isGenerating: Boolean,
    isGeneratingImage: Boolean,
    downloadingMessageId: Long?,
    selectedImageUri: Uri?,
    currentSpeakingId: Long?,
    isSpeechListening: Boolean,
    onInputTextChanged: (String) -> Unit,
    onInputModeChanged: (InputMode) -> Unit,
    setSelectedImageUri: (Uri?) -> Unit,
    onSend: () -> Unit,
    onMenuClicked: () -> Unit,
    onImageClicked: (String) -> Unit,
    onDownloadClicked: (ChatMessage) -> Unit,
    onSpeakClicked: (ChatMessage) -> Unit,
    onStartVoice: () -> Unit,
    onStopVoice: () -> Unit,
    isTablet: Boolean,
    onNewConversation: () -> Unit,
    fontSizeState: FontSize
) {
    val listState = rememberLazyListState()
    val focusManager = LocalFocusManager.current
    val context = LocalContext.current

    // Media picking activity launchers
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            setSelectedImageUri(uri)
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        if (bitmap != null) {
            val file = java.io.File(context.cacheDir, "temp_camera_photo.jpg")
            try {
                FileOutputStream(file).use { out ->
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)
                }
                setSelectedImageUri(Uri.fromFile(file))
            } catch (e: Exception) {
                Toast.makeText(context, "Failed to load captured photo", Toast.LENGTH_SHORT).show()
            }
        }
    }

    val recordAudioPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            onStartVoice()
        } else {
            Toast.makeText(context, "Microphone permission is required for voice input", Toast.LENGTH_SHORT).show()
        }
    }

    // Auto-scroll to bottom on new messages
    LaunchedEffect(messages.size, isGenerating, isGeneratingImage) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Chat Canvas Custom Header
        TopAppBar(
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = currentSession?.title ?: "InfiniteAI",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF34C759))
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Unlimited Live",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
                        color = Color(0xFF34C759)
                    )
                }
            },
            navigationIcon = {
                if (!isTablet) {
                    IconButton(onClick = onMenuClicked) {
                        Icon(Icons.Default.Menu, contentDescription = "History Drawer", tint = Color.White)
                    }
                }
            },
            actions = {
                if (!isTablet) {
                    IconButton(onClick = onNewConversation) {
                        Icon(Icons.Default.Add, contentDescription = "New chat", tint = Color.White)
                    }
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = SlateCard,
                titleContentColor = Color.White
            )
        )

        // Main List area
        if (messages.isEmpty() && !isGenerating && !isGeneratingImage) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                OnboardingEmptyState(
                    onPromptSelected = { prompt, mode ->
                        onInputTextChanged(prompt)
                        onInputModeChanged(mode)
                    }
                )
            }
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(messages) { message ->
                    MessageItem(
                        message = message,
                        onImageClicked = { onImageClicked(message.generatedImageUrl ?: message.text) },
                        onDownloadClicked = { onDownloadClicked(message) },
                        isDownloading = downloadingMessageId == message.id,
                        onSpeakClicked = { onSpeakClicked(message) },
                        isSpeaking = currentSpeakingId == message.id,
                        fontSizeState = fontSizeState
                    )
                }

                if (isGenerating) {
                    item {
                        GeneratingIndicator(text = "InfiniteAI is synthesizing...")
                    }
                }

                if (isGeneratingImage) {
                    item {
                        GeneratingIndicator(text = "Rendering artwork. Free, unlimited & high-res...")
                    }
                }
            }
        }

        // Image Selection Attachment Bar
        if (selectedImageUri != null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SlateCard)
                    .border(BorderStroke(1.dp, BorderAccent))
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .border(1.dp, PinkAccent, RoundedCornerShape(8.dp))
                ) {
                    AsyncImage(
                        model = selectedImageUri,
                        contentDescription = "Selected media",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Image Loaded for Art Editing",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = PinkAccent
                    )
                    Text(
                        text = "Input a style directive below (e.g. \"cyberpunk\", \"anime\")",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
                IconButton(onClick = { setSelectedImageUri(null) }) {
                    Icon(Icons.Default.Close, contentDescription = "Remove photo", tint = Color.Red)
                }
            }
        }

        // Voice Speaking visual overlay
        if (isSpeechListening) {
            val infiniteTransition = rememberInfiniteTransition()
            val micScale by infiniteTransition.animateFloat(
                initialValue = 1f,
                targetValue = 1.4f,
                animationSpec = infiniteRepeatable(
                    animation = tween(800),
                    repeatMode = RepeatMode.Reverse
                )
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(IndigoAccent.copy(alpha = 0.15f))
                    .padding(14.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Mic,
                    contentDescription = "Listening",
                    tint = IndigoAccent,
                    modifier = Modifier
                        .size(24.dp)
                        .scale(micScale)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Listening to your voice...",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.width(12.dp))
                TextButton(onClick = onStopVoice) {
                    Text("Stop", color = PinkAccent, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Input Row Bar
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(SlateCard)
                .border(BorderStroke(1.dp, BorderAccent))
                .padding(12.dp)
        ) {
            // Mode Select buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // AI Chat Tab
                val chatSelected = inputMode == InputMode.CHAT
                val chatBgColor by animateColorAsState(
                    targetValue = if (chatSelected) IndigoAccent.copy(alpha = 0.15f) else Color.Transparent
                )
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(chatBgColor)
                        .border(1.dp, if (chatSelected) IndigoAccent else BorderAccent, RoundedCornerShape(8.dp))
                        .clickable { onInputModeChanged(InputMode.CHAT) },
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(Icons.Default.Palette, contentDescription = "Chat", sizeFilter = 14, tint = if (chatSelected) TextPrimary else TextSecondary)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("AI Chat", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (chatSelected) TextPrimary else TextSecondary)
                }

                // AI Artist Tab
                val artistSelected = inputMode == InputMode.IMAGE
                val artistBgColor by animateColorAsState(
                    targetValue = if (artistSelected) PinkAccent.copy(alpha = 0.15f) else Color.Transparent
                )
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(artistBgColor)
                        .border(1.dp, if (artistSelected) PinkAccent else BorderAccent, RoundedCornerShape(8.dp))
                        .clickable { onInputModeChanged(InputMode.IMAGE) },
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(Icons.Default.Palette, contentDescription = "Artist", sizeFilter = 14, tint = if (artistSelected) TextPrimary else TextSecondary)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("AI Artist", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (artistSelected) TextPrimary else TextSecondary)
                }
            }

            // Input Row Core Text
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                var menuExpanded by remember { mutableStateOf(false) }

                // Media / Camera attachments dropdown
                Box {
                    IconButton(onClick = { menuExpanded = true }) {
                        Icon(
                            Icons.Default.AddPhotoAlternate,
                            contentDescription = "Attach photo",
                            tint = if (selectedImageUri != null) PinkAccent else TextSecondary
                        )
                    }

                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false },
                        modifier = Modifier.background(SlateCard)
                    ) {
                        DropdownMenuItem(
                            text = { Row {
                                Icon(Icons.Default.Image, contentDescription = null, sizeFilter = 16, tint = TextPrimary)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Select from Gallery", color = TextPrimary)
                            }},
                            onClick = {
                                menuExpanded = false
                                galleryLauncher.launch("image/*")
                            }
                        )
                        DropdownMenuItem(
                            text = { Row {
                                Icon(Icons.Default.PhotoCamera, contentDescription = null, sizeFilter = 16, tint = TextPrimary)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Capture Photo", color = TextPrimary)
                            }},
                            onClick = {
                                menuExpanded = false
                                cameraLauncher.launch(null)
                            }
                        )
                    }
                }

                // Voice Input Button
                IconButton(
                    onClick = {
                        if (isSpeechListening) {
                            onStopVoice()
                        } else {
                            recordAudioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                        }
                    }
                ) {
                    Icon(
                        if (isSpeechListening) Icons.Default.MicOff else Icons.Default.Mic,
                        contentDescription = "Voice prompt",
                        tint = if (isSpeechListening) PinkAccent else TextSecondary
                    )
                }

                OutlinedTextField(
                    value = inputText,
                    onValueChange = onInputTextChanged,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("prompt_input_field"),
                    placeholder = {
                        Text(
                            text = when {
                                selectedImageUri != null -> "Style editing command..."
                                inputMode == InputMode.IMAGE -> "Infinite AI prompt..."
                                else -> "Enter instruction..."
                            },
                            fontSize = (14.sp.value * fontSizeState.scale).sp,
                            color = TextSecondary
                        )
                    },
                    maxLines = 4,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = SlateDark,
                        unfocusedContainerColor = SlateDark,
                        focusedBorderColor = if (inputMode == InputMode.IMAGE) PinkAccent else IndigoAccent,
                        unfocusedBorderColor = BorderAccent,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    textStyle = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = (14.sp.value * fontSizeState.scale).sp
                    ),
                    shape = RoundedCornerShape(10.dp),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                    keyboardActions = KeyboardActions(onSend = {
                        onSend()
                        focusManager.clearFocus()
                    })
                )

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(
                    onClick = {
                        onSend()
                        focusManager.clearFocus()
                    },
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(
                            if (inputMode == InputMode.IMAGE || selectedImageUri != null) PinkAccent else IndigoAccent
                        )
                ) {
                    Icon(
                        Icons.Default.Send,
                        contentDescription = "Send",
                        tint = SlateDark,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

/**
 * AI ARTIST CANVAS TAB
 */
@Composable
fun ArtistTabContent(
    isGeneratingImage: Boolean,
    onImageClicked: (String) -> Unit,
    onGenerateCustomImage: (String) -> Unit
) {
    var prompt by remember { mutableStateOf("") }
    var selectedRatio by remember { mutableStateOf("1:1 Square") }
    var selectedPreset by remember { mutableStateOf("Cinematic") }
    
    val presets = listOf(
        "Cinematic" to "Cinematic photorealistic, ultra detailed, cinematic lighting, 8k, bokeh depth",
        "Neon Cyberpunk" to "Neon cyberpunk futuristic city, glowing purple and cyan signs, wet rain reflection",
        "Anime Illustration" to "Anime vector illustration key art, vibrant colors, makoto shinkai style",
        "3D Render" to "Magical unreal engine 5 fantasy render, octane rendering, cute stylized design",
        "Pencil Sketch" to "Handdrawn black and white detailed pencil sketch, cross hatching shading",
        "Vaporwave Retro" to "Retro 80s vaporwave landscape, neon sun, grid lines, purple wireframe"
    )

    val ratios = listOf(
        "1:1 Square" to "1:1 ratio",
        "16:9 Landscape" to "16:9 landscape widescreen",
        "9:16 Portrait" to "9:16 vertical portrait smartphone format",
        "4:3 Photography" to "4:3 classic photo aspect ratio"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .statusBarsPadding()
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(PinkAccent.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Palette, contentDescription = null, sizeFilter = 18, tint = PinkAccent)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "Art Studio Canvas",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = TextPrimary
                )
                Text(
                    text = "Unlimited high-fidelity graphics with Pollinations",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            colors = CardDefaults.cardColors(containerColor = SlateCard),
            border = BorderStroke(1.dp, BorderAccent),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Creative Prompt",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = prompt,
                    onValueChange = { prompt = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("What are you visualizing? (e.g. \"a futuristic cat sailing in space\")", fontSize = 13.sp, color = TextSecondary) },
                    maxLines = 3,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = SlateDark,
                        unfocusedContainerColor = SlateDark,
                        focusedBorderColor = PinkAccent,
                        unfocusedBorderColor = BorderAccent
                    ),
                    shape = RoundedCornerShape(8.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Presets Choice
        Text("Style Presets", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)
        Spacer(modifier = Modifier.height(8.dp))
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(presets) { preset ->
                val isSelected = selectedPreset == preset.first
                val bgCol by animateColorAsState(targetValue = if (isSelected) PinkAccent.copy(alpha = 0.15f) else SlateCard)
                val borderCol = if (isSelected) PinkAccent else BorderAccent
                
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(bgCol)
                        .border(1.dp, borderCol, RoundedCornerShape(10.dp))
                        .clickable { selectedPreset = preset.first }
                        .padding(12.dp)
                ) {
                    Text(preset.first, fontWeight = FontWeight.Bold, color = if (isSelected) PinkAccent else TextPrimary, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(preset.second.take(45) + "...", color = TextSecondary, fontSize = 10.sp, maxLines = 1)
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Ratio Select
        Text("Aspect Ratio", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)
        Spacer(modifier = Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ratios.take(3).forEach { ratio ->
                val isSelected = selectedRatio == ratio.first
                val bgCol by animateColorAsState(targetValue = if (isSelected) PinkAccent.copy(alpha = 0.2f) else SlateCard)
                
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(bgCol)
                        .border(1.dp, if (isSelected) PinkAccent else BorderAccent, RoundedCornerShape(8.dp))
                        .clickable { selectedRatio = ratio.first }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(ratio.first.substringBefore(" "), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (isSelected) PinkAccent else TextPrimary)
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        Button(
            onClick = {
                if (prompt.trim().isNotEmpty()) {
                    val finalPresetPrompt = presets.find { it.first == selectedPreset }?.second ?: ""
                    val finalRatioText = ratios.find { it.first == selectedRatio }?.second ?: ""
                    val finalPromptText = "$prompt, styled in: $finalPresetPrompt. Aspect scale: $finalRatioText."
                    onGenerateCustomImage(finalPromptText)
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            colors = ButtonDefaults.buttonColors(containerColor = PinkAccent),
            shape = RoundedCornerShape(12.dp),
            enabled = prompt.trim().isNotEmpty()
        ) {
            Icon(Icons.Default.Palette, contentDescription = null, tint = SlateDark)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Generate Masterpiece", color = SlateDark, fontWeight = FontWeight.ExtraBold)
        }
    }
}

/**
 * SETTINGS AND ABOUT TAB
 */
@Composable
fun SettingsTabContent(
    themeMode: String,
    fontSizeState: FontSize,
    appLanguage: String,
    setThemeMode: (String) -> Unit,
    setFontSize: (FontSize) -> Unit,
    setLanguage: (String) -> Unit,
    clearHistory: () -> Unit,
    exportHistory: () -> Unit
) {
    val languages = listOf("English", "Spanish", "French", "German", "Japanese", "Portuguese")
    val fontSizes = FontSize.values()
    val themes = listOf("Dark", "Light", "System")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .statusBarsPadding()
            .verticalScroll(androidx.compose.foundation.rememberScrollState())
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(IndigoAccent.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Palette, contentDescription = null, sizeFilter = 18, tint = IndigoAccent)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "Settings & Interface",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = TextPrimary
                )
                Text(
                    text = "Offline local configuration",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // System Settings Card
        Card(
            colors = CardDefaults.cardColors(containerColor = SlateCard),
            border = BorderStroke(1.dp, BorderAccent),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                // Theme Selection
                Column {
                    Text("Theme Style", fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        themes.forEach { theme ->
                            val isSelected = themeMode == theme
                            val col by animateColorAsState(targetValue = if (isSelected) IndigoAccent.copy(alpha = 0.2f) else SlateDark)
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(col)
                                    .border(1.dp, if (isSelected) IndigoAccent else BorderAccent, RoundedCornerShape(8.dp))
                                    .clickable { setThemeMode(theme) }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(theme, color = if (isSelected) IndigoAccent else TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // Font Size selection
                Column {
                    Text("Response Font Scale", fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        fontSizes.forEach { fs ->
                            val isSelected = fontSizeState == fs
                            val col by animateColorAsState(targetValue = if (isSelected) IndigoAccent.copy(alpha = 0.2f) else SlateDark)
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(col)
                                    .border(1.dp, if (isSelected) IndigoAccent else BorderAccent, RoundedCornerShape(8.dp))
                                    .clickable { setFontSize(fs) }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(fs.label, color = if (isSelected) IndigoAccent else TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // Language
                Column {
                    Text("Assistant Language", fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    var langExpanded by remember { mutableStateOf(false) }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(SlateDark)
                            .border(1.dp, BorderAccent, RoundedCornerShape(8.dp))
                            .clickable { langExpanded = true }
                            .padding(12.dp)
                    ) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(appLanguage, color = TextPrimary, fontSize = 13.sp)
                            Icon(Icons.Default.Palette, contentDescription = null, sizeFilter = 14, tint = TextSecondary)
                        }
                        DropdownMenu(
                            expanded = langExpanded,
                            onDismissRequest = { langExpanded = false },
                            modifier = Modifier.background(SlateCard)
                        ) {
                            languages.forEach { lang ->
                                DropdownMenuItem(
                                    text = { Text(lang, color = TextPrimary) },
                                    onClick = {
                                        setLanguage(lang)
                                        langExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Data / Diagnostics Controls Card
        Card(
            colors = CardDefaults.cardColors(containerColor = SlateCard),
            border = BorderStroke(1.dp, BorderAccent),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Memory & Exports", fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 14.sp)
                
                Button(
                    onClick = exportHistory,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = SlateDark),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, BorderAccent)
                ) {
                    Icon(Icons.Default.TextSnippet, contentDescription = null, sizeFilter = 14, tint = TextPrimary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Export Current Chat Log", color = TextPrimary, fontSize = 13.sp)
                }

                Button(
                    onClick = clearHistory,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = SlateDark),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, Color(0xFFE54646).copy(alpha = 0.3f))
                ) {
                    Icon(Icons.Default.DeleteSweep, contentDescription = null, sizeFilter = 14, tint = Color(0xFFE54646))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Delete All Chat History", color = Color(0xFFE54646), fontSize = 13.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // About developers Card
        Card(
            colors = CardDefaults.cardColors(containerColor = SlateCard),
            border = BorderStroke(1.dp, BorderAccent),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            Brush.radialGradient(
                                listOf(PinkAccent, IndigoAccent)
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color(0xFF381E72), modifier = Modifier.size(28.dp))
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text("InfiniteAI", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = TextPrimary)
                Text("Version 1.0", fontSize = 11.sp, color = TextSecondary)
                Spacer(modifier = Modifier.height(8.dp))
                
                Divider(modifier = Modifier.padding(vertical = 4.dp), color = BorderAccent)
                
                Text("Developed by", fontSize = 11.sp, color = TextSecondary, modifier = Modifier.padding(top = 4.dp))
                Text("JK QuantumTech", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = PinkAccent)
                
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "AI-powered Android Assistant built using modern Android technologies (Kotlin, Jetpack Compose, Room and Gemini).",
                    textAlign = TextAlign.Center,
                    fontSize = 11.sp,
                    color = TextSecondary,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
            }
        }
    }
}

/**
 * Onboarding/Empty State view
 */
@Composable
fun OnboardingEmptyState(onPromptSelected: (String, InputMode) -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(
                    Brush.radialGradient(
                        colors = listOf(PinkAccent, IndigoAccent)
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.AutoAwesome,
                contentDescription = null,
                tint = Color(0xFF381E72),
                modifier = Modifier.size(36.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "InfiniteAI Assistant",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 0.5.sp
            ),
            color = TextPrimary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "Unlimited gateway to high-performance chat and stunning image generation.",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(28.dp))

        Text(
            text = "SELECT AN ACTION",
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            ),
            color = PinkAccent,
            modifier = Modifier.padding(bottom = 6.dp)
        )

        PresetCard(
            title = "Create Cosmic Art",
            description = "Generate a neon astronaut floating in digital nebula",
            iconColor = PinkAccent,
            onClick = { onPromptSelected("A professional neon astronaut floating inside a colorful vibrant cyberdigital space nebula, highly detailed, photorealistic, 4k", InputMode.IMAGE) }
        )

        Spacer(modifier = Modifier.height(8.dp))

        PresetCard(
            title = "Multimodal Photo Editing",
            description = "Load a photo from library and ask to edit or change style",
            iconColor = IndigoAccent,
            onClick = { onPromptSelected("Turn this image into a majestic retro cyberpunk scene with purple rain.", InputMode.CHAT) }
        )

        Spacer(modifier = Modifier.height(8.dp))

        PresetCard(
            title = "QuantumTech Assistant",
            description = "Ask questions, write essays, code, or generate text offline",
            iconColor = PinkAccent,
            onClick = { onPromptSelected("Write an immersive and highly engaging sci-fi short story of about 3 paragraphs, set on a distant moon.", InputMode.CHAT) }
        )
    }
}

@Composable
fun PresetCard(
    title: String,
    description: String,
    iconColor: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = SlateCard),
        border = BorderStroke(1.dp, BorderAccent),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(iconColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(16.dp)
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = TextPrimary
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
        }
    }
}

/**
 * INDIVIDUAL CHAT MESSAGE BUBBLE
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MessageItem(
    message: ChatMessage,
    onImageClicked: () -> Unit,
    onDownloadClicked: () -> Unit,
    isDownloading: Boolean,
    onSpeakClicked: () -> Unit,
    isSpeaking: Boolean,
    fontSizeState: FontSize
) {
    val isUser = message.role == "user"
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        if (!isUser) {
            // Assistant Profile Avatar
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(IndigoAccent, PinkAccent)
                        )
                    )
                    .padding(1.dp)
                    .border(1.dp, BorderAccent, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = TextPrimary,
                    modifier = Modifier.size(16.dp)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
        }

        // Message Card
        Column(
            horizontalAlignment = if (isUser) Alignment.End else Alignment.Start,
            modifier = Modifier.weight(1f, fill = false)
        ) {
            if (message.type == "image" || message.generatedImageUrl != null) {
                // Generated image card bubble
                Card(
                    modifier = Modifier
                        .widthIn(max = 280.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .combinedClickable(
                            onClick = onImageClicked,
                            onLongClick = onDownloadClicked
                        ),
                    colors = CardDefaults.cardColors(containerColor = SlateCard),
                    border = BorderStroke(1.dp, BorderAccent)
                ) {
                    Column {
                        // Image Loading viewport
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(260.dp)
                                .background(SlateDark),
                            contentAlignment = Alignment.Center
                        ) {
                            AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(message.generatedImageUrl ?: message.text)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = "AI Artwork Creation",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }

                        // Options footer
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(SlateCard)
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = "AI Masterpiece",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "Tap preview • Hold save",
                                    fontSize = 10.sp,
                                    color = TextSecondary
                                )
                            }

                            Row {
                                IconButton(
                                    onClick = {
                                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                            type = "text/plain"
                                            putExtra(Intent.EXTRA_TEXT, "Look at this spectacular artwork generated by InfiniteAI: ${message.generatedImageUrl ?: message.text}")
                                        }
                                        context.startActivity(Intent.createChooser(shareIntent, "Share Art URL"))
                                    }
                                ) {
                                    Icon(Icons.Default.Share, contentDescription = "Share Link", sizeFilter = 16, tint = TextSecondary)
                                }
                                
                                if (isDownloading) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(20.dp),
                                        color = PinkAccent,
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    IconButton(onClick = onDownloadClicked) {
                                        Icon(
                                            Icons.Default.Download,
                                            contentDescription = "Save to gallery",
                                            tint = PinkAccent,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                // Regular or Multimodal text chat message bubble
                val bubbleBg = if (isUser) SlateCard else SlateCard
                val border = if (isUser) BorderStroke(1.dp, IndigoAccent) else BorderStroke(1.dp, BorderAccent)

                Surface(
                    color = bubbleBg,
                    border = border,
                    shape = RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = if (isUser) 16.dp else 4.dp,
                        bottomEnd = if (isUser) 4.dp else 16.dp
                    )
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        // User message might contain an attached photo
                        if (message.localImageUri != null) {
                            Box(
                                modifier = Modifier
                                    .padding(bottom = 8.dp)
                                    .size(160.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .border(1.dp, PinkAccent, RoundedCornerShape(8.dp))
                            ) {
                                AsyncImage(
                                    model = message.localImageUri,
                                    contentDescription = "Uploaded edit source",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            }
                        }

                        Text(
                            text = message.text,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontSize = (14.sp.value * fontSizeState.scale).sp,
                                lineHeight = (20.sp.value * fontSizeState.scale).sp
                            ),
                            color = TextPrimary
                        )

                        // Action icons for copying, TTS, sharing responses
                        if (!isUser) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                // Speak text (TTS)
                                IconButton(onClick = onSpeakClicked, modifier = Modifier.size(28.dp)) {
                                    Icon(
                                        Icons.Default.VolumeUp,
                                        contentDescription = "Speak response",
                                        tint = if (isSpeaking) PinkAccent else TextSecondary,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                                
                                // Copy responses
                                IconButton(
                                    onClick = {
                                        clipboardManager.setText(AnnotatedString(message.text))
                                        Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(Icons.Default.ContentCopy, contentDescription = "Copy text", sizeFilter = 14, tint = TextSecondary)
                                }

                                // Share response
                                IconButton(
                                    onClick = {
                                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                            type = "text/plain"
                                            putExtra(Intent.EXTRA_TEXT, message.text)
                                        }
                                        context.startActivity(Intent.createChooser(shareIntent, "Share via"))
                                    },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(Icons.Default.Share, contentDescription = "Share text", sizeFilter = 14, tint = TextSecondary)
                                }
                            }
                        }
                    }
                }
            }
        }

        if (isUser) {
            Spacer(modifier = Modifier.width(10.dp))
            // User Avatar profile
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(SlateSurface),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "ME",
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    fontSize = 12.sp
                )
            }
        }
    }
}

/**
 * Beautiful loading indicators
 */
@Composable
fun GeneratingIndicator(text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(SlateDark)
                .border(1.dp, BorderAccent, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(
                color = PinkAccent,
                strokeWidth = 2.dp,
                modifier = Modifier.size(16.dp)
            )
        }
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Card(
            colors = CardDefaults.cardColors(containerColor = SlateCard),
            border = BorderStroke(1.dp, BorderAccent),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = text,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
        }
    }
}

