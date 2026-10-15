package com.example.phonecleaner.ui.settings


import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast

import androidx.fragment.app.Fragment

import com.example.phonecleaner.MainActivity
import com.example.phonecleaner.R
import com.example.phonecleaner.core.prefs.AppPrefs
import com.example.phonecleaner.core.prefs.AppPrefs.selectedLanguageCode
import com.example.phonecleaner.core.translation.TranslationManager
import com.example.phonecleaner.data.model.AutoCleanResult
import com.example.phonecleaner.features.autoclean.AutoCleanScheduleActivity
import com.example.phonecleaner.features.autoclean.AutoCleanScheduler
import com.example.phonecleaner.features.language.LanguageManager
import com.example.phonecleaner.features.language.data.Languages
import com.example.phonecleaner.features.language.ui.LanguageActivity
import com.example.phonecleaner.ui.permission.AppPermissionsActivity
import com.example.phonecleaner.utils.AutoCleanResultStorage
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.switchmaterial.SwitchMaterial

import java.util.Calendar


class GeneralFragment :
    Fragment() {


    companion object {

        private const val LANGUAGE_REQUEST_CODE =
            1001

        private const val AUTO_CLEAN_REQUEST_CODE =
            1002
    }


    // =========================================================
    // Create View
    // =========================================================

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        return inflater.inflate(
            R.layout.fragment_general,
            container,
            false
        )
    }


    // =========================================================
    // View Created
    // =========================================================

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {

        super.onViewCreated(
            view,
            savedInstanceState
        )


        // =====================================================
        // Show bottom navigation
        // =====================================================

        (activity as? MainActivity)
            ?.showBottomNavigation()


        // =====================================================
        // Back Button
        // =====================================================

        view.findViewById<View>(
            R.id.btnGeneralBack
        ).setOnClickListener {

            parentFragmentManager
                .popBackStack()
        }


        // =====================================================
        // Language
        // =====================================================

        view.findViewById<View>(
            R.id.rowLanguage
        ).setOnClickListener {

            val intent = Intent(
                requireContext(),
                LanguageActivity::class.java
            )

            intent.putExtra(
                LanguageActivity.EXTRA_FROM_SETTINGS,
                true
            )

            startActivityForResult(
                intent,
                LANGUAGE_REQUEST_CODE
            )
        }


        // =====================================================
        // Auto-clean Schedule
        // =====================================================

        view.findViewById<View>(
            R.id.rowAutoCleanSchedule
        ).setOnClickListener {

            val intent =
                Intent(
                    requireContext(),
                    AutoCleanScheduleActivity::class.java
                )


            startActivityForResult(
                intent,
                AUTO_CLEAN_REQUEST_CODE
            )
        }


        // =====================================================
        // App Permissions
        // =====================================================

        view.findViewById<View>(
            R.id.rowAppPermissions
        ).setOnClickListener {

            val intent =
                Intent(
                    requireContext(),
                    AppPermissionsActivity::class.java
                )


            startActivity(
                intent
            )
        }


        // =====================================================
        // Reset Settings
        // =====================================================

        view.findViewById<View>(
            R.id.cardResetSettings
        ).setOnClickListener {

            showResetSettingsDialog()
        }


        // =====================================================
        // Run on Startup
        // =====================================================

        val runOnStartupSwitch =
            view.findViewById<SwitchMaterial>(
                R.id.switchRunOnStartup
            )


        runOnStartupSwitch.isChecked =
            AppPrefs.isRunOnStartupEnabled(
                requireContext()
            )


        runOnStartupSwitch
            .setOnCheckedChangeListener { _, isChecked ->

                AppPrefs.setRunOnStartupEnabled(
                    requireContext(),
                    isChecked
                )
            }


        // =====================================================
        // Initial UI
        // =====================================================

        updateAutoCleanSummary(
            view
        )


        updateAutoCleanResult(
            view
        )


        updateSelectedLanguage(
            view
        )
    }


    // =========================================================
    // Refresh when returning to General
    // =========================================================

    override fun onResume() {

        super.onResume()


        view?.let { currentView ->

            updateAutoCleanSummary(
                currentView
            )


            updateAutoCleanResult(
                currentView
            )


            updateSelectedLanguage(
                currentView
            )


            val runOnStartupSwitch =
                currentView.findViewById<SwitchMaterial>(
                    R.id.switchRunOnStartup
                )


            runOnStartupSwitch?.isChecked =
                AppPrefs.isRunOnStartupEnabled(
                    requireContext()
                )
        }
    }


    // =========================================================
    // Auto-clean Schedule Summary
    // =========================================================

    private fun updateAutoCleanSummary(
        view: View
    ) {

        val valueView =
            view.findViewById<TextView>(
                R.id.valueAutoCleanSchedule
            )


        val enabled =
            AppPrefs.isAutoCleanEnabled(
                requireContext()
            )


        if (!enabled) {

            valueView.text =
                "Off"

            return
        }


        val frequency =
            AppPrefs.getAutoCleanFrequency(
                requireContext()
            )


        val hour =
            AppPrefs.getAutoCleanHour(
                requireContext()
            )


        val minute =
            AppPrefs.getAutoCleanMinute(
                requireContext()
            )


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


        calendar.set(
            Calendar.SECOND,
            0
        )


        calendar.set(
            Calendar.MILLISECOND,
            0
        )


        val time =
            DateFormat
                .getTimeFormat(
                    requireContext()
                )
                .format(
                    calendar.time
                )


        val frequencyText =
            when (frequency) {

                "THREE_DAYS" ->
                    "Every 3 days"

                "WEEKLY" ->
                    "Every week"

                else ->
                    "Daily"
            }


        valueView.text =
            "$frequencyText · $time"
    }


    // =========================================================
    // Reset Settings Dialog
    // =========================================================

    private fun showResetSettingsDialog() {

        MaterialAlertDialogBuilder(
            requireContext()
        )
            .setTitle(
                "Reset settings?"
            )
            .setMessage(
                "All Phone Cleaner settings will be restored " +
                        "to their default values.\n\n" +
                        "Your personal files will not be deleted."
            )
            .setNegativeButton(
                "Cancel",
                null
            )
            .setPositiveButton(
                "Reset"
            ) { _, _ ->

                resetAllSettings()
            }
            .show()
    }


    // =========================================================
    // Reset All Settings
    // =========================================================

    private fun resetAllSettings() {

        val context =
            requireContext()


        // =====================================================
        // Cancel Auto-clean schedule
        // =====================================================

        AutoCleanScheduler.cancel(
            context
        )


        // =====================================================
        // Reset saved settings
        // =====================================================

        AppPrefs.resetSettingsToDefaults(
            context
        )


        // =====================================================
        // IMPORTANT:
        // Immediately apply English.
        // =====================================================

        LanguageManager.resetToEnglish(
            context
        )


        // =====================================================
        // Update Run on Startup switch
        // =====================================================

        view?.findViewById<SwitchMaterial>(
            R.id.switchRunOnStartup
        )?.isChecked = false


        // =====================================================
        // Update current General UI
        // =====================================================

        view?.let { currentView ->

            updateAutoCleanSummary(
                currentView
            )


            updateSelectedLanguage(
                currentView
            )
        }


        // =====================================================
        // Confirmation
        // =====================================================

        Toast.makeText(
            context,
            "Settings restored to default",
            Toast.LENGTH_SHORT
        ).show()
    }


    // =========================================================
    // Last Auto-clean Result
    // =========================================================

    private fun updateAutoCleanResult(
        view: View
    ) {

        val resultView =
            view.findViewById<TextView>(
                R.id.valueAutoCleanLastResult
            )


        val result =
            AutoCleanResultStorage.getLastResult(
                requireContext()
            )


        if (result == null) {

            resultView.text =
                "No cleanup has run yet"

            return
        }


        val timeText =
            formatResultTime(
                result
            )


        val filesText =
            if (
                result.deletedCount == 1
            ) {

                "1 file"

            } else {

                "${result.deletedCount} files"
            }


        val spaceText =
            formatStorageSize(
                result.spaceFreedBytes
            )


        resultView.text =
            "Last cleaned: " +
                    "$timeText · " +
                    "$filesText · " +
                    "$spaceText freed"
    }


    // =========================================================
    // Format Auto-clean Result Time
    // =========================================================

    private fun formatResultTime(
        result: AutoCleanResult
    ): String {

        val calendar =
            Calendar.getInstance()


        calendar.timeInMillis =
            result.timestamp


        val now =
            Calendar.getInstance()


        val sameDay =
            calendar.get(
                Calendar.YEAR
            ) ==
                    now.get(
                        Calendar.YEAR
                    ) &&
                    calendar.get(
                        Calendar.DAY_OF_YEAR
                    ) ==
                    now.get(
                        Calendar.DAY_OF_YEAR
                    )


        val time =
            DateFormat
                .getTimeFormat(
                    requireContext()
                )
                .format(
                    calendar.time
                )


        return if (sameDay) {

            "Today $time"

        } else {

            val date =
                DateFormat
                    .getDateFormat(
                        requireContext()
                    )
                    .format(
                        calendar.time
                    )


            "$date $time"
        }
    }


    // =========================================================
    // Format Storage Size
    // =========================================================

    private fun formatStorageSize(
        bytes: Long
    ): String {

        if (bytes <= 0L) {

            return "0 B"
        }


        val kb =
            1024.0


        val mb =
            kb * 1024.0


        val gb =
            mb * 1024.0


        return when {

            bytes >= gb ->

                String.format(
                    "%.1f GB",
                    bytes / gb
                )


            bytes >= mb ->

                String.format(
                    "%.1f MB",
                    bytes / mb
                )


            bytes >= kb ->

                String.format(
                    "%.1f KB",
                    bytes / kb
                )


            else ->
                "$bytes B"
        }
    }


    // =========================================================
    // Update Selected Language
    // =========================================================

    private fun updateSelectedLanguage(
        view: View
    ) {

        val selectedCode =
            requireContext()
                .selectedLanguageCode


        val languageName =
            Languages.ALL
                .firstOrNull {

                    it.translateCode ==
                            selectedCode
                }
                ?.nativeName
                ?: "English"


        view.findViewById<TextView>(
            R.id.valueLanguage
        ).text =
            languageName
    }


    // =========================================================
    // Activity Result
    // =========================================================

    @Deprecated(
        "Use Activity Result API for new implementations"
    )
    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {

        super.onActivityResult(
            requestCode,
            resultCode,
            data
        )


        // =====================================================
        // Language result
        // =====================================================

        if (
            requestCode ==
            LANGUAGE_REQUEST_CODE &&
            resultCode ==
            Activity.RESULT_OK
        ) {

            view?.let {

                updateSelectedLanguage(
                    it
                )
            }


            val selectedCode =
                requireContext()
                    .selectedLanguageCode


            if (
                !selectedCode.isNullOrBlank()
            ) {

                view?.let { currentView ->

                    TranslationManager.translateScreen(

                        root =
                            currentView,

                        languageCode =
                            selectedCode
                    )
                }
            }
        }


        // =====================================================
        // Auto-clean result
        // =====================================================

        if (
            requestCode ==
            AUTO_CLEAN_REQUEST_CODE &&
            resultCode ==
            Activity.RESULT_OK
        ) {

            view?.let {

                updateAutoCleanSummary(
                    it
                )


                updateAutoCleanResult(
                    it
                )
            }
        }
    }
}