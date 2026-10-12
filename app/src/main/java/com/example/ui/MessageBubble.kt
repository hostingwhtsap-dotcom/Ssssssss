package com.example.ui

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Reply
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.InsertDriveFile
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.runtime.remember
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.R
import com.example.model.ChatMessage
import com.example.model.MessageAttachmentType
import com.example.model.MessageStatus
import com.example.ui.theme.WhatsAppCheckBlue
import com.example.ui.theme.WhatsAppCheckGrey
import com.example.ui.theme.WhatsAppNoticeBg
import com.example.ui.theme.WhatsAppNoticeText
import com.example.ui.theme.WhatsAppPinkAccent
import com.example.ui.theme.WhatsAppTextPrimary
import com.example.ui.theme.WhatsAppTextSecondary
import com.example.ui.theme.WhatsAppWhiteIncoming
import com.example.util.MediaStorageHelper
import java.io.File
import java.util.Locale

@Composable
fun EncryptionNoticeBanner(
  modifier: Modifier = Modifier
) {
  Box(
    modifier = modifier
      .fillMaxWidth()
      .padding(horizontal = 20.dp, vertical = 8.dp),
    contentAlignment = Alignment.Center
  ) {
    Surface(
      shape = RoundedCornerShape(8.dp),
      color = WhatsAppNoticeBg,
      shadowElevation = 0.5.dp,
      modifier = Modifier.widthIn(max = 350.dp)
    ) {
      Row(
        modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Icon(
          imageVector = Icons.Filled.Lock,
          contentDescription = null,
          tint = WhatsAppNoticeText,
          modifier = Modifier
            .size(13.dp)
            .padding(end = 3.dp)
        )
        Text(
          text = "Messages and calls are end-to-end encrypted. No one outside of this chat, not even the company, can read, listen to, or share them.",
          color = WhatsAppNoticeText,
          fontSize = 11.5.sp,
          lineHeight = 15.sp,
          textAlign = TextAlign.Center,
          fontWeight = FontWeight.Normal
        )
      }
    }
  }
}

@Composable
fun DatePillBadge(
  dateText: String = "Today",
  modifier: Modifier = Modifier
) {
  Box(
    modifier = modifier
      .fillMaxWidth()
      .padding(vertical = 4.dp),
    contentAlignment = Alignment.Center
  ) {
    Surface(
      shape = RoundedCornerShape(8.dp),
      color = Color.White.copy(alpha = 0.95f),
      shadowElevation = 0.8.dp
    ) {
      Text(
        text = dateText,
        color = Color(0xFF54656F),
        fontSize = 11.8.sp,
        fontWeight = FontWeight.Medium,
        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
      )
    }
  }
}

