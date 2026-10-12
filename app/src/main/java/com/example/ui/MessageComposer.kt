package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.SentimentSatisfiedAlt
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.WhatsAppIconTint
import com.example.ui.theme.WhatsAppInputBg
import com.example.ui.theme.WhatsAppInputHint
import com.example.ui.theme.WhatsAppPauseBtnBg
import com.example.ui.theme.WhatsAppPinkAccent
import com.example.ui.theme.WhatsAppPinkSoftBg
import com.example.ui.theme.WhatsAppTextPrimary
import kotlin.math.roundToInt

@Composable
fun MessageComposer(
  text: String,
  onTextChanged: (String) -> Unit,
  onSendMessage: () -> Unit,
  onEmojiClick: () -> Unit,
  onAttachmentClick: () -> Unit,
  onCameraClick: () -> Unit,
  isRecording: Boolean,
  isRecordingLocked: Boolean = true,
  isRecordingPaused: Boolean,
  recordingDurationSeconds: Int,
  recordingAmplitude: Int = 0,
  liveWaveform: List<Float> = emptyList(),
  onMicClick: () -> Unit = {},
  onStartRecording: () -> Unit = onMicClick,
  onLockRecording: () -> Unit = {},
  onTogglePauseRecording: () -> Unit,
  onCancelRecording: () -> Unit,
  onSendRecording: () -> Unit,
  hasPendingAttachments: Boolean = false,
  modifier: Modifier = Modifier
) {
  var dragOffsetX by remember { mutableFloatStateOf(0f) }
  var dragOffsetY by remember { mutableFloatStateOf(0f) }

  // Pulsing animation for active recording
  val infiniteTransition = rememberInfiniteTransition(label = "recording_pulse")
  val recBlinkAlpha by infiniteTransition.animateFloat(
    initialValue = 1f,
    targetValue = 0.25f,
    animationSpec = infiniteRepeatable(
      animation = tween(600, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "rec_blink"
  )
  val lockArrowOffset by infiniteTransition.animateFloat(
    initialValue = 0f,
    targetValue = -6f,
    animationSpec = infiniteRepeatable(
      animation = tween(700, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "lock_arrow"
  )

  if (isRecording && isRecordingLocked) {
    // Mode 1: WhatsApp Locked Hands-Free Recording UI (matching Reference Screenshot 1)
    Surface(
      shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
      color = Color.White,
      shadowElevation = 4.dp,
      modifier = modifier
        .fillMaxWidth()
        .testTag("voice_recording_panel")
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 20.dp, vertical = 14.dp)
      ) {
        // Top Row: Timer + Live Microphone Waveform Track + View Once "(1)" Icon
        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          // Timer (e.g. 0:06)
          val mins = recordingDurationSeconds / 60
          val secs = recordingDurationSeconds % 60
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
              modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(WhatsAppPinkAccent.copy(alpha = if (isRecordingPaused) 0.5f else recBlinkAlpha))
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = String.format("%d:%02d", mins, secs),
              color = Color(0xFF111B21),
              fontSize = 19.sp,
              fontWeight = FontWeight.SemiBold,
              modifier = Modifier.testTag("recording_timer_text")
            )
          }

          Spacer(modifier = Modifier.width(12.dp))

          // Live audio waveform generated from actual microphone amplitude levels
          LiveWaveformTrack(
            liveAmplitudes = liveWaveform,
            isPaused = isRecordingPaused,
            modifier = Modifier
              .weight(1f)
              .height(28.dp)
              .testTag("recording_waveform_track")
          )

          Spacer(modifier = Modifier.width(12.dp))

          // View Once "(1)" button inside dashed circle
          Box(
            modifier = Modifier
              .size(28.dp)
              .clip(CircleShape)
              .border(1.2.dp, Color(0xFF111B21), CircleShape),
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = "1",
              fontSize = 13.sp,
              fontWeight = FontWeight.Bold,
              color = Color(0xFF111B21)
            )
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Bottom Row: Trash icon + [ Pause / Resume ] pill + Send icon
        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          // Left: Soft pink circular trash button (matched to Screenshot 1)
          Box(
            modifier = Modifier
              .size(50.dp)
              .clip(CircleShape)
              .background(WhatsAppPinkSoftBg)
              .clickable(onClick = onCancelRecording)
              .testTag("cancel_recording_button"),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Filled.Delete,
              contentDescription = "Delete recording",
              tint = WhatsAppPinkAccent,
              modifier = Modifier.size(24.dp)
            )
          }

          // Center: [ Pause / Resume ] elongated pill button (matched to Screenshot 1)
          Surface(
            shape = RoundedCornerShape(23.dp),
            color = WhatsAppPauseBtnBg,
            modifier = Modifier
              .height(46.dp)
              .width(160.dp)
              .clip(RoundedCornerShape(23.dp))
              .clickable(onClick = onTogglePauseRecording)
              .testTag("pause_resume_button")
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 16.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.Center
            ) {
              Icon(
                imageVector = if (isRecordingPaused) Icons.Filled.PlayArrow else Icons.Filled.Pause,
                contentDescription = if (isRecordingPaused) "Resume" else "Pause",
                tint = Color(0xFF111B21),
                modifier = Modifier.size(20.dp)
              )
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = if (isRecordingPaused) "Resume" else "Pause",
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF111B21)
              )
            }
          }

          // Right: Circular Send button (matched to Screenshot 1)
          Box(
            modifier = Modifier
              .size(50.dp)
              .clip(CircleShape)
              .background(WhatsAppPinkAccent)
              .clickable(onClick = onSendRecording)
              .testTag("send_voice_note_button"),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.AutoMirrored.Filled.Send,
              contentDescription = "Send recording",
              tint = Color.White,
              modifier = Modifier.size(22.dp)
            )
          }
        }
      }
    }
  } else if (isRecording && !isRecordingLocked) {
    // Mode 2: WhatsApp Press-and-Hold Recording Active (holding finger, sliding up to lock or left to cancel)
    Box(
      modifier = modifier
        .fillMaxWidth()
        .padding(horizontal = 6.dp, vertical = 5.dp)
    ) {
      // Floating Slide-Up Lock Pill directly above the mic button
      Surface(
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
        shadowElevation = 5.dp,
        modifier = Modifier
          .align(Alignment.BottomEnd)
          .offset(y = (-75).dp)
          .padding(end = 4.dp)
          .width(44.dp)
          .testTag("slide_to_lock_indicator")
      ) {
        Column(
          modifier = Modifier.padding(vertical = 10.dp),
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          Icon(
            imageVector = Icons.Filled.Lock,
            contentDescription = "Slide up to lock",
            tint = Color(0xFF54656F),
            modifier = Modifier.size(18.dp)
          )
          Spacer(modifier = Modifier.height(4.dp))
          Icon(
            imageVector = Icons.Filled.KeyboardArrowUp,
            contentDescription = null,
            tint = WhatsAppPinkAccent,
            modifier = Modifier
              .size(18.dp)
              .offset(y = lockArrowOffset.dp)
          )
        }
      }

      // Main Composer Row during Press-and-Hold
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
      ) {
        // Left capsule: Recording dot + Timer + Live Waveform + Slide to cancel indicator
        Surface(
          shape = RoundedCornerShape(24.dp),
          color = WhatsAppInputBg,
          shadowElevation = 2.dp,
          modifier = Modifier
            .weight(1f)
            .height(48.dp)
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            // Blinking red dot
            Box(
              modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(WhatsAppPinkAccent.copy(alpha = recBlinkAlpha))
            )

            Spacer(modifier = Modifier.width(8.dp))

            // Timer (e.g. 0:03)
            val mins = recordingDurationSeconds / 60
            val secs = recordingDurationSeconds % 60
            Text(
              text = String.format("%d:%02d", mins, secs),
              color = WhatsAppTextPrimary,
              fontSize = 15.sp,
              fontWeight = FontWeight.SemiBold,
              modifier = Modifier.testTag("recording_timer_text")
            )

            Spacer(modifier = Modifier.width(8.dp))

            // Live waveform bars beside timer
            LiveWaveformTrack(
              liveAmplitudes = liveWaveform,
              isPaused = false,
              modifier = Modifier
                .weight(1f)
                .height(24.dp)
                .testTag("recording_live_waveform")
            )

            Spacer(modifier = Modifier.width(8.dp))

            // Slide left to cancel prompt
            Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier.offset { IntOffset(dragOffsetX.roundToInt().coerceIn(-100, 0), 0) }
            ) {
              Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                contentDescription = null,
                tint = Color(0xFF54656F),
                modifier = Modifier.size(18.dp)
              )
              Text(
                text = "Slide to cancel",
                color = Color(0xFF54656F),
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
              )
            }
          }
        }

        Spacer(modifier = Modifier.width(6.dp))

        // Right: Active pressed Microphone button with drag gesture handler
        Box(
          modifier = Modifier
            .size(54.dp)
            .clip(CircleShape)
            .background(WhatsAppPinkAccent)
            .scale(1.1f)
            .testTag("mic_button_held"),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.Filled.Mic,
            contentDescription = "Recording",
            tint = Color.White,
            modifier = Modifier.size(26.dp)
          )
        }
      }
    }
  } else {
    // Normal WhatsApp Composer (Idle State)
    Row(
      modifier = modifier
        .fillMaxWidth()
        .padding(horizontal = 6.dp, vertical = 5.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      // Left capsule: [😀] [Message] [📎] [📷]
      Surface(
        shape = RoundedCornerShape(24.dp),
        color = WhatsAppInputBg,
        shadowElevation = 1.dp,
        modifier = Modifier
          .weight(1f)
          .height(48.dp)
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          // Emoji Button
          IconButton(
            onClick = onEmojiClick,
            modifier = Modifier
              .size(38.dp)
              .testTag("emoji_icon_button")
          ) {
            Icon(
              imageVector = Icons.Outlined.SentimentSatisfiedAlt,
              contentDescription = "Emoji",
              tint = WhatsAppIconTint,
              modifier = Modifier.size(24.dp)
            )
          }

          // Text Input Field
          Box(
            modifier = Modifier
              .weight(1f)
              .padding(horizontal = 4.dp),
            contentAlignment = Alignment.CenterStart
          ) {
            if (text.isEmpty()) {
              Text(
                text = "Message",
                color = WhatsAppInputHint,
                fontSize = 16.sp,
                modifier = Modifier.testTag("message_input_placeholder")
              )
            }
            BasicTextField(
              value = text,
              onValueChange = onTextChanged,
              textStyle = TextStyle(
                color = WhatsAppTextPrimary,
                fontSize = 16.sp
              ),
              maxLines = 4,
              cursorBrush = SolidColor(WhatsAppPinkAccent),
              keyboardOptions = KeyboardOptions(
                imeAction = if (text.isNotBlank()) ImeAction.Send else ImeAction.Default
              ),
              keyboardActions = KeyboardActions(
                onSend = {
                  if (text.isNotBlank()) {
                    onSendMessage()
                  }
                }
              ),
              modifier = Modifier
                .fillMaxWidth()
                .testTag("message_input_field")
            )
          }

          // Attachment Paperclip
          IconButton(
            onClick = onAttachmentClick,
            modifier = Modifier
              .size(38.dp)
              .testTag("attachment_icon_button")
          ) {
            Icon(
              imageVector = Icons.Filled.AttachFile,
              contentDescription = "Attachment",
              tint = WhatsAppIconTint,
              modifier = Modifier
                .size(24.dp)
                .rotate(135f)
            )
          }

          // Dedicated Camera Button (visible when text is empty and no staged attachments)
          AnimatedVisibility(
            visible = text.isEmpty() && !hasPendingAttachments,
            enter = fadeIn(),
            exit = fadeOut()
          ) {
            IconButton(
              onClick = onCameraClick,
              modifier = Modifier
                .size(38.dp)
                .testTag("camera_icon_button")
            ) {
              Icon(
                imageVector = Icons.Filled.CameraAlt,
                contentDescription = "Camera",
                tint = WhatsAppIconTint,
                modifier = Modifier.size(22.dp)
              )
            }
          }
        }
      }

      Spacer(modifier = Modifier.width(5.dp))

      // Right: Circular Action Button
      // If typing or pending attachments -> Send Button.
      // If idle -> Microphone Button with Press-and-Hold & Slide-to-Lock gestures.
      val canSend = text.isNotBlank() || hasPendingAttachments
      if (canSend) {
        Box(
          modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(WhatsAppPinkAccent)
            .clickable { onSendMessage() }
            .testTag("send_button"),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.AutoMirrored.Filled.Send,
            contentDescription = "Send",
            tint = Color.White,
            modifier = Modifier.size(21.dp)
          )
        }
      } else {
        Box(
          modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(WhatsAppPinkAccent)
            .pointerInput(Unit) {
              awaitEachGesture {
                val down = awaitFirstDown(requireUnconsumed = false)
                val downTime = System.currentTimeMillis()
                var isDragging = true
                var accumDragY = 0f
                var accumDragX = 0f
                var hasLocked = false
                var hasCancelled = false

                // Start recording immediately upon touch
                onStartRecording()

                while (isDragging) {
                  val event = awaitPointerEvent()
                  val change = event.changes.firstOrNull { it.id == down.id }
                  if (change == null || !change.pressed) {
                    isDragging = false
                    val duration = System.currentTimeMillis() - downTime
                    if (!hasLocked && !hasCancelled) {
                      if (duration < 350) {
                        // Quick tap: lock hands-free so user can record comfortably
                        onLockRecording()
                      } else {
                        // Released after holding without locking: finish and send immediately!
                        onSendRecording()
                      }
                    }
                  } else {
                    val dragAmount = change.position - change.previousPosition
                    accumDragY += dragAmount.y
                    accumDragX += dragAmount.x
                    dragOffsetY = accumDragY
                    dragOffsetX = accumDragX

                    // Slide upward to lock threshold (-130 px ~ 50dp)
                    if (!hasLocked && accumDragY < -130f) {
                      hasLocked = true
                      onLockRecording()
                    }

                    // Slide left to cancel threshold (-200 px ~ 75dp)
                    if (!hasCancelled && accumDragX < -200f) {
                      hasCancelled = true
                      onCancelRecording()
                      isDragging = false
                    }
                  }
                }
              }
            }
            .testTag("mic_button"),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.Filled.Mic,
            contentDescription = "Microphone",
            tint = Color.White,
            modifier = Modifier.size(24.dp)
          )
        }
      }
    }
  }
}

