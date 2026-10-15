package com.example.phonecleaner.data.scanner

import android.content.Context
import android.net.Uri
import android.os.Environment
import com.example.phonecleaner.data.model.CleanerFile
import com.example.phonecleaner.utils.StoragePermissionHelper
import java.io.File

/*
 * When an app is uninstalled, Android does not always clean up its
 * folder under Android/data/<package> or Android/media/<package> -
 * this is leftover data from an app that no longer exists on the
 * device.
 *
 * These folders are not indexed by MediaStore at all, so - like
 * ThumbnailCacheScanner - this walks the filesystem directly and
 * requires "All files access" (MANAGE_EXTERNAL_STORAGE).
 *
 * Each leftover app's folder is represented as ONE CleanerFile entry
 * (not one entry per file inside it), since these folders can contain
 * thousands of small cache files and the only meaningful action here
 * is "delete this whole leftover folder", not picking individual
 * files inside someone else's old app data.
 */
object LeftoverAppDataScanner {

    fun scanLeftoverAppData(
        context: Context
    ): List<CleanerFile> {

        if (!StoragePermissionHelper.hasAllFilesAccess()) {
            return emptyList()
        }

        val installedPackageNames =
            try {

                context.packageManager
                    .getInstalledApplications(0)
                    .map { it.packageName }
                    .toSet()

            } catch (_: Exception) {

                return emptyList()
            }

        val externalRoot =
            Environment.getExternalStorageDirectory()

        val leftoverFolders =
            mutableListOf<File>()

        listOf("Android/data", "Android/media").forEach { relativePath ->

            val parent =
                File(externalRoot, relativePath)

            try {

                val packageFolders =
                    parent.listFiles() ?: return@forEach

                packageFolders.forEach { packageFolder ->

                    if (!packageFolder.isDirectory) {
                        return@forEach
                    }

                    val looksLikePackageName =
                        packageFolder.name.contains(".")

                    val isLeftover =
                        looksLikePackageName &&
                                !installedPackageNames.contains(
                                    packageFolder.name
                                )

                    if (isLeftover) {
                        leftoverFolders.add(packageFolder)
                    }
                }

            } catch (_: SecurityException) {
                // Not accessible - skip this branch silently.
            }
        }

        return leftoverFolders.mapNotNull { folder ->

            val totalSize =
                folderSizeRecursive(folder)

            if (totalSize <= 0L) {
                return@mapNotNull null
            }

            CleanerFile(
                uri = Uri.fromFile(folder).toString(),
                name = folder.name,
                sizeBytes = totalSize,
                mimeType = "application/vnd.folder",
                dateModified = folder.lastModified() / 1000,
                width = 0,
                height = 0
            )
        }
    }

    private fun folderSizeRecursive(folder: File): Long {

        return try {

            var total = 0L

            val children =
                folder.listFiles() ?: return 0L

            children.forEach { child ->

                total += if (child.isDirectory) {
                    folderSizeRecursive(child)
                } else {
                    child.length()
                }
            }

            total

        } catch (_: SecurityException) {
            0L
        }
    }
}