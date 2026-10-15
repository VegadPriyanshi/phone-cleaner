package com.example.phonecleaner.features.notifications.ui

import android.app.TimePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.text.format.DateFormat
import android.view.View
import android.widget.Toast

import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

import com.example.phonecleaner.R
import com.example.phonecleaner.core.prefs.AppPrefs
import com.example.phonecleaner.databinding.ActivityNotificationSettingsBinding
import com.example.phonecleaner.features.notifications.NotificationHelper
import com.example.phonecleaner.features.notifications.lowstorage.LowStorageScheduler
import com.example.phonecleaner.features.notifications.scheduler.CleaningReminderScheduler
import com.google.android.material.dialog.MaterialAlertDialogBuilder

import java.util.Calendar


class NotificationSettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNotificationSettingsBinding

    private var isLoadingSettings = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityNotificationSettingsBinding.inflate(
            layoutInflater
        )

        setContentView(binding.root)

        setupWindowInsets()
        setupToolbar()

        NotificationHelper.createNotificationChannel(this)

        AppPrefs.initializeNotificationDefaults(this)

        loadNotificationSetting()
        loadCleaningReminderSettings()
        loadLowStorageSwitchState()
        loadUnusedAppsSetting()

        setupSwitches()
        setupRows()
        updateVisibility()
    }


    override fun onResume() {
        super.onResume()

        if (::binding.isInitialized) {
            loadNotificationSetting()
            loadCleaningReminderSettings()
            loadLowStorageSwitchState()
            loadUnusedAppsSetting()
            updateVisibility()
        }
    }


    // =========================================================
    // Window Insets
    // =========================================================

    private fun setupWindowInsets() {

        ViewCompat.setOnApplyWindowInsetsListener(
            binding.topAppBarContainer
        ) { container, insets ->

            val topInset = insets.getInsets(
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

        binding.toolbarNotifications
            .setNavigationOnClickListener {
                finish()
            }
    }


    // =========================================================
    // Notification Permission / Setting
    // =========================================================

    private fun loadNotificationSetting() {

        isLoadingSettings = true

        val notificationsEnabled =
            NotificationHelper.areNotificationsAllowed(this)

        AppPrefs.setNotificationsEnabled(
            this,
            notificationsEnabled
        )

        binding.switchEnableNotifications.isChecked =
            notificationsEnabled

        isLoadingSettings = false
    }


    // =========================================================
    // Cleaning Reminder
    // =========================================================

    private fun loadCleaningReminderSettings() {

        isLoadingSettings = true

        binding.switchCleaningReminder.isChecked =
            AppPrefs.isCleaningReminderEnabled(this)

        val frequency =
            AppPrefs.getAutoCleanFrequency(this)

        binding.valueReminderFrequency.text =
            getFrequencyDisplayText(frequency)

        val hour =
            AppPrefs.getAutoCleanHour(this)

        val minute =
            AppPrefs.getAutoCleanMinute(this)

        binding.valueReminderTime.text =
            formatTime(hour, minute)

        isLoadingSettings = false
    }


    // =========================================================
    // Low Storage
    // =========================================================

    private fun loadLowStorageSwitchState() {

        isLoadingSettings = true

        binding.switchLowStorage.isChecked =
            AppPrefs.isLowStorageAlertEnabled(this)

        isLoadingSettings = false
    }


    // =========================================================
    // Switches
    // =========================================================

    private fun setupSwitches() {

        // -----------------------------------------------------
        // Enable Notifications
        // -----------------------------------------------------

        binding.switchEnableNotifications
            .setOnCheckedChangeListener { _, _ ->

                if (isLoadingSettings) {
                    return@setOnCheckedChangeListener
                }

                openAndroidAppInfo()
            }


        // -----------------------------------------------------
        // Cleaning Reminder
        // -----------------------------------------------------

        binding.switchCleaningReminder
            .setOnCheckedChangeListener { _, isChecked ->

                if (isLoadingSettings) {
                    return@setOnCheckedChangeListener
                }

                if (isChecked) {

                    if (
                        !NotificationHelper.areNotificationsAllowed(
                            this
                        )
                    ) {

                        Toast.makeText(
                            this,
                            "Please enable notifications first.",
                            Toast.LENGTH_SHORT
                        ).show()

                        isLoadingSettings = true

                        binding.switchCleaningReminder.isChecked =
                            false

                        isLoadingSettings = false

                        return@setOnCheckedChangeListener
                    }

                    AppPrefs.setCleaningReminderEnabled(
                        this,
                        true
                    )

                    CleaningReminderScheduler.schedule(
                        this
                    )

                } else {

                    AppPrefs.setCleaningReminderEnabled(
                        this,
                        false
                    )

                    CleaningReminderScheduler.cancel(
                        this
                    )
                }

                updateVisibility()
            }


        // -----------------------------------------------------
        // Low Storage Alert
        // -----------------------------------------------------

        binding.switchLowStorage
            .setOnCheckedChangeListener { _, isChecked ->

                if (isLoadingSettings) {
                    return@setOnCheckedChangeListener
                }

                if (isChecked) {

                    if (
                        !NotificationHelper.areNotificationsAllowed(
                            this
                        )
                    ) {

                        Toast.makeText(
                            this,
                            "Please enable notifications first.",
                            Toast.LENGTH_SHORT
                        ).show()

                        isLoadingSettings = true

                        binding.switchLowStorage.isChecked =
                            false

                        isLoadingSettings = false

                        return@setOnCheckedChangeListener
                    }

                    AppPrefs.setLowStorageAlertEnabled(
                        this,
                        true
                    )

                    AppPrefs.setLowStorageAlertArmed(
                        this,
                        true
                    )

                    /*
                     * Open the detailed Low Storage screen.
                     */
                    openLowStorageSettings()

                } else {

                    AppPrefs.setLowStorageAlertEnabled(
                        this,
                        false
                    )

                    LowStorageScheduler.cancel(
                        this
                    )

                    NotificationHelper.cancelLowStorageNotification(
                        this
                    )
                }
            }


        // -----------------------------------------------------
        // Unused Apps
        // -----------------------------------------------------

        binding.switchUnusedApps
            .setOnCheckedChangeListener { _, isChecked ->

                if (isLoadingSettings) {
                    return@setOnCheckedChangeListener
                }

                if (isChecked) {

                    startActivity(
                        Intent(
                            this,
                            UnusedAppsSettingsActivity::class.java
                        )
                    )

                } else {

                    AppPrefs.setUnusedAppsReminderEnabled(
                        this,
                        false
                    )
                }
            }
    }


    private fun loadUnusedAppsSetting() {

        isLoadingSettings = true

        binding.switchUnusedApps.isChecked =
            AppPrefs.isUnusedAppsReminderEnabled(
                this
            )

        isLoadingSettings = false
    }


    // =========================================================
    // Open Low Storage Screen
    // =========================================================

    private fun openLowStorageSettings() {

        val intent = Intent(
            this,
            LowStorageSettingsActivity::class.java
        )

        startActivity(intent)
    }


    // =========================================================
    // Open Android App Info
    // =========================================================

    private fun openAndroidAppInfo() {

        try {

            val intent = Intent(
                Settings.ACTION_APPLICATION_DETAILS_SETTINGS
            ).apply {

                data = Uri.parse(
                    "package:$packageName"
                )
            }

            startActivity(intent)

        } catch (e: Exception) {

            Toast.makeText(
                this,
                "Unable to open Android app settings.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }


    // =========================================================
    // Rows
    // =========================================================

    private fun setupRows() {

        // Cleaning Reminder Frequency

        binding.rowReminderFrequency
            .setOnClickListener {

                if (
                    !binding.switchCleaningReminder.isChecked
                ) {
                    return@setOnClickListener
                }

                showFrequencyDialog()
            }


        // Cleaning Reminder Time

        binding.rowReminderTime
            .setOnClickListener {

                if (
                    !binding.switchCleaningReminder.isChecked
                ) {
                    return@setOnClickListener
                }

                showTimePicker()
            }


        // Low Storage row

        binding.rowLowStorageAlert
            .setOnClickListener {

                if (
                    binding.switchLowStorage.isChecked
                ) {
                    openLowStorageSettings()
                }
            }
    }


    // =========================================================
    // Visibility
    // =========================================================

    private fun updateVisibility() {

        binding.layoutCleaningReminderOptions.visibility =
            if (
                binding.switchCleaningReminder.isChecked
            ) {
                View.VISIBLE
            } else {
                View.GONE
            }
    }


    // =========================================================
    // Frequency Dialog
    // =========================================================

    private fun showFrequencyDialog() {

        val frequencyCodes = arrayOf(
            "DAILY",
            "THREE_DAYS",
            "WEEKLY"
        )

        val frequencyNames = arrayOf(
            "Daily",
            "Every 3 days",
            "Every week"
        )

        val currentFrequency =
            AppPrefs.getAutoCleanFrequency(this)

        var selectedIndex =
            frequencyCodes.indexOf(currentFrequency)

        if (selectedIndex < 0) {
            selectedIndex = 0
        }

        MaterialAlertDialogBuilder(
            this,
            R.style.ThemeOverlay_PhoneCleaner_FrequencyDialog
        )
            .setTitle("Cleaning reminder frequency")
            .setSingleChoiceItems(
                frequencyNames,
                selectedIndex
            ) { _, which ->

                val selectedFrequency =
                    frequencyCodes[which]

                AppPrefs.setAutoCleanFrequency(
                    this,
                    selectedFrequency
                )

                binding.valueReminderFrequency.text =
                    frequencyNames[which]

                if (
                    binding.switchCleaningReminder.isChecked
                ) {

                    CleaningReminderScheduler.schedule(
                        this
                    )
                }
            }
            .show()
    }


    // =========================================================
    // Time Picker
    // =========================================================

    private fun showTimePicker() {

        val selectedHour =
            AppPrefs.getAutoCleanHour(this)

        val selectedMinute =
            AppPrefs.getAutoCleanMinute(this)

        val timePicker = TimePickerDialog(
            this,

            { _, hourOfDay, minute ->

                AppPrefs.setAutoCleanTime(
                    this,
                    hourOfDay,
                    minute
                )

                binding.valueReminderTime.text =
                    formatTime(
                        hourOfDay,
                        minute
                    )

                if (
                    binding.switchCleaningReminder.isChecked
                ) {

                    CleaningReminderScheduler.schedule(
                        this
                    )
                }
            },

            selectedHour,
            selectedMinute,
            false
        )

        timePicker.show()
    }


    // =========================================================
    // Frequency Display
    // =========================================================

    private fun getFrequencyDisplayText(
        frequency: String
    ): String {

        return when (frequency) {

            "THREE_DAYS" ->
                "Every 3 days"

            "WEEKLY" ->
                "Every week"

            else ->
                "Daily"
        }
    }


    // =========================================================
    // Time Display
    // =========================================================

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
            .getTimeFormat(this)
            .format(calendar.time)
    }
}