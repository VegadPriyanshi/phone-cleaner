package com.example.phonecleaner.features.notifications.ui

import android.app.AppOpsManager
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.text.format.DateFormat
import android.view.View
import android.content.res.ColorStateList
import android.graphics.Color
import android.widget.RadioButton
import androidx.core.widget.CompoundButtonCompat
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.phonecleaner.R

import com.example.phonecleaner.core.prefs.AppPrefs
import com.example.phonecleaner.data.scanner.UnusedAppsScanner
import com.example.phonecleaner.databinding.ActivityUnusedAppsSettingsBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder

import java.util.Calendar


class UnusedAppsSettingsActivity : AppCompatActivity() {

    private lateinit var binding:
            ActivityUnusedAppsSettingsBinding


    /*
     * TRUE while the unused-app scanner is running.
     *
     * Current Check must NOT open the Unused Apps screen
     * while this is true.
     */
    private var isCheckingUnusedApps = false


    /*
     * Prevents the switch listener from running while
     * settings are being loaded.
     */
    private var isLoadingSettings = false


    /*
     * Small local cache for the last completed result.
     *
     * This is intentionally separate from AppPrefs because
     * AppPrefs already handles the feature settings.
     *
     * We use this cache so that returning from the Unused Apps
     * screen does NOT start another scan.
     */
    private val resultPrefs by lazy {

        getSharedPreferences(
            "unused_apps_last_result",
            Context.MODE_PRIVATE
        )
    }


    companion object {

        private const val KEY_LAST_COUNT =
            "last_unused_apps_count"

        private const val KEY_LAST_DAYS =
            "last_unused_apps_days"

        private const val KEY_HAS_RESULT =
            "has_unused_apps_result"
    }


    // =========================================================
    // onCreate
    // =========================================================

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {

        super.onCreate(
            savedInstanceState
        )


        binding =
            ActivityUnusedAppsSettingsBinding.inflate(
                layoutInflater
            )


        setContentView(
            binding.root
        )


        setupWindowInsets()

        setupToolbar()

        loadSettings()

        setupSwitch()

        setupRows()

        setupUsageAccessButton()

        refreshUsageAccessStatus()


        /*
         * IMPORTANT:
         *
         * Do NOT call refreshUnusedAppsCount() from onResume().
         *
         * We only start the first check here when the settings
         * screen is actually created.
         *
         * When the user opens Unused Apps and presses Back,
         * this Activity is resumed, but no new scan is started.
         */
        if (
            AppPrefs.isUnusedAppsReminderEnabled(
                this
            )
        ) {

            showLastCompletedResult()

            startUnusedAppsCheck()
        }
    }


    // =========================================================
    // onResume
    // =========================================================

    override fun onResume() {

        super.onResume()


        if (!::binding.isInitialized) {

            return
        }


        /*
         * Refresh only the Usage Access UI.
         *
         * DO NOT start unused-app scanning here.
         *
         * This is the important fix for:
         *
         * Unused Apps
         *      ↓
         * Back
         *      ↓
         * Current Check starts scanning again
         */
        refreshUsageAccessStatus()


        /*
         * If the feature is OFF, keep the card disabled.
         */
        if (
            !AppPrefs.isUnusedAppsReminderEnabled(
                this
            )
        ) {

            updateCurrentCheckCardState(
                false
            )


            showReminderDisabledState()
        }
    }


    // =========================================================
    // Window Insets
    // =========================================================

    private fun setupWindowInsets() {

        ViewCompat.setOnApplyWindowInsetsListener(
            binding.topAppBarContainer
        ) { container, insets ->

            val topInset =
                insets.getInsets(
                    WindowInsetsCompat.Type.statusBars()
                ).top


            container.setPadding(
                0,
                topInset,
                0,
                0
            )


            insets
        }
    }


    // =========================================================
    // Toolbar
    // =========================================================

    private fun setupToolbar() {

        binding.toolbarUnusedApps
            .setNavigationOnClickListener {

                finish()
            }
    }


    // =========================================================
    // Load Settings
    // =========================================================

