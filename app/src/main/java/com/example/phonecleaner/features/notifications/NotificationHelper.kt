package com.example.phonecleaner.features.notifications

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log

import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat

import com.example.phonecleaner.MainActivity
import com.example.phonecleaner.R
import com.example.phonecleaner.utils.StorageInfo
import com.example.phonecleaner.utils.StorageUtils

import kotlin.math.roundToInt


object NotificationHelper {

    // =========================================================
    // Notification Channel
    // =========================================================

    const val CHANNEL_ID =
        "phone_cleaner_notifications"

    private const val CHANNEL_NAME =
        "Phone Cleaner"

    private const val CHANNEL_DESCRIPTION =
        "Cleaning reminders and storage alerts"


    // =========================================================
    // Notification IDs
    // =========================================================

    private const val TEST_NOTIFICATION_ID = 1001

    private const val CLEANING_REMINDER_NOTIFICATION_ID = 2001

    private const val LOW_STORAGE_NOTIFICATION_ID = 4001


    // =========================================================
    // PendingIntent Request Codes
    // =========================================================

    private const val REQUEST_OPEN_APP = 4101

    private const val REQUEST_CLEAN_NOW = 4102


    // =========================================================
    // Create Notification Channel
    // =========================================================

    fun createNotificationChannel(
        context: Context
    ) {

        if (
            Build.VERSION.SDK_INT <
            Build.VERSION_CODES.O
        ) {
            return
        }


        val channel =
            NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {

                description =
                    CHANNEL_DESCRIPTION
            }


        val notificationManager =
            context.getSystemService(
                Context.NOTIFICATION_SERVICE
            ) as NotificationManager


        notificationManager.createNotificationChannel(
            channel
        )


        Log.d(
            "NotificationHelper",
            "Notification channel created/verified."
        )
    }


    // =========================================================
    // Check Android Notification Permission
    // =========================================================

    fun hasNotificationPermission(
        context: Context
    ): Boolean {

        if (
            Build.VERSION.SDK_INT >=
            Build.VERSION_CODES.TIRAMISU
        ) {

            return ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        }


        return true
    }


    // =========================================================
    // Check Whether Notifications Are Enabled
    // =========================================================

    fun areNotificationsAllowed(
        context: Context
    ): Boolean {

        return NotificationManagerCompat
            .from(context)
            .areNotificationsEnabled()
    }


    // =========================================================
    // Check Everything
    // =========================================================

    fun canSendNotifications(
        context: Context
    ): Boolean {

        return hasNotificationPermission(context) &&
                areNotificationsAllowed(context)
    }


    // =========================================================
    // Test Notification
    // =========================================================

    @RequiresPermission(
        Manifest.permission.POST_NOTIFICATIONS
    )
    fun showTestNotification(
        context: Context
    ) {

        // Make sure the channel exists.
        createNotificationChannel(context)


        if (!canSendNotifications(context)) {

            Log.e(
                "NotificationHelper",
                "Test notification cannot be sent."
            )

            Log.e(
                "NotificationHelper",
                "POST_NOTIFICATIONS = " +
                        hasNotificationPermission(context)
            )

            Log.e(
                "NotificationHelper",
                "Notifications enabled = " +
                        areNotificationsAllowed(context)
            )

            return
        }


        val notification =
            NotificationCompat.Builder(
                context,
                CHANNEL_ID
            )
                .setSmallIcon(
                    android.R.drawable.ic_dialog_info
                )
                .setContentTitle(
                    "Phone Cleaner"
                )
                .setContentText(
                    "Notifications are now enabled."
                )
                .setPriority(
                    NotificationCompat.PRIORITY_HIGH
                )
                .setAutoCancel(true)
                .build()


        NotificationManagerCompat
            .from(context)
            .notify(
                TEST_NOTIFICATION_ID,
                notification
            )


        Log.d(
            "NotificationHelper",
            "Test notification sent successfully."
        )
    }


    // =========================================================
    // Cleaning Reminder Notification
    // =========================================================

    @RequiresPermission(
        Manifest.permission.POST_NOTIFICATIONS
    )
    fun showCleaningReminderNotification(
        context: Context
    ) {

        // Make sure the channel exists.
        createNotificationChannel(context)


        if (!canSendNotifications(context)) {

            Log.e(
                "NotificationHelper",
                "Cleaning reminder cannot be sent."
            )

            return
        }


        val notification =
            NotificationCompat.Builder(
                context,
                CHANNEL_ID
            )
                .setSmallIcon(
                    android.R.drawable.ic_dialog_info
                )
                .setContentTitle(
                    "Time to clean your phone"
                )
                .setContentText(
                    "Free up space by removing unnecessary files."
                )
                .setStyle(
                    NotificationCompat.BigTextStyle()
                        .bigText(
                            "Your cleaning reminder is here. " +
                                    "Open Phone Cleaner and scan your phone " +
                                    "to see what can be safely removed."
                        )
                )
                .setPriority(
                    NotificationCompat.PRIORITY_HIGH
                )
                .setAutoCancel(true)
                .build()


        NotificationManagerCompat
            .from(context)
            .notify(
                CLEANING_REMINDER_NOTIFICATION_ID,
                notification
            )


        Log.d(
            "NotificationHelper",
            "Cleaning reminder notification sent successfully."
        )
    }


