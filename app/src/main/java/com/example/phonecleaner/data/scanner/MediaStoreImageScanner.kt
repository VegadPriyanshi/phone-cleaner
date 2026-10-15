package com.example.phonecleaner.data.scanner

import android.content.ContentResolver
import android.provider.MediaStore
import com.example.phonecleaner.data.model.CleanerFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MediaStoreImageScanner(
    private val contentResolver: ContentResolver
) {

    suspend fun scan(
        onProgress: (scanned: Int, total: Int, sizeBytes: Long) -> Unit
    ): List<CleanerFile> = withContext(Dispatchers.IO) {

        val images = mutableListOf<CleanerFile>()

        val collection =
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI

        val projection = arrayOf(
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DISPLAY_NAME,
            MediaStore.Images.Media.SIZE,
            MediaStore.Images.Media.MIME_TYPE,
            MediaStore.Images.Media.DATE_MODIFIED,
            MediaStore.Images.Media.WIDTH,
            MediaStore.Images.Media.HEIGHT
        )

        val cursor = contentResolver.query(
            collection,
            projection,
            null,
            null,
            "${MediaStore.Images.Media.DATE_MODIFIED} DESC"
        )

        cursor?.use {

            val idColumn =
                it.getColumnIndexOrThrow(
                    MediaStore.Images.Media._ID
                )

            val nameColumn =
                it.getColumnIndexOrThrow(
                    MediaStore.Images.Media.DISPLAY_NAME
                )

            val sizeColumn =
                it.getColumnIndexOrThrow(
                    MediaStore.Images.Media.SIZE
                )

            val mimeColumn =
                it.getColumnIndexOrThrow(
                    MediaStore.Images.Media.MIME_TYPE
                )

            val dateColumn =
                it.getColumnIndexOrThrow(
                    MediaStore.Images.Media.DATE_MODIFIED
                )

            val widthColumn =
                it.getColumnIndexOrThrow(
                    MediaStore.Images.Media.WIDTH
                )

            val heightColumn =
                it.getColumnIndexOrThrow(
                    MediaStore.Images.Media.HEIGHT
                )

            val total =
                it.count

            var scanned = 0
            var totalSize = 0L

            while (it.moveToNext()) {

                val id =
                    it.getLong(idColumn)

                val name =
                    it.getString(nameColumn) ?: "Unknown image"

                val size =
                    it.getLong(sizeColumn)

                val mimeType =
                    it.getString(mimeColumn)

                val dateModified =
                    it.getLong(dateColumn)

                val width =
                    it.getInt(widthColumn)

                val height =
                    it.getInt(heightColumn)

                val uri =
                    MediaStore.Images.Media
                        .getContentUri(
                            MediaStore.VOLUME_EXTERNAL_PRIMARY,
                            id
                        )

                val cleanerFile =
                    CleanerFile(
                        uri = uri.toString(),
                        name = name,
                        sizeBytes = size,
                        mimeType = mimeType,
                        dateModified = dateModified,
                        width = width,
                        height = height
                    )

                images.add(cleanerFile)

                scanned++
                totalSize += size

                onProgress(
                    scanned,
                    total,
                    totalSize
                )
            }
        }

        images
    }
}