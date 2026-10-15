package com.example.phonecleaner.core.prefs

import android.content.Context

import com.example.phonecleaner.features.notifications.lowstorage.LowStorageAlertPolicy


object AppPrefs {

    private const val PREFS_NAME =
        "phone_cleaner_settings"


    private const val KEY_CLEANING_REMINDER_ENABLED =
        "cleaning_reminder_enabled"


    // =========================================================
    // Shared Preferences
    // =========================================================

    private fun prefs(
        context: Context
    ) =
        context.getSharedPreferences(
            PREFS_NAME,
            Context.MODE_PRIVATE
        )


    // =========================================================
    // Language
    // =========================================================

    private const val KEY_SELECTED_LANGUAGE =
        "selected_language"


    // =========================================================
// Onboarding
// =========================================================

    private const val KEY_ONBOARDING_COMPLETED =
        "onboarding_completed"


    fun isOnboardingCompleted(
        context: Context
    ): Boolean {

        return prefs(context)
            .getBoolean(
                KEY_ONBOARDING_COMPLETED,
                false
            )
    }


    fun setOnboardingCompleted(
        context: Context,
        completed: Boolean
    ) {

        prefs(context)
            .edit()
            .putBoolean(
                KEY_ONBOARDING_COMPLETED,
                completed
            )
            .commit()
    }

    private const val KEY_NOTIFICATION_DEFAULTS_MIGRATION =
        "notification_defaults_migration_v1"


    var Context.selectedLanguageCode: String?
        get() =
            prefs(this)
                .getString(
                    KEY_SELECTED_LANGUAGE,
                    null
                )

        set(value) {

            prefs(this)
                .edit()
                .putString(
                    KEY_SELECTED_LANGUAGE,
                    value
                )
                .apply()
        }


    // =========================================================
    // Auto-clean Schedule
    // =========================================================

    private const val KEY_AUTO_CLEAN_ENABLED =
        "auto_clean_enabled"

    private const val KEY_AUTO_CLEAN_FREQUENCY =
        "auto_clean_frequency"

    private const val KEY_AUTO_CLEAN_HOUR =
        "auto_clean_hour"

    private const val KEY_AUTO_CLEAN_MINUTE =
        "auto_clean_minute"


    fun isAutoCleanEnabled(
        context: Context
    ): Boolean {

        return prefs(context)
            .getBoolean(
                KEY_AUTO_CLEAN_ENABLED,
                false
            )
    }


    fun setAutoCleanEnabled(
        context: Context,
        enabled: Boolean
    ) {

        prefs(context)
            .edit()
            .putBoolean(
                KEY_AUTO_CLEAN_ENABLED,
                enabled
            )
            .apply()
    }


    fun getAutoCleanFrequency(
        context: Context
    ): String {

        return prefs(context)
            .getString(
                KEY_AUTO_CLEAN_FREQUENCY,
                "DAILY"
            )
            ?: "DAILY"
    }


    fun setAutoCleanFrequency(
        context: Context,
        frequency: String
    ) {

        prefs(context)
            .edit()
            .putString(
                KEY_AUTO_CLEAN_FREQUENCY,
                frequency
            )
            .apply()
    }


    fun getAutoCleanHour(
        context: Context
    ): Int {

        return prefs(context)
            .getInt(
                KEY_AUTO_CLEAN_HOUR,
                22
            )
    }


    fun getAutoCleanMinute(
        context: Context
    ): Int {

        return prefs(context)
            .getInt(
                KEY_AUTO_CLEAN_MINUTE,
                0
            )
    }


    fun setAutoCleanTime(
        context: Context,
        hour: Int,
        minute: Int
    ) {

        prefs(context)
            .edit()
            .putInt(
                KEY_AUTO_CLEAN_HOUR,
                hour
            )
            .putInt(
                KEY_AUTO_CLEAN_MINUTE,
                minute
            )
            .apply()
    }


    // =========================================================
    // Enable Notifications
    // =========================================================

    private const val KEY_NOTIFICATIONS_ENABLED =
        "notifications_enabled"


    fun areNotificationsEnabled(
        context: Context
    ): Boolean {

        return prefs(context)
            .getBoolean(
                KEY_NOTIFICATIONS_ENABLED,
                false
            )
    }


    fun setNotificationsEnabled(
        context: Context,
        enabled: Boolean
    ) {

        prefs(context)
            .edit()
            .putBoolean(
                KEY_NOTIFICATIONS_ENABLED,
                enabled
            )
            .apply()
    }


    // =========================================================
    // Run on Startup
    // =========================================================

    private const val KEY_RUN_ON_STARTUP =
        "run_on_startup"


    fun isRunOnStartupEnabled(
        context: Context
    ): Boolean {

        return prefs(context)
            .getBoolean(
                KEY_RUN_ON_STARTUP,
                false
            )
    }


    fun setRunOnStartupEnabled(
        context: Context,
        enabled: Boolean
    ) {

        prefs(context)
            .edit()
            .putBoolean(
                KEY_RUN_ON_STARTUP,
                enabled
            )
            .apply()
    }


