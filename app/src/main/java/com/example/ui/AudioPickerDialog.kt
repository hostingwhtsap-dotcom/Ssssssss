package com.example.ui

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import com.example.ui.theme.WhatsAppGreenPrimary
import com.example.ui.theme.WhatsAppTextPrimary
import com.example.ui.theme.WhatsAppTextSecondary
import com.example.util.DeviceAudioItem
import com.example.util.MediaStorageHelper
import com.example.util.VoiceAudioHelper

@Composable
fun AudioPickerDialog(
  onDismiss: () -> Unit,
  onSelectAudio: (uri: Uri, fileName: String) -> Unit,
  onOpenSystemFilePicker: () -> Unit
) {
  val context = LocalContext.current
  var searchQuery by remember { mutableStateOf("") }
  var audioList by remember { mutableStateOf<List<DeviceAudioItem>>(emptyList()) }
  var hasPermission by remember {
    mutableStateOf(checkAudioPermission(context))
  }
  var selectedItem by remember { mutableStateOf<DeviceAudioItem?>(null) }
  var currentlyPreviewingUri by remember { mutableStateOf<Uri?>(null) }
  val previewAudioHelper = remember { VoiceAudioHelper(context) }

  val permissionToRequest = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
    Manifest.permission.READ_MEDIA_AUDIO
  } else {
    Manifest.permission.READ_EXTERNAL_STORAGE
  }

  val permissionLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.RequestPermission()
  ) { isGranted ->
    hasPermission = isGranted
    if (isGranted) {
      audioList = MediaStorageHelper.queryDeviceAudioFiles(context, searchQuery)
    }
  }

  LaunchedEffect(hasPermission, searchQuery) {
    if (hasPermission) {
      audioList = MediaStorageHelper.queryDeviceAudioFiles(context, searchQuery)
    }
  }

  DisposableEffect(Unit) {
    onDispose {
      previewAudioHelper.releaseAll()
    }
  }

  Dialog(
    onDismissRequest = {
      previewAudioHelper.releaseAll()
      onDismiss()
    },
    properties = DialogProperties(
      usePlatformDefaultWidth = false,
      decorFitsSystemWindows = false
    )
  ) {
    Surface(
      modifier = Modifier
        .fillMaxSize()
        .statusBarsPadding()
        .navigationBarsPadding()
        .imePadding()
        .testTag("audio_picker_dialog"),
      color = Color(0xFFF7F8FA)
    ) {
      Column(modifier = Modifier.fillMaxSize()) {
        // WhatsApp-like Top App Bar
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .background(WhatsAppGreenPrimary)
            .padding(horizontal = 8.dp, vertical = 12.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          IconButton(
            onClick = {
              previewAudioHelper.releaseAll()
              onDismiss()
            },
            modifier = Modifier.testTag("audio_picker_back_button")
          ) {
            Icon(
              imageVector = Icons.AutoMirrored.Filled.ArrowBack,
              contentDescription = "Back",
              tint = Color.White
            )
          }

          Spacer(modifier = Modifier.width(4.dp))

          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = "Send audio",
              color = Color.White,
              fontSize = 19.sp,
              fontWeight = FontWeight.Bold
            )
            Text(
              text = if (hasPermission) "${audioList.size} audio files found" else "Device audio",
              color = Color.White.copy(alpha = 0.85f),
              fontSize = 12.sp
            )
          }

          if (selectedItem != null) {
            IconButton(
              onClick = {
                selectedItem?.let { item ->
                  previewAudioHelper.releaseAll()
                  onSelectAudio(item.uri, item.displayName)
                }
              },
              modifier = Modifier.testTag("audio_picker_send_button")
            ) {
              Icon(
                imageVector = Icons.AutoMirrored.Filled.Send,
                contentDescription = "Send",
                tint = Color.White
              )
            }
          }
        }

        // Search Bar & System Picker Action
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(12.dp)
        ) {
          OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier
              .fillMaxWidth()
              .testTag("audio_search_input"),
            placeholder = { Text("Search songs, artists, audio files...", fontSize = 14.sp) },
            leadingIcon = {
              Icon(
                imageVector = Icons.Filled.Search,
                contentDescription = "Search",
                tint = WhatsAppTextSecondary
              )
            },
            trailingIcon = {
              if (searchQuery.isNotEmpty()) {
                IconButton(onClick = { searchQuery = "" }) {
                  Icon(
                    imageVector = Icons.Filled.Clear,
                    contentDescription = "Clear search",
                    tint = WhatsAppTextSecondary
                  )
                }
              }
            },
            singleLine = true,
            shape = RoundedCornerShape(24.dp),
            colors = OutlinedTextFieldDefaults.colors(
              focusedBorderColor = WhatsAppGreenPrimary,
              unfocusedBorderColor = Color(0xFFE0E0E0),
              focusedContainerColor = Color(0xFFF7F8FA),
              unfocusedContainerColor = Color(0xFFF7F8FA)
            )
          )

          Spacer(modifier = Modifier.height(10.dp))

          // Native Android Document / Storage Action
          Card(
            onClick = {
              previewAudioHelper.releaseAll()
              onOpenSystemFilePicker()
            },
            colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
              .fillMaxWidth()
              .testTag("browse_system_audio_files_button")
          ) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Box(
                modifier = Modifier
                  .size(36.dp)
                  .clip(CircleShape)
                  .background(WhatsAppGreenPrimary),
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  imageVector = Icons.Filled.FolderOpen,
                  contentDescription = null,
                  tint = Color.White,
                  modifier = Modifier.size(20.dp)
                )
              }
              Spacer(modifier = Modifier.width(12.dp))
              Column(modifier = Modifier.weight(1f)) {
                Text(
                  text = "Browse device storage / Files",
                  fontWeight = FontWeight.SemiBold,
                  fontSize = 14.sp,
                  color = WhatsAppGreenPrimary
                )
                Text(
                  text = "Choose MP3, M4A, AAC, WAV, OGG, FLAC directly",
                  fontSize = 11.5.sp,
                  color = WhatsAppTextSecondary
                )
              }
            }
          }
        }

        // Permission Prompt if needed
        if (!hasPermission) {
          Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0)),
            modifier = Modifier
              .fillMaxWidth()
              .padding(16.dp),
            shape = RoundedCornerShape(12.dp)
          ) {
            Column(
              modifier = Modifier.padding(16.dp),
              horizontalAlignment = Alignment.CenterHorizontally
            ) {
              Text(
                text = "Audio Permission Required",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = Color(0xFFE65100)
              )
              Spacer(modifier = Modifier.height(6.dp))
              Text(
                text = "Grant access to your device's audio files to view and send MP3, M4A, and music tracks.",
                fontSize = 12.5.sp,
                color = WhatsAppTextPrimary,
                lineHeight = 17.sp
              )
              Spacer(modifier = Modifier.height(12.dp))
              Button(
                onClick = { permissionLauncher.launch(permissionToRequest) },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE65100)),
                shape = RoundedCornerShape(8.dp)
              ) {
                Text("Allow Access", color = Color.White, fontWeight = FontWeight.Bold)
              }
            }
          }
        }

        // Audio List
        if (hasPermission && audioList.isEmpty()) {
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .weight(1f)
              .padding(24.dp),
            contentAlignment = Alignment.Center
          ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
              Icon(
                imageVector = Icons.Filled.Audiotrack,
                contentDescription = null,
                tint = Color(0xFFB0BEC5),
                modifier = Modifier.size(64.dp)
              )
              Spacer(modifier = Modifier.height(12.dp))
              Text(
                text = if (searchQuery.isNotEmpty()) "No matching audio files" else "No audio tracks in library",
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp,
                color = WhatsAppTextPrimary
              )
              Spacer(modifier = Modifier.height(6.dp))
              Text(
                text = "Tap 'Browse device storage / Files' above to pick any audio file from Downloads or Phone Storage.",
                fontSize = 13.sp,
                color = WhatsAppTextSecondary,
                lineHeight = 18.sp,
                modifier = Modifier.padding(horizontal = 16.dp)
              )
            }
          }
        } else {
          LazyColumn(
            modifier = Modifier
              .fillMaxWidth()
              .weight(1f)
          ) {
            items(audioList, key = { it.id }) { item ->
              val isSelected = selectedItem?.id == item.id
              val isPreviewing = currentlyPreviewingUri == item.uri

              AudioItemRow(
                item = item,
                isSelected = isSelected,
                isPreviewing = isPreviewing,
                onTogglePlayPreview = {
                  if (isPreviewing) {
                    previewAudioHelper.stopPlayback()
                    currentlyPreviewingUri = null
                  } else {
                    currentlyPreviewingUri = item.uri
                    // Copy temporary file or play directly
                    try {
                      val tempFile = MediaStorageHelper.copyUriToInternalStorage(context, item.uri, "audio_preview")
                      if (tempFile != null) {
                        previewAudioHelper.togglePlay(tempFile.absolutePath) { playing ->
                          if (!playing) currentlyPreviewingUri = null
                        }
                      }
                    } catch (_: Exception) {
                      currentlyPreviewingUri = null
                    }
                  }
                },
                onClick = {
                  selectedItem = if (isSelected) null else item
                },
                onDoubleSend = {
                  previewAudioHelper.releaseAll()
                  onSelectAudio(item.uri, item.displayName)
                }
              )
            }
          }
        }

        // Bottom Confirmation Bar if item is selected
        selectedItem?.let { item ->
          Surface(
            modifier = Modifier
              .fillMaxWidth()
              .background(Color.White),
            shadowElevation = 8.dp
          ) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Column(modifier = Modifier.weight(1f)) {
                Text(
                  text = item.displayName,
                  fontWeight = FontWeight.Bold,
                  fontSize = 14.sp,
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis
                )
                Text(
                  text = "${item.durationFormatted} • ${item.sizeFormatted}",
                  fontSize = 12.sp,
                  color = WhatsAppTextSecondary
                )
              }

              Spacer(modifier = Modifier.width(12.dp))

              Button(
                onClick = {
                  previewAudioHelper.releaseAll()
                  onSelectAudio(item.uri, item.displayName)
                },
                colors = ButtonDefaults.buttonColors(containerColor = WhatsAppGreenPrimary),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.testTag("confirm_send_audio_button")
              ) {
                Icon(
                  imageVector = Icons.AutoMirrored.Filled.Send,
                  contentDescription = null,
                  modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("Send Audio")
              }
            }
          }
        }
      }
    }
  }
}

