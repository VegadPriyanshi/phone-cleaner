package com.example.phonecleaner.data.scanner

import android.content.ContentResolver
import android.content.ContentUris
import android.content.Context
import android.provider.MediaStore
import com.example.phonecleaner.data.model.CleanerFile

class ScreenshotScanner(
    private val context: Context
) {

    private val contentResolver: ContentResolver =
        context.contentResolver

    fun scan(): List<CleanerFile> {

        val screenshots = mutableListOf<CleanerFile>()

        val projection = arrayOf(
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DISPLAY_NAME,
            MediaStore.Images.Media.SIZE,
            MediaStore.Images.Media.MIME_TYPE,
            MediaStore.Images.Media.DATE_MODIFIED,
            MediaStore.Images.Media.WIDTH,
            MediaStore.Images.Media.HEIGHT,
            MediaStore.Images.Media.RELATIVE_PATH
        )

        val sortOrder =
            "${MediaStore.Images.Media.DATE_MODIFIED} DESC"

        contentResolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            projection,
            null,
            null,
            sortOrder
        )?.use { cursor ->

            val idColumn =
                cursor.getColumnIndexOrThrow(
                    MediaStore.Images.Media._ID
                )

            val nameColumn =
                cursor.getColumnIndexOrThrow(
                    MediaStore.Images.Media.DISPLAY_NAME
                )

            val sizeColumn =
                cursor.getColumnIndexOrThrow(
                    MediaStore.Images.Media.SIZE
                )

            val mimeTypeColumn =
                cursor.getColumnIndexOrThrow(
                    MediaStore.Images.Media.MIME_TYPE
                )

            val dateModifiedColumn =
                cursor.getColumnIndexOrThrow(
                    MediaStore.Images.Media.DATE_MODIFIED
                )

            val widthColumn =
                cursor.getColumnIndexOrThrow(
                    MediaStore.Images.Media.WIDTH
                )

            val heightColumn =
                cursor.getColumnIndexOrThrow(
                    MediaStore.Images.Media.HEIGHT
                )

            val relativePathColumn =
                cursor.getColumnIndexOrThrow(
                    MediaStore.Images.Media.RELATIVE_PATH
                )

            while (cursor.moveToNext()) {

                val id =
                    cursor.getLong(idColumn)

                val name =
                    cursor.getString(nameColumn)
                        ?: ""

                val sizeBytes =
                    cursor.getLong(sizeColumn)

                val mimeType =
                    cursor.getString(mimeTypeColumn)

                val dateModified =
                    cursor.getLong(dateModifiedColumn)

                val width =
                    cursor.getInt(widthColumn)

                val height =
                    cursor.getInt(heightColumn)

                val relativePath =
                    cursor.getString(relativePathColumn)
                        ?: ""

                if (!isScreenshot(
                        name = name,
                        relativePath = relativePath
                    )
                ) {
                    continue
                }

                val uri =
                    ContentUris.withAppendedId(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        id
                    )

                screenshots.add(
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

        return screenshots
    }

    private fun isScreenshot(
        name: String,
        relativePath: String
    ): Boolean {

        val normalizedName =
            name.lowercase()

        val normalizedPath =
            relativePath.lowercase()

        val screenshotPath =
            normalizedPath.contains("screenshot")

        val screenshotName =
            normalizedName.startsWith("screenshot") ||
                    normalizedName.contains("screen_shot") ||
                    normalizedName.contains("screen-shot") ||
                    normalizedName.contains("screencap") ||
                    normalizedName.contains("screen_cap")

        return screenshotPath || screenshotName
    }
}