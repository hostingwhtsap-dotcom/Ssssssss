package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.InsertDriveFile
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.model.MessageAttachmentType
import com.example.model.SharedContentItem
import com.example.ui.theme.WhatsAppGreenPrimary
import com.example.ui.theme.WhatsAppTextPrimary
import com.example.ui.theme.WhatsAppTextSecondary

@Composable
fun SharedMediaStageBar(
  items: List<SharedContentItem>,
  onRemoveItem: (SharedContentItem) -> Unit,
  onClearAll: () -> Unit
) {
  if (items.isEmpty()) return

  Card(
    modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = 8.dp, vertical = 4.dp)
      .testTag("shared_media_stage_bar"),
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(containerColor = Color(0xFFF0F4F2)),
    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 8.dp)
    ) {
      // Header: Item count & Cancel all
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Box(
            modifier = Modifier
              .size(8.dp)
              .clip(CircleShape)
              .background(WhatsAppGreenPrimary)
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = "Shared Content (${items.size})",
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = WhatsAppTextPrimary
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = "• Ready to send",
            fontSize = 11.5.sp,
            color = WhatsAppGreenPrimary,
            fontWeight = FontWeight.Medium
          )
        }

        Text(
          text = "Cancel All",
          color = Color(0xFFD32F2F),
          fontSize = 12.sp,
          fontWeight = FontWeight.SemiBold,
          modifier = Modifier
            .clickable(onClick = onClearAll)
            .padding(4.dp)
            .testTag("shared_stage_cancel_all")
        )
      }

      Spacer(modifier = Modifier.height(6.dp))

      // Horizontal list of staged items
      LazyRow(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        items(items, key = { it.id }) { item ->
          StagedItemCard(
            item = item,
            onRemove = { onRemoveItem(item) }
          )
        }
      }
    }
  }
}

@Composable
private fun StagedItemCard(
  item: SharedContentItem,
  onRemove: () -> Unit
) {
  Box(
    modifier = Modifier
      .size(width = 110.dp, height = 90.dp)
  ) {
    Card(
      modifier = Modifier
        .size(width = 100.dp, height = 82.dp)
        .align(Alignment.BottomStart),
      shape = RoundedCornerShape(10.dp),
      colors = CardDefaults.cardColors(containerColor = Color.White),
      border = borderModifier(item.type)
    ) {
      when (item.type) {
        MessageAttachmentType.IMAGE -> {
          Box(modifier = Modifier.fillMaxWidth()) {
            AsyncImage(
              model = item.file,
              contentDescription = "Shared image",
              contentScale = ContentScale.Crop,
              modifier = Modifier
                .fillMaxWidth()
                .height(82.dp)
            )
          }
        }

        MessageAttachmentType.VIDEO -> {
          Box(modifier = Modifier.fillMaxWidth()) {
            AsyncImage(
              model = item.thumbnailFile ?: item.file,
              contentDescription = "Shared video",
              contentScale = ContentScale.Crop,
              modifier = Modifier
                .fillMaxWidth()
                .height(82.dp)
            )
            Box(
              modifier = Modifier
                .size(24.dp)
                .align(Alignment.Center)
                .clip(CircleShape)
                .background(Color.Black.copy(alpha = 0.6f)),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Filled.PlayArrow,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(16.dp)
              )
            }
          }
        }

        MessageAttachmentType.AUDIO -> {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(8.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            Box(
              modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(Color(0xFFE0663B).copy(alpha = 0.15f)),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Filled.Audiotrack,
                contentDescription = null,
                tint = Color(0xFFE0663B),
                modifier = Modifier.size(18.dp)
              )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = item.fileName,
              fontSize = 11.sp,
              fontWeight = FontWeight.Medium,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis
            )
            Text(
              text = item.sizeOrDuration,
              fontSize = 9.5.sp,
              color = WhatsAppTextSecondary
            )
          }
        }

        else -> {
          // Document / other
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(8.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            Box(
              modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(Color(0xFF7E57C2).copy(alpha = 0.15f)),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Filled.InsertDriveFile,
                contentDescription = null,
                tint = Color(0xFF7E57C2),
                modifier = Modifier.size(18.dp)
              )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = item.fileName,
              fontSize = 11.sp,
              fontWeight = FontWeight.Medium,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis
            )
            Text(
              text = item.sizeOrDuration,
              fontSize = 9.5.sp,
              color = WhatsAppTextSecondary
            )
          }
        }
      }
    }

    // Small circular remove (X) button on top right
    Box(
      modifier = Modifier
        .size(22.dp)
        .align(Alignment.TopEnd)
        .clip(CircleShape)
        .background(Color(0xFF37474F))
        .border(1.5.dp, Color.White, CircleShape)
        .clickable(onClick = onRemove)
        .testTag("remove_shared_item_${item.id}"),
      contentAlignment = Alignment.Center
    ) {
      Icon(
        imageVector = Icons.Filled.Close,
        contentDescription = "Remove item",
        tint = Color.White,
        modifier = Modifier.size(12.dp)
      )
    }
  }
}

private fun borderModifier(type: MessageAttachmentType): androidx.compose.foundation.BorderStroke {
  val borderColor = when (type) {
    MessageAttachmentType.AUDIO -> Color(0xFFE0663B).copy(alpha = 0.4f)
    MessageAttachmentType.DOCUMENT -> Color(0xFF7E57C2).copy(alpha = 0.4f)
    else -> Color(0xFFCFD8DC)
  }
  return androidx.compose.foundation.BorderStroke(1.dp, borderColor)
}
