package com.example.phonecleaner.data.scanner

import android.content.ContentResolver
import android.content.ContentUris
import android.content.Context
import android.provider.MediaStore
import com.example.phonecleaner.data.model.CleanerFile

class VideoScanner(
    private val context: Context
) {

    private val contentResolver: ContentResolver =
        context.contentResolver

    fun scan(): List<CleanerFile> {

        val videos =
            mutableListOf<CleanerFile>()

        val projection =
            arrayOf(
                MediaStore.Video.Media._ID,
                MediaStore.Video.Media.DISPLAY_NAME,
                MediaStore.Video.Media.SIZE,
                MediaStore.Video.Media.MIME_TYPE,
                MediaStore.Video.Media.DATE_MODIFIED,
                MediaStore.Video.Media.WIDTH,
                MediaStore.Video.Media.HEIGHT
            )

        val sortOrder =
            "${MediaStore.Video.Media.DATE_MODIFIED} DESC"

        contentResolver.query(
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
            projection,
            null,
            null,
            sortOrder
        )?.use { cursor ->

            val idColumn =
                cursor.getColumnIndexOrThrow(
                    MediaStore.Video.Media._ID
                )

            val nameColumn =
                cursor.getColumnIndexOrThrow(
                    MediaStore.Video.Media.DISPLAY_NAME
                )

            val sizeColumn =
                cursor.getColumnIndexOrThrow(
                    MediaStore.Video.Media.SIZE
                )

            val mimeTypeColumn =
                cursor.getColumnIndexOrThrow(
                    MediaStore.Video.Media.MIME_TYPE
                )

            val dateModifiedColumn =
                cursor.getColumnIndexOrThrow(
                    MediaStore.Video.Media.DATE_MODIFIED
                )

            val widthColumn =
                cursor.getColumnIndexOrThrow(
                    MediaStore.Video.Media.WIDTH
                )

            val heightColumn =
                cursor.getColumnIndexOrThrow(
                    MediaStore.Video.Media.HEIGHT
                )

            while (cursor.moveToNext()) {

                val id =
                    cursor.getLong(
                        idColumn
                    )

                val name =
                    cursor.getString(
                        nameColumn
                    ) ?: ""

                val sizeBytes =
                    cursor.getLong(
                        sizeColumn
                    )

                val mimeType =
                    cursor.getString(
                        mimeTypeColumn
                    )

                val dateModified =
                    cursor.getLong(
                        dateModifiedColumn
                    )

                val width =
                    cursor.getInt(
                        widthColumn
                    )

                val height =
                    cursor.getInt(
                        heightColumn
                    )

                val uri =
                    ContentUris.withAppendedId(
                        MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                        id
                    )

                videos.add(
                    CleanerFile(
                        uri = uri.toString(),
                        name = name,
                        sizeBytes = sizeBytes,
                        mimeType = mimeType,
                        dateModified = dateModified,
                        width = width,
                        height = height
                    )
                )
            }
        }

        return videos
    }
}