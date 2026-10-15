package com.example.phonecleaner.data.scanner

import android.content.ContentUris
import android.content.Context
import android.provider.MediaStore
import com.example.phonecleaner.data.model.CleanerFile

object DocumentScanner {

    private val supportedExtensions = setOf(
        "pdf",
        "doc",
        "docx",
        "xls",
        "xlsx",
        "ppt",
        "pptx",
        "txt",
        "csv",
        "rtf",
        "odt",
        "ods",
        "odp",
        "epub",
        "json",
        "xml",
        "html",
        "htm"
    )

    private val supportedMimeTypes = setOf(
        "application/pdf",
        "application/msword",
        "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
        "application/vnd.ms-excel",
        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
        "application/vnd.ms-powerpoint",
        "application/vnd.openxmlformats-officedocument.presentationml.presentation",
        "text/plain",
        "text/csv",
        "text/rtf",
        "application/rtf",
        "application/vnd.oasis.opendocument.text",
        "application/vnd.oasis.opendocument.spreadsheet",
        "application/vnd.oasis.opendocument.presentation",
        "application/epub+zip",
        "application/json",
        "application/xml",
        "text/xml",
        "text/html"
    )

    fun scanDocuments(
        context: Context
    ): List<CleanerFile> {

        val resolver =
            context.contentResolver

        val collection =
            MediaStore.Files.getContentUri("external")

        val projection =
            arrayOf(
                MediaStore.Files.FileColumns._ID,
                MediaStore.Files.FileColumns.DISPLAY_NAME,
                MediaStore.Files.FileColumns.SIZE,
                MediaStore.Files.FileColumns.MIME_TYPE,
                MediaStore.Files.FileColumns.DATE_MODIFIED
            )

        val results =
            mutableListOf<CleanerFile>()

        try {

            resolver.query(
                collection,
                projection,
                "${MediaStore.Files.FileColumns.SIZE} > 0",
                null,
                "${MediaStore.Files.FileColumns.DATE_MODIFIED} DESC"
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

                val modifiedColumn =
                    cursor.getColumnIndexOrThrow(
                        MediaStore.Files.FileColumns.DATE_MODIFIED
                    )

                while (cursor.moveToNext()) {

                    val name =
                        cursor.getString(nameColumn)
                            ?: continue

                    val mimeType =
                        cursor.getString(mimeColumn)
                            ?.lowercase()

                    val extension =
                        name.substringAfterLast(
                            '.',
                            ""
                        ).lowercase()

                    val isSupported =
                        extension in supportedExtensions ||
                                mimeType in supportedMimeTypes

                    if (!isSupported) {
                        continue
                    }

                    val id =
                        cursor.getLong(idColumn)

                    val size =
                        cursor.getLong(sizeColumn)

                    val modified =
                        cursor.getLong(modifiedColumn)

                    val uri =
                        ContentUris.withAppendedId(
                            collection,
                            id
                        )

                    results.add(
                        CleanerFile(
                            uri = uri.toString(),
                            name = name,
                            sizeBytes = size,
                            mimeType = mimeType,
                            dateModified = modified,
                            width = 0,
                            height = 0
                        )
                    )
                }
            }

        } catch (_: SecurityException) {

            // Permission unavailable.

        } catch (_: Exception) {

            // Keep scan safe if MediaStore has an issue.
        }

        return results.distinctBy {
            it.uri
        }
    }
}