@Composable
fun MessageBubbleItem(
  message: ChatMessage,
  isAudioPlaying: Boolean = false,
  audioPlaybackProgress: Float = 0f,
  onImageClick: (String) -> Unit = {},
  onVideoClick: (String) -> Unit = {},
  onAudioTogglePlay: (String) -> Unit = {},
  onSeekAudio: (String, Float) -> Unit = { _, _ -> },
  modifier: Modifier = Modifier
) {
  val isOutgoing = message.isOutgoing
  val isVoiceNote = message.attachmentType == MessageAttachmentType.VOICE_NOTE
  val isImage = message.attachmentType == MessageAttachmentType.IMAGE
  val isVideo = message.attachmentType == MessageAttachmentType.VIDEO

  // Outgoing voice notes use WhatsAppPinkAccent (#E91E63) as seen in Screenshot 3
  val bubbleColor = when {
    isOutgoing && isVoiceNote -> WhatsAppPinkAccent
    isOutgoing -> Color(0xFFD9FDD3)
    else -> WhatsAppWhiteIncoming
  }

  val bubbleShape = if (isOutgoing) {
    RoundedCornerShape(topStart = 16.dp, topEnd = 4.dp, bottomStart = 16.dp, bottomEnd = 16.dp)
  } else {
    RoundedCornerShape(topStart = 4.dp, topEnd = 16.dp, bottomStart = 16.dp, bottomEnd = 16.dp)
  }

  Row(
    modifier = modifier
      .fillMaxWidth()
      .padding(
        start = if (isOutgoing) 36.dp else 8.dp,
        end = if (isOutgoing) 8.dp else 36.dp,
        top = 2.dp,
        bottom = 2.dp
      ),
    horizontalArrangement = if (isOutgoing) Arrangement.End else Arrangement.Start,
    verticalAlignment = Alignment.CenterVertically
  ) {
    // Quick forward/share button on the left of outgoing image/video (matched to Screenshot 3)
    if (isOutgoing && (isImage || isVideo)) {
      Box(
        modifier = Modifier
          .padding(end = 8.dp)
          .size(34.dp)
          .clip(CircleShape)
          .background(Color.White)
          .clickable { /* quick forward action */ },
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = Icons.AutoMirrored.Filled.Reply,
          contentDescription = "Forward",
          tint = Color(0xFF54656F),
          modifier = Modifier.size(18.dp)
        )
      }
    }

    Surface(
      shape = bubbleShape,
      color = bubbleColor,
      shadowElevation = 0.8.dp,
      modifier = Modifier
        .widthIn(max = 290.dp)
        .testTag(if (isOutgoing) "outgoing_bubble_${message.id}" else "incoming_bubble_${message.id}")
    ) {
      if (isVoiceNote) {
        // Voice Note Bubble matching Screenshot 3
        VoiceNoteBubbleContent(
          message = message,
          isOutgoing = isOutgoing,
          isPlaying = isAudioPlaying,
          playbackProgress = audioPlaybackProgress,
          onTogglePlay = {
            message.mediaPath?.let { path -> onAudioTogglePlay(path) }
          },
          onSeek = { progress ->
            message.mediaPath?.let { path -> onSeekAudio(path, progress) }
          }
        )
      } else if (isImage) {
        // Full Image Bubble with overlay timestamp matching Screenshot 3
        ImageBubbleContent(
          message = message,
          onImageClick = onImageClick
        )
      } else if (isVideo) {
        // Video Bubble with overlay play button & duration
        VideoBubbleContent(
          message = message,
          onVideoClick = onVideoClick
        )
      } else {
        // Standard Text or Document Bubble
        Column(
          modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp)
        ) {
          if (message.attachmentType == MessageAttachmentType.DOCUMENT) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 3.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Box(
                modifier = Modifier
                  .size(36.dp)
                  .clip(RoundedCornerShape(6.dp))
                  .background(Color(0xFF7F66FF).copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  imageVector = Icons.Filled.InsertDriveFile,
                  contentDescription = "Document",
                  tint = Color(0xFF7F66FF),
                  modifier = Modifier.size(20.dp)
                )
              }
              Spacer(modifier = Modifier.width(8.dp))
              Column {
                Text(
                  text = message.attachmentName ?: "Document.pdf",
                  color = WhatsAppTextPrimary,
                  fontSize = 13.5.sp,
                  fontWeight = FontWeight.Medium
                )
                Text(
                  text = message.attachmentSize ?: "Document File",
                  color = WhatsAppTextSecondary,
                  fontSize = 11.sp
                )
              }
            }
          } else if (message.attachmentType == MessageAttachmentType.AUDIO) {
            Column(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)) {
              // Prominent label for staff/admin to distinguish device audio files from in-app voice notes
              Surface(
                shape = RoundedCornerShape(4.dp),
                color = Color(0xFFFFECB3),
                modifier = Modifier.padding(bottom = 4.dp)
              ) {
                Row(
                  modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Icon(
                    imageVector = Icons.Filled.Audiotrack,
                    contentDescription = null,
                    tint = Color(0xFFE65100),
                    modifier = Modifier.size(12.dp)
                  )
                  Spacer(modifier = Modifier.width(4.dp))
                  Text(
                    text = "Forwarded Audio",
                    color = Color(0xFFE65100),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                  )
                }
              }

              Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Box(
                  modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFE0663B).copy(alpha = 0.2f))
                    .clickable {
                      message.mediaPath?.let { onAudioTogglePlay(it) }
                    },
                  contentAlignment = Alignment.Center
                ) {
                  Icon(
                    imageVector = if (isAudioPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                    contentDescription = if (isAudioPlaying) "Pause" else "Play audio file",
                    tint = Color(0xFFE0663B),
                    modifier = Modifier.size(24.dp)
                  )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                  Text(
                    text = message.attachmentName ?: "Audio File.mp3",
                    color = WhatsAppTextPrimary,
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2
                  )
                  Text(
                    text = message.attachmentSize ?: "Audio File",
                    color = WhatsAppTextSecondary,
                    fontSize = 11.sp
                  )
                }
              }
            }
          }

          if (message.text.isNotBlank()) {
            Text(
              text = message.text,
              color = WhatsAppTextPrimary,
              fontSize = 15.sp,
              lineHeight = 19.sp,
              modifier = Modifier.padding(bottom = 1.dp)
            )
          }

          // Timestamp & Status Row
          Row(
            modifier = Modifier
              .align(Alignment.End)
              .padding(top = 1.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = message.timestamp,
              color = WhatsAppTextSecondary,
              fontSize = 10.8.sp
            )
            if (isOutgoing) {
              Spacer(modifier = Modifier.width(3.dp))
              DoubleTicksIcon(status = message.status, tint = WhatsAppCheckBlue)
            }
          }
        }
      }
    }
  }
}