/**
 * Renders real-time live audio waveform bars based on microphone amplitude levels.
 * Louder sounds generate taller bars; quieter sounds generate smaller bars.
 */
@Composable
fun LiveWaveformTrack(
  liveAmplitudes: List<Float>,
  isPaused: Boolean,
  modifier: Modifier = Modifier
) {
  val targetBarCount = 22
  // Take the most recent samples from the microphone, or pad with quiet baseline levels
  val displayBars = remember(liveAmplitudes, isPaused) {
    if (liveAmplitudes.isEmpty()) {
      List(targetBarCount) { 0.12f }
    } else if (liveAmplitudes.size <= targetBarCount) {
      val paddingCount = targetBarCount - liveAmplitudes.size
      List(paddingCount) { 0.12f } + liveAmplitudes
    } else {
      liveAmplitudes.takeLast(targetBarCount)
    }
  }

  Row(
    modifier = modifier,
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.Center
  ) {
    displayBars.forEach { amp ->
      val barH = if (isPaused) {
        5.dp
      } else {
        (4 + (amp.coerceIn(0.08f, 1.0f) * 20)).dp
      }
      Box(
        modifier = Modifier
          .padding(horizontal = 1.2.dp)
          .width(2.5.dp)
          .height(barH)
          .clip(RoundedCornerShape(1.dp))
          .background(Color(0xFF54656F))
      )
    }
  }
}
