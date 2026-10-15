package com.example.phonecleaner.features.autoclean

import android.app.TimePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.Toast

import androidx.appcompat.app.AppCompatActivity

import com.example.phonecleaner.R
import com.example.phonecleaner.core.prefs.AppPrefs
import com.example.phonecleaner.databinding.ActivityAutoCleanScheduleBinding

import java.util.Calendar

class AutoCleanScheduleActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAutoCleanScheduleBinding

    private var selectedFrequency = "DAILY"

    private var selectedHour = 22

    private var selectedMinute = 0


    override fun onCreate(
        savedInstanceState: Bundle?
    ) {

        super.onCreate(savedInstanceState)

        binding =
            ActivityAutoCleanScheduleBinding.inflate(
                layoutInflater
            )

        setContentView(binding.root)


        loadSavedSettings()

        setupViews()

        updateEnabledState()
    }


    // =========================================================
    // Load Saved Settings
    // =========================================================

    private fun loadSavedSettings() {

        binding.switchAutoClean.isChecked =
            AppPrefs.isAutoCleanEnabled(this)


        selectedFrequency =
            AppPrefs.getAutoCleanFrequency(this)


        selectedHour =
            AppPrefs.getAutoCleanHour(this)


        selectedMinute =
            AppPrefs.getAutoCleanMinute(this)


        when (selectedFrequency) {

            "DAILY" -> {
                binding.rbEveryDay.isChecked = true
            }

            "THREE_DAYS" -> {
                binding.rbEveryThreeDays.isChecked = true
            }

            "WEEKLY" -> {
                binding.rbEveryWeek.isChecked = true
            }
        }


        updateTimeText()
    }


    // =========================================================
    // Setup Views
    // =========================================================

    private fun setupViews() {

        binding.btnBack.setOnClickListener {

            finish()
        }


        binding.switchAutoClean.setOnCheckedChangeListener {
                _,
                _ ->

            updateEnabledState()
        }


        binding.radioGroupFrequency.setOnCheckedChangeListener {
                _,
                checkedId ->

            selectedFrequency =
                when (checkedId) {

                    R.id.rbEveryThreeDays ->
                        "THREE_DAYS"

                    R.id.rbEveryWeek ->
                        "WEEKLY"

                    else ->
                        "DAILY"
                }
        }


        binding.rowTime.setOnClickListener {

            if (binding.switchAutoClean.isChecked) {

                showTimePicker()
            }
        }


        binding.btnSave.setOnClickListener {

            saveSettings()
        }
    }


    // =========================================================
    // Enable / Disable Controls
    // =========================================================

    private fun updateEnabledState() {

        val enabled =
            binding.switchAutoClean.isChecked


        binding.frequencyContainer.isEnabled =
            enabled

        binding.radioGroupFrequency.isEnabled =
            enabled

        binding.rbEveryDay.isEnabled =
            enabled

        binding.rbEveryThreeDays.isEnabled =
            enabled

        binding.rbEveryWeek.isEnabled =
            enabled

        binding.rowTime.isEnabled =
            enabled

        binding.tvSelectedTime.alpha =
            if (enabled) 1f else 0.5f

        binding.tvTimeLabel.alpha =
            if (enabled) 1f else 0.5f
    }


    // =========================================================
    // Time Picker
    // =========================================================

    private fun showTimePicker() {

        val picker =
            TimePickerDialog(
                this,

                { _, hourOfDay, minute ->

                    selectedHour =
                        hourOfDay

                    selectedMinute =
                        minute

                    updateTimeText()
                },

                selectedHour,
                selectedMinute,
                false
            )


        picker.show()
    }


    // =========================================================
    // Update Time Text
    // =========================================================

    private fun updateTimeText() {

        val calendar =
            Calendar.getInstance()

        calendar.set(
            Calendar.HOUR_OF_DAY,
            selectedHour
        )

        calendar.set(
            Calendar.MINUTE,
            selectedMinute
        )


        val timeFormat =
            android.text.format.DateFormat
                .getTimeFormat(this)


        binding.tvSelectedTime.text =
            timeFormat.format(calendar.time)
    }


    // =========================================================
    // Save
    // =========================================================

    private fun saveSettings() {

        val enabled =
            binding.switchAutoClean.isChecked


        // ---------------------------------------------------------
        // Save Auto-clean enabled state
        // ---------------------------------------------------------

        AppPrefs.setAutoCleanEnabled(
            this,
            enabled
        )


        // ---------------------------------------------------------
        // Save frequency
        // ---------------------------------------------------------

        AppPrefs.setAutoCleanFrequency(
            this,
            selectedFrequency
        )


        // ---------------------------------------------------------
        // Save selected time
        // ---------------------------------------------------------

        AppPrefs.setAutoCleanTime(
            this,
            selectedHour,
            selectedMinute
        )


        // ---------------------------------------------------------
        // Schedule / cancel Auto-clean
        // ---------------------------------------------------------

        if (enabled) {

            val repeatIntervalDays =
                when (selectedFrequency) {

                    "THREE_DAYS" -> 3L

                    "WEEKLY" -> 7L

                    else -> 1L
                }


            AutoCleanScheduler.schedule(
                context = this,
                repeatIntervalDays = repeatIntervalDays,
                hour = selectedHour,
                minute = selectedMinute
            )

            AutoCleanScheduler.observeWorkStatus(this)

        } else {

            AutoCleanScheduler.cancel(
                context = this
            )
        }


        // ---------------------------------------------------------
        // Show confirmation
        // ---------------------------------------------------------

        Toast.makeText(
            this,
            if (enabled) {

                getString(
                    R.string.auto_clean_schedule_saved
                )

            } else {

                getString(
                    R.string.auto_clean_schedule_disabled
                )
            },
            Toast.LENGTH_SHORT
        ).show()


        setResult(
            RESULT_OK
        )

        finish()
    }

}