    // =========================================================
    // Low Storage Alert
    // =========================================================

    private const val KEY_LOW_STORAGE_ENABLED =
        "low_storage_alert_enabled"

    private const val KEY_LOW_STORAGE_THRESHOLD =
        "low_storage_alert_threshold"

    private const val KEY_LOW_STORAGE_FREQUENCY =
        "low_storage_alert_frequency"

    private const val KEY_LOW_STORAGE_LAST_ALERT =
        "low_storage_alert_last_time"

    private const val KEY_LOW_STORAGE_ARMED =
        "low_storage_alert_armed"


    fun isLowStorageAlertEnabled(
        context: Context
    ): Boolean =
        prefs(context).getBoolean(
            KEY_LOW_STORAGE_ENABLED,
            false
        )


    fun setLowStorageAlertEnabled(
        context: Context,
        enabled: Boolean
    ) {

        prefs(context)
            .edit()
            .putBoolean(
                KEY_LOW_STORAGE_ENABLED,
                enabled
            )
            .apply()
    }


    /**
     * Alert when FREE storage is below this percentage.
     */
    fun getLowStorageThreshold(
        context: Context
    ): Int {

        val saved =
            prefs(context).getInt(
                KEY_LOW_STORAGE_THRESHOLD,
                LowStorageAlertPolicy.DEFAULT_THRESHOLD
            )

        val step =
            LowStorageAlertPolicy.THRESHOLD_STEP

        return ((saved / step) * step).coerceIn(
            LowStorageAlertPolicy.MIN_THRESHOLD,
            LowStorageAlertPolicy.MAX_THRESHOLD
        )
    }


    fun setLowStorageThreshold(
        context: Context,
        percent: Int
    ) {

        prefs(context)
            .edit()
            .putInt(
                KEY_LOW_STORAGE_THRESHOLD,
                percent
            )
            .apply()
    }


    /**
     * ONCE, DAILY or WEEKLY.
     */
    fun getLowStorageFrequency(
        context: Context
    ): String =
        prefs(context).getString(
            KEY_LOW_STORAGE_FREQUENCY,
            LowStorageAlertPolicy.FREQ_DAILY
        )
            ?: LowStorageAlertPolicy.FREQ_DAILY


    fun setLowStorageFrequency(
        context: Context,
        frequency: String
    ) {

        prefs(context)
            .edit()
            .putString(
                KEY_LOW_STORAGE_FREQUENCY,
                frequency
            )
            .apply()
    }


    fun getLowStorageLastAlertTime(
        context: Context
    ): Long =
        prefs(context).getLong(
            KEY_LOW_STORAGE_LAST_ALERT,
            0L
        )


    fun setLowStorageLastAlertTime(
        context: Context,
        timeMillis: Long
    ) {

        prefs(context)
            .edit()
            .putLong(
                KEY_LOW_STORAGE_LAST_ALERT,
                timeMillis
            )
            .apply()
    }


    /**
     * Used by the "Once" frequency.
     */
    fun isLowStorageAlertArmed(
        context: Context
    ): Boolean =
        prefs(context).getBoolean(
            KEY_LOW_STORAGE_ARMED,
            true
        )


    fun setLowStorageAlertArmed(
        context: Context,
        armed: Boolean
    ) {

        prefs(context)
            .edit()
            .putBoolean(
                KEY_LOW_STORAGE_ARMED,
                armed
            )
            .apply()
    }


    // =========================================================
    // Unused Apps Reminder
    // =========================================================

    private const val KEY_UNUSED_APPS_ENABLED =
        "unused_apps_reminder_enabled"

    private const val KEY_UNUSED_APPS_DAYS =
        "unused_apps_days"

    private const val KEY_UNUSED_APPS_FREQUENCY =
        "unused_apps_frequency"

    private const val KEY_UNUSED_APPS_HOUR =
        "unused_apps_hour"

    private const val KEY_UNUSED_APPS_MINUTE =
        "unused_apps_minute"


    /**
     * Whether the Unused Apps Reminder is enabled.
     *
     * Default:
     * OFF
     */
    fun isUnusedAppsReminderEnabled(
        context: Context
    ): Boolean =
        prefs(context).getBoolean(
            KEY_UNUSED_APPS_ENABLED,
            false
        )


    fun setUnusedAppsReminderEnabled(
        context: Context,
        enabled: Boolean
    ) {

        prefs(context)
            .edit()
            .putBoolean(
                KEY_UNUSED_APPS_ENABLED,
                enabled
            )
            .apply()
    }


    // =========================================================
    // Unused Apps Threshold
    // =========================================================

    /**
     * Number of days an app must remain unused
     * before it can be included in the reminder.
     *
     * Default:
     * 60 days
     */
    fun getUnusedAppsDays(
        context: Context
    ): Int =
        prefs(context).getInt(
            KEY_UNUSED_APPS_DAYS,
            60
        )


    fun setUnusedAppsDays(
        context: Context,
        days: Int
    ) {

        prefs(context)
            .edit()
            .putInt(
                KEY_UNUSED_APPS_DAYS,
                days
            )
            .apply()
    }


