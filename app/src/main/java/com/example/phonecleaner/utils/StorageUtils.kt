package com.example.phonecleaner.utils

import android.app.usage.StorageStatsManager
import android.content.Context
import android.os.Environment
import android.os.StatFs
import android.os.storage.StorageManager
import java.util.Locale

data class StorageInfo(
    val totalBytes: Long,
    val usedBytes: Long,
    val freeBytes: Long,
    val usedPercentage: Int
) {
    /** Free space as an exact percentage (0.0 - 100.0). */
    val freePercentage: Double
        get() = if (totalBytes > 0) {
            (freeBytes.toDouble() / totalBytes.toDouble()) * 100.0
        } else {
            0.0
        }
}

object StorageUtils {

    /**
     * Returns storage info for the primary internal storage volume.
     *
     * Uses StorageStatsManager (API 26+) instead of raw StatFs, because
     * StatFs on getExternalStorageDirectory() only reflects the size of the
     * /data user-partition. On many devices the manufacturer reserves part
     * of the flash chip for the system/cache partitions, so StatFs reports
     * a smaller "total" than the phone actually has (e.g. 49 GB instead of
     * the real 64 GB). Android Settings > Storage gets its numbers from
     * StorageStatsManager, so using the same API here makes our numbers
     * match what the user sees in Settings.
     */
    fun getStorageInfo(context: Context): StorageInfo {

        val totalBytes: Long
        val freeBytes: Long

        val storageStatsManager =
            context.getSystemService(Context.STORAGE_STATS_SERVICE)
                    as? StorageStatsManager

        if (storageStatsManager != null) {
            // StorageManager.UUID_DEFAULT represents the primary internal
            // storage volume - the same one shown in Settings > Storage.
            totalBytes = storageStatsManager.getTotalBytes(
                StorageManager.UUID_DEFAULT
            )
            freeBytes = storageStatsManager.getFreeBytes(
                StorageManager.UUID_DEFAULT
            )
        } else {
            // Fallback for the rare case the service isn't available.
            val storagePath = Environment
                .getExternalStorageDirectory()
                .path

            val statFs = StatFs(storagePath)

            totalBytes = statFs.totalBytes
            freeBytes = statFs.availableBytes
        }

        val usedBytes = totalBytes - freeBytes

        val usedPercentage = if (totalBytes > 0) {
            ((usedBytes.toDouble() / totalBytes.toDouble()) * 100)
                .toInt()
                .coerceIn(0, 100)
        } else {
            0
        }

        return StorageInfo(
            totalBytes = totalBytes,
            usedBytes = usedBytes,
            freeBytes = freeBytes,
            usedPercentage = usedPercentage
        )
    }

    fun formatStorageSize(bytes: Long): String {

        val gb = bytes / (1024.0 * 1024.0 * 1024.0)

        return if (gb >= 10) {
            String.format(Locale.US, "%.1f GB", gb)
        } else {
            String.format(Locale.US, "%.2f GB", gb)
        }
    }
}