package com.example.phonecleaner.features.notifications.scheduler

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log

import com.example.phonecleaner.core.prefs.AppPrefs
import com.example.phonecleaner.features.notifications.receiver.CleaningReminderReceiver

import java.util.Calendar


object CleaningReminderScheduler {

    private const val TAG =
        "CleaningReminderScheduler"

    private const val REQUEST_CODE =
        3001


    // =========================================================
    // Schedule
    // =========================================================

    fun schedule(
        context: Context
    ) {

        val appContext =
            context.applicationContext


        if (
            !AppPrefs.isCleaningReminderEnabled(
                appContext
            )
        ) {
            return
        }


        cancel(appContext)


        val hour =
            AppPrefs.getAutoCleanHour(
                appContext
            )

        val minute =
            AppPrefs.getAutoCleanMinute(
                appContext
            )

        val frequency =
            AppPrefs.getAutoCleanFrequency(
                appContext
            )


        val triggerTime =
            calculateNextTriggerTime(
                hour = hour,
                minute = minute,
                frequency = frequency
            )


        val alarmManager =
            appContext.getSystemService(
                Context.ALARM_SERVICE
            ) as AlarmManager


        val pendingIntent =
            createPendingIntent(
                appContext
            )


        /*
         * Inexact alarm:
         *
         * We do not need exact timing for a normal
         * cleaning reminder.
         *
         * This also avoids requesting the special
         * SCHEDULE_EXACT_ALARM permission.
         */

        if (
            Build.VERSION.SDK_INT >=
            Build.VERSION_CODES.M
        ) {

            alarmManager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerTime,
                pendingIntent
            )

        } else {

            alarmManager.set(
                AlarmManager.RTC_WAKEUP,
                triggerTime,
                pendingIntent
            )
        }


        Log.d(
            TAG,
            "Cleaning reminder scheduled: $triggerTime"
        )
    }


    // =========================================================
    // Cancel
    // =========================================================

    fun cancel(
        context: Context
    ) {

        val appContext =
            context.applicationContext


        val alarmManager =
            appContext.getSystemService(
                Context.ALARM_SERVICE
            ) as AlarmManager


        alarmManager.cancel(
            createPendingIntent(
                appContext
            )
        )


        Log.d(
            TAG,
            "Cleaning reminder cancelled"
        )
    }


    // =========================================================
    // Reschedule after reboot / app restart
    // =========================================================

    fun rescheduleIfEnabled(
        context: Context
    ) {

        if (
            AppPrefs.isCleaningReminderEnabled(
                context
            )
        ) {

            schedule(context)
        }
    }


    // =========================================================
    // PendingIntent
    // =========================================================

    private fun createPendingIntent(
        context: Context
    ): PendingIntent {

        val intent =
            Intent(
                context,
                CleaningReminderReceiver::class.java
            ).apply {

                action =
                    CleaningReminderReceiver.ACTION_CLEANING_REMINDER
            }


        return PendingIntent.getBroadcast(
            context,
            REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or
                    PendingIntent.FLAG_IMMUTABLE
        )
    }


    // =========================================================
    // Calculate Next Trigger
    // =========================================================

    private fun calculateNextTriggerTime(
        hour: Int,
        minute: Int,
        frequency: String
    ): Long {

        val now =
            Calendar.getInstance()


        val next =
            Calendar.getInstance()


        next.set(
            Calendar.HOUR_OF_DAY,
            hour
        )

        next.set(
            Calendar.MINUTE,
            minute
        )

        next.set(
            Calendar.SECOND,
            0
        )

        next.set(
            Calendar.MILLISECOND,
            0
        )


        /*
         * If today's time has already passed,
         * start from tomorrow.
         */

        if (
            next.timeInMillis <=
            now.timeInMillis
        ) {

            next.add(
                Calendar.DAY_OF_YEAR,
                1
            )
        }


        /*
         * For a newly scheduled reminder, the first
         * notification occurs at the selected time.
         *
         * After the notification fires, the receiver
         * schedules the next occurrence according
         * to the selected frequency.
         */

        return next.timeInMillis
    }


    // =========================================================
    // Calculate Next Occurrence After Notification
    // =========================================================

    fun scheduleNextAfterNotification(
        context: Context
    ) {

        val appContext =
            context.applicationContext


        if (
            !AppPrefs.isCleaningReminderEnabled(
                appContext
            )
        ) {
            return
        }


        val frequency =
            AppPrefs.getAutoCleanFrequency(
                appContext
            )


        val hour =
            AppPrefs.getAutoCleanHour(
                appContext
            )


        val minute =
            AppPrefs.getAutoCleanMinute(
                appContext
            )


        val days =
            when (frequency) {

                "THREE_DAYS" ->
                    3

                "WEEKLY" ->
                    7

                else ->
                    1
            }


        val next =
            Calendar.getInstance()


        next.add(
            Calendar.DAY_OF_YEAR,
            days
        )


        next.set(
            Calendar.HOUR_OF_DAY,
            hour
        )

        next.set(
            Calendar.MINUTE,
            minute
        )

        next.set(
            Calendar.SECOND,
            0
        )

        next.set(
            Calendar.MILLISECOND,
            0
        )


        val alarmManager =
            appContext.getSystemService(
                Context.ALARM_SERVICE
            ) as AlarmManager


        val pendingIntent =
            createPendingIntent(
                appContext
            )


        if (
            Build.VERSION.SDK_INT >=
            Build.VERSION_CODES.M
        ) {

            alarmManager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                next.timeInMillis,
                pendingIntent
            )

        } else {

            alarmManager.set(
                AlarmManager.RTC_WAKEUP,
                next.timeInMillis,
                pendingIntent
            )
        }


        Log.d(
            TAG,
            "Next cleaning reminder scheduled: ${next.time}"
        )
    }
}