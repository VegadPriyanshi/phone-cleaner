package com.example.phonecleaner.features.notifications.lowstorage

import java.util.concurrent.TimeUnit

/**
 * Decides whether a low storage notification should be shown now.
 * Pure logic (no Android classes) so it is easy to unit test.
 */
object LowStorageAlertPolicy {

    const val FREQ_ONCE = "ONCE"
    const val FREQ_DAILY = "DAILY"
    const val FREQ_WEEKLY = "WEEKLY"

    const val MIN_THRESHOLD = 5
    const val MAX_THRESHOLD = 50
    const val THRESHOLD_STEP = 5
    const val DEFAULT_THRESHOLD = 15

    /**
     * Call this only when free storage is already below the threshold.
     *
     * ONCE   -> one alert per "drop". It is armed again when free space
     *           goes back above the threshold, or the user changes settings.
     * DAILY  -> at most one alert every 24 hours while storage stays low.
     * WEEKLY -> at most one alert every 7 days while storage stays low.
     */
    fun shouldNotify(
        frequency: String,
        armed: Boolean,
        lastAlertTimeMillis: Long,
        nowMillis: Long
    ): Boolean {

        val elapsed = nowMillis - lastAlertTimeMillis

        return when (frequency) {

            FREQ_ONCE ->
                armed

            FREQ_WEEKLY ->
                lastAlertTimeMillis == 0L ||
                        elapsed >= TimeUnit.DAYS.toMillis(7)

            else ->
                lastAlertTimeMillis == 0L ||
                        elapsed >= TimeUnit.DAYS.toMillis(1)
        }
    }
}
