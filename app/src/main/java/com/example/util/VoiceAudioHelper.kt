package com.example.util

import android.content.Context
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import java.io.File

private const val TAG = "VoiceAudioHelper"

class VoiceAudioHelper(private val context: Context) {

  private var mediaRecorder: MediaRecorder? = null
  private var currentRecordingFile: File? = null
  private var mediaPlayer: MediaPlayer? = null
  private var currentlyPlayingPath: String? = null

  private val progressHandler = Handler(Looper.getMainLooper())
  private var onProgressCallback: ((isPlaying: Boolean, positionMs: Int, durationMs: Int, progress: Float) -> Unit)? = null

  private val progressRunnable = object : Runnable {
    override fun run() {
      val player = mediaPlayer
      if (player != null && player.isPlaying) {
        val pos = player.currentPosition
        val dur = player.duration.coerceAtLeast(1)
        val progress = (pos.toFloat() / dur.toFloat()).coerceIn(0f, 1f)
        onProgressCallback?.invoke(true, pos, dur, progress)
        progressHandler.postDelayed(this, 50)
      }
    }
  }

  fun startRecording(): File? {
    try {
      stopRecordingInternal()
      stopPlayback()

      val file = MediaStorageHelper.createVoiceRecordingFile(context)
      currentRecordingFile = file

      val recorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        MediaRecorder(context)
      } else {
        @Suppress("DEPRECATION")
        MediaRecorder()
      }

      recorder.apply {
        setAudioSource(MediaRecorder.AudioSource.MIC)
        setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
        setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
        setAudioEncodingBitRate(128000)
        setAudioSamplingRate(44100)
        setOutputFile(file.absolutePath)
        prepare()
        start()
      }
      mediaRecorder = recorder
      Log.d(TAG, "Recording started into: ${file.absolutePath}")
      return file
    } catch (e: Exception) {
      Log.e(TAG, "Failed to start audio recording", e)
      cancelRecording()
      return null
    }
  }

  fun getMaxAmplitude(): Int {
    return try {
      mediaRecorder?.maxAmplitude ?: 0
    } catch (e: Exception) {
      0
    }
  }

  fun getNormalizedAmplitude(): Float {
    val amp = getMaxAmplitude()
    return (amp.toFloat() / 25000f).coerceIn(0.08f, 1.0f)
  }

  fun pauseRecording() {
    try {
      mediaRecorder?.pause()
    } catch (e: Exception) {
      Log.e(TAG, "Error pausing recording", e)
    }
  }

  fun resumeRecording() {
    try {
      mediaRecorder?.resume()
    } catch (e: Exception) {
      Log.e(TAG, "Error resuming recording", e)
    }
  }

  fun stopRecording(): File? {
    val file = currentRecordingFile
    try {
      mediaRecorder?.apply {
        stop()
        release()
      }
    } catch (e: Exception) {
      Log.e(TAG, "Error stopping media recorder", e)
    } finally {
      mediaRecorder = null
      currentRecordingFile = null
    }
    return if (file != null && file.exists() && file.length() > 0) file else null
  }

  fun cancelRecording() {
    try {
      mediaRecorder?.apply {
        stop()
        release()
      }
    } catch (_: Exception) {
    } finally {
      mediaRecorder = null
      currentRecordingFile?.delete()
      currentRecordingFile = null
    }
  }

  private fun stopRecordingInternal() {
    try {
      mediaRecorder?.release()
    } catch (_: Exception) {}
    mediaRecorder = null
  }

  // Playback functions
  fun togglePlay(
    filePath: String,
    onProgressUpdate: (isPlaying: Boolean, positionMs: Int, durationMs: Int, progress: Float) -> Unit
  ) {
    if (currentlyPlayingPath == filePath && mediaPlayer?.isPlaying == true) {
      pausePlayback()
      val pos = mediaPlayer?.currentPosition ?: 0
      val dur = (mediaPlayer?.duration ?: 1).coerceAtLeast(1)
      onProgressUpdate(false, pos, dur, (pos.toFloat() / dur.toFloat()).coerceIn(0f, 1f))
      return
    }

    stopPlayback()

    try {
      onProgressCallback = onProgressUpdate
      val player = MediaPlayer().apply {
        setDataSource(filePath)
        prepare()
        start()
        setOnCompletionListener {
          val dur = duration.coerceAtLeast(1)
          stopProgressTracking()
          currentlyPlayingPath = null
          onProgressCallback?.invoke(false, 0, dur, 0f)
        }
      }
      mediaPlayer = player
      currentlyPlayingPath = filePath
      startProgressTracking()
      val dur = player.duration.coerceAtLeast(1)
      onProgressUpdate(true, 0, dur, 0f)
    } catch (e: Exception) {
      Log.e(TAG, "Error starting audio playback", e)
      stopProgressTracking()
      onProgressUpdate(false, 0, 0, 0f)
    }
  }

  fun togglePlay(
    filePath: String,
    onPlaybackStateChanged: (isPlaying: Boolean) -> Unit
  ) {
    togglePlay(filePath) { isPlaying, _, _, _ ->
      onPlaybackStateChanged(isPlaying)
    }
  }

  fun seekTo(filePath: String, progress: Float) {
    if (currentlyPlayingPath == filePath && mediaPlayer != null) {
      try {
        val dur = mediaPlayer?.duration ?: return
        val targetMs = (dur * progress.coerceIn(0f, 1f)).toInt()
        mediaPlayer?.seekTo(targetMs)
        onProgressCallback?.invoke(mediaPlayer?.isPlaying == true, targetMs, dur, progress)
      } catch (e: Exception) {
        Log.e(TAG, "Error seeking audio", e)
      }
    }
  }

  fun pausePlayback() {
    try {
      if (mediaPlayer?.isPlaying == true) {
        mediaPlayer?.pause()
      }
    } catch (_: Exception) {}
    stopProgressTracking()
  }

  fun stopPlayback() {
    stopProgressTracking()
    try {
      mediaPlayer?.apply {
        if (isPlaying) stop()
        release()
      }
    } catch (_: Exception) {}
    mediaPlayer = null
    currentlyPlayingPath = null
  }

  private fun startProgressTracking() {
    progressHandler.removeCallbacks(progressRunnable)
    progressHandler.post(progressRunnable)
  }

  private fun stopProgressTracking() {
    progressHandler.removeCallbacks(progressRunnable)
  }

  fun isPlaying(filePath: String): Boolean {
    return currentlyPlayingPath == filePath && (mediaPlayer?.isPlaying == true)
  }

  fun releaseAll() {
    cancelRecording()
    stopPlayback()
    stopProgressTracking()
  }
}