@Composable
private fun VoiceNoteBubbleContent(
  message: ChatMessage,
  isOutgoing: Boolean,
  isPlaying: Boolean,
  playbackProgress: Float = 0f,
  onTogglePlay: () -> Unit,
  onSeek: (Float) -> Unit = {}
) {
  val contentColor = if (isOutgoing) Color.White else WhatsAppTextPrimary

  // Retrieve actual recorded waveform or generate a stable, natural waveform for this audio
  val waveformBars = remember(message.id, message.waveform, message.mediaPath) {
    if (message.waveform.isNotEmpty()) {
      message.waveform
    } else {
      MediaStorageHelper.generateWaveform(message.mediaPath, 30)
    }
  }

  Column(
    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      modifier = Modifier.fillMaxWidth()
    ) {
      // Circular sender avatar with small white mic badge (matched to Screenshot 3)
      Box(
        modifier = Modifier.size(42.dp),
        contentAlignment = Alignment.BottomEnd
      ) {
        Image(
          painter = painterResource(id = R.drawable.company_logo),
          contentDescription = "Avatar",
          contentScale = ContentScale.Crop,
          modifier = Modifier
            .size(38.dp)
            .clip(CircleShape)
        )
        // Mic badge on bottom-right of circle
        Box(
          modifier = Modifier
            .size(16.dp)
            .clip(CircleShape)
            .background(if (isOutgoing) Color.White else WhatsAppPinkAccent),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.Filled.Mic,
            contentDescription = null,
            tint = if (isOutgoing) WhatsAppPinkAccent else Color.White,
            modifier = Modifier.size(10.dp)
          )
        }
      }

      Spacer(modifier = Modifier.width(6.dp))

      // White Play/Pause triangle button
      Box(
        modifier = Modifier
          .size(32.dp)
          .clip(CircleShape)
          .clickable(onClick = onTogglePlay)
          .testTag("play_pause_voice_${message.id}"),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
          contentDescription = if (isPlaying) "Pause" else "Play",
          tint = contentColor,
          modifier = Modifier.size(24.dp)
        )
      }

      Spacer(modifier = Modifier.width(4.dp))

      // Waveform line with real amplitude bars and interactive scrub/seek
      BoxWithConstraints(
        modifier = Modifier
          .weight(1f)
          .height(32.dp)
          .pointerInput(message.id) {
            detectTapGestures { offset ->
              if (size.width > 0) {
                val progress = (offset.x / size.width).coerceIn(0f, 1f)
                onSeek(progress)
              }
            }
          },
        contentAlignment = Alignment.Center
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.Center
        ) {
          // Left track dot
          Box(
            modifier = Modifier
              .size(4.dp)
              .clip(CircleShape)
              .background(contentColor)
          )
          Spacer(modifier = Modifier.width(2.dp))

          val totalBars = waveformBars.size.coerceAtLeast(1)
          waveformBars.forEachIndexed { index, amp ->
            val barFraction = index.toFloat() / (totalBars - 1).coerceAtLeast(1)
            val isPlayed = (isPlaying && barFraction <= playbackProgress) || (!isPlaying && playbackProgress > 0f && barFraction <= playbackProgress)
            val barColor = if (isOutgoing) {
              if (isPlayed) Color.White else Color.White.copy(alpha = 0.42f)
            } else {
              if (isPlayed) WhatsAppPinkAccent else Color(0xFF8696A0)
            }
            // Bar height from real amplitude level
            val barHeight = (4 + (amp.coerceIn(0.08f, 1.0f) * 18)).dp

            Box(
              modifier = Modifier
                .padding(horizontal = 1.1.dp)
                .width(2.2.dp)
                .height(barHeight)
                .clip(RoundedCornerShape(1.dp))
                .background(barColor)
            )
          }

          Spacer(modifier = Modifier.width(2.dp))
          // Right track dot
          Box(
            modifier = Modifier
              .size(4.dp)
              .clip(CircleShape)
              .background(contentColor.copy(alpha = if (playbackProgress >= 0.98f) 1f else 0.5f))
          )
        }
      }
    }

    Spacer(modifier = Modifier.height(2.dp))

    // Bottom info row: Duration / Elapsed Time on left, Time + Double checkmarks on right
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 4.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      val durationLabel = if (isPlaying && playbackProgress > 0f) {
        val totalSecs = parseDurationToSeconds(message.audioDuration ?: "0:03")
        val curSecs = (totalSecs * playbackProgress).toInt()
        String.format(Locale.US, "%d:%02d", curSecs / 60, curSecs % 60)
      } else {
        message.audioDuration ?: "0:03"
      }

      Text(
        text = durationLabel,
        color = contentColor.copy(alpha = 0.9f),
        fontSize = 11.sp,
        fontWeight = FontWeight.Medium,
        modifier = Modifier.testTag("voice_note_duration_${message.id}")
      )

      Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
          text = message.timestamp,
          color = contentColor.copy(alpha = 0.9f),
          fontSize = 11.sp
        )
        if (isOutgoing) {
          Spacer(modifier = Modifier.width(3.dp))
          DoubleTicksIcon(status = message.status, tint = Color.White)
        }
      }
    }
  }
}

