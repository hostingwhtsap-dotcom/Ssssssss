package com.example.model

enum class MessageStatus {
  SENT,
  DELIVERED,
  READ
}

enum class MessageAttachmentType {
  NONE,
  IMAGE,
  VIDEO,
  VOICE_NOTE,
  AUDIO,
  DOCUMENT
}

data class ChatMessage(
  val id: String,
  val text: String = "",
  val timestamp: String,
  val isOutgoing: Boolean,
  val status: MessageStatus = MessageStatus.READ,
  val attachmentType: MessageAttachmentType = MessageAttachmentType.NONE,
  val mediaPath: String? = null,
  val thumbnailPath: String? = null,
  val attachmentName: String? = null,
  val attachmentSize: String? = null,
  val audioDuration: String? = null,
  val waveform: List<Float> = emptyList()
)
