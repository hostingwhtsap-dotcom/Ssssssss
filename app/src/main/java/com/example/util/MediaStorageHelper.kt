package com.example.util

import android.content.ContentUris
import android.content.Context
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.util.Log
import com.example.model.ChatMessage
import com.example.model.MessageAttachmentType
import com.example.model.MessageStatus
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class DeviceAudioItem(
  val id: Long,
  val uri: Uri,
  val displayName: String,
  val title: String,
  val artist: String,
  val durationMs: Long,
  val durationFormatted: String,
  val sizeBytes: Long,
  val sizeFormatted: String,
  val mimeType: String
)

private const val TAG = "MediaStorageHelper"

object MediaStorageHelper {

  private fun getMediaDirectory(context: Context): File {
    val dir = File(context.filesDir, "chat_media")
    if (!dir.exists()) {
      dir.mkdirs()
    }
    return dir
  }

  fun saveBitmapToInternalStorage(context: Context, bitmap: Bitmap, prefix: String = "IMG"): File? {
    return try {
      val dir = getMediaDirectory(context)
      val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss_SSS", Locale.US).format(Date())
      val file = File(dir, "${prefix}_$timeStamp.jpg")
      FileOutputStream(file).use { out ->
        bitmap.compress(Bitmap.CompressFormat.JPEG, 92, out)
      }
      file
    } catch (e: Exception) {
      Log.e(TAG, "Failed to save bitmap", e)
      null
    }
  }

  fun copyUriToInternalStorage(context: Context, uri: Uri, defaultExtension: String = "dat"): File? {
    return try {
      val dir = getMediaDirectory(context)
      val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss_SSS", Locale.US).format(Date())
      val file = File(dir, "MEDIA_${timeStamp}.$defaultExtension")
      context.contentResolver.openInputStream(uri)?.use { input: InputStream ->
        FileOutputStream(file).use { output ->
          input.copyTo(output)
        }
      }
      file
    } catch (e: Exception) {
      Log.e(TAG, "Failed to copy URI to storage", e)
      null
    }
  }

  fun createVoiceRecordingFile(context: Context): File {
    val dir = getMediaDirectory(context)
    val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss_SSS", Locale.US).format(Date())
    return File(dir, "VOICE_$timeStamp.m4a")
  }

