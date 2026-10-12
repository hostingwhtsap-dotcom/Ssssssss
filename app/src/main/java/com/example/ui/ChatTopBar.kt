package com.example.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.WhatsAppGreenHeader

@Composable
fun ChatTopBar(
  isTyping: Boolean = false,
  onAudioCallClick: () -> Unit,
  onVideoCallClick: () -> Unit,
  onBackClick: () -> Unit = {},
  modifier: Modifier = Modifier
) {
  Box(
    modifier = modifier
      .fillMaxWidth()
      .background(WhatsAppGreenHeader)
      .statusBarsPadding()
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .height(58.dp)
        .padding(horizontal = 4.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      // Back button
      IconButton(
        onClick = onBackClick,
        modifier = Modifier
          .size(40.dp)
          .testTag("back_button")
      ) {
        Icon(
          imageVector = Icons.AutoMirrored.Filled.ArrowBack,
          contentDescription = "Back",
          tint = Color.White
        )
      }

      // Company logo avatar
      Box(
        modifier = Modifier
          .size(38.dp)
          .clip(CircleShape)
          .background(Color.White.copy(alpha = 0.2f))
      ) {
        Image(
          painter = painterResource(id = R.drawable.company_logo),
          contentDescription = "Apex Customer Care Logo",
          contentScale = ContentScale.Crop,
          modifier = Modifier
            .size(38.dp)
            .clip(CircleShape)
        )
      }

      Spacer(modifier = Modifier.width(9.dp))

      // Company Name & status (online / typing)
      Column(
        modifier = Modifier.weight(1f)
      ) {
        Text(
          text = "Apex Customer Care",
          color = Color.White,
          fontSize = 16.sp,
          fontWeight = FontWeight.SemiBold,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis
        )

        Text(
          text = if (isTyping) "typing..." else "online",
          color = if (isTyping) Color(0xFFD9FDD3) else Color.White.copy(alpha = 0.85f),
          fontSize = 12.sp,
          fontWeight = if (isTyping) FontWeight.Medium else FontWeight.Normal,
          maxLines = 1
        )
      }

      // Video Call Icon on the right
      IconButton(
        onClick = onVideoCallClick,
        modifier = Modifier
          .size(42.dp)
          .testTag("video_call_button")
      ) {
        Icon(
          imageVector = Icons.Filled.Videocam,
          contentDescription = "Video Call",
          tint = Color.White
        )
      }

      // Audio Call Icon on the right
      IconButton(
        onClick = onAudioCallClick,
        modifier = Modifier
          .size(42.dp)
          .testTag("audio_call_button")
      ) {
        Icon(
          imageVector = Icons.Filled.Call,
          contentDescription = "Audio Call",
          tint = Color.White
        )
      }
    }
  }
}
