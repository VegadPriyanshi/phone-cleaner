package com.example.phonecleaner.features.autoclean

import android.content.Context
import android.net.Uri
import android.os.Build
import android.util.Log

import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

import com.example.phonecleaner.data.model.AutoCleanResult
import com.example.phonecleaner.data.scanner.EmptyFileScanner
import com.example.phonecleaner.data.scanner.JunkFileScanner
import com.example.phonecleaner.data.scanner.LeftoverAppDataScanner
import com.example.phonecleaner.data.scanner.OldApkScanner
import com.example.phonecleaner.data.scanner.ThumbnailCacheScanner
import com.example.phonecleaner.utils.AutoCleanResultStorage

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

import java.io.File


class AutoCleanWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(
    appContext,
    workerParams
) {

    companion object {

        private const val TAG =
            "PhoneCleanerAutoClean"

        // =========================================================
        // Safe Temporary File Rules
        // =========================================================

        private val TEMPORARY_EXTENSIONS =
            setOf(
                ".tmp",
                ".temp"
            )

        private val TEMPORARY_NAME_KEYWORDS =
            setOf(
                ".tmp.",
                ".temp.",
                "_tmp",
                "_temp",
                "temp_",
                "tmp_"
            )

        // =========================================================
        // Unsafe Empty File Extensions
        // =========================================================

        private val UNSAFE_EMPTY_EXTENSIONS =
            setOf(
                ".jpg",
                ".jpeg",
                ".png",
                ".webp",
                ".gif",
                ".mp4",
                ".mkv",
                ".mov",
                ".avi",
                ".3gp",
                ".mp3",
                ".wav",
                ".m4a",
                ".aac",
                ".flac"
            )

        // =========================================================
        // Safe Empty File Name Rules
        // =========================================================

        private val SAFE_EMPTY_NAME_KEYWORDS =
            setOf(
                "tmp",
                "temp",
                "cache",
                "temporary",
                "legacy_not_exist"
            )

        // =========================================================
        // Old APK Safety Rule
        // =========================================================

        private const val OLD_APK_DAYS = 7L

        private const val MILLIS_PER_DAY =
            24L * 60L * 60L * 1000L

        private const val OLD_APK_AGE_MILLIS =
            OLD_APK_DAYS * MILLIS_PER_DAY

        // =========================================================
        // Leftover App Data Safety Paths
        // =========================================================

        /*
         * LeftoverAppDataScanner returns folders from:
         *
         * /storage/emulated/0/Android/data/<package>
         * /storage/emulated/0/Android/media/<package>
         *
         * Auto-clean must NEVER delete arbitrary file:// paths.
         */

        private const val ANDROID_DATA_PATH =
            "/Android/data/"

        private const val ANDROID_MEDIA_PATH =
            "/Android/media/"
    }


    // =============================================================
    // Worker
    // =============================================================

    override suspend fun doWork(): Result {

        return try {

            Log.d(
                TAG,
                "===================================="
            )

            Log.d(
                TAG,
                "Auto-clean started"
            )

            Log.d(
                TAG,
                "AutoCleanWorker STARTED"
            )


            // =====================================================
            // Android Version Check
            // =====================================================

            if (
                Build.VERSION.SDK_INT <
                Build.VERSION_CODES.R
            ) {

                Log.d(
                    TAG,
                    "Auto-clean requires Android 11 or newer"
                )

                return Result.success()
            }


            // =====================================================
            // Scan Junk / Temporary Files
            // =====================================================

            val junkFiles =
                withContext(Dispatchers.IO) {

                    JunkFileScanner.scanJunkFiles(
                        applicationContext
                    )
                }

            Log.d(
                TAG,
                "Junk files found: ${junkFiles.size}"
            )


            // =====================================================
            // Find Safe Temporary Files
            // =====================================================

            val safeTemporaryFiles =
                junkFiles.filter { cleanerFile ->

                    isSafeTemporaryFile(
                        cleanerFile.name
                    )
                }

            Log.d(
                TAG,
                "Safe temporary files: " +
                        safeTemporaryFiles.size
            )

            safeTemporaryFiles.forEach { cleanerFile ->

                Log.d(
                    TAG,
                    "SAFE TEMPORARY FILE -> " +
                            "name=${cleanerFile.name}, " +
                            "uri=${cleanerFile.uri}, " +
                            "size=${cleanerFile.sizeBytes}"
                )
            }


            // =====================================================
            // Scan Empty Files
            // =====================================================

            val emptyFiles =
                withContext(Dispatchers.IO) {

                    EmptyFileScanner.scanEmptyFiles(
                        applicationContext
                    )
                }

            Log.d(
                TAG,
                "Empty files found: " +
                        emptyFiles.size
            )


            // =====================================================
            // Find Safe Empty Files
            // =====================================================

            val safeEmptyFiles =
                emptyFiles.filter { emptyFile ->

                    isSafeEmptyFile(
                        emptyFile.name
                    )
                }

            Log.d(
                TAG,
                "Safe empty files: " +
                        safeEmptyFiles.size
            )

            safeEmptyFiles.forEach { emptyFile ->

                Log.d(
                    TAG,
                    "SAFE EMPTY FILE -> " +
                            "name=${emptyFile.name}, " +
                            "uri=${emptyFile.uri}, " +
                            "size=${emptyFile.sizeBytes}"
                )
            }


            // =====================================================
            // Scan APK Files
            // =====================================================

            val apkFiles =
                withContext(Dispatchers.IO) {

                    OldApkScanner.scanOldApkFiles(
                        applicationContext
                    )
                }

            Log.d(
                TAG,
                "APK files found: " +
                        apkFiles.size
            )


            // =====================================================
            // Find Safe Old APK Files
            // =====================================================

            val safeOldApkFiles =
                apkFiles.filter { apkFile ->

                    isSafeOldApkFile(
                        apkFile.dateModified
                    )
                }

            Log.d(
                TAG,
                "Safe old APK files: " +
                        safeOldApkFiles.size
            )

            safeOldApkFiles.forEach { apkFile ->

                Log.d(
                    TAG,
                    "SAFE OLD APK -> " +
                            "name=${apkFile.name}, " +
                            "uri=${apkFile.uri}, " +
                            "size=${apkFile.sizeBytes}, " +
                            "dateModified=${apkFile.dateModified}"
                )
            }


            // =====================================================
            // Scan Thumbnail Cache Files
            // =====================================================

            val thumbnailCacheFiles =
                withContext(Dispatchers.IO) {

                    ThumbnailCacheScanner.scanThumbnailCaches(
                        applicationContext
                    )
                }

            Log.d(
                TAG,
                "Thumbnail cache files found: " +
                        thumbnailCacheFiles.size
            )

            thumbnailCacheFiles.forEach { thumbnailFile ->

                Log.d(
                    TAG,
                    "SAFE THUMBNAIL CACHE -> " +
                            "name=${thumbnailFile.name}, " +
                            "uri=${thumbnailFile.uri}, " +
                            "size=${thumbnailFile.sizeBytes}"
                )
            }


            // =====================================================
            // Scan Leftover App Data
            // =====================================================

            val leftoverAppDataFiles =
                withContext(Dispatchers.IO) {

                    LeftoverAppDataScanner.scanLeftoverAppData(
                        applicationContext
                    )
                }

            Log.d(
                TAG,
                "Leftover app data folders found: " +
                        leftoverAppDataFiles.size
            )

            leftoverAppDataFiles.forEach { leftoverFolder ->

                Log.d(
                    TAG,
                    "SAFE LEFTOVER APP DATA -> " +
                            "name=${leftoverFolder.name}, " +
                            "uri=${leftoverFolder.uri}, " +
                            "size=${leftoverFolder.sizeBytes}"
                )
            }


            // =====================================================
            // Nothing To Clean
            // =====================================================

            if (
                safeTemporaryFiles.isEmpty() &&
                safeEmptyFiles.isEmpty() &&
                safeOldApkFiles.isEmpty() &&
                thumbnailCacheFiles.isEmpty() &&
                leftoverAppDataFiles.isEmpty()
            ) {

                Log.d(
                    TAG,
                    "No safe files found for Auto-clean"
                )

                Log.d(
                    TAG,
                    "Auto-clean finished"
                )

                Log.d(
                    TAG,
                    "Deleted: 0"
                )

                Log.d(
                    TAG,
                    "Failed: 0"
                )

                Log.d(
                    TAG,
                    "Space freed: 0 bytes"
                )

                AutoCleanResultStorage.saveResult(
                    applicationContext,
                    AutoCleanResult(
                        timestamp =
                            System.currentTimeMillis(),
                        deletedCount = 0,
                        failedCount = 0,
                        spaceFreedBytes = 0L,
                        temporaryFilesDeleted = 0,
                        emptyFilesDeleted = 0,
                        oldApkFilesDeleted = 0,
                        thumbnailCacheFilesDeleted = 0,
                        leftoverAppDataDeleted = 0
                    )
                )

                Log.d(
                    TAG,
                    "Auto-clean result saved"
                )

                Log.d(
                    TAG,
                    "===================================="
                )

                return Result.success()
            }


            // =====================================================
            // Counters
            // =====================================================

            var deletedCount = 0
            var failedCount = 0

            var spaceFreedBytes = 0L

            var temporaryFilesDeleted = 0
            var emptyFilesDeleted = 0
            var oldApkFilesDeleted = 0
            var thumbnailCacheFilesDeleted = 0
            var leftoverAppDataDeleted = 0


            // =====================================================
            // Delete Safe Temporary Files
            // =====================================================

            withContext(Dispatchers.IO) {

                safeTemporaryFiles.forEach { cleanerFile ->

                    try {

                        val uri =
                            Uri.parse(
                                cleanerFile.uri
                            )

                        if (
                            !uri.scheme.equals(
                                "content",
                                ignoreCase = true
                            )
                        ) {

                            Log.w(
                                TAG,
                                "Skipping non-content temporary file: " +
                                        cleanerFile.name
                            )

                            failedCount++

                            return@forEach
                        }

                        val deletedRows =
                            applicationContext
                                .contentResolver
                                .delete(
                                    uri,
                                    null,
                                    null
                                )

                        if (deletedRows > 0) {

                            deletedCount++
                            temporaryFilesDeleted++

                            spaceFreedBytes +=
                                cleanerFile.sizeBytes

                            Log.d(
                                TAG,
                                "Deleted temporary file: " +
                                        cleanerFile.name +
                                        ", freed=" +
                                        cleanerFile.sizeBytes +
                                        " bytes"
                            )

                        } else {

                            failedCount++

                            Log.w(
                                TAG,
                                "Could not delete temporary file: " +
                                        cleanerFile.name
                            )
                        }

                    } catch (
                        securityException: SecurityException
                    ) {

                        failedCount++

                        Log.e(
                            TAG,
                            "Permission denied deleting temporary file: " +
                                    cleanerFile.name,
                            securityException
                        )

                    } catch (
                        exception: Exception
                    ) {

                        failedCount++

                        Log.e(
                            TAG,
                            "Failed to delete temporary file: " +
                                    cleanerFile.name,
                            exception
                        )
                    }
                }
            }


            // =====================================================
            // Delete Safe Empty Files
            // =====================================================

            withContext(Dispatchers.IO) {

                safeEmptyFiles.forEach { emptyFile ->

                    try {

                        val uri =
                            Uri.parse(
                                emptyFile.uri
                            )

                        if (
                            !uri.scheme.equals(
                                "content",
                                ignoreCase = true
                            )
                        ) {

                            Log.w(
                                TAG,
                                "Skipping non-content empty file: " +
                                        emptyFile.name
                            )

                            failedCount++

                            return@forEach
                        }

                        val deletedRows =
                            applicationContext
                                .contentResolver
                                .delete(
                                    uri,
                                    null,
                                    null
                                )

                        if (deletedRows > 0) {

                            deletedCount++
                            emptyFilesDeleted++

                            spaceFreedBytes +=
                                emptyFile.sizeBytes

                            Log.d(
                                TAG,
                                "Deleted safe empty file: " +
                                        emptyFile.name +
                                        ", freed=" +
                                        emptyFile.sizeBytes +
                                        " bytes"
                            )

                        } else {

                            failedCount++

                            Log.w(
                                TAG,
                                "Could not delete safe empty file: " +
                                        emptyFile.name
                            )
                        }

                    } catch (
                        securityException: SecurityException
                    ) {

                        failedCount++

                        Log.e(
                            TAG,
                            "Permission denied deleting empty file: " +
                                    emptyFile.name,
                            securityException
                        )

                    } catch (
                        exception: Exception
                    ) {

                        failedCount++

                        Log.e(
                            TAG,
                            "Failed to delete empty file: " +
                                    emptyFile.name,
                            exception
                        )
                    }
                }
            }


            // =====================================================
            // Delete Safe Old APK Files
            // =====================================================

            withContext(Dispatchers.IO) {

                safeOldApkFiles.forEach { apkFile ->

                    try {

                        val uri =
                            Uri.parse(
                                apkFile.uri
                            )

                        if (
                            !uri.scheme.equals(
                                "content",
                                ignoreCase = true
                            )
                        ) {

                            Log.w(
                                TAG,
                                "Skipping non-content APK file: " +
                                        apkFile.name
                            )

                            failedCount++

                            return@forEach
                        }

                        val deletedRows =
                            applicationContext
                                .contentResolver
                                .delete(
                                    uri,
                                    null,
                                    null
                                )

                        if (deletedRows > 0) {

                            deletedCount++
                            oldApkFilesDeleted++

                            spaceFreedBytes +=
                                apkFile.sizeBytes

                            Log.d(
                                TAG,
                                "Deleted old APK file: " +
                                        apkFile.name +
                                        ", freed=" +
                                        apkFile.sizeBytes +
                                        " bytes"
                            )

                        } else {

                            failedCount++

                            Log.w(
                                TAG,
                                "Could not delete old APK file: " +
                                        apkFile.name
                            )
                        }

                    } catch (
                        securityException: SecurityException
                    ) {

                        failedCount++

                        Log.e(
                            TAG,
                            "Permission denied deleting APK file: " +
                                    apkFile.name,
                            securityException
                        )

                    } catch (
                        exception: Exception
                    ) {

                        failedCount++

                        Log.e(
                            TAG,
                            "Failed to delete APK file: " +
                                    apkFile.name,
                            exception
                        )
                    }
                }
            }


            // =====================================================
            // Delete Thumbnail Cache Files
            // =====================================================

            withContext(Dispatchers.IO) {

                thumbnailCacheFiles.forEach { thumbnailFile ->

                    try {

                        val uri =
                            Uri.parse(
                                thumbnailFile.uri
                            )

                        if (
                            !uri.scheme.equals(
                                "file",
                                ignoreCase = true
                            )
                        ) {

                            Log.w(
                                TAG,
                                "Skipping non-file thumbnail cache: " +
                                        thumbnailFile.name
                            )

                            failedCount++

                            return@forEach
                        }

                        val filePath =
                            uri.path

                        if (filePath.isNullOrBlank()) {

                            failedCount++

                            Log.w(
                                TAG,
                                "Invalid thumbnail cache path: " +
                                        thumbnailFile.name
                            )

                            return@forEach
                        }

                        /*
                         * Keep supporting files.
                         *
                         * .nomedia and .database_uuid should not
                         * be automatically removed.
                         */

                        if (
                            thumbnailFile.name.equals(
                                ".nomedia",
                                ignoreCase = true
                            ) ||
                            thumbnailFile.name.equals(
                                ".database_uuid",
                                ignoreCase = true
                            )
                        ) {

                            Log.d(
                                TAG,
                                "Keeping thumbnail support file: " +
                                        thumbnailFile.name
                            )

                            return@forEach
                        }

                        val file =
                            File(filePath)

                        if (!file.exists()) {

                            Log.d(
                                TAG,
                                "Thumbnail cache already missing: " +
                                        thumbnailFile.name
                            )

                            return@forEach
                        }

                        if (!file.isFile) {

                            failedCount++

                            Log.w(
                                TAG,
                                "Thumbnail path is not a file: " +
                                        thumbnailFile.name
                            )

                            return@forEach
                        }

                        val deleted =
                            file.delete()

                        if (deleted) {

                            deletedCount++
                            thumbnailCacheFilesDeleted++

                            spaceFreedBytes +=
                                thumbnailFile.sizeBytes

                            Log.d(
                                TAG,
                                "Deleted thumbnail cache file: " +
                                        thumbnailFile.name +
                                        ", freed=" +
                                        thumbnailFile.sizeBytes +
                                        " bytes"
                            )

                        } else {

                            failedCount++

                            Log.w(
                                TAG,
                                "Could not delete thumbnail cache file: " +
                                        thumbnailFile.name
                            )
                        }

                    } catch (
                        securityException: SecurityException
                    ) {

                        failedCount++

                        Log.e(
                            TAG,
                            "Permission denied deleting thumbnail cache: " +
                                    thumbnailFile.name,
                            securityException
                        )

                    } catch (
                        exception: Exception
                    ) {

                        failedCount++

                        Log.e(
                            TAG,
                            "Failed to delete thumbnail cache: " +
                                    thumbnailFile.name,
                            exception
                        )
                    }
                }
            }


            // =====================================================
            // Delete Leftover App Data
            // =====================================================

            withContext(Dispatchers.IO) {

                leftoverAppDataFiles.forEach { leftoverFolder ->

                    try {

                        val uri =
                            Uri.parse(
                                leftoverFolder.uri
                            )

                        // -------------------------------------------------
                        // Leftover App Data must use file:// URI
                        // -------------------------------------------------

                        if (
                            !uri.scheme.equals(
                                "file",
                                ignoreCase = true
                            )
                        ) {

                            Log.w(
                                TAG,
                                "Skipping non-file leftover app data: " +
                                        leftoverFolder.name
                            )

                            failedCount++

                            return@forEach
                        }

                        val folderPath =
                            uri.path

                        if (folderPath.isNullOrBlank()) {

                            failedCount++

                            Log.w(
                                TAG,
                                "Invalid leftover app data path: " +
                                        leftoverFolder.name
                            )

                            return@forEach
                        }

                        // -------------------------------------------------
                        // Safety check
                        // -------------------------------------------------

                        if (
                            !isSafeLeftoverAppDataPath(
                                folderPath
                            )
                        ) {

                            failedCount++

                            Log.w(
                                TAG,
                                "UNSAFE leftover app data path skipped: " +
                                        folderPath
                            )

                            return@forEach
                        }

                        val folder =
                            File(folderPath)

                        // -------------------------------------------------
                        // Must be a directory
                        // -------------------------------------------------

                        if (!folder.exists()) {

                            Log.d(
                                TAG,
                                "Leftover folder already missing: " +
                                        leftoverFolder.name
                            )

                            return@forEach
                        }

                        if (!folder.isDirectory) {

                            failedCount++

                            Log.w(
                                TAG,
                                "Leftover app data path is not a directory: " +
                                        leftoverFolder.name
                            )

                            return@forEach
                        }

                        // -------------------------------------------------
                        // Recursive deletion
                        // -------------------------------------------------

                        val deleted =
                            deleteDirectoryRecursively(
                                folder
                            )

                        if (deleted) {

                            deletedCount++
                            leftoverAppDataDeleted++

                            spaceFreedBytes +=
                                leftoverFolder.sizeBytes

                            Log.d(
                                TAG,
                                "Deleted leftover app data: " +
                                        leftoverFolder.name +
                                        ", freed=" +
                                        leftoverFolder.sizeBytes +
                                        " bytes"
                            )

                        } else {

                            failedCount++

                            Log.w(
                                TAG,
                                "Could not completely delete leftover app data: " +
                                        leftoverFolder.name
                            )
                        }

                    } catch (
                        securityException: SecurityException
                    ) {

                        failedCount++

                        Log.e(
                            TAG,
                            "Permission denied deleting leftover app data: " +
                                    leftoverFolder.name,
                            securityException
                        )

                    } catch (
                        exception: Exception
                    ) {

                        failedCount++

                        Log.e(
                            TAG,
                            "Failed to delete leftover app data: " +
                                    leftoverFolder.name,
                            exception
                        )
                    }
                }
            }


            // =====================================================
            // Final Result
            // =====================================================

            Log.d(
                TAG,
                "Auto-clean finished"
            )

            Log.d(
                TAG,
                "Deleted: $deletedCount"
            )

            Log.d(
                TAG,
                "Failed: $failedCount"
            )

            Log.d(
                TAG,
                "Space freed: $spaceFreedBytes bytes"
            )


            // =====================================================
            // Save Auto-clean Result
            // =====================================================

            val autoCleanResult =
                AutoCleanResult(
                    timestamp =
                        System.currentTimeMillis(),

                    deletedCount =
                        deletedCount,

                    failedCount =
                        failedCount,

                    spaceFreedBytes =
                        spaceFreedBytes,

                    temporaryFilesDeleted =
                        temporaryFilesDeleted,

                    emptyFilesDeleted =
                        emptyFilesDeleted,

                    oldApkFilesDeleted =
                        oldApkFilesDeleted,

                    thumbnailCacheFilesDeleted =
                        thumbnailCacheFilesDeleted,

                    leftoverAppDataDeleted =
                        leftoverAppDataDeleted
                )

            AutoCleanResultStorage.saveResult(
                applicationContext,
                autoCleanResult
            )

            Log.d(
                TAG,
                "Auto-clean result saved"
            )

            Log.d(
                TAG,
                "===================================="
            )

            Result.success()

        } catch (exception: Exception) {

            Log.e(
                TAG,
                "Auto-clean failed",
                exception
            )

            Result.retry()
        }
    }


    // =============================================================
    // Safe Temporary File Check
    // =============================================================

    private fun isSafeTemporaryFile(
        fileName: String
    ): Boolean {

        val lowerCaseName =
            fileName.lowercase()

        val hasTemporaryExtension =
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

        return hasTemporaryExtension ||
                hasTemporaryKeyword
    }


    // =============================================================
    // Safe Empty File Check
    // =============================================================

    private fun isSafeEmptyFile(
        fileName: String
    ): Boolean {

        val lowerCaseName =
            fileName.lowercase()

        if (
            UNSAFE_EMPTY_EXTENSIONS.any { extension ->

                lowerCaseName.endsWith(
                    extension
                )
            }
        ) {

            return false
        }

        return SAFE_EMPTY_NAME_KEYWORDS.any { keyword ->

            lowerCaseName.contains(
                keyword
            )
        }
    }


    // =============================================================
    // Safe Old APK Check
    // =============================================================

    private fun isSafeOldApkFile(
        dateModified: Long
    ): Boolean {

        val modifiedTimeMillis =
            dateModified * 1000L

        val currentTimeMillis =
            System.currentTimeMillis()

        val ageMillis =
            currentTimeMillis - modifiedTimeMillis

        if (ageMillis < 0L) {
            return false
        }

        return ageMillis >= OLD_APK_AGE_MILLIS
    }


    // =============================================================
    // Leftover App Data Path Safety Check
    // =============================================================

    private fun isSafeLeftoverAppDataPath(
        path: String
    ): Boolean {

        val normalizedPath =
            path
                .replace(
                    '\\',
                    '/'
                )
                .lowercase()

        /*
         * The scanner only returns directories from:
         *
         * /Android/data/<package>
         * /Android/media/<package>
         *
         * Make sure the Worker only deletes from those
         * locations.
         */

        val isAndroidData =
            normalizedPath.contains(
                ANDROID_DATA_PATH.lowercase()
            )

        val isAndroidMedia =
            normalizedPath.contains(
                ANDROID_MEDIA_PATH.lowercase()
            )

        if (
            !isAndroidData &&
            !isAndroidMedia
        ) {

            return false
        }

        /*
         * Do not allow deletion of the Android/data or
         * Android/media root itself.
         */

        if (
            normalizedPath.endsWith(
                "/android/data"
            ) ||
            normalizedPath.endsWith(
                "/android/media"
            )
        ) {

            return false
        }

        /*
         * The scanner identifies package folders by a
         * package-like name containing ".".
         *
         * Make sure the final path component also looks
         * like a package name.
         */

        val packageName =
            normalizedPath
                .substringAfterLast(
                    "/"
                )

        return packageName.contains(".")
    }


    // =============================================================
    // Recursive Directory Delete
    // =============================================================

    private fun deleteDirectoryRecursively(
        directory: File
    ): Boolean {

        return try {

            val children =
                directory.listFiles()

            if (children != null) {

                for (child in children) {

                    val deletedChild =

                        if (child.isDirectory) {

                            deleteDirectoryRecursively(
                                child
                            )

                        } else {

                            child.delete()
                        }

                    if (!deletedChild) {

                        Log.w(
                            TAG,
                            "Could not delete: " +
                                    child.absolutePath
                        )

                        return false
                    }
                }
            }

            /*
             * At this point the directory should be empty.
             */

            directory.delete()

        } catch (
            securityException: SecurityException
        ) {

            Log.e(
                TAG,
                "Permission denied deleting directory: " +
                        directory.absolutePath,
                securityException
            )

            false

        } catch (
            exception: Exception
        ) {

            Log.e(
                TAG,
                "Error recursively deleting directory: " +
                        directory.absolutePath,
                exception
            )

            false
        }
    }
}