    private fun loadSettings() {

        isLoadingSettings = true


        val enabled =
            AppPrefs.isUnusedAppsReminderEnabled(
                this
            )


        binding.switchUnusedApps.isChecked =
            enabled


        binding.valueUnusedDays.text =
            getDaysText(
                AppPrefs.getUnusedAppsDays(
                    this
                )
            )


        binding.valueUnusedFrequency.text =
            getFrequencyText(
                AppPrefs.getUnusedAppsFrequency(
                    this
                )
            )


        binding.valueUnusedTime.text =
            formatTime(
                AppPrefs.getUnusedAppsHour(
                    this
                ),
                AppPrefs.getUnusedAppsMinute(
                    this
                )
            )


        updateOptionsVisibility(
            enabled
        )


        /*
         * If the reminder is OFF, Current Check is disabled.
         *
         * If the reminder is ON, we temporarily enable it.
         * startUnusedAppsCheck() will disable it again while
         * scanning.
         */
        updateCurrentCheckCardState(
            enabled
        )


        if (!enabled) {

            showReminderDisabledState()
        }


        isLoadingSettings = false
    }


    // =========================================================
    // Main Switch
    // =========================================================

    private fun setupSwitch() {

        binding.switchUnusedApps
            .setOnCheckedChangeListener { _, isChecked ->

                if (isLoadingSettings) {

                    return@setOnCheckedChangeListener
                }


                // =================================================
                // TURN ON
                // =================================================

                if (isChecked) {

                    /*
                     * Usage Access is required.
                     */
                    if (!hasUsageAccess()) {

                        isLoadingSettings = true


                        binding.switchUnusedApps
                            .isChecked = false


                        isLoadingSettings = false


                        updateOptionsVisibility(
                            false
                        )


                        updateCurrentCheckCardState(
                            false
                        )


                        showReminderDisabledState()


                        openUsageAccessSettings()


                        return@setOnCheckedChangeListener
                    }


                    /*
                     * Save enabled state.
                     */
                    AppPrefs.setUnusedAppsReminderEnabled(
                        this,
                        true
                    )


                    /*
                     * Show configuration rows.
                     */
                    updateOptionsVisibility(
                        true
                    )


                    /*
                     * Start a new check.
                     *
                     * The Current Check card will be disabled
                     * until the scan finishes.
                     */
                    startUnusedAppsCheck()
                }


                // =================================================
                // TURN OFF
                // =================================================

                else {

                    /*
                     * Disable reminder.
                     */
                    AppPrefs.setUnusedAppsReminderEnabled(
                        this,
                        false
                    )


                    /*
                     * Hide configuration options.
                     */
                    updateOptionsVisibility(
                        false
                    )


                    /*
                     * Disable Current Check.
                     */
                    updateCurrentCheckCardState(
                        false
                    )


                    /*
                     * Stop treating the card as available.
                     */
                    isCheckingUnusedApps = false


                    showReminderDisabledState()
                }
            }
    }


    // =========================================================
    // Rows
    // =========================================================

    private fun setupRows() {

        // =========================================================
        // UNUSED DAYS
        // =========================================================

        binding.rowUnusedDays.setOnClickListener {

            if (
                !binding.switchUnusedApps.isChecked
            ) {

                return@setOnClickListener
            }


            /*
             * Do not allow changing settings while scanning.
             */
            if (isCheckingUnusedApps) {

                return@setOnClickListener
            }


            showUnusedDaysDialog()
        }


        // =========================================================
        // CHECK FREQUENCY
        // =========================================================

        binding.rowUnusedFrequency.setOnClickListener {

            if (
                !binding.switchUnusedApps.isChecked
            ) {

                return@setOnClickListener
            }


            showFrequencyDialog()
        }


        // =========================================================
        // REMINDER TIME
        // =========================================================

        binding.rowUnusedTime.setOnClickListener {

            if (
                !binding.switchUnusedApps.isChecked
            ) {

                return@setOnClickListener
            }


            showTimePicker()
        }


        // =========================================================
        // CURRENT CHECK CARD
        // =========================================================

        binding.cardCurrentCheck.setOnClickListener {

            /*
             * If reminder is OFF:
             *
             * Do nothing.
             */
            if (
                !binding.switchUnusedApps.isChecked
            ) {

                return@setOnClickListener
            }


            /*
             * IMPORTANT:
             *
             * If scanning is still running,
             * DO NOT open the Unused Apps list.
             */
            if (isCheckingUnusedApps) {

                return@setOnClickListener
            }


            /*
             * Also make sure we actually have a completed
             * result before opening the list.
             */
            if (!hasLastCompletedResult()) {

                return@setOnClickListener
            }


            /*
             * Only now can the user open the Unused Apps list.
             */
            val intent =
                Intent(
                    this,
                    com.example.phonecleaner.ui.apps.UnusedAppsActivity::class.java
                )


            startActivity(
                intent
            )
        }
    }


