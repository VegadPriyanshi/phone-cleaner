package com.example.phonecleaner.data.scanner

import android.content.ContentUris
import android.content.Context
import android.os.Build
import android.provider.MediaStore
import androidx.annotation.RequiresApi
import com.example.phonecleaner.data.model.CleanerFile

object JunkFileScanner {

    /*
     * Files matching these extensions are considered
     * temporary-file candidates.
     */
    private val TEMPORARY_EXTENSIONS =
        setOf(
            ".tmp",
            ".temp"
        )

    /*
     * File names containing these words are also treated
     * as temporary-file candidates.
     */
    private val TEMPORARY_NAME_KEYWORDS =
        setOf(
            ".tmp.",
            ".temp.",
            "_tmp",
            "_temp",
            "temp_",
            "tmp_"
        )

    @RequiresApi(Build.VERSION_CODES.R)
    fun scanJunkFiles(
        context: Context
    ): List<CleanerFile> {

        val junkFiles =
            mutableListOf<CleanerFile>()

        val projection =
            arrayOf(
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

        /*
         * We query MediaStore files and then apply our
         * conservative junk-file rules in Kotlin.
         */
        try {

            context.contentResolver.query(
                collection,
                projection,
                null,
                null,
                "${MediaStore.Files.FileColumns.SIZE} ASC"
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

                    val lowerCaseName =
                        name.lowercase()

                    val isTemporaryExtension =
                        TEMPORARY_EXTENSIONS.any { extension ->

                            lowerCaseName.endsWith(
                                extension
                            )
                        }

                    val hasTemporaryKeyword =
                        TEMPORARY_NAME_KEYWORDS.any { keyword ->

                            lowerCaseName.contains(
                                keyword
                            )
                        }
                    val isMediaFile =
                        mimeType?.startsWith("image/") == true ||
                                mimeType?.startsWith("video/") == true ||
                                mimeType?.startsWith("audio/") == true

                    val isEmptyFile =
                        size == 0L && !isMediaFile

                    /*
                     * A file becomes a junk candidate if it is:
                     *
                     * 1. A known temporary extension
                     * 2. Has a known temporary naming pattern
                     * 3. Is an empty file
                     */
                    val isJunk =
                        isTemporaryExtension ||
                                hasTemporaryKeyword ||
                                isEmptyFile

                    if (!isJunk) {
                        continue
                    }

                    val uri =
                        when {

                            mimeType
                                ?.startsWith("video/") == true -> {

                                ContentUris.withAppendedId(
                                    MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                                    id
                                )
                            }

                            mimeType
                                ?.startsWith("image/") == true -> {

                                ContentUris.withAppendedId(
                                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                                    id
                                )
                            }

                            mimeType
                                ?.startsWith("audio/") == true -> {

                                ContentUris.withAppendedId(
                                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                                    id
                                )
                            }

                            else -> {

                                MediaStore.Files.getContentUri(
                                    "external",
                                    id
                                )
                            }
                        }

                    junkFiles.add(
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

        } catch (_: SecurityException) {

            /*
             * If Android restricts access to some MediaStore
             * files, return whatever was discovered safely.
             */
            return junkFiles
        }

        return junkFiles
    }
}