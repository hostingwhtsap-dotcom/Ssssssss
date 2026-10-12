package com.example

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardDoubleArrowDown
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import android.os.Build
import com.example.model.ChatMessage
import com.example.model.MessageAttachmentType
import com.example.model.MessageStatus
import com.example.model.SharedContentItem
import com.example.ui.AttachmentMenuSheet
import com.example.ui.AudioCallScreen
import com.example.ui.AudioPickerDialog
import com.example.ui.ChatTopBar
import com.example.ui.DatePillBadge
import com.example.ui.EmojiPickerView
import com.example.ui.EncryptionNoticeBanner
import com.example.ui.ImageViewerDialog
import com.example.ui.MediaPreviewDialog
import com.example.ui.MessageBubbleItem
import com.example.ui.MessageComposer
import com.example.ui.SharedMediaStageBar
import com.example.ui.VideoCallScreen
import com.example.ui.WhatsAppWallpaper
import com.example.ui.theme.CustomerSupportTheme
import com.example.ui.theme.WhatsAppChatBg
import com.example.ui.theme.WhatsAppPinkAccent
import com.example.ui.theme.WhatsAppTextPrimary
import com.example.util.MediaStorageHelper
import com.example.util.VoiceAudioHelper
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private const val TAG = "MainActivity"

class MainActivity : ComponentActivity() {
  private val currentIntentState = mutableStateOf<Intent?>(null)

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    currentIntentState.value = intent
    setContent {
      CustomerSupportTheme {
        CustomerChatApp(incomingIntent = currentIntentState.value)
      }
    }
  }

  override fun onNewIntent(intent: Intent) {
    super.onNewIntent(intent)
    setIntent(intent)
    currentIntentState.value = intent
  }
}

enum class ActiveCallState {
  NONE,
  AUDIO,
  VIDEO
}