    // =========================================================
    // Current Check Card State
    // =========================================================

    private fun updateCurrentCheckCardState(
        enabled: Boolean
    ) {

        binding.cardCurrentCheck.isEnabled =
            enabled


        binding.cardCurrentCheck.isClickable =
            enabled


        binding.cardCurrentCheck.isFocusable =
            enabled


        binding.cardCurrentCheck.alpha =
            if (enabled) {

                1.0f

            } else {

                0.60f
            }
    }


    // =========================================================
    // Usage Access Button
    // =========================================================

    private fun setupUsageAccessButton() {

        binding.btnUsageAccess
            .setOnClickListener {

                openUsageAccessSettings()
            }
    }


    // =========================================================
    // Options Visibility
    // =========================================================

    private fun updateOptionsVisibility(
        enabled: Boolean
    ) {

        binding.layoutUnusedOptions.visibility =
            if (enabled) {

                View.VISIBLE

            } else {

                View.GONE
            }
    }


    // =========================================================
    // Reminder Disabled State
    // =========================================================

    private fun showReminderDisabledState() {

        binding.tvUnusedCount.text =
            "Unused apps reminder is turned off.\n" +
                    "Turn it on to check for unused apps."
    }


    // =========================================================
    // UNUSED DAYS DIALOG
    // =========================================================

    private fun showUnusedDaysDialog() {

        val values =
            arrayOf(
                30,
                60,
                90,
                180
            )

        val labels =
            arrayOf(
                "30 days",
                "60 days",
                "90 days",
                "180 days"
            )

        val current =
            AppPrefs.getUnusedAppsDays(
                this
            )

        val selectedIndex =
            values.indexOf(current).let {

                if (it >= 0) {
                    it
                } else {
                    0
                }
            }

        MaterialAlertDialogBuilder(
            this,
            R.style.ThemeOverlay_PhoneCleaner_FrequencyDialog
        )
            .setTitle(
                "Remind me about apps unused for"
            )
            .setSingleChoiceItems(
                labels,
                selectedIndex
            ) { _, which ->

                val selectedDays =
                    values[which]

                AppPrefs.setUnusedAppsDays(
                    this,
                    selectedDays
                )

                binding.valueUnusedDays.text =
                    labels[which]

                /*
                 * Start the new check using the newly
                 * selected number of days.
                 */
                if (
                    binding.switchUnusedApps.isChecked
                ) {

                    startUnusedAppsCheck()
                }

                /*
                 * IMPORTANT:
                 *
                 * Do NOT call dialog.dismiss().
                 *
                 * The dialog stays open and the radio
                 * selection changes.
                 */
            }
            .show()
    }

    // =========================================================
    // FREQUENCY DIALOG
    // =========================================================

