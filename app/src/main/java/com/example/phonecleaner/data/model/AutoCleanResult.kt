package com.example.phonecleaner.data.model

data class AutoCleanResult(

    // When Auto-clean finished
    val timestamp: Long,

    // Total number of files/folders successfully deleted
    val deletedCount: Int,

    // Number of files/folders that could not be deleted
    val failedCount: Int,

    // Space freed by Auto-clean
    val spaceFreedBytes: Long,

    // Deleted temporary files
    val temporaryFilesDeleted: Int,

    // Deleted empty files
    val emptyFilesDeleted: Int,

    // Deleted old APK files
    val oldApkFilesDeleted: Int,

    // Deleted thumbnail cache files
    val thumbnailCacheFilesDeleted: Int,

    // Deleted leftover app-data folders
    val leftoverAppDataDeleted: Int
) {

    // Total number of successfully cleaned categories/items
    val totalDeletedCount: Int
        get() = deletedCount
}