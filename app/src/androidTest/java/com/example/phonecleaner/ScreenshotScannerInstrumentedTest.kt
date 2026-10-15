package com.example.phonecleaner

import android.Manifest
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.phonecleaner.data.scanner.ScreenshotScanner
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ScreenshotScannerInstrumentedTest {

    @Test
    fun scanScreenshots() {

        val instrumentation =
            InstrumentationRegistry.getInstrumentation()

        val context =
            instrumentation.targetContext

        if (Build.VERSION.SDK_INT >= 33) {

            instrumentation.uiAutomation
                .grantRuntimePermission(
                    context.packageName,
                    Manifest.permission.READ_MEDIA_IMAGES
                )
        }

        val contentResolver =
            context.contentResolver

        var mediaStoreCount = 0

        val projection = arrayOf(
            MediaStore.Images.Media.DISPLAY_NAME,
            MediaStore.Images.Media.RELATIVE_PATH
        )

        contentResolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            projection,
            null,
            null,
            "${MediaStore.Images.Media.DATE_MODIFIED} DESC"
        )?.use { cursor ->

            mediaStoreCount = cursor.count

            Log.d(
                "ScreenshotScannerTest",
                "MediaStore image count: $mediaStoreCount"
            )

            val nameColumn =
                cursor.getColumnIndexOrThrow(
                    MediaStore.Images.Media.DISPLAY_NAME
                )

            val pathColumn =
                cursor.getColumnIndexOrThrow(
                    MediaStore.Images.Media.RELATIVE_PATH
                )

            var index = 0

            while (cursor.moveToNext() && index < 10) {

                val name =
                    cursor.getString(nameColumn) ?: ""

                val path =
                    cursor.getString(pathColumn) ?: ""

                Log.d(
                    "ScreenshotScannerTest",
                    "MediaStore $index: $name | $path"
                )

                index++
            }
        }

        val results =
            ScreenshotScanner(context).scan()

        Log.d(
            "ScreenshotScannerTest",
            "ScreenshotScanner detected: ${results.size}"
        )

        results
            .take(10)
            .forEachIndexed { index, file ->

                Log.d(
                    "ScreenshotScannerTest",
                    "Scanner $index: ${file.name} | ${file.uri}"
                )
            }
    }
}