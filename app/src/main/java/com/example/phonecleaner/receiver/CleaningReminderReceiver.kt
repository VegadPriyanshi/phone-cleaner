package com.example.phonecleaner.features.notifications.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

import com.example.phonecleaner.features.notifications.NotificationHelper
import com.example.phonecleaner.features.notifications.scheduler.CleaningReminderScheduler


class CleaningReminderReceiver :
    BroadcastReceiver() {

    companion object {

        const val ACTION_CLEANING_REMINDER =
            "com.example.phonecleaner.ACTION_CLEANING_REMINDER"
    }


    override fun onReceive(
        context: Context,
        intent: Intent?
    ) {

        if (
            intent?.action !=
            ACTION_CLEANING_REMINDER
        ) {
            return
        }


        /*
         * If the user turned the reminder OFF,
         * do not show anything.
         */

        if (
            !com.example.phonecleaner.core.prefs.AppPrefs
                .isCleaningReminderEnabled(
                    context
                )
        ) {
            return
        }


        /*
         * Notification permission / system notification
         * state must still allow notifications.
         */

        if (
            !NotificationHelper.canSendNotifications(
                context
            )
        ) {

            return
        }


        /*
         * Show the actual cleaning reminder.
         */

        NotificationHelper.showCleaningReminderNotification(
            context
        )


        /*
         * Schedule the next reminder.
         */

        CleaningReminderScheduler
            .scheduleNextAfterNotification(
                context
            )
    }
}