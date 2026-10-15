package com.example.phonecleaner.data.scanner

import com.example.phonecleaner.data.model.CleanerFile
import com.example.phonecleaner.data.model.JunkCategory

object JunkCategoryScanner {

    fun createCategories(
        junkFiles: List<CleanerFile>,
        oldApkFiles: List<CleanerFile>,
        thumbnailCacheFiles: List<CleanerFile> = emptyList(),
        leftoverAppDataFiles: List<CleanerFile> = emptyList()
    ): List<JunkCategory> {

        val categories = mutableListOf<JunkCategory>()

        // Temporary files
        val temporaryFiles = junkFiles.filter { file ->
            val name = file.name.lowercase()

            name.endsWith(".tmp") ||
                    name.endsWith(".temp") ||
                    name.contains(".tmp.") ||
                    name.contains(".temp.") ||
                    name.contains("_tmp") ||
                    name.contains("_temp") ||
                    name.startsWith("tmp_") ||
                    name.startsWith("temp_")
        }

        if (temporaryFiles.isNotEmpty()) {
            categories.add(
                JunkCategory(
                    id = "temporary_files",
                    title = "Temporary Files",
                    itemCount = temporaryFiles.size,
                    sizeBytes = temporaryFiles.sumOf { it.sizeBytes },
                    files = temporaryFiles
                )
            )
        }

        // Old APK files
        if (oldApkFiles.isNotEmpty()) {
            categories.add(
                JunkCategory(
                    id = "old_apk_files",
                    title = "Old APK Files",
                    itemCount = oldApkFiles.size,
                    sizeBytes = oldApkFiles.sumOf { it.sizeBytes },
                    files = oldApkFiles
                )
            )
        }

        // Empty files
        val emptyFiles = junkFiles.filter { file ->
            file.sizeBytes == 0L &&
                    !temporaryFiles.contains(file)
        }

        if (emptyFiles.isNotEmpty()) {
            categories.add(
                JunkCategory(
                    id = "empty_files",
                    title = "Empty Files",
                    itemCount = emptyFiles.size,
                    sizeBytes = emptyFiles.sumOf { it.sizeBytes },
                    files = emptyFiles
                )
            )
        }

        // Thumbnail caches
        if (thumbnailCacheFiles.isNotEmpty()) {
            categories.add(
                JunkCategory(
                    id = "thumbnail_caches",
                    title = "Thumbnail Caches",
                    itemCount = thumbnailCacheFiles.size,
                    sizeBytes = thumbnailCacheFiles.sumOf { it.sizeBytes },
                    files = thumbnailCacheFiles
                )
            )
        }

        // Leftover app data (uninstalled apps)
        if (leftoverAppDataFiles.isNotEmpty()) {
            categories.add(
                JunkCategory(
                    id = "leftover_app_data",
                    title = "Leftover App Data",
                    itemCount = leftoverAppDataFiles.size,
                    sizeBytes = leftoverAppDataFiles.sumOf { it.sizeBytes },
                    files = leftoverAppDataFiles
                )
            )
        }

        // Any remaining junk files
        val categorizedFiles = temporaryFiles + emptyFiles

        val otherFiles = junkFiles.filter { file ->
            !categorizedFiles.contains(file)
        }

        if (otherFiles.isNotEmpty()) {
            categories.add(
                JunkCategory(
                    id = "other_junk",
                    title = "Other Junk",
                    itemCount = otherFiles.size,
                    sizeBytes = otherFiles.sumOf { it.sizeBytes },
                    files = otherFiles
                )
            )
        }

        return categories
    }
}