data class PendingMediaPreview(
  val file: File,
  val isVideo: Boolean,
  val duration: String = "",
  val thumbnailFile: File? = null
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerChatApp(incomingIntent: Intent? = null) {
  val context = LocalContext.current
  val voiceAudioHelper = remember { VoiceAudioHelper(context) }

  // Load persistent messages from internal storage
  val messages = remember {
    mutableStateListOf<ChatMessage>().apply {
      val saved = MediaStorageHelper.loadMessages(context)
      addAll(saved)
    }
  }

  // Staged system share target items
  val stagedSharedItems = remember { mutableStateListOf<SharedContentItem>() }
  var showAudioPickerDialog by remember { mutableStateOf(false) }

  var inputText by remember { mutableStateOf("") }
  var isTyping by remember { mutableStateOf(false) }

  // Handle incoming Android system share intents
  LaunchedEffect(incomingIntent) {
    val received = incomingIntent ?: return@LaunchedEffect
    if (received.action == Intent.ACTION_SEND || received.action == Intent.ACTION_SEND_MULTIPLE) {
      handleIncomingShareIntent(context, received) { items, sharedText ->
        if (items.isNotEmpty()) {
          stagedSharedItems.addAll(items)
        }
        if (sharedText.isNotBlank()) {
          inputText = if (inputText.isNotBlank()) "$inputText\n$sharedText" else sharedText
        }
      }
    }
  }

  // Voice recording states matching Screenshot 1 and WhatsApp gestures
  var isRecording by remember { mutableStateOf(false) }
  var isRecordingLocked by remember { mutableStateOf(false) }
  var isRecordingPaused by remember { mutableStateOf(false) }
  var recordingDurationSeconds by remember { mutableIntStateOf(0) }
  var recordingAmplitude by remember { mutableIntStateOf(0) }
  val liveWaveformSamples = remember { mutableStateListOf<Float>() }
  var currentlyPlayingAudioPath by remember { mutableStateOf<String?>(null) }
  var audioPlaybackProgress by remember { mutableFloatStateOf(0f) }
  var audioPlaybackPositionMs by remember { mutableIntStateOf(0) }

  var showEmojiPicker by remember { mutableStateOf(false) }
  var showAttachmentSheet by remember { mutableStateOf(false) }
  var activeCallState by remember { mutableStateOf(ActiveCallState.NONE) }
  var pendingMediaPreview by remember { mutableStateOf<PendingMediaPreview?>(null) }
  var viewerImagePath by remember { mutableStateOf<String?>(null) }

  var hasCameraPermissionState by remember {
    mutableStateOf(
      ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
    )
  }

  val listState = rememberLazyListState()
  val coroutineScope = rememberCoroutineScope()
  val snackbarHostState = remember { SnackbarHostState() }
  val attachmentSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

  // Scroll to bottom button visibility (matched to Screenshot 3)
  val showScrollToBottom by remember {
    derivedStateOf {
      listState.firstVisibleItemIndex > 1
    }
  }

  fun getTimeString(): String {
    val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
    return timeFormat.format(Date())
  }

  fun appendAndSaveMessage(newMsg: ChatMessage) {
    messages.add(newMsg)
    MediaStorageHelper.saveMessages(context, messages)
    coroutineScope.launch {
      delay(50)
      listState.animateScrollToItem(messages.size + 2)
    }
  }

  fun triggerAutoReply(triggerTopic: String) {
    coroutineScope.launch {
      delay(700)
      isTyping = true
      delay(1300)
      isTyping = false

      val replyText = when {
        triggerTopic.contains("photo", ignoreCase = true) || triggerTopic.contains("image", ignoreCase = true) ->
          "We have received your image attachment. Our technical specialist is inspecting it now."
        triggerTopic.contains("video", ignoreCase = true) ->
          "Thank you for sharing the video clip. A customer representative is reviewing it."
        triggerTopic.contains("document", ignoreCase = true) || triggerTopic.contains("file", ignoreCase = true) ->
          "Thank you for providing the document. It has been queued for verification."
        triggerTopic.contains("voice", ignoreCase = true) || triggerTopic.contains("audio", ignoreCase = true) ->
          "Voice note received. A support agent is listening to your message."
        else ->
          "Thank you for contacting Apex Customer Care! An agent will respond shortly."
      }

      val replyMessage = ChatMessage(
        id = "agent_${System.currentTimeMillis()}",
        text = replyText,
        timestamp = getTimeString(),
        isOutgoing = false
      )
      appendAndSaveMessage(replyMessage)
    }
  }

  // Contract Launchers for Attachments & Pickers
  val documentPickerLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.GetContent()
  ) { uri: Uri? ->
    uri?.let {
      val (fileName, sizeStr, _) = MediaStorageHelper.getFileMetadata(context, it, "Document.pdf")
      val ext = fileName.substringAfterLast('.', "pdf")
      val savedFile = MediaStorageHelper.copyUriToInternalStorage(context, it, ext)
      if (savedFile != null) {
        val newMsg = ChatMessage(
          id = "doc_${System.currentTimeMillis()}",
          text = "",
          timestamp = getTimeString(),
          isOutgoing = true,
          attachmentType = MessageAttachmentType.DOCUMENT,
          mediaPath = savedFile.absolutePath,
          attachmentName = fileName,
          attachmentSize = sizeStr
        )
        appendAndSaveMessage(newMsg)
        triggerAutoReply("document")
      }
    }
  }

  val galleryPickerLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.PickVisualMedia()
  ) { uri: Uri? ->
    uri?.let {
      val mimeType = context.contentResolver.getType(it) ?: ""
      val isVideo = mimeType.startsWith("video/") ||
        (it.lastPathSegment?.lowercase()?.let { name ->
          name.endsWith(".mp4") || name.endsWith(".mkv") || name.endsWith(".mov") || name.endsWith(".webm") || name.endsWith(".3gp")
        } == true)
      val ext = if (isVideo) "mp4" else "jpg"
      val savedFile = MediaStorageHelper.copyUriToInternalStorage(context, it, ext)
      if (savedFile != null) {
        if (isVideo) {
          val (thumbnail, duration) = MediaStorageHelper.extractVideoMetadata(context, savedFile)
          pendingMediaPreview = PendingMediaPreview(
            file = savedFile,
            isVideo = true,
            duration = duration,
            thumbnailFile = thumbnail
          )
        } else {
          pendingMediaPreview = PendingMediaPreview(
            file = savedFile,
            isVideo = false
          )
        }
      }
    }
  }

  val cameraCaptureLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.TakePicturePreview()
  ) { bitmap ->
    if (bitmap != null) {
      val savedFile = MediaStorageHelper.saveBitmapToInternalStorage(context, bitmap, "CAM")
      if (savedFile != null) {
        pendingMediaPreview = PendingMediaPreview(
          file = savedFile,
          isVideo = false
        )
      }
    }
  }

  val audioSAFLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.OpenDocument()
  ) { uri: Uri? ->
    uri?.let {
      try {
        val (fileName, sizeStr, durationStr) = MediaStorageHelper.getFileMetadata(context, it, "Audio.mp3")
        val ext = fileName.substringAfterLast('.', "mp3")
        val savedFile = MediaStorageHelper.copyUriToInternalStorage(context, it, ext)
        if (savedFile != null) {
          val sizeLabel = if (durationStr.isNotBlank()) "$durationStr • $sizeStr" else sizeStr
          val newMsg = ChatMessage(
            id = "audio_${System.currentTimeMillis()}",
            text = "",
            timestamp = getTimeString(),
            isOutgoing = true,
            status = MessageStatus.READ,
            attachmentType = MessageAttachmentType.AUDIO,
            mediaPath = savedFile.absolutePath,
            attachmentName = fileName,
            attachmentSize = sizeLabel,
            audioDuration = durationStr
          )
          appendAndSaveMessage(newMsg)
          triggerAutoReply("audio")
        }
      } catch (e: Exception) {
        Log.e(TAG, "Error handling picked audio file", e)
      }
    }
  }

  // Permission Launchers
  val cameraPermissionLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.RequestPermission()
  ) { isGranted ->
    hasCameraPermissionState = isGranted
    if (isGranted) {
      cameraCaptureLauncher.launch(null)
    } else {
      coroutineScope.launch {
        snackbarHostState.showSnackbar("Camera permission is required to capture photos.")
      }
    }
  }

  val videoCallPermissionsLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.RequestMultiplePermissions()
  ) { permissionsMap ->
    val cameraGranted = permissionsMap[Manifest.permission.CAMERA] ?: false
    hasCameraPermissionState = cameraGranted
    if (cameraGranted) {
      activeCallState = ActiveCallState.VIDEO
    } else {
      coroutineScope.launch {
        snackbarHostState.showSnackbar("Camera permission is required to start a video call.")
      }
    }
  }

  val micPermissionLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.RequestPermission()
  ) { isGranted ->
    if (isGranted) {
      val file = voiceAudioHelper.startRecording()
      if (file != null) {
        isRecording = true
        isRecordingLocked = true
        isRecordingPaused = false
        recordingDurationSeconds = 0
        liveWaveformSamples.clear()
      }
    } else {
      coroutineScope.launch {
        snackbarHostState.showSnackbar("Microphone permission is required for voice messages.")
      }
    }
  }

  val audioCallPermissionLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.RequestPermission()
  ) { isGranted ->
    if (isGranted) {
      activeCallState = ActiveCallState.AUDIO
    } else {
      coroutineScope.launch {
        snackbarHostState.showSnackbar("Microphone permission is required for voice calls.")
      }
    }
  }

  fun openCameraDirectly() {
    val permissionStatus = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
    if (permissionStatus == PackageManager.PERMISSION_GRANTED) {
      hasCameraPermissionState = true
      cameraCaptureLauncher.launch(null)
    } else {
      cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
    }
  }

  fun startVoiceRecording(locked: Boolean = false) {
    val permissionStatus = ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO)
    if (permissionStatus == PackageManager.PERMISSION_GRANTED) {
      val file = voiceAudioHelper.startRecording()
      if (file != null) {
        isRecording = true
        isRecordingLocked = locked
        isRecordingPaused = false
        recordingDurationSeconds = 0
        liveWaveformSamples.clear()
      }
    } else {
      micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
    }
  }

  fun lockVoiceRecording() {
    if (isRecording) {
      isRecordingLocked = true
    } else {
      startVoiceRecording(locked = true)
    }
  }

  fun startAudioCall() {
    val micStatus = ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO)
    if (micStatus == PackageManager.PERMISSION_GRANTED) {
      activeCallState = ActiveCallState.AUDIO
    } else {
      audioCallPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
    }
  }

  fun startVideoCall() {
    val cameraStatus = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
    if (cameraStatus == PackageManager.PERMISSION_GRANTED) {
      hasCameraPermissionState = true
      activeCallState = ActiveCallState.VIDEO
    } else {
      videoCallPermissionsLauncher.launch(
        arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO)
      )
    }
  }

  // Live audio waveform & timer polling from microphone amplitude levels
  LaunchedEffect(isRecording, isRecordingPaused) {
    if (isRecording && !isRecordingPaused) {
      var tickerMs = 0
      while (isRecording && !isRecordingPaused) {
        delay(70)
        val amp = voiceAudioHelper.getMaxAmplitude()
        recordingAmplitude = amp
        val normalized = (amp.toFloat() / 24000f).coerceIn(0.08f, 1.0f)
        liveWaveformSamples.add(normalized)
        if (liveWaveformSamples.size > 120) {
          liveWaveformSamples.removeAt(0)
        }
        tickerMs += 70
        if (tickerMs >= 1000) {
          tickerMs = 0
          recordingDurationSeconds++
        }
      }
    }
  }

  // Cleanup on disposal
  DisposableEffect(Unit) {
    onDispose {
      voiceAudioHelper.releaseAll()
    }
  }

  fun sendTextMessage() {
    val textToSend = inputText.trim()

    // Dispatch staged system shared content items first if any
    if (stagedSharedItems.isNotEmpty()) {
      val itemsToSend = stagedSharedItems.toList()
      stagedSharedItems.clear()

      itemsToSend.forEachIndexed { index, shared ->
        val captionForMsg = if (index == itemsToSend.size - 1) textToSend else ""
        val newMsg = ChatMessage(
          id = "share_${System.currentTimeMillis()}_$index",
          text = captionForMsg,
          timestamp = getTimeString(),
          isOutgoing = true,
          status = MessageStatus.READ,
          attachmentType = shared.type,
          mediaPath = shared.file.absolutePath,
          thumbnailPath = shared.thumbnailFile?.absolutePath,
          attachmentName = shared.fileName,
          attachmentSize = shared.sizeOrDuration,
          audioDuration = if (shared.type == MessageAttachmentType.AUDIO || shared.type == MessageAttachmentType.VIDEO) shared.sizeOrDuration else null
        )
        appendAndSaveMessage(newMsg)
      }
      inputText = ""
      showEmojiPicker = false
      triggerAutoReply("shared content")
      return
    }

    if (textToSend.isEmpty()) return

    val newMsg = ChatMessage(
      id = "user_${System.currentTimeMillis()}",
      text = textToSend,
      timestamp = getTimeString(),
      isOutgoing = true,
      status = MessageStatus.READ
    )
    appendAndSaveMessage(newMsg)
    inputText = ""
    showEmojiPicker = false

    triggerAutoReply(textToSend)
  }

  fun finishAndSendVoiceNote() {
    val recordedFile = voiceAudioHelper.stopRecording()
    val mins = recordingDurationSeconds / 60
    val secs = recordingDurationSeconds % 60
    val durationStr = String.format("%d:%02d", mins, secs)

    // Resample actual live microphone amplitude levels into 30 bars
    val waveformData = MediaStorageHelper.resampleWaveform(liveWaveformSamples.toList(), targetCount = 30)

    isRecording = false
    isRecordingLocked = false
    isRecordingPaused = false
    recordingDurationSeconds = 0
    recordingAmplitude = 0
    liveWaveformSamples.clear()

    if (recordedFile != null && recordedFile.exists()) {
      val newMsg = ChatMessage(
        id = "voice_${System.currentTimeMillis()}",
        text = "",
        timestamp = getTimeString(),
        isOutgoing = true,
        status = MessageStatus.READ,
        attachmentType = MessageAttachmentType.VOICE_NOTE,
        mediaPath = recordedFile.absolutePath,
        audioDuration = durationStr,
        waveform = waveformData
      )
      appendAndSaveMessage(newMsg)
      triggerAutoReply("voice")
    }
  }

  fun cancelVoiceRecording() {
    voiceAudioHelper.cancelRecording()
    isRecording = false
    isRecordingLocked = false
    isRecordingPaused = false
    recordingDurationSeconds = 0
    recordingAmplitude = 0
    liveWaveformSamples.clear()
  }

  fun togglePauseRecording() {
    if (isRecordingPaused) {
      voiceAudioHelper.resumeRecording()
      isRecordingPaused = false
    } else {
      voiceAudioHelper.pauseRecording()
      isRecordingPaused = true
    }
  }

  fun playVideo(videoPath: String) {
    try {
      val file = File(videoPath)
      if (file.exists()) {
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val intent = Intent(Intent.ACTION_VIEW).apply {
          setDataAndType(uri, "video/*")
          addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(intent)
      }
    } catch (e: Exception) {
      Log.e(TAG, "Error opening video", e)
      coroutineScope.launch {
        snackbarHostState.showSnackbar("Unable to play video on this device.")
      }
    }
  }

  // Keyboard and navigation bar insets
  val imeBottom = WindowInsets.ime.asPaddingValues().calculateBottomPadding()
  val navBottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
  val composerBottomPadding = if (imeBottom > 0.dp) imeBottom else navBottom

  // Active Calling Screens
  when (activeCallState) {
    ActiveCallState.AUDIO -> {
      AudioCallScreen(onEndCall = { activeCallState = ActiveCallState.NONE })
      return
    }
    ActiveCallState.VIDEO -> {
      VideoCallScreen(
        hasCameraPermission = hasCameraPermissionState,
        onEndCall = { activeCallState = ActiveCallState.NONE },
        onScreenShareToggled = { isSharing ->
          Log.d(TAG, "Screen sharing toggled: $isSharing")
        }
      )
      return
    }
    ActiveCallState.NONE -> Unit
  }

  Scaffold(
    modifier = Modifier.fillMaxSize(),
    snackbarHost = { SnackbarHost(snackbarHostState) },
    contentWindowInsets = WindowInsets(0.dp, 0.dp, 0.dp, 0.dp),
    topBar = {
      ChatTopBar(
        isTyping = isTyping,
        onAudioCallClick = { startAudioCall() },
        onVideoCallClick = { startVideoCall() },
        onBackClick = {
          coroutineScope.launch {
            snackbarHostState.showSnackbar("Apex Customer Care")
          }
        }
      )
    }
  ) { paddingValues ->
    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(top = paddingValues.calculateTopPadding())
        .background(WhatsAppChatBg)
    ) {
      WhatsAppWallpaper {
        Column(
          modifier = Modifier
            .fillMaxSize()
            .padding(bottom = composerBottomPadding)
        ) {
          // Chat messages list
          LazyColumn(
            state = listState,
            modifier = Modifier
              .weight(1f)
              .fillMaxWidth()
              .testTag("chat_messages_list")
          ) {
            // End-to-End Encryption Notice Banner
            item(key = "encryption_banner") {
              EncryptionNoticeBanner()
            }

            // Date Badge (matched to Screenshot 3: "Today")
            item(key = "date_pill") {
              DatePillBadge(dateText = "Today")
            }

            // Real messages list
            items(messages, key = { it.id }) { message ->
              val isPlayingThis = message.mediaPath != null && currentlyPlayingAudioPath == message.mediaPath
              MessageBubbleItem(
                message = message,
                isAudioPlaying = isPlayingThis,
                audioPlaybackProgress = if (isPlayingThis) audioPlaybackProgress else 0f,
                onImageClick = { path -> viewerImagePath = path },
                onVideoClick = { path -> playVideo(path) },
                onAudioTogglePlay = { path ->
                  voiceAudioHelper.togglePlay(path) { isPlaying, posMs, _, progress ->
                    if (isPlaying) {
                      currentlyPlayingAudioPath = path
                      audioPlaybackProgress = progress
                      audioPlaybackPositionMs = posMs
                    } else {
                      if (currentlyPlayingAudioPath == path) {
                        currentlyPlayingAudioPath = null
                        audioPlaybackProgress = 0f
                        audioPlaybackPositionMs = 0
                      }
                    }
                  }
                },
                onSeekAudio = { path, progress ->
                  voiceAudioHelper.seekTo(path, progress)
                  audioPlaybackProgress = progress
                }
              )
            }

            item(key = "bottom_space") {
              Spacer(modifier = Modifier.height(4.dp))
            }
          }

          // Staged System Shared Content Bar (Android Sharesheet Target)
          if (stagedSharedItems.isNotEmpty()) {
            SharedMediaStageBar(
              items = stagedSharedItems,
              onRemoveItem = { item -> stagedSharedItems.remove(item) },
              onClearAll = { stagedSharedItems.clear() }
            )
          }

          // Message Composer & Voice Recording Panel matching Screenshots
          MessageComposer(
            text = inputText,
            onTextChanged = { inputText = it },
            onSendMessage = { sendTextMessage() },
            onEmojiClick = { showEmojiPicker = !showEmojiPicker },
            onAttachmentClick = { showAttachmentSheet = true },
            onCameraClick = { openCameraDirectly() },
            isRecording = isRecording,
            isRecordingLocked = isRecordingLocked,
            isRecordingPaused = isRecordingPaused,
            recordingDurationSeconds = recordingDurationSeconds,
            recordingAmplitude = recordingAmplitude,
            liveWaveform = liveWaveformSamples,
            onMicClick = { startVoiceRecording(locked = true) },
            onStartRecording = { startVoiceRecording(locked = false) },
            onLockRecording = { lockVoiceRecording() },
            onTogglePauseRecording = { togglePauseRecording() },
            onCancelRecording = { cancelVoiceRecording() },
            onSendRecording = { finishAndSendVoiceNote() },
            hasPendingAttachments = stagedSharedItems.isNotEmpty()
          )

          // Integrated Emoji Picker View (Visible when emoji icon is toggled)
          if (showEmojiPicker) {
            EmojiPickerView(
              onEmojiSelected = { emoji ->
                inputText += emoji
              },
              onBackspaceClick = {
                if (inputText.isNotEmpty()) {
                  inputText = inputText.dropLast(1)
                }
              }
            )
          }
        }
      }

      // Floating Scroll-To-Bottom FAB matching Screenshot 3
      AnimatedVisibility(
        visible = showScrollToBottom,
        enter = fadeIn(),
        exit = fadeOut(),
        modifier = Modifier
          .align(Alignment.BottomEnd)
          .padding(end = 16.dp, bottom = composerBottomPadding + 64.dp)
      ) {
        Surface(
          shape = CircleShape,
          color = Color.White,
          shadowElevation = 3.dp,
          modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .clickable {
              coroutineScope.launch {
                listState.animateScrollToItem(messages.size + 1)
              }
            }
            .testTag("scroll_to_bottom_button")
        ) {
          Box(contentAlignment = Alignment.Center) {
            Icon(
              imageVector = Icons.Filled.KeyboardDoubleArrowDown,
              contentDescription = "Scroll to bottom",
              tint = Color(0xFF54656F),
              modifier = Modifier.size(20.dp)
            )
          }
        }
      }
    }
  }

  // Pre-Send Media Preview Dialog (Images & Videos)
  pendingMediaPreview?.let { preview ->
    MediaPreviewDialog(
      mediaFile = preview.file,
      isVideo = preview.isVideo,
      videoDuration = preview.duration,
      thumbnailFile = preview.thumbnailFile,
      onSend = { caption ->
        val newMsg = if (preview.isVideo) {
          ChatMessage(
            id = "vid_${System.currentTimeMillis()}",
            text = caption,
            timestamp = getTimeString(),
            isOutgoing = true,
            attachmentType = MessageAttachmentType.VIDEO,
            mediaPath = preview.file.absolutePath,
            thumbnailPath = preview.thumbnailFile?.absolutePath,
            audioDuration = preview.duration
          )
        } else {
          ChatMessage(
            id = "img_${System.currentTimeMillis()}",
            text = caption,
            timestamp = getTimeString(),
            isOutgoing = true,
            attachmentType = MessageAttachmentType.IMAGE,
            mediaPath = preview.file.absolutePath,
            attachmentName = preview.file.name
          )
        }
        appendAndSaveMessage(newMsg)
        pendingMediaPreview = null
        triggerAutoReply(if (preview.isVideo) "video" else "photo")
      },
      onCancel = {
        pendingMediaPreview = null
      }
    )
  }

  // Full Screen Image Viewer on tap
  viewerImagePath?.let { path ->
    ImageViewerDialog(
      imagePath = path,
      onDismiss = { viewerImagePath = null }
    )
  }

  // WhatsApp Attachment Menu Bottom Sheet (4 options: Gallery, Camera, Documents, Audio)
  if (showAttachmentSheet) {
    AttachmentMenuSheet(
      sheetState = attachmentSheetState,
      onDismiss = { showAttachmentSheet = false },
      onPickGallery = {
        galleryPickerLauncher.launch(
          PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo)
        )
      },
      onCaptureCamera = { openCameraDirectly() },
      onPickDocuments = { documentPickerLauncher.launch("*/*") },
      onPickAudio = {
        showAudioPickerDialog = true
      }
    )
  }

  // WhatsApp Audio Picker Dialog (Device Media Query & Audio Formats: MP3, M4A, AAC, WAV, OGG, FLAC)
  if (showAudioPickerDialog) {
    AudioPickerDialog(
      onDismiss = { showAudioPickerDialog = false },
      onSelectAudio = { uri, fileName ->
        showAudioPickerDialog = false
        try {
          val (actualName, sizeStr, durationStr) = MediaStorageHelper.getFileMetadata(context, uri, fileName)
          val ext = actualName.substringAfterLast('.', "mp3")
          val savedFile = MediaStorageHelper.copyUriToInternalStorage(context, uri, ext)
          if (savedFile != null) {
            val sizeLabel = if (durationStr.isNotBlank()) "$durationStr • $sizeStr" else sizeStr
            val newMsg = ChatMessage(
              id = "audio_${System.currentTimeMillis()}",
              text = "",
              timestamp = getTimeString(),
              isOutgoing = true,
              status = MessageStatus.READ,
              attachmentType = MessageAttachmentType.AUDIO,
              mediaPath = savedFile.absolutePath,
              attachmentName = actualName,
              attachmentSize = sizeLabel,
              audioDuration = durationStr
            )
            appendAndSaveMessage(newMsg)
            triggerAutoReply("audio")
          }
        } catch (e: Exception) {
          Log.e(TAG, "Error sending audio from picker", e)
        }
      },
      onOpenSystemFilePicker = {
        showAudioPickerDialog = false
        audioSAFLauncher.launch(
          arrayOf(
            "audio/*",
            "audio/mpeg",
            "audio/mp4",
            "audio/x-m4a",
            "audio/aac",
            "audio/wav",
            "audio/x-wav",
            "audio/ogg",
            "audio/flac"
          )
        )
      }
    )
  }
}