private fun parseDurationToSeconds(durationStr: String): Int {
  val clean = durationStr.substringBefore(" •").trim()
  val parts = clean.split(":")
  return if (parts.size == 2) {
    val m = parts[0].toIntOrNull() ?: 0
    val s = parts[1].toIntOrNull() ?: 0
    (m * 60 + s).coerceAtLeast(1)
  } else {
    3
  }
}

@Composable
private fun ImageBubbleContent(
  message: ChatMessage,
  onImageClick: (String) -> Unit
) {
  val imagePath = message.mediaPath
  Box(
    modifier = Modifier
      .width(260.dp)
      .clip(RoundedCornerShape(14.dp))
      .clickable { imagePath?.let { onImageClick(it) } }
  ) {
    if (!imagePath.isNullOrBlank()) {
      AsyncImage(
        model = File(imagePath),
        contentDescription = "Photo",
        contentScale = ContentScale.Crop,
        modifier = Modifier
          .fillMaxWidth()
          .height(220.dp)
      )
    }

    // Gradient scrim at bottom for crystal clear timestamp legibility (matched to Screenshot 3)
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .height(48.dp)
        .align(Alignment.BottomCenter)
        .background(
          Brush.verticalGradient(
            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.6f))
          )
        )
    )

    // Overlaid Timestamp and Checkmarks at bottom right
    Row(
      modifier = Modifier
        .align(Alignment.BottomEnd)
        .padding(end = 8.dp, bottom = 6.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Text(
        text = message.timestamp,
        color = Color.White,
        fontSize = 11.sp,
        fontWeight = FontWeight.Medium
      )
      if (message.isOutgoing) {
        Spacer(modifier = Modifier.width(3.dp))
        DoubleTicksIcon(status = message.status, tint = Color.White)
      }
    }
  }
}

