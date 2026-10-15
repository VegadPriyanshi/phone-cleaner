package com.example.phonecleaner.utils

import android.content.Context


object ScanSummaryPreferences {

    private const val PREF_NAME =
        "phone_cleaner_scan_summary"


    // =========================================================
    // SCAN COMPLETED
    // =========================================================

    private const val KEY_SCAN_COMPLETED =
        "scan_completed"


    // =========================================================
    // PHOTOS
    // =========================================================

    private const val KEY_PHOTO_COUNT =
        "photo_count"

    private const val KEY_PHOTO_SIZE =
        "photo_size"


    // =========================================================
    // VIDEOS
    // =========================================================

    private const val KEY_VIDEO_COUNT =
        "video_count"

    private const val KEY_VIDEO_SIZE =
        "video_size"


    // =========================================================
    // APPS
    // =========================================================

    private const val KEY_APP_SIZE =
        "app_size"


    // =========================================================
    // DUPLICATE PHOTOS
    // =========================================================

    private const val KEY_DUPLICATE_GROUP_COUNT =
        "duplicate_group_count"

    private const val KEY_DUPLICATE_PHOTO_COUNT =
        "duplicate_photo_count"


    // =========================================================
    // LARGE FILES
    // =========================================================

    private const val KEY_LARGE_FILE_COUNT =
        "large_file_count"

    private const val KEY_LARGE_FILE_SIZE =
        "large_file_size"


    // =========================================================
    // JUNK FILES
    // =========================================================

    private const val KEY_JUNK_FILE_COUNT =
        "junk_file_count"

    private const val KEY_JUNK_FILE_SIZE =
        "junk_file_size"


    // =========================================================
    // DOCUMENTS
    // =========================================================

    private const val KEY_DOCUMENT_COUNT =
        "document_count"

    private const val KEY_DOCUMENT_SIZE =
        "document_size"


    // =========================================================
    // SCREENSHOTS
    // =========================================================

    private const val KEY_SCREENSHOT_COUNT =
        "screenshot_count"

    private const val KEY_SCREENSHOT_SIZE =
        "screenshot_size"


    // =========================================================
    // AUDIO
    // =========================================================

    private const val KEY_AUDIO_COUNT =
        "audio_count"

    private const val KEY_AUDIO_SIZE =
        "audio_size"


    // =========================================================
    // SHARED PREFERENCES
    // =========================================================

    private fun preferences(
        context: Context
    ) =
        context.getSharedPreferences(
            PREF_NAME,
            Context.MODE_PRIVATE
        )


    // =========================================================
    // SCAN COMPLETED FLAG
    // =========================================================

    fun markScanCompleted(
        context: Context
    ) {

        preferences(
            context
        )
            .edit()
            .putBoolean(
                KEY_SCAN_COMPLETED,
                true
            )
            .apply()
    }


    fun hasCompletedScan(
        context: Context
    ): Boolean {

        return preferences(
            context
        )
            .getBoolean(
                KEY_SCAN_COMPLETED,
                false
            )
    }


    // =========================================================
    // PHOTOS
    // =========================================================

    fun savePhotoSummary(
        context: Context,
        count: Int,
        sizeBytes: Long
    ) {

        preferences(
            context
        )
            .edit()
            .putInt(
                KEY_PHOTO_COUNT,
                count
            )
            .putLong(
                KEY_PHOTO_SIZE,
                sizeBytes
            )
            .apply()
    }


    fun getPhotoCount(
        context: Context
    ): Int =

        preferences(
            context
        )
            .getInt(
                KEY_PHOTO_COUNT,
                0
            )


    fun getPhotoSize(
        context: Context
    ): Long =

        preferences(
            context
        )
            .getLong(
                KEY_PHOTO_SIZE,
                0L
            )


    // =========================================================
    // VIDEOS
    // =========================================================

    fun saveVideoSummary(
        context: Context,
        count: Int,
        sizeBytes: Long
    ) {

        preferences(
            context
        )
            .edit()
            .putInt(
                KEY_VIDEO_COUNT,
                count
            )
            .putLong(
                KEY_VIDEO_SIZE,
                sizeBytes
            )
            .apply()
    }


    fun getVideoCount(
        context: Context
    ): Int =

        preferences(
            context
        )
            .getInt(
                KEY_VIDEO_COUNT,
                0
            )


    fun getVideoSize(
        context: Context
    ): Long =

        preferences(
            context
        )
            .getLong(
                KEY_VIDEO_SIZE,
                0L
            )


    // =========================================================
    // APPS
    // =========================================================

    fun saveAppSize(
        context: Context,
        sizeBytes: Long
    ) {

        preferences(
            context
        )
            .edit()
            .putLong(
                KEY_APP_SIZE,
                sizeBytes
            )
            .apply()
    }


    fun getAppSize(
        context: Context
    ): Long =

