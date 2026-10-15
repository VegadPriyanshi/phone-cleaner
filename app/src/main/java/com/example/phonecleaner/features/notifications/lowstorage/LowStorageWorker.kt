package com.example.phonecleaner.features.notifications.lowstorage

import android.Manifest
import android.content.Context
import android.util.Log
import androidx.annotation.RequiresPermission

import androidx.work.Worker
import androidx.work.WorkerParameters

import com.example.phonecleaner.core.prefs.AppPrefs
import com.example.phonecleaner.features.notifications.NotificationHelper
import com.example.phonecleaner.utils.StorageUtils


class LowStorageWorker(
    context: Context,
    params: WorkerParameters
) : Worker(context, params) {

    companion object {
        private const val TAG = "LowStorageWorker"
    }

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    override fun doWork(): Result {

        val context = applicationContext

        Log.d(
            TAG,
            "========== LOW STORAGE CHECK START =========="
        )


        // =========================================================
        // 1. Check whether Low Storage Alert is enabled
        // =========================================================

        val alertEnabled =
            AppPrefs.isLowStorageAlertEnabled(context)

        Log.d(
            TAG,
            "Low storage alert enabled = $alertEnabled"
        )

        if (!alertEnabled) {

            Log.d(
                TAG,
                "Low storage alert is disabled. Nothing to do."
            )

            return Result.success()
        }


        // =========================================================
        // 2. Read current storage
        // =========================================================

        val storageInfo = try {

            StorageUtils.getStorageInfo(context)

        } catch (e: Exception) {

            Log.e(
                TAG,
                "Failed to read storage information",
                e
            )

            return Result.retry()
        }


        // =========================================================
        // 3. Read threshold
        // =========================================================

        val threshold =
            AppPrefs.getLowStorageThreshold(context)

        val freePercentage =
            storageInfo.freePercentage


        Log.d(
            TAG,
            "Storage: free=$freePercentage% | " +
                    "threshold=$threshold% | " +
                    "freeBytes=${storageInfo.freeBytes}"
        )


        // =========================================================
        // 4. Storage is NOT low
        // =========================================================

        if (freePercentage >= threshold) {

            Log.d(
                TAG,
                "Storage is OK: $freePercentage% >= $threshold%"
            )

            // Allow the alert to trigger again when
            // storage becomes low in the future.
            AppPrefs.setLowStorageAlertArmed(
                context,
                true
            )

            // Remove an old low-storage notification.
            NotificationHelper.cancelLowStorageNotification(
                context
            )

            Log.d(
                TAG,
                "Old low-storage notification cancelled."
            )

            Log.d(
                TAG,
                "========== LOW STORAGE CHECK END =========="
            )

            return Result.success()
        }


        // =========================================================
        // 5. Storage IS low
        // =========================================================

        Log.d(
            TAG,
            "LOW STORAGE DETECTED: " +
                    "$freePercentage% < $threshold%"
        )


        // =========================================================
        // 6. Check notification permission
        // =========================================================

        val notificationPermission =
            NotificationHelper.hasNotificationPermission(
                context
            )

        val notificationsAllowed =
            NotificationHelper.areNotificationsAllowed(
                context
            )


        Log.d(
            TAG,
            "Notification permission = $notificationPermission"
        )

        Log.d(
            TAG,
            "Notifications allowed = $notificationsAllowed"
        )


        if (!notificationPermission) {

            Log.e(
                TAG,
                "POST_NOTIFICATIONS permission is NOT granted."
            )

            return Result.success()
        }


        if (!notificationsAllowed) {

            Log.e(
                TAG,
                "Notifications are disabled by Android."
            )

            return Result.success()
        }


        // =========================================================
        // 7. Check alert frequency / armed state
        // =========================================================

        val frequency =
            AppPrefs.getLowStorageFrequency(context)

        val armed =
            AppPrefs.isLowStorageAlertArmed(context)

        val lastAlertTime =
            AppPrefs.getLowStorageLastAlertTime(context)

        val now =
            System.currentTimeMillis()


        Log.d(
            TAG,
            "frequency=$frequency"
        )

        Log.d(
            TAG,
            "armed=$armed"
        )

        Log.d(
            TAG,
            "lastAlertTime=$lastAlertTime"
        )


        val shouldNotify =
            LowStorageAlertPolicy.shouldNotify(
                frequency = frequency,
                armed = armed,
                lastAlertTimeMillis = lastAlertTime,
                nowMillis = now
            )


        Log.d(
            TAG,
            "shouldNotify=$shouldNotify"
        )


        if (!shouldNotify) {

            Log.d(
                TAG,
                "Policy says notification should NOT be shown."
            )

            Log.d(
                TAG,
                "========== LOW STORAGE CHECK END =========="
            )

            return Result.success()
        }


        // =========================================================
        // 8. Show notification
        // =========================================================

        Log.d(
            TAG,
            "Posting LOW STORAGE notification..."
        )


        val notificationPosted =
            NotificationHelper.showLowStorageNotification(
                context,
                storageInfo
            )


        if (notificationPosted) {

            AppPrefs.setLowStorageLastAlertTime(
                context,
                now
            )

            AppPrefs.setLowStorageAlertArmed(
                context,
                false
            )

            Log.d(
                TAG,
                "Low storage notification was successfully posted."
            )

        } else {

            Log.e(
                TAG,
                "Low storage notification was NOT posted."
            )
        }


        Log.d(
            TAG,
            "Low storage notification posted."
        )

        Log.d(
            TAG,
            "========== LOW STORAGE CHECK END =========="
        )


        return Result.success()
    }
}