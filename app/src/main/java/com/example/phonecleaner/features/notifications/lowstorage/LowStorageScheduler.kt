package com.example.phonecleaner.features.notifications.lowstorage

import android.content.Context

import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager

import com.example.phonecleaner.core.prefs.AppPrefs

import java.util.concurrent.TimeUnit


object LowStorageScheduler {

    // =========================================================
    // Unique Work Names
    // =========================================================

    private const val WORK_NAME_PERIODIC =
        "phone_cleaner_low_storage_check"

    private const val WORK_NAME_NOW =
        "phone_cleaner_low_storage_check_now"


    // =========================================================
    // Background Check Interval
    // =========================================================

    /*
     * Check storage every 6 hours.
     *
     * WorkManager survives app restarts and device reboots,
     * so a BOOT_COMPLETED receiver is not required for this
     * periodic work.
     */
    private const val CHECK_INTERVAL_HOURS = 6L


    // =========================================================
    // Schedule Periodic Check
    // =========================================================

    fun schedule(
        context: Context
    ) {

        val applicationContext =
            context.applicationContext

        val workManager =
            WorkManager.getInstance(
                applicationContext
            )


        val request =
            PeriodicWorkRequestBuilder<LowStorageWorker>(
                CHECK_INTERVAL_HOURS,
                TimeUnit.HOURS
            )
                .build()


        /*
         * KEEP is important here.
         *
         * If the periodic work already exists,
         * WorkManager keeps the existing work instead
         * of creating another periodic worker.
         */
        workManager.enqueueUniquePeriodicWork(
            WORK_NAME_PERIODIC,
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )
    }


    // =========================================================
    // Cancel All Low Storage Work
    // =========================================================

    fun cancel(
        context: Context
    ) {

        val applicationContext =
            context.applicationContext

        val workManager =
            WorkManager.getInstance(
                applicationContext
            )


        // Cancel periodic storage checks.
        workManager.cancelUniqueWork(
            WORK_NAME_PERIODIC
        )


        // Cancel any currently queued immediate check.
        workManager.cancelUniqueWork(
            WORK_NAME_NOW
        )
    }


    // =========================================================
    // Run One Check Immediately
    // =========================================================

    fun checkNow(
        context: Context
    ) {

        val applicationContext =
            context.applicationContext

        val workManager =
            WorkManager.getInstance(
                applicationContext
            )


        val request =
            OneTimeWorkRequestBuilder<LowStorageWorker>()
                .build()


        /*
         * KEEP prevents multiple immediate checks from
         * being queued/replaced when the user changes
         * settings several times quickly.
         *
         * The periodic worker remains untouched.
         */
        workManager.enqueueUniqueWork(
            WORK_NAME_NOW,
            ExistingWorkPolicy.KEEP,
            request
        )
    }


    // =========================================================
    // Restore Periodic Work If Enabled
    // =========================================================

    fun rescheduleIfEnabled(
        context: Context
    ) {

        if (
            AppPrefs.isLowStorageAlertEnabled(
                context
            )
        ) {

            schedule(
                context
            )
        }
    }
}