@Composable
private fun AudioItemRow(
  item: DeviceAudioItem,
  isSelected: Boolean,
  isPreviewing: Boolean,
  onTogglePlayPreview: () -> Unit,
  onClick: () -> Unit,
  onDoubleSend: () -> Unit
) {
  val bgColor = if (isSelected) Color(0xFFE8F5E9) else Color.White

  Row(
    modifier = Modifier
      .fillMaxWidth()
      .background(bgColor)
      .clickable(onClick = onClick)
      .padding(horizontal = 16.dp, vertical = 10.dp),
    verticalAlignment = Alignment.CenterVertically
  ) {
    // Play preview button
    Box(
      modifier = Modifier
        .size(42.dp)
        .clip(CircleShape)
        .background(if (isPreviewing) WhatsAppGreenPrimary else Color(0xFFE0663B).copy(alpha = 0.15f))
        .clickable(onClick = onTogglePlayPreview),
      contentAlignment = Alignment.Center
    ) {
      Icon(
        imageVector = if (isPreviewing) Icons.Filled.Pause else Icons.Filled.PlayArrow,
        contentDescription = if (isPreviewing) "Pause preview" else "Play preview",
        tint = if (isPreviewing) Color.White else Color(0xFFE0663B),
        modifier = Modifier.size(22.dp)
      )
    }

    Spacer(modifier = Modifier.width(12.dp))

    Column(modifier = Modifier.weight(1f)) {
      Text(
        text = item.displayName,
        fontSize = 14.5.sp,
        fontWeight = FontWeight.SemiBold,
        color = WhatsAppTextPrimary,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
      )
      Spacer(modifier = Modifier.height(2.dp))
      Text(
        text = "${item.artist} • ${item.durationFormatted} • ${item.sizeFormatted}",
        fontSize = 12.sp,
        color = WhatsAppTextSecondary,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
      )
    }

    Spacer(modifier = Modifier.width(8.dp))

    // Direct Send Action
    IconButton(
      onClick = onDoubleSend,
      modifier = Modifier.size(36.dp)
    ) {
      Icon(
        imageVector = Icons.AutoMirrored.Filled.Send,
        contentDescription = "Send this audio",
        tint = if (isSelected) WhatsAppGreenPrimary else Color(0xFF9E9E9E),
        modifier = Modifier.size(18.dp)
      )
    }
  }
}

private fun checkAudioPermission(context: Context): Boolean {
  val perm = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
    Manifest.permission.READ_MEDIA_AUDIO
  } else {
    Manifest.permission.READ_EXTERNAL_STORAGE
  }
  return ContextCompat.checkSelfPermission(context, perm) == PackageManager.PERMISSION_GRANTED
}
