package com.example.phonecleaner.data.model

data class InstalledApp(
    val packageName: String,
    val appName: String,
    val sizeBytes: Long,
    val lastUsedTime: Long,
    val isSystemApp: Boolean,
    val isUpdatedSystemApp: Boolean
)