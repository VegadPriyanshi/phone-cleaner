package com.example.phonecleaner.data.scanner

import android.content.ContentUris
import android.content.Context
import android.os.Build
import android.provider.MediaStore
import androidx.annotation.RequiresApi
import com.example.phonecleaner.data.model.CleanerFile

object OldApkScanner {

    @RequiresApi(Build.VERSION_CODES.R)
    fun scanOldApkFiles(
        context: Context
    ): List<CleanerFile> {

        val apkFiles = mutableListOf<CleanerFile>()

        val projection = arrayOf(
            MediaStore.Files.FileColumns._ID,
            MediaStore.Files.FileColumns.DISPLAY_NAME,
            MediaStore.Files.FileColumns.SIZE,
            MediaStore.Files.FileColumns.MIME_TYPE,
            MediaStore.Files.FileColumns.DATE_MODIFIED
        )

        val collection =
            MediaStore.Files.getContentUri("external")

        try {

            context.contentResolver.query(
                collection,
                projection,
                null,
                null,
                "${MediaStore.Files.FileColumns.SIZE} DESC"
            )?.use { cursor ->

                val idColumn =
                    cursor.getColumnIndexOrThrow(
                        MediaStore.Files.FileColumns._ID
                    )

                val nameColumn =
                    cursor.getColumnIndexOrThrow(
                        MediaStore.Files.FileColumns.DISPLAY_NAME
                    )

                val sizeColumn =
                    cursor.getColumnIndexOrThrow(
                        MediaStore.Files.FileColumns.SIZE
                    )

                val mimeColumn =
                    cursor.getColumnIndexOrThrow(
                        MediaStore.Files.FileColumns.MIME_TYPE
                    )

                val dateColumn =
                    cursor.getColumnIndexOrThrow(
                        MediaStore.Files.FileColumns.DATE_MODIFIED
                    )

                while (cursor.moveToNext()) {

                    val id =
                        cursor.getLong(idColumn)

                    val name =
                        cursor.getString(nameColumn)
                            ?: continue

                    val size =
                        cursor.getLong(sizeColumn)

                    val mimeType =
                        cursor.getString(mimeColumn)

                    val dateModified =
                        cursor.getLong(dateColumn)

                    /*
                     * APK files are normally identified by:
                     *
                     * 1. MIME type:
                     *    application/vnd.android.package-archive
                     *
                     * 2. File extension:
                     *    .apk
                     *
                     * We use both checks because some devices/file
                     * providers may not return the MIME type correctly.
                     */
                    val isApkByMimeType =
                        mimeType.equals(
                            "application/vnd.android.package-archive",
                            ignoreCase = true
                        )

                    val isApkByExtension =
                        name.endsWith(
                            ".apk",
                            ignoreCase = true
                        )

                    if (!isApkByMimeType && !isApkByExtension) {
                        continue
                    }

                    val uri =
                        ContentUris.withAppendedId(
                            MediaStore.Files.getContentUri("external"),
                            id
                        )

                    apkFiles.add(
                        CleanerFile(
                            uri = uri.toString(),
                            name = name,
                            sizeBytes = size,
                            mimeType = mimeType
                                ?: "application/vnd.android.package-archive",
                            dateModified = dateModified,
                            width = 0,
                            height = 0
                        )
                    )
                }
            }

        } catch (_: SecurityException) {

            /*
             * Return whatever APK files were safely discovered.
             */
            return apkFiles
        }

        return apkFiles
    }
}