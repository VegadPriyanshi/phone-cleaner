package com.example.phonecleaner.data.model

data class JunkCategory(
    val id: String,
    val title: String,
    val itemCount: Int,
    val sizeBytes: Long,
    val files: List<CleanerFile>
)