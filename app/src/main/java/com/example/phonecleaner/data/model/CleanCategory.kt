package com.example.phonecleaner.data.model

data class CleanCategory(
    val id: String,
    val title: String,
    val itemCount: Int,
    val sizeBytes: Long,
    val iconResId: Int,
    val isSelected: Boolean = false
)