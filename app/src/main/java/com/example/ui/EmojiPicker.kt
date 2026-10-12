package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.WhatsAppGreenHeader
import com.example.ui.theme.WhatsAppTextPrimary

@Composable
fun EmojiPickerView(
  onEmojiSelected: (String) -> Unit,
  onBackspaceClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  val categoryNames = listOf("Smileys", "Hands", "Hearts", "Objects")

  val categoryEmojis = listOf(
    // Smileys
    listOf("😀", "😃", "😄", "😁", "😆", "😅", "😂", "🤣", "😊", "😇", "🙂", "🙃", "😉", "😌", "😍", "🥰", "😘", "😗", "😙", "😚", "😋", "😛", "😜", "🤪", "😝", "🤗", "🤭", "🤫", "🤔", "🤐", "🤨", "😐", "😑", "😶", "😏", "😒", "🙄", "😬", "😮", "😴"),
    // Hands & Gestures
    listOf("👍", "👎", "👏", "🙌", "👐", "🤲", "🤝", "🙏", "✌️", "🤞", "🤟", "🤘", "🤙", "👈", "👉", "👆", "👇", "☝️", "👋", "🤚", "🖐️", "✋", "🖖", "✍️", "🤳", "💪"),
    // Hearts & Emotions
    listOf("❤️", "🧡", "💛", "💚", "💙", "💜", "🖤", "🤍", "🤎", "💔", "❣️", "💕", "💞", "💓", "💗", "💖", "💘", "💝", "💟", "✨", "🔥", "🎉", "⭐", "🌟"),
    // Objects & Work
    listOf("📦", "📱", "💻", "⌨️", "🖥️", "🖨️", "📞", "📟", "📠", "🔋", "🔌", "💡", "🔦", "🕯️", "📄", "📁", "📂", "💼", "✉️", "📧", "📩", "📨", "🏷️", "📌")
  )

  var selectedCategoryIndex by remember { mutableIntStateOf(0) }

  Surface(
    shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
    color = Color(0xFFF7F7F8),
    shadowElevation = 4.dp,
    modifier = modifier
      .fillMaxWidth()
      .height(240.dp)
      .testTag("emoji_picker_tray")
  ) {
    Column(
      modifier = Modifier.fillMaxWidth()
    ) {
      // Category header tabs
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .background(Color(0xFFEDEDED))
          .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        LazyRow(
          modifier = Modifier.weight(1f),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          itemsIndexed(categoryNames) { index, title ->
            val isSelected = selectedCategoryIndex == index
            Box(
              modifier = Modifier
                .clickable { selectedCategoryIndex = index }
                .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
              Text(
                text = title,
                fontSize = 12.5.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) WhatsAppGreenHeader else WhatsAppTextPrimary
              )
            }
          }
        }

        // Backspace action
        IconButton(
          onClick = onBackspaceClick,
          modifier = Modifier.size(34.dp)
        ) {
          Icon(
            imageVector = Icons.AutoMirrored.Filled.Backspace,
            contentDescription = "Backspace",
            tint = Color(0xFF54656F),
            modifier = Modifier.size(20.dp)
          )
        }
      }

      // Emojis grid
      LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 40.dp),
        modifier = Modifier
          .fillMaxWidth()
          .weight(1f)
          .padding(horizontal = 6.dp, vertical = 6.dp)
      ) {
        val emojis = categoryEmojis.getOrElse(selectedCategoryIndex) { categoryEmojis[0] }
        items(emojis) { emoji ->
          Box(
            modifier = Modifier
              .clickable { onEmojiSelected(emoji) }
              .padding(4.dp),
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = emoji,
              fontSize = 24.sp
            )
          }
        }
      }
    }
  }
}