@Composable
private fun VideoBubbleContent(
  message: ChatMessage,
  onVideoClick: (String) -> Unit
) {
  val videoPath = message.mediaPath
  val thumbPath = message.thumbnailPath

  Box(
    modifier = Modifier
      .width(260.dp)
      .height(210.dp)
      .clip(RoundedCornerShape(14.dp))
      .clickable { videoPath?.let { onVideoClick(it) } },
    contentAlignment = Alignment.Center
  ) {
    if (!thumbPath.isNullOrBlank() && File(thumbPath).exists()) {
      AsyncImage(
        model = File(thumbPath),
        contentDescription = "Video thumbnail",
        contentScale = ContentScale.Crop,
        modifier = Modifier.fillMaxSize()
      )
    } else {
      Box(
        modifier = Modifier
          .fillMaxSize()
          .background(Color(0xFF1E2A30))
      )
    }

    // Center circular play button
    Box(
      modifier = Modifier
        .size(52.dp)
        .clip(CircleShape)
        .background(Color.Black.copy(alpha = 0.6f)),
      contentAlignment = Alignment.Center
    ) {
      Icon(
        imageVector = Icons.Filled.PlayArrow,
        contentDescription = "Play Video",
        tint = Color.White,
        modifier = Modifier.size(34.dp)
      )
    }

    // Duration badge on bottom-left
    if (!message.audioDuration.isNullOrBlank()) {
      Surface(
        shape = RoundedCornerShape(10.dp),
        color = Color.Black.copy(alpha = 0.65f),
        modifier = Modifier
          .align(Alignment.BottomStart)
          .padding(8.dp)
      ) {
        Row(
          modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Icon(
            imageVector = Icons.Filled.Videocam,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(12.dp)
          )
          Spacer(modifier = Modifier.width(3.dp))
          Text(
            text = message.audioDuration,
            color = Color.White,
            fontSize = 11.sp
          )
        }
      }
    }

    // Overlaid Timestamp on bottom-right
    Row(
      modifier = Modifier
        .align(Alignment.BottomEnd)
        .padding(end = 8.dp, bottom = 6.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Text(
        text = message.timestamp,
        color = Color.White,
        fontSize = 11.sp
      )
      if (message.isOutgoing) {
        Spacer(modifier = Modifier.width(3.dp))
        DoubleTicksIcon(status = message.status, tint = Color.White)
      }
    }
  }
}

@Composable
private fun DoubleTicksIcon(
  status: MessageStatus,
  tint: Color
) {
  when (status) {
    MessageStatus.READ, MessageStatus.DELIVERED -> {
      Icon(
        imageVector = Icons.Filled.DoneAll,
        contentDescription = "Read",
        tint = tint,
        modifier = Modifier.size(15.dp)
      )
    }
    MessageStatus.SENT -> {
      Icon(
        imageVector = Icons.Filled.Done,
        contentDescription = "Sent",
        tint = tint,
        modifier = Modifier.size(15.dp)
      )
    }
  }
}
