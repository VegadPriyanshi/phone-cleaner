package com.example.phonecleaner

import android.Manifest
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.phonecleaner.data.scanner.VideoScanner
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class VideoScannerInstrumentedTest {

    @Test
    fun scanVideos() {

        val instrumentation =
            InstrumentationRegistry.getInstrumentation()

        val context =
            instrumentation.targetContext

        if (Build.VERSION.SDK_INT >= 33) {

            instrumentation.uiAutomation
                .grantRuntimePermission(
                    context.packageName,
                    Manifest.permission.READ_MEDIA_VIDEO
                )
        }

        val permission =
            if (Build.VERSION.SDK_INT >= 33) {
                context.checkSelfPermission(
                    Manifest.permission.READ_MEDIA_VIDEO
                )
            } else {
                0
            }

        Log.d(
            "VideoScannerTest",
            "READ_MEDIA_VIDEO permission: $permission"
        )

        val projection =
            arrayOf(
                MediaStore.Video.Media.DISPLAY_NAME,
                MediaStore.Video.Media.SIZE,
                MediaStore.Video.Media.MIME_TYPE
            )

        var mediaStoreCount = 0

        context.contentResolver.query(
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
            projection,
            null,
            null,
            "${MediaStore.Video.Media.DATE_MODIFIED} DESC"
        )?.use { cursor ->

            mediaStoreCount =
                cursor.count

            Log.d(
                "VideoScannerTest",
                "MediaStore video count: $mediaStoreCount"
            )

            val nameColumn =
                cursor.getColumnIndexOrThrow(
                    MediaStore.Video.Media.DISPLAY_NAME
                )

            val sizeColumn =
                cursor.getColumnIndexOrThrow(
                    MediaStore.Video.Media.SIZE
                )

            val mimeColumn =
                cursor.getColumnIndexOrThrow(
                    MediaStore.Video.Media.MIME_TYPE
                )

            var index = 0

            while (
                cursor.moveToNext() &&
                index < 10
            ) {

                val name =
                    cursor.getString(
                        nameColumn
                    ) ?: ""

                val size =
                    cursor.getLong(
                        sizeColumn
                    )

                val mime =
                    cursor.getString(
                        mimeColumn
                    ) ?: ""

                Log.d(
                    "VideoScannerTest",
                    "MediaStore $index: " +
                            "$name | $size bytes | $mime"
                )

                index++
            }
        }

        val results =
            VideoScanner(context).scan()

        val totalSize =
            results.sumOf {
                it.sizeBytes
            }

        Log.d(
            "VideoScannerTest",
            "VideoScanner detected: ${results.size}"
        )

        Log.d(
            "VideoScannerTest",
            "VideoScanner total size: $totalSize bytes"
        )

        results
            .take(10)
            .forEachIndexed { index, video ->

                Log.d(
                    "VideoScannerTest",
                    "Scanner $index: " +
                            "${video.name} | " +
                            "${video.sizeBytes} bytes | " +
                            "${video.width}x${video.height} | " +
                            video.uri
                )
            }
    }
}