        preferences(
            context
        )
            .getLong(
                KEY_APP_SIZE,
                0L
            )


    // =========================================================
    // DUPLICATE PHOTOS
    // =========================================================

    fun saveDuplicateSummary(
        context: Context,
        groupCount: Int,
        photoCount: Int
    ) {

        preferences(
            context
        )
            .edit()
            .putInt(
                KEY_DUPLICATE_GROUP_COUNT,
                groupCount
            )
            .putInt(
                KEY_DUPLICATE_PHOTO_COUNT,
                photoCount
            )
            .apply()
    }


    fun getDuplicateGroupCount(
        context: Context
    ): Int =

        preferences(
            context
        )
            .getInt(
                KEY_DUPLICATE_GROUP_COUNT,
                0
            )


    fun getDuplicatePhotoCount(
        context: Context
    ): Int =

        preferences(
            context
        )
            .getInt(
                KEY_DUPLICATE_PHOTO_COUNT,
                0
            )


    // =========================================================
    // LARGE FILES
    // =========================================================

    fun saveLargeFileSummary(
        context: Context,
        count: Int,
        sizeBytes: Long
    ) {

        preferences(
            context
        )
            .edit()
            .putInt(
                KEY_LARGE_FILE_COUNT,
                count
            )
            .putLong(
                KEY_LARGE_FILE_SIZE,
                sizeBytes
            )
            .apply()
    }


    fun getLargeFileCount(
        context: Context
    ): Int =

        preferences(
            context
        )
            .getInt(
                KEY_LARGE_FILE_COUNT,
                0
            )


    fun getLargeFileSize(
        context: Context
    ): Long =

        preferences(
            context
        )
            .getLong(
                KEY_LARGE_FILE_SIZE,
                0L
            )


    // =========================================================
    // JUNK FILES
    // =========================================================

    fun saveJunkFileSummary(
        context: Context,
        count: Int,
        sizeBytes: Long
    ) {

        preferences(
            context
        )
            .edit()
            .putInt(
                KEY_JUNK_FILE_COUNT,
                count
            )
            .putLong(
                KEY_JUNK_FILE_SIZE,
                sizeBytes
            )
            .apply()
    }


    fun getJunkFileCount(
        context: Context
    ): Int =

        preferences(
            context
        )
            .getInt(
                KEY_JUNK_FILE_COUNT,
                0
            )


    fun getJunkFileSize(
        context: Context
    ): Long =

        preferences(
            context
        )
            .getLong(
                KEY_JUNK_FILE_SIZE,
                0L
            )


    // =========================================================
    // DOCUMENTS
    // =========================================================

    fun saveDocumentSummary(
        context: Context,
        count: Int,
        sizeBytes: Long
    ) {

        preferences(
            context
        )
            .edit()
            .putInt(
                KEY_DOCUMENT_COUNT,
                count
            )
            .putLong(
                KEY_DOCUMENT_SIZE,
                sizeBytes
            )
            .apply()
    }


    fun getDocumentCount(
        context: Context
    ): Int =

        preferences(
            context
        )
            .getInt(
                KEY_DOCUMENT_COUNT,
                0
            )


    fun getDocumentSize(
        context: Context
    ): Long =

        preferences(
            context
        )
            .getLong(
                KEY_DOCUMENT_SIZE,
                0L
            )


    // =========================================================
    // SCREENSHOTS
    // =========================================================

    fun saveScreenshotSummary(
        context: Context,
        count: Int,
        sizeBytes: Long
    ) {

        preferences(
            context
        )
            .edit()
            .putInt(
                KEY_SCREENSHOT_COUNT,
                count
            )
            .putLong(
                KEY_SCREENSHOT_SIZE,
                sizeBytes
            )
            .apply()
    }


    fun getScreenshotCount(
        context: Context
    ): Int =

        preferences(
            context
        )
            .getInt(
                KEY_SCREENSHOT_COUNT,
                0
            )


    fun getScreenshotSize(
        context: Context
    ): Long =

        preferences(
            context
        )
            .getLong(
                KEY_SCREENSHOT_SIZE,
                0L
            )


    // =========================================================
    // AUDIO
    // =========================================================

    fun saveAudioSummary(
        context: Context,
        count: Int,
        sizeBytes: Long
    ) {

        preferences(
            context
        )
            .edit()
            .putInt(
                KEY_AUDIO_COUNT,
                count
            )
            .putLong(
                KEY_AUDIO_SIZE,
                sizeBytes
            )
            .apply()
    }


    fun getAudioCount(
        context: Context
    ): Int =

        preferences(
            context
        )
            .getInt(
                KEY_AUDIO_COUNT,
                0
            )


    fun getAudioSize(
        context: Context
    ): Long =

        preferences(
            context
        )
            .getLong(
                KEY_AUDIO_SIZE,
                0L
            )
}