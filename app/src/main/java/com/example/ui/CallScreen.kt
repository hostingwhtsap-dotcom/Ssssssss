package com.example.ui

import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.ScreenShare
import androidx.compose.material.icons.filled.StopScreenShare
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.VideocamOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.R
import kotlinx.coroutines.delay

private const val TAG = "CallScreen"

@Composable
fun AudioCallScreen(
  onEndCall: () -> Unit,
  modifier: Modifier = Modifier
) {
  var isMuted by remember { mutableStateOf(false) }
  var isSpeakerOn by remember { mutableStateOf(false) }
  var callDurationSeconds by remember { mutableIntStateOf(0) }
  var callStatus by remember { mutableStateOf("Calling...") }

  LaunchedEffect(Unit) {
    try {
      delay(1200)
      callStatus = "Ringing..."
      delay(1800)
      callStatus = "Connected"
      while (true) {
        delay(1000)
        callDurationSeconds++
      }
    } catch (e: Exception) {
      Log.e(TAG, "Audio call simulation error", e)
      callStatus = "Unable to connect the call. Please try again."
    }
  }

  val formattedTimer = String.format(
    "%02d:%02d",
    callDurationSeconds / 60,
    callDurationSeconds % 60
  )

  Box(
    modifier = modifier
      .fillMaxSize()
      .background(Color(0xFF0B141B))
      .statusBarsPadding()
      .navigationBarsPadding()
      .testTag("audio_call_screen")
  ) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(24.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.SpaceBetween
    ) {
      // Top section with Contact Name & Call State (Clean, no technical text)
      Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(top = 40.dp)
      ) {
        Text(
          text = "Apex Customer Care",
          fontSize = 24.sp,
          fontWeight = FontWeight.Bold,
          color = Color.White
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
          text = if (callStatus == "Connected") formattedTimer else callStatus,
          fontSize = 15.sp,
          color = Color(0xFF8696A0)
        )
      }

      // Center Avatar
      Box(
        modifier = Modifier
          .size(130.dp)
          .clip(CircleShape)
          .background(Color.White.copy(alpha = 0.1f)),
        contentAlignment = Alignment.Center
      ) {
        Image(
          painter = painterResource(id = R.drawable.company_logo),
          contentDescription = "Apex Avatar",
          contentScale = ContentScale.Crop,
          modifier = Modifier
            .size(120.dp)
            .clip(CircleShape)
        )
      }

      // Bottom Call Controls
      Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(bottom = 20.dp)
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceEvenly,
          verticalAlignment = Alignment.CenterVertically
        ) {
          // Mute Button
          CallActionButton(
            icon = if (isMuted) Icons.Filled.MicOff else Icons.Filled.Mic,
            label = if (isMuted) "Unmute" else "Mute",
            isActive = isMuted,
            onClick = { isMuted = !isMuted }
          )

          // Speaker Button
          CallActionButton(
            icon = Icons.Filled.VolumeUp,
            label = "Speaker",
            isActive = isSpeakerOn,
            onClick = { isSpeakerOn = !isSpeakerOn }
          )

          // End Call Button
          Box(
            modifier = Modifier
              .size(60.dp)
              .clip(CircleShape)
              .background(Color(0xFFE53935))
              .clickable(onClick = onEndCall)
              .testTag("end_audio_call_button"),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Filled.CallEnd,
              contentDescription = "End Call",
              tint = Color.White,
              modifier = Modifier.size(30.dp)
            )
          }
        }
      }
    }
  }
}

