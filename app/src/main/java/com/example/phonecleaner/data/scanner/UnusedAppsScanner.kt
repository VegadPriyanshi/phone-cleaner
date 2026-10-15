package com.example.phonecleaner.data.scanner

import android.content.Context
import com.example.phonecleaner.data.model.InstalledApp
import com.example.phonecleaner.utils.UsageAccessUtils

object UnusedAppsScanner {

    fun findUnusedApps(
        context: Context,
        unusedDays: Int
    ): List<InstalledApp> {

        /*
         * Usage Access is required because Android provides
         * last-used information through UsageStatsManager.
         */
        if (
            !UsageAccessUtils.hasUsageAccess(
                context
            )
        ) {
            return emptyList()
        }

        /*
         * Reuse the existing installed-app scanner.
         *
         * We do NOT create another installed-app scanner.
         */
        val installedApps =
            InstalledAppScanner.scan(
                context
            )

        val currentTime =
            System.currentTimeMillis()

        val thresholdMillis =
            unusedDays.toLong() *
                    24L *
                    60L *
                    60L *
                    1000L

        val cutoffTime =
            currentTime -
                    thresholdMillis

        /*
         * Only return user-installed apps that have
         * actually recorded a last-used timestamp and
         * were last used before the cutoff.
         */
        return installedApps
            .filter { app ->

                // Do not show normal system apps.
                !app.isSystemApp &&

                        // Do not show updated system apps.
                        !app.isUpdatedSystemApp &&

                        /*
                         * 0 means Android has no usage
                         * information for this application.
                         */
                        app.lastUsedTime > 0L &&

                        /*
                         * App has not been used for the
                         * configured number of days.
                         */
                        app.lastUsedTime < cutoffTime
            }
            .sortedBy {
                it.lastUsedTime
            }
    }
}