private fun handleIncomingShareIntent(
  context: android.content.Context,
  intent: Intent,
  onStaged: (List<SharedContentItem>, String) -> Unit
) {
  val stagedList = mutableListOf<SharedContentItem>()
  var extraText = ""

  try {
    if (intent.action == Intent.ACTION_SEND) {
      if (intent.hasExtra(Intent.EXTRA_TEXT)) {
        extraText = intent.getStringExtra(Intent.EXTRA_TEXT) ?: ""
      }
      val uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        intent.getParcelableExtra(Intent.EXTRA_STREAM, Uri::class.java)
      } else {
        @Suppress("DEPRECATION")
        intent.getParcelableExtra<Uri>(Intent.EXTRA_STREAM)
      } ?: intent.data

      uri?.let {
        processSingleSharedUri(context, it)?.let { item -> stagedList.add(item) }
      }
    } else if (intent.action == Intent.ACTION_SEND_MULTIPLE) {
      if (intent.hasExtra(Intent.EXTRA_TEXT)) {
        extraText = intent.getStringExtra(Intent.EXTRA_TEXT) ?: ""
      }
      val uris = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        intent.getParcelableArrayListExtra(Intent.EXTRA_STREAM, Uri::class.java)
      } else {
        @Suppress("DEPRECATION")
        intent.getParcelableArrayListExtra<Uri>(Intent.EXTRA_STREAM)
      }

      uris?.forEach { uri ->
        processSingleSharedUri(context, uri)?.let { item -> stagedList.add(item) }
      }

      if (stagedList.isEmpty() && intent.clipData != null) {
        val count = intent.clipData?.itemCount ?: 0
        for (i in 0 until count) {
          val itemUri = intent.clipData?.getItemAt(i)?.uri
          itemUri?.let { uri ->
            processSingleSharedUri(context, uri)?.let { item -> stagedList.add(item) }
          }
        }
      }
    }
  } catch (e: Exception) {
    Log.e("MainActivity", "Error handling shared intent", e)
  }

  onStaged(stagedList, extraText)
}