@Composable
fun VideoCallScreen(
  hasCameraPermission: Boolean,
  onEndCall: () -> Unit,
  onScreenShareToggled: (Boolean) -> Unit,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val lifecycleOwner = LocalLifecycleOwner.current

  var isMuted by remember { mutableStateOf(false) }
  var isVideoEnabled by remember { mutableStateOf(true) }
  var isScreenSharing by remember { mutableStateOf(false) }
  var isFrontCamera by remember { mutableStateOf(true) }
  var callDurationSeconds by remember { mutableIntStateOf(0) }
  var callStatus by remember { mutableStateOf("Connecting Video...") }
  var cameraProviderInstance by remember { mutableStateOf<ProcessCameraProvider?>(null) }
  var cameraInitializationFailed by remember { mutableStateOf(false) }

  // Call duration counter
  LaunchedEffect(Unit) {
    try {
      delay(1500)
      callStatus = "Connected"
      while (true) {
        delay(1000)
        callDurationSeconds++
      }
    } catch (e: Exception) {
      Log.e(TAG, "Video call timer error", e)
    }
  }

  // Ensure camera resources are released on disposal
  DisposableEffect(lifecycleOwner) {
    onDispose {
      try {
        cameraProviderInstance?.unbindAll()
        Log.d(TAG, "Camera resources successfully released upon exiting video call")
      } catch (e: Exception) {
        Log.e(TAG, "Error releasing camera resources", e)
      }
    }
  }

  val formattedTimer = String.format(
    "%02d:%02d",
    callDurationSeconds / 60,
    callDurationSeconds % 60
  )

  Box(
    modifier = modifier
      .fillMaxSize()
      .background(Color(0xFF0F1B21))
      .statusBarsPadding()
      .navigationBarsPadding()
      .testTag("video_call_screen")
  ) {
    // 1. Main Video Area: Remote participant stream representation / Screen Sharing
    Box(
      modifier = Modifier.fillMaxSize(),
      contentAlignment = Alignment.Center
    ) {
      if (isScreenSharing) {
        // Screen Sharing Active Banner
        Surface(
          shape = RoundedCornerShape(12.dp),
          color = Color(0xFF00A884).copy(alpha = 0.95f),
          modifier = Modifier.padding(24.dp)
        ) {
          Column(
            modifier = Modifier.padding(18.dp),
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            Icon(
              imageVector = Icons.Filled.ScreenShare,
              contentDescription = null,
              tint = Color.White,
              modifier = Modifier.size(36.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
              text = "You are sharing your screen",
              color = Color.White,
              fontSize = 16.sp,
              fontWeight = FontWeight.Bold
            )
            Text(
              text = "The support agent can now view your screen.",
              color = Color.White.copy(alpha = 0.9f),
              fontSize = 12.sp,
              textAlign = TextAlign.Center
            )
          }
        }
      } else {
        // Remote Company Video View
        Column(
          horizontalAlignment = Alignment.CenterHorizontally,
          modifier = Modifier.padding(bottom = 60.dp)
        ) {
          Image(
            painter = painterResource(id = R.drawable.company_logo),
            contentDescription = "Apex Video Stream",
            contentScale = ContentScale.Crop,
            modifier = Modifier
              .size(110.dp)
              .clip(CircleShape)
          )
          Spacer(modifier = Modifier.height(14.dp))
          Text(
            text = "Apex Customer Care",
            color = Color.White,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
          )
          Text(
            text = if (callStatus == "Connected") formattedTimer else callStatus,
            color = Color(0xFF8696A0),
            fontSize = 14.sp
          )
        }
      }
    }

    // 2. Real Local Camera Video Preview (Picture-in-Picture floating view in top-right)
    if (hasCameraPermission && isVideoEnabled) {
      Box(
        modifier = Modifier
          .align(Alignment.TopEnd)
          .padding(top = 64.dp, end = 16.dp)
          .size(width = 110.dp, height = 155.dp)
          .clip(RoundedCornerShape(12.dp))
          .background(Color.Black)
          .border(1.5.dp, Color.White.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
          .testTag("local_camera_preview")
      ) {
        AndroidView(
          factory = { ctx ->
            val previewView = PreviewView(ctx).apply {
              implementationMode = PreviewView.ImplementationMode.COMPATIBLE
            }
            val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
            cameraProviderFuture.addListener({
              try {
                val cameraProvider = cameraProviderFuture.get()
                cameraProviderInstance = cameraProvider
                val selector = if (isFrontCamera) {
                  CameraSelector.DEFAULT_FRONT_CAMERA
                } else {
                  CameraSelector.DEFAULT_BACK_CAMERA
                }
                val preview = Preview.Builder().build().also {
                  it.setSurfaceProvider(previewView.surfaceProvider)
                }
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(lifecycleOwner, selector, preview)
                Log.d(TAG, "Local camera preview successfully attached and streaming")
              } catch (exc: Exception) {
                Log.e(TAG, "Camera initialization failed", exc)
                cameraInitializationFailed = true
              }
            }, ContextCompat.getMainExecutor(ctx))
            previewView
          },
          update = { previewView ->
            cameraProviderInstance?.let { provider ->
              try {
                val selector = if (isFrontCamera) {
                  CameraSelector.DEFAULT_FRONT_CAMERA
                } else {
                  CameraSelector.DEFAULT_BACK_CAMERA
                }
                val preview = Preview.Builder().build().also {
                  it.setSurfaceProvider(previewView.surfaceProvider)
                }
                provider.unbindAll()
                provider.bindToLifecycle(lifecycleOwner, selector, preview)
              } catch (e: Exception) {
                Log.e(TAG, "Error updating camera selector", e)
              }
            }
          },
          modifier = Modifier.fillMaxSize()
        )
      }
    } else if (!isVideoEnabled) {
      // Local Camera Paused Inset
      Box(
        modifier = Modifier
          .align(Alignment.TopEnd)
          .padding(top = 64.dp, end = 16.dp)
          .size(width = 110.dp, height = 155.dp)
          .clip(RoundedCornerShape(12.dp))
          .background(Color(0xFF1E2A30))
          .border(1.dp, Color.Gray, RoundedCornerShape(12.dp)),
        contentAlignment = Alignment.Center
      ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
          Icon(
            imageVector = Icons.Filled.VideocamOff,
            contentDescription = "Camera Off",
            tint = Color(0xFF8696A0),
            modifier = Modifier.size(24.dp)
          )
          Spacer(modifier = Modifier.height(4.dp))
          Text(
            text = "Video Off",
            color = Color(0xFF8696A0),
            fontSize = 11.sp
          )
        }
      }
    }

    // 3. Top Bar (Status indicator & Camera Switch Button)
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color.Black.copy(alpha = 0.6f)
      ) {
        Row(
          modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Box(
            modifier = Modifier
              .size(8.dp)
              .clip(CircleShape)
              .background(if (isScreenSharing) Color(0xFF00A884) else Color(0xFF4CAF50))
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = if (isScreenSharing) "Screen Sharing" else "End-to-End Encrypted",
            color = Color.White,
            fontSize = 12.sp
          )
        }
      }

      // Switch Camera button (Front / Back camera toggle)
      if (hasCameraPermission && isVideoEnabled) {
        Box(
          modifier = Modifier
            .size(42.dp)
            .clip(CircleShape)
            .background(Color.Black.copy(alpha = 0.6f))
            .clickable { isFrontCamera = !isFrontCamera }
            .testTag("switch_camera_button"),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.Filled.Cameraswitch,
            contentDescription = "Switch Camera",
            tint = Color.White,
            modifier = Modifier.size(22.dp)
          )
        }
      }
    }

    // 4. Bottom Controls (Screen Share, Mute, Video, End Call)
    Column(
      modifier = Modifier
        .align(Alignment.BottomCenter)
        .fillMaxWidth()
        .padding(bottom = 24.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
      ) {
        // Screen Share control (ONLY inside active video call)
        CallActionButton(
          icon = if (isScreenSharing) Icons.Filled.StopScreenShare else Icons.Filled.ScreenShare,
          label = if (isScreenSharing) "Stop Share" else "Share Screen",
          isActive = isScreenSharing,
          onClick = {
            isScreenSharing = !isScreenSharing
            onScreenShareToggled(isScreenSharing)
          }
        )

        // Mic toggle
        CallActionButton(
          icon = if (isMuted) Icons.Filled.MicOff else Icons.Filled.Mic,
          label = if (isMuted) "Unmute" else "Mute",
          isActive = isMuted,
          onClick = { isMuted = !isMuted }
        )

        // Video toggle
        CallActionButton(
          icon = if (isVideoEnabled) Icons.Filled.Videocam else Icons.Filled.VideocamOff,
          label = if (isVideoEnabled) "Cam Off" else "Cam On",
          isActive = !isVideoEnabled,
          onClick = {
            isVideoEnabled = !isVideoEnabled
            if (!isVideoEnabled) {
              cameraProviderInstance?.unbindAll()
            }
          }
        )

        // End Call button
        Box(
          modifier = Modifier
            .size(60.dp)
            .clip(CircleShape)
            .background(Color(0xFFE53935))
            .clickable(onClick = {
              cameraProviderInstance?.unbindAll()
              onEndCall()
            })
            .testTag("end_video_call_button"),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.Filled.CallEnd,
            contentDescription = "End Call",
            tint = Color.White,
            modifier = Modifier.size(30.dp)
          )
        }
      }
    }
  }
}

@Composable
private fun CallActionButton(
  icon: androidx.compose.ui.graphics.vector.ImageVector,
  label: String,
  isActive: Boolean,
  onClick: () -> Unit
) {
  Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    modifier = Modifier.clickable(onClick = onClick)
  ) {
    Box(
      modifier = Modifier
        .size(50.dp)
        .clip(CircleShape)
        .background(if (isActive) Color(0xFF00A884) else Color.White.copy(alpha = 0.2f)),
      contentAlignment = Alignment.Center
    ) {
      Icon(
        imageVector = icon,
        contentDescription = label,
        tint = Color.White,
        modifier = Modifier.size(24.dp)
      )
    }
    Spacer(modifier = Modifier.height(4.dp))
    Text(
      text = label,
      color = Color.White,
      fontSize = 11.sp
    )
  }
}
