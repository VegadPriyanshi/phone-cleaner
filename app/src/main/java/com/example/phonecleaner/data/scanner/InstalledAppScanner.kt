package com.example.phonecleaner.data.scanner

import android.app.usage.StorageStatsManager
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.pm.ApplicationInfo
import android.os.Process
import android.os.storage.StorageManager
import com.example.phonecleaner.data.model.InstalledApp

object InstalledAppScanner {

    fun scan(context: Context): List<InstalledApp> {

        val packageManager = context.packageManager



        val storageStatsManager =
            context.getSystemService(
                Context.STORAGE_STATS_SERVICE
            ) as StorageStatsManager

        val usageStatsManager =
            context.getSystemService(
                Context.USAGE_STATS_SERVICE
            ) as UsageStatsManager

        val storageUuid =
            StorageManager.UUID_DEFAULT

        val usageStatsMap =
            getLastUsedTimes(usageStatsManager)

        return packageManager
            .getInstalledApplications(0)
            .map { applicationInfo ->

                val appName =
                    packageManager
                        .getApplicationLabel(applicationInfo)
                        .toString()

                val isSystemApp =
                    (applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0




                val sizeBytes =
                    getAppSize(
                        storageStatsManager = storageStatsManager,
                        storageUuid = storageUuid,
                        applicationInfo = applicationInfo
                    )

                val lastUsedTime =
                    usageStatsMap[applicationInfo.packageName] ?: 0L

                InstalledApp(
                    packageName = applicationInfo.packageName,
                    appName = appName,
                    sizeBytes = sizeBytes,
                    lastUsedTime = lastUsedTime,
                    isSystemApp = isSystemApp,
                    isUpdatedSystemApp =
                        (applicationInfo.flags and ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0
                )
            }
            .sortedBy {
                it.appName.lowercase()
            }
    }

    private fun getAppSize(
        storageStatsManager: StorageStatsManager,
        storageUuid: java.util.UUID,
        applicationInfo: ApplicationInfo
    ): Long {

        return try {

            val storageStats =
                storageStatsManager.queryStatsForPackage(
                    storageUuid,
                    applicationInfo.packageName,
                    Process.myUserHandle()
                )

            storageStats.appBytes +
                    storageStats.dataBytes +
                    storageStats.cacheBytes

        } catch (_: Exception) {

            0L
        }
    }

    private fun getLastUsedTimes(
        usageStatsManager: UsageStatsManager
    ): Map<String, Long> {

        val endTime = System.currentTimeMillis()

        // Look back one year for app usage.
        val startTime =
            endTime - (365L * 24L * 60L * 60L * 1000L)

        return try {

            usageStatsManager
                .queryAndAggregateUsageStats(
                    startTime,
                    endTime
                )
                .mapValues { (_, usageStats) ->
                    usageStats.lastTimeUsed
                }

        } catch (_: Exception) {

            emptyMap()
        }
    }
}