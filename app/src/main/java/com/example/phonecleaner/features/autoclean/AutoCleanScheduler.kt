package com.example.phonecleaner.features.autoclean

import android.content.Context
import android.util.Log
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.Calendar
import java.util.concurrent.TimeUnit

object AutoCleanScheduler {

    private const val TAG = "PhoneCleanerAutoClean"

    private const val WORK_NAME =
        "phone_cleaner_auto_clean"

    fun schedule(
        context: Context,
        repeatIntervalDays: Long,
        hour: Int,
        minute: Int
    ) {

        val initialDelay =
            calculateInitialDelay(
                hour,
                minute
            )

        Log.d(TAG, "Scheduling Auto-clean")
        Log.d(TAG, "Frequency days = $repeatIntervalDays")
        Log.d(
            TAG,
            "Selected time = %02d:%02d".format(
                hour,
                minute
            )
        )
        Log.d(
            TAG,
            "Initial delay = $initialDelay ms"
        )

        val workRequest =
            PeriodicWorkRequestBuilder<AutoCleanWorker>(
                repeatIntervalDays,
                TimeUnit.DAYS
            )
                .setInitialDelay(
                    initialDelay,
                    TimeUnit.MILLISECONDS
                )
                .build()

        WorkManager
            .getInstance(context.applicationContext)
            .enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.REPLACE,
                workRequest
            )

        Log.d(
            TAG,
            "Auto-clean WorkManager request enqueued"
        )
    }

    fun observeWorkStatus(
        context: Context
    ) {

        WorkManager
            .getInstance(context.applicationContext)
            .getWorkInfosForUniqueWorkLiveData(WORK_NAME)
            .observeForever { workInfos ->

                if (workInfos.isNullOrEmpty()) {

                    Log.d(
                        TAG,
                        "WORK STATUS = NO WORK FOUND"
                    )

                    return@observeForever
                }

                workInfos.forEach { workInfo ->

                    Log.d(
                        TAG,
                        "WORK STATUS = ${workInfo.state}"
                    )

                    Log.d(
                        TAG,
                        "WORK ID = ${workInfo.id}"
                    )

                    Log.d(
                        TAG,
                        "NEXT RUN = ${workInfo.nextScheduleTimeMillis}"
                    )

                    Log.d(
                        TAG,
                        "RUN ATTEMPT COUNT = ${workInfo.runAttemptCount}"
                    )
                }
            }
    }

    private fun calculateInitialDelay(
        hour: Int,
        minute: Int
    ): Long {

        val now =
            Calendar.getInstance()

        val nextRun =
            Calendar.getInstance().apply {

                set(
                    Calendar.HOUR_OF_DAY,
                    hour
                )

                set(
                    Calendar.MINUTE,
                    minute
                )

                set(
                    Calendar.SECOND,
                    0
                )

                set(
                    Calendar.MILLISECOND,
                    0
                )

                if (timeInMillis <= now.timeInMillis) {

                    add(
                        Calendar.DAY_OF_YEAR,
                        1
                    )
                }
            }

        return nextRun.timeInMillis -
                now.timeInMillis
    }

    fun cancel(
        context: Context
    ) {

        Log.d(
            TAG,
            "Cancelling Auto-clean WorkManager request"
        )

        WorkManager
            .getInstance(context.applicationContext)
            .cancelUniqueWork(
                WORK_NAME
            )
    }


    fun runWorkerOnceForTesting(context: Context) {

        Log.d(
            TAG,
            "TEST: Enqueuing AutoCleanWorker immediately"
        )

        val workRequest =
            androidx.work.OneTimeWorkRequestBuilder<AutoCleanWorker>()
                .build()

        WorkManager
            .getInstance(context.applicationContext)
            .enqueue(workRequest)

        Log.d(
            TAG,
            "TEST: AutoCleanWorker one-time request enqueued"
        )
    }
}