package com.example.phonecleaner.data.scanner

import android.content.Context
import android.os.Build
import android.provider.MediaStore
import android.util.Log

import com.example.phonecleaner.data.model.CleanerFile

object EmptyFileScanner {

    private const val TAG = "EmptyFileScanner"

    fun scanEmptyFiles(
        context: Context
    ): List<CleanerFile> {

        val emptyFiles = mutableListOf<CleanerFile>()

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {

            Log.d(
                TAG,
                "Empty file scanning requires Android 11+"
            )

            return emptyFiles
        }

        val collection =
            MediaStore.Files.getContentUri(
                MediaStore.VOLUME_EXTERNAL
            )

        val projection = arrayOf(
            MediaStore.Files.FileColumns._ID,
            MediaStore.Files.FileColumns.DISPLAY_NAME,
            MediaStore.Files.FileColumns.SIZE,
            MediaStore.Files.FileColumns.MEDIA_TYPE,
            MediaStore.Files.FileColumns.MIME_TYPE,
            MediaStore.Files.FileColumns.DATE_MODIFIED
        )

        val selection =
            "${MediaStore.Files.FileColumns.SIZE} = ?"

        val selectionArgs =
            arrayOf("0")

        try {

            context.contentResolver.query(
                collection,
                projection,
                selection,
                selectionArgs,
                "${MediaStore.Files.FileColumns.DISPLAY_NAME} ASC"
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

                val mediaTypeColumn =
                    cursor.getColumnIndexOrThrow(
                        MediaStore.Files.FileColumns.MEDIA_TYPE
                    )

                val mimeTypeColumn =
                    cursor.getColumnIndexOrThrow(
                        MediaStore.Files.FileColumns.MIME_TYPE
                    )

                val dateModifiedColumn =
                    cursor.getColumnIndexOrThrow(
                        MediaStore.Files.FileColumns.DATE_MODIFIED
                    )

                while (cursor.moveToNext()) {

                    val id =
                        cursor.getLong(idColumn)

                    val name =
                        cursor.getString(nameColumn)
                            ?: "Unknown"

                    val size =
                        cursor.getLong(sizeColumn)

                    val mediaType =
                        cursor.getInt(mediaTypeColumn)

                    val mimeType =
                        cursor.getString(mimeTypeColumn)

                    val dateModified =
                        cursor.getLong(dateModifiedColumn)

                    /*
                     * Auto-clean should not consider
                     * zero-byte photos, videos or audio.
                     *
                     * We only consider non-media files.
                     */
                    if (
                        size == 0L &&
                        mediaType ==
                        MediaStore.Files.FileColumns.MEDIA_TYPE_NONE
                    ) {

                        val uri =
                            MediaStore.Files.getContentUri(
                                MediaStore.VOLUME_EXTERNAL,
                                id
                            )

                        emptyFiles.add(
                            CleanerFile(
                                name = name,
                                uri = uri.toString(),
                                sizeBytes = size,
                                mimeType =
                                    mimeType
                                        ?: "application/octet-stream",

                                dateModified = dateModified,

                                /*
                                 * Empty files are not image/video
                                 * media, so dimensions are 0.
                                 */
                                width = 0,
                                height = 0
                            )
                        )
                    }
                }
            }

        } catch (exception: Exception) {

            Log.e(
                TAG,
                "Failed to scan empty files",
                exception
            )
        }

        Log.d(
            TAG,
            "Empty files found: ${emptyFiles.size}"
        )

        emptyFiles.forEach { file ->

            Log.d(
                TAG,
                "EMPTY FILE -> " +
                        "name=${file.name}, " +
                        "uri=${file.uri}, " +
                        "size=${file.sizeBytes}"
            )
        }

        return emptyFiles
    }
}