package com.example.phonecleaner.data.scanner

import android.content.ContentResolver
import android.content.ContentUris
import android.content.Context
import android.provider.MediaStore
import com.example.phonecleaner.data.model.CleanerFile

class AudioScanner(
    private val context: Context
) {

    private val contentResolver: ContentResolver =
        context.contentResolver

    fun scan(): List<CleanerFile> {

        val audioFiles = mutableListOf<CleanerFile>()

        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.DISPLAY_NAME,
            MediaStore.Audio.Media.SIZE,
            MediaStore.Audio.Media.MIME_TYPE,
            MediaStore.Audio.Media.DATE_MODIFIED
        )

        val sortOrder =
            "${MediaStore.Audio.Media.DATE_MODIFIED} DESC"

        contentResolver.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            projection,
            null,
            null,
            sortOrder
        )?.use { cursor ->

            val idColumn =
                cursor.getColumnIndexOrThrow(
                    MediaStore.Audio.Media._ID
                )

            val nameColumn =
                cursor.getColumnIndexOrThrow(
                    MediaStore.Audio.Media.DISPLAY_NAME
                )

            val sizeColumn =
                cursor.getColumnIndexOrThrow(
                    MediaStore.Audio.Media.SIZE
                )

            val mimeTypeColumn =
                cursor.getColumnIndexOrThrow(
                    MediaStore.Audio.Media.MIME_TYPE
                )

            val dateModifiedColumn =
                cursor.getColumnIndexOrThrow(
                    MediaStore.Audio.Media.DATE_MODIFIED
                )

            while (cursor.moveToNext()) {

                val id =
                    cursor.getLong(idColumn)

                val name =
                    cursor.getString(nameColumn) ?: ""

                val sizeBytes =
                    cursor.getLong(sizeColumn)

                val mimeType =
                    cursor.getString(mimeTypeColumn)

                val dateModified =
                    cursor.getLong(dateModifiedColumn)

                val uri =
                    ContentUris.withAppendedId(
                        MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                        id
                    )

                audioFiles.add(
                    CleanerFile(
                        uri = uri.toString(),
                        name = name,
                        sizeBytes = sizeBytes,
                        mimeType = mimeType,
                        dateModified = dateModified,
                        width = 0,
                        height = 0
                    )
                )
            }
        }

        return audioFiles
    }
}