package com.example.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import com.example.ui.theme.WhatsAppChatBg

@Composable
fun WhatsAppWallpaper(
  modifier: Modifier = Modifier,
  content: @Composable () -> Unit
) {
  Box(
    modifier = modifier
      .fillMaxSize()
      .background(WhatsAppChatBg)
  ) {
    Canvas(modifier = Modifier.fillMaxSize()) {
      drawWhatsAppDoodles(this)
    }
    content()
  }
}

private fun drawWhatsAppDoodles(scope: DrawScope) {
  val width = scope.size.width
  val height = scope.size.height

  // Subtle doodle tint matching reference screenshots wallpaper
  val doodleColor = Color(0x10D81B60)
  val stroke = Stroke(width = 1.6f)

  val cellWidth = 120f
  val cellHeight = 120f
  var row = 0

  var y = 20f
  while (y < height) {
    var x = if (row % 2 == 0) 20f else 80f
    var col = 0
    while (x < width) {
      val doodleIndex = (row * 3 + col) % 8
      drawSingleDoodle(scope, doodleIndex, Offset(x, y), doodleColor, stroke)
      x += cellWidth
      col++
    }
    y += cellHeight
    row++
  }
}

private fun drawSingleDoodle(
  scope: DrawScope,
  index: Int,
  center: Offset,
  color: Color,
  stroke: Stroke
) {
  when (index) {
    0 -> {
      // Speech bubble doodle
      val path = Path().apply {
        moveTo(center.x - 14f, center.y - 10f)
        lineTo(center.x + 14f, center.y - 10f)
        quadraticTo(center.x + 18f, center.y - 10f, center.x + 18f, center.y - 6f)
        lineTo(center.x + 18f, center.y + 6f)
        quadraticTo(center.x + 18f, center.y + 10f, center.x + 14f, center.y + 10f)
        lineTo(center.x - 6f, center.y + 10f)
        lineTo(center.x - 14f, center.y + 16f)
        lineTo(center.x - 12f, center.y + 10f)
        lineTo(center.x - 14f, center.y + 10f)
        quadraticTo(center.x - 18f, center.y + 10f, center.x - 18f, center.y + 6f)
        lineTo(center.x - 18f, center.y - 6f)
        quadraticTo(center.x - 18f, center.y - 10f, center.x - 14f, center.y - 10f)
        close()
      }
      scope.drawPath(path, color, style = stroke)
    }
    1 -> {
      // Little heart doodle
      val path = Path().apply {
        moveTo(center.x, center.y + 8f)
        cubicTo(
          center.x - 14f, center.y - 4f,
          center.x - 14f, center.y - 12f,
          center.x, center.y - 4f
        )
        cubicTo(
          center.x + 14f, center.y - 12f,
          center.x + 14f, center.y - 4f,
          center.x, center.y + 8f
        )
      }
      scope.drawPath(path, color, style = stroke)
    }
    2 -> {
      // Clock doodle
      scope.drawCircle(color, radius = 12f, center = center, style = stroke)
      scope.drawLine(color, start = center, end = Offset(center.x, center.y - 6f), strokeWidth = 1.8f)
      scope.drawLine(color, start = center, end = Offset(center.x + 5f, center.y), strokeWidth = 1.8f)
    }
    3 -> {
      // Coffee cup doodle
      val path = Path().apply {
        moveTo(center.x - 10f, center.y - 8f)
        lineTo(center.x + 8f, center.y - 8f)
        lineTo(center.x + 6f, center.y + 8f)
        quadraticTo(center.x, center.y + 11f, center.x - 8f, center.y + 8f)
        close()
      }
      scope.drawPath(path, color, style = stroke)
      // handle
      scope.drawArc(
        color = color,
        startAngle = 270f,
        sweepAngle = 180f,
        useCenter = false,
        topLeft = Offset(center.x + 4f, center.y - 5f),
        size = Size(10f, 10f),
        style = stroke
      )
    }
    4 -> {
      // Star doodle
      val starPath = Path().apply {
        moveTo(center.x, center.y - 12f)
        lineTo(center.x + 3f, center.y - 4f)
        lineTo(center.x + 12f, center.y - 4f)
        lineTo(center.x + 5f, center.y + 2f)
        lineTo(center.x + 8f, center.y + 11f)
        lineTo(center.x, center.y + 6f)
        lineTo(center.x - 8f, center.y + 11f)
        lineTo(center.x - 5f, center.y + 2f)
        lineTo(center.x - 12f, center.y - 4f)
        lineTo(center.x - 3f, center.y - 4f)
        close()
      }
      scope.drawPath(starPath, color, style = stroke)
    }
    5 -> {
      // Musical note doodle
      scope.drawCircle(color, radius = 5f, center = Offset(center.x - 6f, center.y + 6f), style = stroke)
      scope.drawLine(color, start = Offset(center.x - 1f, center.y + 6f), end = Offset(center.x - 1f, center.y - 10f), strokeWidth = 1.8f)
      scope.drawLine(color, start = Offset(center.x - 1f, center.y - 10f), end = Offset(center.x + 9f, center.y - 8f), strokeWidth = 1.8f)
      scope.drawLine(color, start = Offset(center.x + 9f, center.y - 8f), end = Offset(center.x + 9f, center.y + 4f), strokeWidth = 1.8f)
      scope.drawCircle(color, radius = 5f, center = Offset(center.x + 4f, center.y + 4f), style = stroke)
    }
    6 -> {
      // Paper plane doodle
      val planePath = Path().apply {
        moveTo(center.x - 12f, center.y - 10f)
        lineTo(center.x + 12f, center.y)
        lineTo(center.x - 12f, center.y + 10f)
        lineTo(center.x - 6f, center.y)
        close()
      }
      scope.drawPath(planePath, color, style = stroke)
      scope.drawLine(color, start = Offset(center.x - 6f, center.y), end = Offset(center.x + 12f, center.y), strokeWidth = 1.4f)
    }
    7 -> {
      // Sparkle / camera doodle
      val cameraPath = Path().apply {
        moveTo(center.x - 12f, center.y - 6f)
        lineTo(center.x - 6f, center.y - 6f)
        lineTo(center.x - 3f, center.y - 10f)
        lineTo(center.x + 3f, center.y - 10f)
        lineTo(center.x + 6f, center.y - 6f)
        lineTo(center.x + 12f, center.y - 6f)
        lineTo(center.x + 12f, center.y + 9f)
        lineTo(center.x - 12f, center.y + 9f)
        close()
      }
      scope.drawPath(cameraPath, color, style = stroke)
      scope.drawCircle(color, radius = 4.5f, center = Offset(center.x, center.y + 1f), style = stroke)
    }
  }
}