    // =========================================================
    // Create Open App PendingIntent
    // =========================================================

    private fun createOpenAppPendingIntent(
        context: Context,
        requestCode: Int,
        openTab: String?
    ): PendingIntent {

        val intent =
            Intent(
                context,
                MainActivity::class.java
            ).apply {

                flags =
                    Intent.FLAG_ACTIVITY_NEW_TASK or
                            Intent.FLAG_ACTIVITY_CLEAR_TASK


                if (openTab != null) {

                    putExtra(
                        MainActivity.EXTRA_OPEN_TAB,
                        openTab
                    )
                }
            }


        return PendingIntent.getActivity(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or
                    PendingIntent.FLAG_IMMUTABLE
        )
    }


    // =========================================================
    // Low Storage Notification
    // =========================================================

    @RequiresPermission(
        Manifest.permission.POST_NOTIFICATIONS
    )
    fun showLowStorageNotification(
        context: Context,
        storageInfo: StorageInfo
    ): Boolean {

        val tag = "NotificationHelper"

        Log.d(
            tag,
            "========== LOW STORAGE NOTIFICATION START =========="
        )


        // =========================================================
        // 1. Create / verify notification channel
        // =========================================================

        createNotificationChannel(context)


        // =========================================================
        // 2. Check permission
        // =========================================================

        val permissionGranted =
            hasNotificationPermission(context)

        val notificationsEnabled =
            areNotificationsAllowed(context)


        Log.d(
            tag,
            "POST_NOTIFICATIONS = $permissionGranted"
        )

        Log.d(
            tag,
            "Notifications enabled = $notificationsEnabled"
        )


        if (!permissionGranted) {

            Log.e(
                tag,
                "POST_NOTIFICATIONS permission is NOT granted."
            )

            return false
        }


        if (!notificationsEnabled) {

            Log.e(
                tag,
                "Notifications are disabled by Android."
            )

            return false
        }


        // =========================================================
        // 3. Check notification channel
        // =========================================================

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            val notificationManager =
                context.getSystemService(
                    Context.NOTIFICATION_SERVICE
                ) as NotificationManager


            val channel =
                notificationManager.getNotificationChannel(
                    CHANNEL_ID
                )


            if (channel == null) {

                Log.e(
                    tag,
                    "Notification channel does NOT exist."
                )

                return false
            }


            Log.d(
                tag,
                "Channel ID = ${channel.id}"
            )

            Log.d(
                tag,
                "Channel importance = ${channel.importance}"
            )

            Log.d(
                tag,
                "Channel name = ${channel.name}"
            )


            if (
                channel.importance ==
                NotificationManager.IMPORTANCE_NONE
            ) {

                Log.e(
                    tag,
                    "Notification channel is BLOCKED by Android."
                )

                return false
            }
        }


        // =========================================================
        // 4. Storage information
        // =========================================================

        val freePercent =
            storageInfo.freePercentage.roundToInt()


        val freeText =
            StorageUtils.formatStorageSize(
                storageInfo.freeBytes
            )


        val message =
            "Only $freePercent% free ($freeText). " +
                    "Clear junk to get space back."


        Log.d(
            tag,
            "Notification message = $message"
        )


        // =========================================================
        // 5. Build notification
        // =========================================================

        val notification =
            NotificationCompat.Builder(
                context,
                CHANNEL_ID
            )
                .setSmallIcon(
                    R.drawable.ic_storage
                )
                .setContentTitle(
                    "Storage is running low"
                )
                .setContentText(
                    message
                )
                .setStyle(
                    NotificationCompat.BigTextStyle()
                        .bigText(message)
                )
                .setContentIntent(
                    createOpenAppPendingIntent(
                        context,
                        REQUEST_OPEN_APP,
                        null
                    )
                )
                .addAction(
                    0,
                    "Clean now",
                    createOpenAppPendingIntent(
                        context,
                        REQUEST_CLEAN_NOW,
                        MainActivity.TAB_CLEAN
                    )
                )
                .setPriority(
                    NotificationCompat.PRIORITY_HIGH
                )
                .setAutoCancel(true)
                .build()


        // =========================================================
        // 6. Post notification
        // =========================================================

        return try {

            NotificationManagerCompat
                .from(context)
                .notify(
                    LOW_STORAGE_NOTIFICATION_ID,
                    notification
                )


            Log.d(
                tag,
                "NotificationManagerCompat.notify() completed."
            )

            Log.d(
                tag,
                "Low storage notification sent successfully."
            )

            Log.d(
                tag,
                "========== LOW STORAGE NOTIFICATION END =========="
            )

            true

        } catch (e: SecurityException) {

            Log.e(
                tag,
                "SecurityException while posting notification.",
                e
            )

            false

        } catch (e: Exception) {

            Log.e(
                tag,
                "Exception while posting notification.",
                e
            )

            false
        }
    }

    // =========================================================
    // Cancel Low Storage Notification
    // =========================================================

    fun cancelLowStorageNotification(
        context: Context
    ) {

        NotificationManagerCompat
            .from(context)
            .cancel(
                LOW_STORAGE_NOTIFICATION_ID
            )


        Log.d(
            "NotificationHelper",
            "Low storage notification cancelled."
        )
    }
}