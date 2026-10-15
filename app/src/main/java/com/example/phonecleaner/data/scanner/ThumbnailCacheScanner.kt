package com.example.phonecleaner.data.scanner

import android.content.Context
import android.net.Uri
import android.os.Environment
import com.example.phonecleaner.data.model.CleanerFile
import com.example.phonecleaner.utils.StoragePermissionHelper
import java.io.File

/*
 * Thumbnail cache files (e.g. DCIM/.thumbnails, Pictures/.thumbnails)
 * are regenerated automatically by the gallery/camera apps, so they
 * are always safe to delete.
 *
 * They live in hidden dot-folders, which MediaStore does NOT index -
 * so unlike JunkFileScanner/OldApkScanner (which query MediaStore),
 * this scanner walks the filesystem directly. That requires "All
 * files access" (MANAGE_EXTERNAL_STORAGE), which is why every entry
 * point here checks StoragePermissionHelper.hasAllFilesAccess()
 * first and returns an empty list rather than crashing if it isn't
 * granted yet.
 */
object ThumbnailCacheScanner {

    /*
     * Root folders most likely to contain a .thumbnails cache.
     * Kept to a short, known list (rather than crawling all of
     * external storage) so the scan stays fast.
     */
    private val ROOT_FOLDERS_TO_CHECK =
        listOf(
            "DCIM",
            "Pictures",
            "Movies",
            "Download"
        )

    private const val MAX_SEARCH_DEPTH = 3

    fun scanThumbnailCaches(
        context: Context
    ): List<CleanerFile> {

        if (!StoragePermissionHelper.hasAllFilesAccess()) {
            return emptyList()
        }

        val externalRoot =
            Environment.getExternalStorageDirectory()

        val thumbnailFolders =
            mutableListOf<File>()

        ROOT_FOLDERS_TO_CHECK.forEach { folderName ->

            val rootFolder =
                File(externalRoot, folderName)

            findThumbnailFolders(
                folder = rootFolder,
                depth = 0,
                results = thumbnailFolders
            )
        }

        val thumbnailFiles =
            mutableListOf<CleanerFile>()

        thumbnailFolders.forEach { folder ->

            collectFiles(
                folder = folder,
                results = thumbnailFiles
            )
        }

        return thumbnailFiles
    }

    /*
     * Recursively looks (up to MAX_SEARCH_DEPTH) for any folder
     * literally named ".thumbnails" under the given folder.
     */
    private fun findThumbnailFolders(
        folder: File,
        depth: Int,
        results: MutableList<File>
    ) {

        if (depth > MAX_SEARCH_DEPTH) {
            return
        }

        if (!folder.exists() || !folder.isDirectory) {
            return
        }

        try {

            val children =
                folder.listFiles() ?: return

            children.forEach { child ->

                if (!child.isDirectory) {
                    return@forEach
                }

                if (child.name.equals(
                        ".thumbnails",
                        ignoreCase = true
                    )
                ) {
                    results.add(child)
                } else {

                    findThumbnailFolders(
                        folder = child,
                        depth = depth + 1,
                        results = results
                    )
                }
            }

        } catch (_: SecurityException) {
            // Not accessible - skip this branch silently.
        }
    }

    private fun collectFiles(
        folder: File,
        results: MutableList<CleanerFile>
    ) {

        try {

            val children =
                folder.listFiles() ?: return

            children.forEach { child ->

                if (child.isDirectory) {

                    collectFiles(
                        folder = child,
                        results = results
                    )

                } else if (child.isFile) {

                    results.add(
                        CleanerFile(
                            uri = Uri.fromFile(child).toString(),
                            name = child.name,
                            sizeBytes = child.length(),
                            mimeType = null,
                            dateModified = child.lastModified() / 1000,
                            width = 0,
                            height = 0
                        )
                    )
                }
            }

        } catch (_: SecurityException) {
            // Not accessible - skip this branch silently.
        }
    }
}