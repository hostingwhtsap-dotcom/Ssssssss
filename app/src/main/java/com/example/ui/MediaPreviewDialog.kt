package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.ui.theme.WhatsAppGreenFab
import java.io.File

@Composable
fun MediaPreviewDialog(
  mediaFile: File,
  isVideo: Boolean,
  videoDuration: String = "",
  thumbnailFile: File? = null,
  onSend: (caption: String) -> Unit,
  onCancel: () -> Unit
) {
  var captionText by remember { mutableStateOf("") }

  Dialog(
    onDismissRequest = onCancel,
    properties = DialogProperties(
      usePlatformDefaultWidth = false,
      decorFitsSystemWindows = false
    )
  ) {
    Box(
      modifier = Modifier
        .fillMaxSize()
        .background(Color.Black)
        .statusBarsPadding()
        .navigationBarsPadding()
        .imePadding()
        .testTag("media_preview_dialog")
    ) {
      Column(
        modifier = Modifier.fillMaxSize()
      ) {
        // Top action bar with back button
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 8.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          IconButton(
            onClick = onCancel,
            modifier = Modifier.size(40.dp)
          ) {
            Icon(
              imageVector = Icons.AutoMirrored.Filled.ArrowBack,
              contentDescription = "Cancel",
              tint = Color.White
            )
          }
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = if (isVideo) "Video Preview" else "Photo Preview",
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold
          )
        }

        // Center Preview Area
        Box(
          modifier = Modifier
            .weight(1f)
            .fillMaxWidth()
            .padding(horizontal = 12.dp),
          contentAlignment = Alignment.Center
        ) {
          if (isVideo) {
            // Video Thumbnail preview with play icon & duration
            Box(contentAlignment = Alignment.Center) {
              AsyncImage(
                model = thumbnailFile ?: mediaFile,
                contentDescription = "Video Preview",
                contentScale = ContentScale.Fit,
                modifier = Modifier
                  .fillMaxSize()
                  .clip(RoundedCornerShape(8.dp))
              )
              // Large play icon
              Box(
                modifier = Modifier
                  .size(64.dp)
                  .clip(CircleShape)
                  .background(Color.Black.copy(alpha = 0.6f)),
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  imageVector = Icons.Filled.PlayArrow,
                  contentDescription = "Play",
                  tint = Color.White,
                  modifier = Modifier.size(36.dp)
                )
              }
              // Duration chip
              if (videoDuration.isNotBlank()) {
                Surface(
                  shape = RoundedCornerShape(12.dp),
                  color = Color.Black.copy(alpha = 0.7f),
                  modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(12.dp)
                ) {
                  Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    Icon(
                      imageVector = Icons.Filled.Videocam,
                      contentDescription = null,
                      tint = Color.White,
                      modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                      text = videoDuration,
                      color = Color.White,
                      fontSize = 12.sp,
                      fontWeight = FontWeight.Medium
                    )
                  }
                }
              }
            }
          } else {
            // Real photo preview
            AsyncImage(
              model = mediaFile,
              contentDescription = "Photo Preview",
              contentScale = ContentScale.Fit,
              modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(8.dp))
            )
          }
        }

        // Bottom WhatsApp-style Caption & Send Bar
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 10.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          // Caption input pill
          Surface(
            shape = RoundedCornerShape(24.dp),
            color = Color(0xFF1F2C34),
            modifier = Modifier
              .weight(1f)
              .height(48.dp)
          ) {
            Box(
              modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
              contentAlignment = Alignment.CenterStart
            ) {
              if (captionText.isEmpty()) {
                Text(
                  text = "Add a caption...",
                  color = Color(0xFF8696A0),
                  fontSize = 15.sp
                )
              }
              BasicTextField(
                value = captionText,
                onValueChange = { captionText = it },
                textStyle = TextStyle(
                  color = Color.White,
                  fontSize = 15.sp
                ),
                cursorBrush = SolidColor(WhatsAppGreenFab),
                modifier = Modifier
                  .fillMaxWidth()
                  .testTag("caption_input_field")
              )
            }
          }

          Spacer(modifier = Modifier.width(8.dp))

          // Green Send Button
          Box(
            modifier = Modifier
              .size(48.dp)
              .clip(CircleShape)
              .background(WhatsAppGreenFab)
              .clickable { onSend(captionText.trim()) }
              .testTag("send_media_button"),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.AutoMirrored.Filled.Send,
              contentDescription = "Send Media",
              tint = Color.White,
              modifier = Modifier.size(22.dp)
            )
          }
        }
      }
    }
  }
}
