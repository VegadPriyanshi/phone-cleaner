package com.example.phonecleaner.domain.usecase

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import com.example.phonecleaner.data.model.CleanerFile
import com.example.phonecleaner.data.scanner.AudioScanner
import com.example.phonecleaner.data.scanner.LargeFileScanner
import com.example.phonecleaner.data.scanner.MediaStoreImageScanner
import com.example.phonecleaner.data.scanner.ScreenshotScanner
import com.example.phonecleaner.data.scanner.VideoScanner
import com.example.phonecleaner.data.scanner.OldApkScanner

class ScanPhoneUseCase(
    private val context: Context
) {

    suspend fun scanImages(
        onProgress: (
            scanned: Int,
            total: Int,
            sizeBytes: Long
        ) -> Unit
    ): List<CleanerFile> {

        val scanner =
            MediaStoreImageScanner(
                context.contentResolver
            )

        return scanner.scan(
            onProgress = onProgress
        )
    }

    suspend fun scanScreenshots(): List<CleanerFile> {

        val scanner =
            ScreenshotScanner(context)

        return scanner.scan()
    }

    suspend fun scanVideos(): List<CleanerFile> {

        val scanner =
            VideoScanner(context)

        return scanner.scan()
    }

    suspend fun scanAudio(): List<CleanerFile> {

        val scanner =
            AudioScanner(context)

        return scanner.scan()
    }

    @RequiresApi(Build.VERSION_CODES.R)
    fun scanLargeFiles(): List<CleanerFile> {

        return LargeFileScanner.scanLargeFiles(
            context
        )
    }

    fun scanOldApkFiles(): List<CleanerFile> {

        return OldApkScanner.scanOldApkFiles(
            context
        )
    }
}