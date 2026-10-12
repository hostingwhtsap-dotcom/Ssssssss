package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Customer Support", appName)
  }

  @Test
  fun `formatFileSize correctly formats bytes`() {
    val b = com.example.util.MediaStorageHelper.formatFileSize(500)
    assertEquals("500 B", b)

    val kb = com.example.util.MediaStorageHelper.formatFileSize(1024 * 50)
    assertEquals("50 KB", kb)

    val mb = com.example.util.MediaStorageHelper.formatFileSize(1024 * 1024 * 3 + 500000)
    assertEquals("3.5 MB", mb)
  }

  @Test
  fun `chat message preserves audio attachment type and name`() {
    val msg = com.example.model.ChatMessage(
      id = "audio_123",
      text = "",
      timestamp = "10:30 AM",
      isOutgoing = true,
      attachmentType = com.example.model.MessageAttachmentType.AUDIO,
      attachmentName = "meeting_notes.mp3",
      attachmentSize = "2:15 • 1.8 MB",
      mediaPath = "/fake/path/meeting_notes.mp3"
    )
    assertEquals(com.example.model.MessageAttachmentType.AUDIO, msg.attachmentType)
    assertEquals("meeting_notes.mp3", msg.attachmentName)
    assertEquals("2:15 • 1.8 MB", msg.attachmentSize)
  }

  @Test
  fun `queryDeviceAudioFiles safely executes and returns list`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val list = com.example.util.MediaStorageHelper.queryDeviceAudioFiles(context)
    org.junit.Assert.assertNotNull(list)
  }

  @Test
  fun `shared content item holds staged media correctly`() {
    val file = java.io.File(ApplicationProvider.getApplicationContext<Context>().filesDir, "sample.mp3")
    val item = com.example.model.SharedContentItem(
      file = file,
      type = com.example.model.MessageAttachmentType.AUDIO,
      fileName = "sample.mp3",
      sizeOrDuration = "3.2 MB"
    )
    assertEquals("sample.mp3", item.fileName)
    assertEquals(com.example.model.MessageAttachmentType.AUDIO, item.type)
    assertEquals("3.2 MB", item.sizeOrDuration)
  }

  @Test
  fun `activity handles ACTION_SEND and ACTION_SEND_MULTIPLE intent filters`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val packageManager = context.packageManager

    // Test single send intent
    val sendIntent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
      type = "audio/*"
    }
    val resolveSend = packageManager.queryIntentActivities(sendIntent, 0)
    org.junit.Assert.assertTrue("Should match ACTION_SEND intent filter", resolveSend.any { it.activityInfo.packageName == context.packageName })

    // Test multiple send intent
    val sendMultipleIntent = android.content.Intent(android.content.Intent.ACTION_SEND_MULTIPLE).apply {
      type = "image/*"
    }
    val resolveMultiple = packageManager.queryIntentActivities(sendMultipleIntent, 0)
    org.junit.Assert.assertTrue("Should match ACTION_SEND_MULTIPLE intent filter", resolveMultiple.any { it.activityInfo.packageName == context.packageName })
  }
}
