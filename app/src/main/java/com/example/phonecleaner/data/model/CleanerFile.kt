package com.example.phonecleaner.data.model

data class CleanerFile(
    val uri: String,
    val name: String,
    val sizeBytes: Long,
    val mimeType: String?,
    val dateModified: Long,
    val width: Int,
    val height: Int
)