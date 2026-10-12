package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
  primary = WhatsAppGreenHeader,
  onPrimary = Color.White,
  primaryContainer = WhatsAppGreenFab,
  onPrimaryContainer = Color.White,
  secondary = WhatsAppGreenDark,
  onSecondary = Color.White,
  background = WhatsAppChatBg,
  onBackground = WhatsAppTextPrimary,
  surface = Color.White,
  onSurface = WhatsAppTextPrimary,
  surfaceVariant = WhatsAppInputBg,
  onSurfaceVariant = WhatsAppIconTint
)

@Composable
fun CustomerSupportTheme(
  content: @Composable () -> Unit
) {
  MaterialTheme(
    colorScheme = LightColorScheme,
    typography = Typography,
    content = content
  )
}
