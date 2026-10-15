package com.example.phonecleaner.data.scanner

import android.content.Context
import android.os.Build
import android.provider.MediaStore
import androidx.annotation.RequiresApi
import com.example.phonecleaner.data.model.CleanerFile
import android.content.ContentUris

object LargeFileScanner {

    /*
     * Files at or above 100 MB are considered large.
     */
    private const val LARGE_FILE_THRESHOLD =
        100L * 1024L * 1024L

    @RequiresApi(Build.VERSION_CODES.R)
    fun scanLargeFiles(
        context: Context
    ): List<CleanerFile> {

        val largeFiles =
            mutableListOf<CleanerFile>()

        val projection = arrayOf(
            MediaStore.Files.FileColumns._ID,
            MediaStore.Files.FileColumns.DISPLAY_NAME,
            MediaStore.Files.FileColumns.SIZE,
            MediaStore.Files.FileColumns.MIME_TYPE,
            MediaStore.Files.FileColumns.DATE_MODIFIED
        )

        val collection =
            MediaStore.Files.getContentUri(
                "external"
            )

        val selection =
            "${MediaStore.Files.FileColumns.SIZE} >= ?"

        val selectionArgs =
            arrayOf(
                LARGE_FILE_THRESHOLD.toString()
            )

        val sortOrder =
            "${MediaStore.Files.FileColumns.SIZE} DESC"

        try {

            context.contentResolver.query(
                collection,
                projection,
                selection,
                selectionArgs,
                sortOrder
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
                            ?: "Unknown file"

                    val size =
                        cursor.getLong(sizeColumn)

                    val mimeType =
                        cursor.getString(mimeColumn)

                    val dateModified =
                        cursor.getLong(dateColumn)

                    val uri =
                        when {
                            mimeType?.startsWith("video/") == true -> {
                                ContentUris.withAppendedId(
                                    MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                                    id
                                )
                            }

                            mimeType?.startsWith("image/") == true -> {
                                ContentUris.withAppendedId(
                                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                                    id
                                )
                            }

                            mimeType?.startsWith("audio/") == true -> {
                                ContentUris.withAppendedId(
                                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                                    id
                                )
                            }

                            else -> {

                                android.util.Log.d(
                                    "LargeFileScanner",
                                    "NON_MEDIA_LARGE_FILE: $name | $size bytes | MIME: $mimeType"
                                )

                                MediaStore.Files.getContentUri(
                                    "external",
                                    id
                                )
                            }
                        }

                    largeFiles.add(
                        CleanerFile(
                            uri = uri.toString(),
                            name = name,
                            sizeBytes = size,
                            mimeType = mimeType,
                            dateModified = dateModified,
                            width = 0,
                            height = 0
                        )
                    )
                }
            }

        } catch (securityException: SecurityException) {

            /*
             * The caller may not have access to
             * all indexed files on this Android version.
             *
             * Return the files discovered so far
             * instead of crashing the application.
             */

            return largeFiles
        }

        return largeFiles
    }
}