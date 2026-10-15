package com.example.phonecleaner.features.notifications.ui

import android.os.Bundle
import android.widget.Toast

import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

import com.example.phonecleaner.R
import com.example.phonecleaner.core.prefs.AppPrefs
import com.example.phonecleaner.databinding.ActivityLowStorageSettingsBinding
import com.example.phonecleaner.features.notifications.NotificationHelper
import com.example.phonecleaner.features.notifications.lowstorage.LowStorageAlertPolicy
import com.example.phonecleaner.features.notifications.lowstorage.LowStorageScheduler
import com.example.phonecleaner.utils.StorageInfo
import com.example.phonecleaner.utils.StorageUtils
import com.google.android.material.slider.Slider

import java.util.Locale


class LowStorageSettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLowStorageSettingsBinding

    private var isLoadingSettings = false

    private var storageInfo: StorageInfo? = null


    // =========================================================
    // Lifecycle
    // =========================================================

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding =
            ActivityLowStorageSettingsBinding.inflate(
                layoutInflater
            )

        setContentView(binding.root)

        setupWindowInsets()
        setupToolbar()

        loadSettings()

        setupSwitch()
        setupThresholdSlider()
        setupFrequency()

        refreshStorageStatus()

        if (
            AppPrefs.isLowStorageAlertEnabled(this)
        ) {
            LowStorageScheduler.schedule(this)
            LowStorageScheduler.checkNow(this)
        }
    }


    override fun onResume() {
        super.onResume()

        if (::binding.isInitialized) {
            loadSettings()
            refreshStorageStatus()
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

        binding.btnBack.setOnClickListener {
            finish()
        }
    }


    // =========================================================
    // Load Settings
    // =========================================================

    private fun loadSettings() {

        isLoadingSettings = true

        binding.switchLowStorageDetail.isChecked =
            AppPrefs.isLowStorageAlertEnabled(this)

        binding.sliderLowStorageThreshold.value =
            AppPrefs.getLowStorageThreshold(
                this
            ).toFloat()

        binding.toggleLowStorageFrequency.check(
            frequencyToButtonId(
                AppPrefs.getLowStorageFrequency(
                    this
                )
            )
        )

        isLoadingSettings = false
    }


    // =========================================================
    // Switch
    // =========================================================

    private fun setupSwitch() {

        binding.switchLowStorageDetail
            .setOnCheckedChangeListener { _, isChecked ->

                if (isLoadingSettings) {
                    return@setOnCheckedChangeListener
                }


                if (isChecked) {

                    // -------------------------------------------------
                    // Check notification permission
                    // -------------------------------------------------

                    if (
                        !NotificationHelper
                            .areNotificationsAllowed(this)
                    ) {

                        Toast.makeText(
                            this,
                            "Please enable notifications first.",
                            Toast.LENGTH_SHORT
                        ).show()

                        isLoadingSettings = true

                        binding.switchLowStorageDetail
                            .isChecked = false

                        isLoadingSettings = false

                        return@setOnCheckedChangeListener
                    }


                    // -------------------------------------------------
                    // Enable alert
                    // -------------------------------------------------

                    AppPrefs.setLowStorageAlertEnabled(
                        this,
                        true
                    )

                    AppPrefs.setLowStorageAlertArmed(
                        this,
                        true
                    )


                    // -------------------------------------------------
                    // Schedule low storage checker
                    // -------------------------------------------------

                    LowStorageScheduler.schedule(
                        this
                    )

                    LowStorageScheduler.checkNow(
                        this
                    )

                } else {

                    // -------------------------------------------------
                    // Disable alert
                    // -------------------------------------------------

                    AppPrefs.setLowStorageAlertEnabled(
                        this,
                        false
                    )


                    // -------------------------------------------------
                    // Cancel scheduler
                    // -------------------------------------------------

                    LowStorageScheduler.cancel(
                        this
                    )


                    // -------------------------------------------------
                    // Remove notification
                    // -------------------------------------------------

                    NotificationHelper
                        .cancelLowStorageNotification(
                            this
                        )
                }
            }
    }


    // =========================================================
    // Threshold Slider
    // =========================================================

    private fun setupThresholdSlider() {

        binding.sliderLowStorageThreshold
            .addOnChangeListener { _, value, fromUser ->

                if (isLoadingSettings) {
                    return@addOnChangeListener
                }

                val percent = value.toInt()

                updateThresholdUi(percent)

                if (fromUser) {

                    AppPrefs.setLowStorageThreshold(
                        this,
                        percent
                    )
                }
            }


        binding.sliderLowStorageThreshold
            .addOnSliderTouchListener(
                object : Slider.OnSliderTouchListener {

                    override fun onStartTrackingTouch(
                        slider: Slider
                    ) {
                        // Nothing required.
                    }


                    override fun onStopTrackingTouch(
                        slider: Slider
                    ) {

                        AppPrefs.setLowStorageAlertArmed(
                            this@LowStorageSettingsActivity,
                            true
                        )


                        if (
                            AppPrefs.isLowStorageAlertEnabled(
                                this@LowStorageSettingsActivity
                            )
                        ) {

                            LowStorageScheduler.checkNow(
                                this@LowStorageSettingsActivity
                            )
                        }
                    }
                }
            )
    }


    // =========================================================
    // Frequency
    // =========================================================

    private fun setupFrequency() {

        binding.toggleLowStorageFrequency
            .addOnButtonCheckedListener {
                    _,
                    checkedId,
                    isChecked ->

                if (
                    !isChecked ||
                    isLoadingSettings
                ) {
                    return@addOnButtonCheckedListener
                }


                // -------------------------------------------------
                // Save frequency
                // -------------------------------------------------

                AppPrefs.setLowStorageFrequency(
                    this,
                    buttonIdToFrequency(
                        checkedId
                    )
                )


                // -------------------------------------------------
                // Re-arm alert
                // -------------------------------------------------

                AppPrefs.setLowStorageAlertArmed(
                    this,
                    true
                )


                // -------------------------------------------------
                // Reschedule when enabled
                // -------------------------------------------------

                if (
                    AppPrefs.isLowStorageAlertEnabled(
                        this
                    )
                ) {

                    LowStorageScheduler.schedule(
                        this
                    )

                    LowStorageScheduler.checkNow(
                        this
                    )
                }
            }
    }


    // =========================================================
    // Storage Information
    // =========================================================

    private fun refreshStorageStatus() {

        val info =
            try {

                StorageUtils.getStorageInfo(
                    this
                )

            } catch (e: Exception) {

                return
            }


        // Keep the latest real storage information.
        storageInfo = info


        // -------------------------------------------------
        // Format storage
        // -------------------------------------------------

        val used =
            StorageUtils.formatStorageSize(
                info.usedBytes
            )

        val total =
            StorageUtils.formatStorageSize(
                info.totalBytes
            )

        val free =
            StorageUtils.formatStorageSize(
                info.freeBytes
            )


        // -------------------------------------------------
        // Storage usage
        // -------------------------------------------------

        binding.tvLowStorageUsage.text =
            "$used of $total used"


        // -------------------------------------------------
        // Free storage
        // -------------------------------------------------

        binding.tvLowStorageFree.text =
            String.format(
                Locale.US,
                "Free %s (%.0f%%)",
                free,
                info.freePercentage
            )


        // -------------------------------------------------
        // Progress
        // -------------------------------------------------

        binding.lowStorageProgress.progress =
            info.usedPercentage


        // -------------------------------------------------
        // Threshold
        // -------------------------------------------------

        updateThresholdUi(
            binding.sliderLowStorageThreshold
                .value
                .toInt()
        )
    }


    // =========================================================
    // Threshold UI
    // =========================================================

    private fun updateThresholdUi(
        percent: Int
    ) {

        // -------------------------------------------------
        // Threshold percentage
        // -------------------------------------------------

        binding.valueStorageThreshold.text =
            "$percent%"


        // -------------------------------------------------
        // Threshold label
        // -------------------------------------------------

        binding.tvThresholdMarkerLabel.text =
            "Alert at $percent% free"


        // -------------------------------------------------
        // Storage info
        // -------------------------------------------------

        val info = storageInfo

        if (info == null) {
            return
        }


        // -------------------------------------------------
        // Calculate threshold storage amount
        // -------------------------------------------------

        val thresholdBytes =
            (info.totalBytes * percent) / 100


        binding.tvThresholdApprox.text =
            "about " +
                    StorageUtils.formatStorageSize(
                        thresholdBytes
                    )


        // -------------------------------------------------
        // Progress color
        // -------------------------------------------------

        val barColor =
            if (
                info.freePercentage < percent
            ) {

                R.color.low_storage_alert_red

            } else {

                R.color.primary
            }


        binding.lowStorageProgress
            .setIndicatorColor(
                ContextCompat.getColor(
                    this,
                    barColor
                )
            )
    }


    // =========================================================
    // Frequency → Button
    // =========================================================

    private fun frequencyToButtonId(
        frequency: String
    ): Int {

        return when (frequency) {

            LowStorageAlertPolicy.FREQ_ONCE ->
                R.id.btnLowStorageOnce

            LowStorageAlertPolicy.FREQ_WEEKLY ->
                R.id.btnLowStorageWeekly

            else ->
                R.id.btnLowStorageDaily
        }
    }


    // =========================================================
    // Button → Frequency
    // =========================================================

    private fun buttonIdToFrequency(
        buttonId: Int
    ): String {

        return when (buttonId) {

            R.id.btnLowStorageOnce ->
                LowStorageAlertPolicy.FREQ_ONCE

            R.id.btnLowStorageWeekly ->
                LowStorageAlertPolicy.FREQ_WEEKLY

            else ->
                LowStorageAlertPolicy.FREQ_DAILY
        }
    }
}