  fun extractVideoMetadata(context: Context, videoFile: File): Pair<File?, String> {
    var thumbnailFile: File? = null
    var durationFormatted = "0:00"
    val retriever = MediaMetadataRetriever()
    try {
      retriever.setDataSource(videoFile.absolutePath)
      val durationMsStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
      val durationMs = durationMsStr?.toLongOrNull() ?: 0L
      val seconds = (durationMs / 1000) % 60
      val minutes = (durationMs / 1000) / 60
      durationFormatted = String.format("%d:%02d", minutes, seconds)

      val frameBitmap = retriever.getFrameAtTime(1000000, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
        ?: retriever.frameAtTime
      if (frameBitmap != null) {
        thumbnailFile = saveBitmapToInternalStorage(context, frameBitmap, "THUMB")
      }
    } catch (e: Exception) {
      Log.e(TAG, "Error extracting video metadata", e)
    } finally {
      try {
        retriever.release()
      } catch (_: Exception) {}
    }
    return Pair(thumbnailFile, durationFormatted)
  }

  fun getFileMetadata(context: Context, uri: Uri, defaultName: String = "File"): Triple<String, String, String> {
    var fileName = defaultName
    var fileSizeFormatted = "File"
    var durationFormatted = ""

    try {
      val cursor = context.contentResolver.query(uri, null, null, null, null)
      cursor?.use {
        if (it.moveToFirst()) {
          val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
          if (nameIndex != -1) {
            val name = it.getString(nameIndex)
            if (!name.isNullOrBlank()) {
              fileName = name
            }
          }
          val sizeIndex = it.getColumnIndex(OpenableColumns.SIZE)
          if (sizeIndex != -1) {
            val sizeBytes = it.getLong(sizeIndex)
            if (sizeBytes > 0) {
              fileSizeFormatted = formatFileSize(sizeBytes)
            }
          }
        }
      }
    } catch (e: Exception) {
      Log.e(TAG, "Error querying file metadata", e)
    }

    if (fileName == defaultName) {
      uri.lastPathSegment?.let { segment ->
        val clean = segment.substringAfterLast('/')
        if (clean.isNotBlank()) fileName = clean
      }
    }

    // Attempt duration extraction if it's audio
    try {
      val retriever = MediaMetadataRetriever()
      retriever.setDataSource(context, uri)
      val durationMsStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
      val durationMs = durationMsStr?.toLongOrNull() ?: 0L
      if (durationMs > 0) {
        val seconds = (durationMs / 1000) % 60
        val minutes = (durationMs / 1000) / 60
        durationFormatted = String.format("%d:%02d", minutes, seconds)
      }
      retriever.release()
    } catch (_: Exception) {}

    return Triple(fileName, fileSizeFormatted, durationFormatted)
  }

  fun formatFileSize(bytes: Long): String {
    return when {
      bytes >= 1024 * 1024 -> String.format(Locale.US, "%.1f MB", bytes / (1024.0 * 1024.0))
      bytes >= 1024 -> String.format(Locale.US, "%d KB", bytes / 1024)
      else -> "$bytes B"
    }
  }

  fun queryDeviceAudioFiles(context: Context, searchQuery: String = ""): List<DeviceAudioItem> {
    val audioList = mutableListOf<DeviceAudioItem>()
    val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
      MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
    } else {
      MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
    }

    val projection = arrayOf(
      MediaStore.Audio.Media._ID,
      MediaStore.Audio.Media.DISPLAY_NAME,
      MediaStore.Audio.Media.TITLE,
      MediaStore.Audio.Media.ARTIST,
      MediaStore.Audio.Media.DURATION,
      MediaStore.Audio.Media.SIZE,
      MediaStore.Audio.Media.MIME_TYPE
    )

    val selection = StringBuilder("(${MediaStore.Audio.Media.IS_MUSIC} != 0 OR ${MediaStore.Audio.Media.MIME_TYPE} LIKE 'audio/%' OR ${MediaStore.Audio.Media.DISPLAY_NAME} LIKE '%.mp3' OR ${MediaStore.Audio.Media.DISPLAY_NAME} LIKE '%.m4a' OR ${MediaStore.Audio.Media.DISPLAY_NAME} LIKE '%.aac' OR ${MediaStore.Audio.Media.DISPLAY_NAME} LIKE '%.wav' OR ${MediaStore.Audio.Media.DISPLAY_NAME} LIKE '%.ogg' OR ${MediaStore.Audio.Media.DISPLAY_NAME} LIKE '%.flac')")
    val selectionArgs = mutableListOf<String>()

    if (searchQuery.isNotBlank()) {
      selection.append(" AND (${MediaStore.Audio.Media.TITLE} LIKE ? OR ${MediaStore.Audio.Media.DISPLAY_NAME} LIKE ? OR ${MediaStore.Audio.Media.ARTIST} LIKE ?)")
      val pattern = "%$searchQuery%"
      selectionArgs.add(pattern)
      selectionArgs.add(pattern)
      selectionArgs.add(pattern)
    }

    val sortOrder = "${MediaStore.Audio.Media.DATE_ADDED} DESC"

    try {
      context.contentResolver.query(
        collection,
        projection,
        selection.toString(),
        if (selectionArgs.isNotEmpty()) selectionArgs.toTypedArray() else null,
        sortOrder
      )?.use { cursor ->
        val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
        val nameColumn = cursor.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME)
        val titleColumn = cursor.getColumnIndex(MediaStore.Audio.Media.TITLE)
        val artistColumn = cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST)
        val durationColumn = cursor.getColumnIndex(MediaStore.Audio.Media.DURATION)
        val sizeColumn = cursor.getColumnIndex(MediaStore.Audio.Media.SIZE)
        val mimeColumn = cursor.getColumnIndex(MediaStore.Audio.Media.MIME_TYPE)

        while (cursor.moveToNext()) {
          val id = cursor.getLong(idColumn)
          val contentUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id)
          val displayName = if (nameColumn != -1) cursor.getString(nameColumn) ?: "Audio_$id.mp3" else "Audio_$id.mp3"
          val title = if (titleColumn != -1) cursor.getString(titleColumn) ?: displayName else displayName
          val artist = if (artistColumn != -1) cursor.getString(artistColumn) ?: "Unknown Artist" else "Unknown Artist"
          val durationMs = if (durationColumn != -1) cursor.getLong(durationColumn) else 0L
          val sizeBytes = if (sizeColumn != -1) cursor.getLong(sizeColumn) else 0L
          val mime = if (mimeColumn != -1) cursor.getString(mimeColumn) ?: "audio/mpeg" else "audio/mpeg"

          val durationSecs = durationMs / 1000
          val durationFormatted = String.format(Locale.US, "%d:%02d", durationSecs / 60, durationSecs % 60)
          val sizeFormatted = formatFileSize(sizeBytes)

          audioList.add(
            DeviceAudioItem(
              id = id,
              uri = contentUri,
              displayName = displayName,
              title = title,
              artist = if (artist.contains("unknown", ignoreCase = true)) "Audio Track" else artist,
              durationMs = durationMs,
              durationFormatted = durationFormatted,
              sizeBytes = sizeBytes,
              sizeFormatted = sizeFormatted,
              mimeType = mime
            )
          )
        }
      }
    } catch (e: Exception) {
      Log.e(TAG, "Error querying device audio files", e)
    }

    return audioList
  }

  // Persistent Messages File
  private fun getMessagesFile(context: Context): File {
    return File(context.filesDir, "chat_messages_store.json")
  }

  fun saveMessages(context: Context, messages: List<ChatMessage>) {
    try {
      val jsonArray = JSONArray()
      for (msg in messages) {
        val obj = JSONObject().apply {
          put("id", msg.id)
          put("text", msg.text)
          put("timestamp", msg.timestamp)
          put("isOutgoing", msg.isOutgoing)
          put("status", msg.status.name)
          put("attachmentType", msg.attachmentType.name)
          put("mediaPath", msg.mediaPath ?: "")
          put("thumbnailPath", msg.thumbnailPath ?: "")
          put("attachmentName", msg.attachmentName ?: "")
          put("attachmentSize", msg.attachmentSize ?: "")
          put("audioDuration", msg.audioDuration ?: "")
          put("waveform", msg.waveform.joinToString(","))
        }
        jsonArray.put(obj)
      }
      val file = getMessagesFile(context)
      FileOutputStream(file).use { out ->
        out.write(jsonArray.toString().toByteArray())
      }
    } catch (e: Exception) {
      Log.e(TAG, "Failed to persist chat messages", e)
    }
  }

  fun resampleWaveform(samples: List<Float>, targetCount: Int = 30): List<Float> {
    if (samples.isEmpty()) return generateWaveform(null, targetCount)
    if (samples.size == targetCount) return samples
    if (samples.size < targetCount) {
      val result = ArrayList<Float>(targetCount)
      for (i in 0 until targetCount) {
        val srcIndex = (i.toFloat() / (targetCount - 1) * (samples.size - 1)).toInt().coerceIn(0, samples.lastIndex)
        result.add(samples[srcIndex])
      }
      return result
    }
    val chunkSize = samples.size.toFloat() / targetCount
    val result = ArrayList<Float>(targetCount)
    for (i in 0 until targetCount) {
      val start = (i * chunkSize).toInt().coerceIn(0, samples.lastIndex)
      val end = ((i + 1) * chunkSize).toInt().coerceIn(start + 1, samples.size)
      val sub = samples.subList(start, end)
      val avg = if (sub.isNotEmpty()) sub.average().toFloat() else 0.2f
      result.add(avg.coerceIn(0.08f, 1.0f))
    }
    return result
  }

  fun generateWaveform(filePath: String?, targetCount: Int = 30): List<Float> {
    val seed = filePath?.hashCode()?.toLong() ?: 42L
    val rng = java.util.Random(seed)
    val list = ArrayList<Float>(targetCount)
    var current = 0.25f + rng.nextFloat() * 0.4f
    for (i in 0 until targetCount) {
      // Natural speech envelope with varying bursts and pauses
      val delta = (rng.nextFloat() - 0.49f) * 0.4f
      current = (current + delta).coerceIn(0.1f, 0.95f)
      list.add(current)
    }
    return list
  }

  fun loadMessages(context: Context): List<ChatMessage> {
    val file = getMessagesFile(context)
    if (!file.exists()) return emptyList()
    return try {
      val jsonString = file.readText()
      val jsonArray = JSONArray(jsonString)
      val list = mutableListOf<ChatMessage>()
      for (i in 0 until jsonArray.length()) {
        val obj = jsonArray.getJSONObject(i)
        val path = obj.optString("mediaPath").takeIf { it.isNotBlank() }
        val rawWaveform = obj.optString("waveform").takeIf { it.isNotBlank() }?.let { str ->
          str.split(",").mapNotNull { it.toFloatOrNull() }
        } ?: if (obj.optString("attachmentType") == MessageAttachmentType.VOICE_NOTE.name ||
                obj.optString("attachmentType") == MessageAttachmentType.AUDIO.name) {
          generateWaveform(path, 30)
        } else {
          emptyList()
        }

        list.add(
          ChatMessage(
            id = obj.getString("id"),
            text = obj.getString("text"),
            timestamp = obj.getString("timestamp"),
            isOutgoing = obj.getBoolean("isOutgoing"),
            status = try {
              MessageStatus.valueOf(obj.getString("status"))
            } catch (_: Exception) {
              MessageStatus.READ
            },
            attachmentType = try {
              MessageAttachmentType.valueOf(obj.getString("attachmentType"))
            } catch (_: Exception) {
              MessageAttachmentType.NONE
            },
            mediaPath = path,
            thumbnailPath = obj.optString("thumbnailPath").takeIf { it.isNotBlank() },
            attachmentName = obj.optString("attachmentName").takeIf { it.isNotBlank() },
            attachmentSize = obj.optString("attachmentSize").takeIf { it.isNotBlank() },
            audioDuration = obj.optString("audioDuration").takeIf { it.isNotBlank() },
            waveform = rawWaveform
          )
        )
      }
      list
    } catch (e: Exception) {
      Log.e(TAG, "Failed to load chat messages", e)
      emptyList()
    }
  }
}
