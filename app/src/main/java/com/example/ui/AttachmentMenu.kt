package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.InsertDriveFile
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.WhatsAppTextPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttachmentMenuSheet(
  sheetState: SheetState,
  onDismiss: () -> Unit,
  onPickGallery: () -> Unit,
  onCaptureCamera: () -> Unit,
  onPickDocuments: () -> Unit,
  onPickAudio: () -> Unit
) {
  ModalBottomSheet(
    onDismissRequest = onDismiss,
    sheetState = sheetState,
    containerColor = Color.White,
    dragHandle = {
      Box(
        modifier = Modifier
          .padding(top = 10.dp, bottom = 12.dp)
          .width(36.dp)
          .height(4.dp)
          .clip(RoundedCornerShape(2.dp))
          .background(Color(0xFFB0B0B0))
      )
    },
    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 12.dp)
        .testTag("attachment_menu_sheet"),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      // Exactly 4 options: Gallery, Camera, Documents, Audio
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceAround
      ) {
        AttachmentGridItem(
          label = "Gallery",
          icon = Icons.Filled.PhotoLibrary,
          iconTint = Color(0xFF1E88E5),
          onClick = {
            onDismiss()
            onPickGallery()
          }
        )
        AttachmentGridItem(
          label = "Camera",
          icon = Icons.Filled.CameraAlt,
          iconTint = Color(0xFFE91E63),
          onClick = {
            onDismiss()
            onCaptureCamera()
          }
        )
        AttachmentGridItem(
          label = "Documents",
          icon = Icons.Filled.InsertDriveFile,
          iconTint = Color(0xFF7E57C2),
          onClick = {
            onDismiss()
            onPickDocuments()
          }
        )
        AttachmentGridItem(
          label = "Audio",
          icon = Icons.Filled.Headphones,
          iconTint = Color(0xFFE0663B),
          onClick = {
            onDismiss()
            onPickAudio()
          }
        )
      }

      Spacer(modifier = Modifier.height(28.dp))
    }
  }
}

@Composable
private fun AttachmentGridItem(
  label: String,
  icon: ImageVector,
  iconTint: Color,
  onClick: () -> Unit
) {
  Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    modifier = Modifier
      .clickable(onClick = onClick)
      .padding(4.dp)
  ) {
    Box(
      modifier = Modifier
        .size(56.dp)
        .clip(RoundedCornerShape(18.dp))
        .background(Color(0xFFF8F9FA))
        .border(1.dp, Color(0xFFE4E6EB), RoundedCornerShape(18.dp)),
      contentAlignment = Alignment.Center
    ) {
      Icon(
        imageVector = icon,
        contentDescription = label,
        tint = iconTint,
        modifier = Modifier.size(26.dp)
      )
    }

    Spacer(modifier = Modifier.height(6.dp))

    Text(
      text = label,
      fontSize = 12.5.sp,
      color = WhatsAppTextPrimary
    )
  }
}
