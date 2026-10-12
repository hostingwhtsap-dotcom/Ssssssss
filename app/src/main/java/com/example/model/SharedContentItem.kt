package com.example.model

import java.io.File
import java.util.UUID

data class SharedContentItem(
  val id: String = UUID.randomUUID().toString(),
  val file: File,
  val type: MessageAttachmentType,
  val fileName: String,
  val sizeOrDuration: String,
  val isVideo: Boolean = false,
  val thumbnailFile: File? = null
)