    private fun showFrequencyDialog() {

        val values =
            arrayOf(
                "WEEKLY",
                "MONTHLY"
            )

        val labels =
            arrayOf(
                "Weekly",
                "Monthly"
            )

        val current =
            AppPrefs.getUnusedAppsFrequency(
                this
            )

        val selectedIndex =
            values.indexOf(current).let {

                if (it >= 0) {
                    it
                } else {
                    0
                }
            }

        MaterialAlertDialogBuilder(
            this,
            R.style.ThemeOverlay_PhoneCleaner_FrequencyDialog
        )
            .setTitle(
                "Check frequency"
            )
            .setSingleChoiceItems(
                labels,
                selectedIndex
            ) { _, which ->

                AppPrefs.setUnusedAppsFrequency(
                    this,
                    values[which]
                )

                binding.valueUnusedFrequency.text =
                    labels[which]

                /*
                 * IMPORTANT:
                 *
                 * Do NOT dismiss the dialog.
                 *
                 * User can change Weekly/Monthly while
                 * the dialog remains open.
                 */
            }
            .show()
    }




    // =========================================================
    // TIME PICKER
    // =========================================================

    private fun showTimePicker() {

        val hour =
            AppPrefs.getUnusedAppsHour(
                this
            )


        val minute =
            AppPrefs.getUnusedAppsMinute(
                this
            )


        TimePickerDialog(

            this,

            { _, selectedHour, selectedMinute ->

                AppPrefs.setUnusedAppsTime(
                    this,
                    selectedHour,
                    selectedMinute
                )


                binding.valueUnusedTime.text =
                    formatTime(
                        selectedHour,
                        selectedMinute
                    )
            },

            hour,
            minute,

            DateFormat.is24HourFormat(
                this
            )

        ).show()
    }


    // =========================================================
    // USAGE ACCESS STATUS
    // =========================================================

    private fun refreshUsageAccessStatus() {

        val allowed =
            hasUsageAccess()


        if (allowed) {

            binding.tvUsageAccessStatus.text =
                "Usage Access is enabled"


            binding.btnUsageAccess.text =
                "Access granted"


            binding.btnUsageAccess.isEnabled =
                false

        } else {

            binding.tvUsageAccessStatus.text =
                "Allow access so Phone Cleaner can check when apps were last used."


            binding.btnUsageAccess.text =
                "Allow access"


            binding.btnUsageAccess.isEnabled =
                true
        }
    }


    // =========================================================
    // CHECK USAGE ACCESS
    // =========================================================

    private fun hasUsageAccess(): Boolean {

        val appOpsManager =
            getSystemService(
                Context.APP_OPS_SERVICE
            ) as AppOpsManager


        val mode =
            if (
                Build.VERSION.SDK_INT >=
                Build.VERSION_CODES.Q
            ) {

                appOpsManager.unsafeCheckOpNoThrow(

                    AppOpsManager.OPSTR_GET_USAGE_STATS,

                    android.os.Process.myUid(),

                    packageName
                )

            } else {

                @Suppress("DEPRECATION")

                appOpsManager.checkOpNoThrow(

                    AppOpsManager.OPSTR_GET_USAGE_STATS,

                    android.os.Process.myUid(),

                    packageName
                )
            }


        return mode ==
                AppOpsManager.MODE_ALLOWED
    }


    // =========================================================
    // OPEN USAGE ACCESS
    // =========================================================

    private fun openUsageAccessSettings() {

        try {

            startActivity(
                Intent(
                    Settings.ACTION_USAGE_ACCESS_SETTINGS
                )
            )

        } catch (_: Exception) {

            // Settings screen unavailable.
        }
    }


    // =========================================================
    // START UNUSED APPS CHECK
    // =========================================================