private fun processSingleSharedUri(context: android.content.Context, uri: Uri): SharedContentItem? {
  return try {
    val mimeType = context.contentResolver.getType(uri) ?: ""
    val (fileName, sizeStr, durationStr) = MediaStorageHelper.getFileMetadata(context, uri, "SharedFile")
    val ext = fileName.substringAfterLast('.', "")
    val isImage = mimeType.startsWith("image/") || ext in listOf("jpg", "jpeg", "png", "webp", "gif")
    val isVideo = mimeType.startsWith("video/") || ext in listOf("mp4", "mkv", "mov", "webm", "3gp")
    val isAudio = mimeType.startsWith("audio/") || ext in listOf("mp3", "m4a", "aac", "wav", "ogg", "flac")

    val fileExt = if (ext.isNotBlank()) ext else when {
      isImage -> "jpg"
      isVideo -> "mp4"
      isAudio -> "mp3"
      else -> "bin"
    }

    val savedFile = MediaStorageHelper.copyUriToInternalStorage(context, uri, fileExt) ?: return null

    when {
      isVideo -> {
        val (thumbnail, duration) = MediaStorageHelper.extractVideoMetadata(context, savedFile)
        SharedContentItem(
          file = savedFile,
          type = MessageAttachmentType.VIDEO,
          fileName = fileName,
          sizeOrDuration = duration.ifBlank { sizeStr },
          isVideo = true,
          thumbnailFile = thumbnail
        )
      }
      isAudio -> {
        val label = if (durationStr.isNotBlank()) "$durationStr • $sizeStr" else sizeStr
        SharedContentItem(
          file = savedFile,
          type = MessageAttachmentType.AUDIO,
          fileName = fileName,
          sizeOrDuration = label
        )
      }
      isImage -> {
        SharedContentItem(
          file = savedFile,
          type = MessageAttachmentType.IMAGE,
          fileName = fileName,
          sizeOrDuration = sizeStr
        )
      }
      else -> {
        SharedContentItem(
          file = savedFile,
          type = MessageAttachmentType.DOCUMENT,
          fileName = fileName,
          sizeOrDuration = sizeStr
        )
      }
    }
  } catch (e: Exception) {
    Log.e("MainActivity", "Failed to process shared URI: $uri", e)
    null
  }
}