    // =========================================================
    // Unused Apps Frequency
    // =========================================================

    /**
     * Supported values:
     *
     * WEEKLY
     * MONTHLY
     */
    fun getUnusedAppsFrequency(
        context: Context
    ): String =
        prefs(context).getString(
            KEY_UNUSED_APPS_FREQUENCY,
            "WEEKLY"
        )
            ?: "WEEKLY"


    fun setUnusedAppsFrequency(
        context: Context,
        frequency: String
    ) {

        prefs(context)
            .edit()
            .putString(
                KEY_UNUSED_APPS_FREQUENCY,
                frequency
            )
            .apply()
    }


    // =========================================================
    // Unused Apps Reminder Time
    // =========================================================

    fun getUnusedAppsHour(
        context: Context
    ): Int =
        prefs(context).getInt(
            KEY_UNUSED_APPS_HOUR,
            9
        )


    fun getUnusedAppsMinute(
        context: Context
    ): Int =
        prefs(context).getInt(
            KEY_UNUSED_APPS_MINUTE,
            0
        )


    fun setUnusedAppsTime(
        context: Context,
        hour: Int,
        minute: Int
    ) {

        prefs(context)
            .edit()
            .putInt(
                KEY_UNUSED_APPS_HOUR,
                hour
            )
            .putInt(
                KEY_UNUSED_APPS_MINUTE,
                minute
            )
            .apply()
    }


    // =========================================================
    // Reset All Settings
    // =========================================================

    fun resetSettingsToDefaults(
        context: Context
    ) {

        prefs(context)
            .edit()

            // ---------------------------------------------
            // Language
            // ---------------------------------------------

            .putString(
                KEY_SELECTED_LANGUAGE,
                "en"
            )

            .putBoolean(
                KEY_ONBOARDING_COMPLETED,
                false
            )


            // ---------------------------------------------
            // Auto-clean
            // ---------------------------------------------

            .putBoolean(
                KEY_AUTO_CLEAN_ENABLED,
                false
            )

            .putString(
                KEY_AUTO_CLEAN_FREQUENCY,
                "DAILY"
            )

            .putInt(
                KEY_AUTO_CLEAN_HOUR,
                22
            )

            .putInt(
                KEY_AUTO_CLEAN_MINUTE,
                0
            )


            // ---------------------------------------------
            // Notifications
            // ---------------------------------------------

            .putBoolean(
                KEY_NOTIFICATIONS_ENABLED,
                false
            )

            .putBoolean(
                KEY_CLEANING_REMINDER_ENABLED,
                false
            )


            // ---------------------------------------------
            // Low Storage Alert
            // ---------------------------------------------

            .putBoolean(
                KEY_LOW_STORAGE_ENABLED,
                false
            )

            .putInt(
                KEY_LOW_STORAGE_THRESHOLD,
                LowStorageAlertPolicy.DEFAULT_THRESHOLD
            )

            .putString(
                KEY_LOW_STORAGE_FREQUENCY,
                LowStorageAlertPolicy.FREQ_DAILY
            )

            .putLong(
                KEY_LOW_STORAGE_LAST_ALERT,
                0L
            )

            .putBoolean(
                KEY_LOW_STORAGE_ARMED,
                true
            )


            // ---------------------------------------------
            // Unused Apps Reminder
            // ---------------------------------------------

            .putBoolean(
                KEY_UNUSED_APPS_ENABLED,
                false
            )

            .putInt(
                KEY_UNUSED_APPS_DAYS,
                60
            )

            .putString(
                KEY_UNUSED_APPS_FREQUENCY,
                "WEEKLY"
            )

            .putInt(
                KEY_UNUSED_APPS_HOUR,
                9
            )

            .putInt(
                KEY_UNUSED_APPS_MINUTE,
                0
            )


            // ---------------------------------------------
            // Run on Startup
            // ---------------------------------------------

            .putBoolean(
                KEY_RUN_ON_STARTUP,
                false
            )

            .apply()
    }

    // =========================================================
    // Cleaning Reminder
    // =========================================================

    fun isCleaningReminderEnabled(
        context: Context
    ): Boolean =
        prefs(context).getBoolean(
            KEY_CLEANING_REMINDER_ENABLED,
            false
        )


    fun setCleaningReminderEnabled(
        context: Context,
        enabled: Boolean
    ) {

        prefs(context)
            .edit()
            .putBoolean(
                KEY_CLEANING_REMINDER_ENABLED,
                enabled
            )
            .apply()
    }

    fun initializeNotificationDefaults(context: Context) {

        val preferences = prefs(context)

        // Already initialized.
        if (
            preferences.getBoolean(
                KEY_NOTIFICATION_DEFAULTS_MIGRATION,
                false
            )
        ) {
            return
        }

        preferences
            .edit()
            .putBoolean(
                KEY_LOW_STORAGE_ENABLED,
                false
            )
            .putBoolean(
                KEY_UNUSED_APPS_ENABLED,
                false
            )
            .putBoolean(
                KEY_NOTIFICATION_DEFAULTS_MIGRATION,
                true
            )
            .apply()
    }
}