    private fun startUnusedAppsCheck() {

        /*
         * Do not start another scan if one is already running.
         */
        if (isCheckingUnusedApps) {

            return
        }


        /*
         * Reminder must be enabled.
         */
        if (
            !AppPrefs.isUnusedAppsReminderEnabled(
                this
            )
        ) {

            showReminderDisabledState()

            updateCurrentCheckCardState(
                false
            )

            return
        }


        /*
         * Usage Access is required.
         */
        if (!hasUsageAccess()) {

            binding.tvUnusedCount.text =
                "Usage Access is required."


            updateCurrentCheckCardState(
                false
            )


            return
        }


        /*
         * Mark scanning as active BEFORE starting
         * the background work.
         */
        isCheckingUnusedApps = true


        /*
         * Disable Current Check immediately.
         *
         * This prevents the user from opening the
         * Unused Apps list while scanning.
         */
        updateCurrentCheckCardState(
            false
        )


        /*
         * Show checking state.
         */
        binding.tvUnusedCount.text =
            "Checking installed apps..."


        val unusedDays =
            AppPrefs.getUnusedAppsDays(
                this
            )


        Thread {

            val unusedApps =
                try {

                    /*
                     * IMPORTANT:
                     *
                     * This is the same scanner used by
                     * the Unused Apps list.
                     */
                    UnusedAppsScanner.findUnusedApps(

                        applicationContext,

                        unusedDays
                    )

                } catch (
                    _: Exception
                ) {

                    emptyList()
                }


            val count =
                unusedApps.size


            runOnUiThread {

                /*
                 * Scan is finished.
                 */
                isCheckingUnusedApps =
                    false


                /*
                 * Check whether the feature is still enabled.
                 */
                if (
                    !AppPrefs.isUnusedAppsReminderEnabled(
                        this
                    )
                ) {

                    updateCurrentCheckCardState(
                        false
                    )


                    showReminderDisabledState()


                    return@runOnUiThread
                }


                /*
                 * SAVE the last completed result.
                 */
                saveLastCompletedResult(
                    count,
                    unusedDays
                )


                /*
                 * Display the completed result.
                 */
                showCompletedResult(
                    count,
                    unusedDays
                )


                /*
                 * NOW Current Check becomes clickable.
                 */
                updateCurrentCheckCardState(
                    true
                )
            }

        }.start()
    }


    // =========================================================
    // SAVE LAST COMPLETED RESULT
    // =========================================================

    private fun saveLastCompletedResult(
        count: Int,
        days: Int
    ) {

        resultPrefs.edit()

            .putInt(
                KEY_LAST_COUNT,
                count
            )

            .putInt(
                KEY_LAST_DAYS,
                days
            )

            .putBoolean(
                KEY_HAS_RESULT,
                true
            )

            .apply()
    }


    // =========================================================
    // HAS LAST RESULT
    // =========================================================

    private fun hasLastCompletedResult(): Boolean {

        return resultPrefs.getBoolean(
            KEY_HAS_RESULT,
            false
        )
    }


    // =========================================================
    // SHOW LAST COMPLETED RESULT
    // =========================================================

    private fun showLastCompletedResult() {

        if (!hasLastCompletedResult()) {

            return
        }


        val count =
            resultPrefs.getInt(
                KEY_LAST_COUNT,
                0
            )


        val days =
            resultPrefs.getInt(
                KEY_LAST_DAYS,
                AppPrefs.getUnusedAppsDays(
                    this
                )
            )


        showCompletedResult(
            count,
            days
        )
    }


    // =========================================================
    // SHOW COMPLETED RESULT
    // =========================================================

    private fun showCompletedResult(
        count: Int,
        days: Int
    ) {

        binding.tvUnusedCount.text =
            when {

                count == 0 -> {

                    "No unused apps found"
                }


                count == 1 -> {

                    "1 app hasn't been used " +
                            "in over $days days"
                }


                else -> {

                    "$count apps haven't been used " +
                            "in over $days days"
                }
            }
    }


    // =========================================================
    // TEXT HELPERS
    // =========================================================

    private fun getDaysText(
        days: Int
    ): String {

        return "$days days"
    }


    private fun getFrequencyText(
        frequency: String
    ): String {

        return when (frequency) {

            "MONTHLY" ->
                "Monthly"

            else ->
                "Weekly"
        }
    }


    private fun formatTime(
        hour: Int,
        minute: Int
    ): String {

        val calendar =
            Calendar.getInstance()


        calendar.set(
            Calendar.HOUR_OF_DAY,
            hour
        )


        calendar.set(
            Calendar.MINUTE,
            minute
        )


        return DateFormat
            .getTimeFormat(
                this
            )
            .format(
                calendar.time
            )
    }
}