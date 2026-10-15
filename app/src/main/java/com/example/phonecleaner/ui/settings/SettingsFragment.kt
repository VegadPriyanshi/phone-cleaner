package com.example.phonecleaner.ui.settings

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import com.example.phonecleaner.features.privacy.PrivacyPolicyActivity
import com.example.phonecleaner.MainActivity
import com.example.phonecleaner.R
import com.example.phonecleaner.features.about.AboutPhoneCleanerActivity
import com.example.phonecleaner.features.notifications.ui.NotificationSettingsActivity
import com.google.android.material.bottomsheet.BottomSheetDialog


class SettingsFragment : Fragment() {

    companion object {

        private const val PREFS_NAME =
            "phone_cleaner_preferences"

        private const val KEY_THEME =
            "theme_mode"

        private const val THEME_LIGHT =
            "light"

        private const val THEME_DARK =
            "dark"

        private const val THEME_SYSTEM =
            "system"
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        return inflater.inflate(
            R.layout.fragment_settings,
            container,
            false
        )
    }


    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {

        super.onViewCreated(
            view,
            savedInstanceState
        )


        (activity as? MainActivity)
            ?.showBottomNavigation()


        // =====================================================
        // General
        // =====================================================

        view.findViewById<View>(
            R.id.cardGeneral
        ).setOnClickListener {

            parentFragmentManager
                .beginTransaction()
                .replace(
                    R.id.mainFragmentContainer,
                    GeneralFragment()
                )
                .addToBackStack(null)
                .commit()
        }


        // =====================================================
        // Notifications
        // =====================================================

        view.findViewById<View>(
            R.id.cardNotifications
        ).setOnClickListener {

            val intent =
                Intent(
                    requireContext(),
                    NotificationSettingsActivity::class.java
                )

            startActivity(
                intent
            )
        }


        // =====================================================
        // Appearance
        // =====================================================

        view.findViewById<View>(
            R.id.cardAppearance
        ).setOnClickListener {

            showAppearanceBottomSheet()
        }


        // =====================================================
        // About Phone Cleaner
        // =====================================================

        view.findViewById<View>(
            R.id.cardAbout
        ).setOnClickListener {

            val intent =
                Intent(
                    requireContext(),
                    AboutPhoneCleanerActivity::class.java
                )

            startActivity(
                intent
            )
        }

        view.findViewById<View>(
            R.id.cardPrivacy
        ).setOnClickListener {

            val intent =
                Intent(
                    requireContext(),
                    PrivacyPolicyActivity::class.java
                )

            startActivity(intent)
        }
    }


    override fun onResume() {

        super.onResume()

        (activity as? MainActivity)
            ?.showBottomNavigation()
    }


    // =========================================================
    // Appearance Bottom Sheet
    // =========================================================

    private fun showAppearanceBottomSheet() {

        val dialog =
            BottomSheetDialog(
                requireContext()
            )


        val sheetView =
            layoutInflater.inflate(
                R.layout.bottom_sheet_appearance,
                null
            )


        dialog.setContentView(
            sheetView
        )


        val radioLight =
            sheetView.findViewById<android.widget.RadioButton>(
                R.id.radioThemeLight
            )


        val radioDark =
            sheetView.findViewById<android.widget.RadioButton>(
                R.id.radioThemeDark
            )


        val radioSystem =
            sheetView.findViewById<android.widget.RadioButton>(
                R.id.radioThemeSystem
            )


        val cardLight =
            sheetView.findViewById<View>(
                R.id.cardThemeLight
            )


        val cardDark =
            sheetView.findViewById<View>(
                R.id.cardThemeDark
            )


        val cardSystem =
            sheetView.findViewById<View>(
                R.id.cardThemeSystem
            )


        // =====================================================
        // Restore Current Selection
        // =====================================================

        val savedTheme =

            requireContext()
                .getSharedPreferences(
                    PREFS_NAME,
                    Context.MODE_PRIVATE
                )
                .getString(
                    KEY_THEME,
                    THEME_SYSTEM
                )


        when (savedTheme) {

            THEME_LIGHT -> {

                radioLight.isChecked =
                    true
            }

            THEME_DARK -> {

                radioDark.isChecked =
                    true
            }

            else -> {

                radioSystem.isChecked =
                    true
            }
        }


        // =====================================================
        // Light
        // =====================================================

        cardLight.setOnClickListener {

            radioLight.isChecked =
                true

            radioDark.isChecked =
                false

            radioSystem.isChecked =
                false


            saveTheme(
                THEME_LIGHT
            )


            AppCompatDelegate.setDefaultNightMode(
                AppCompatDelegate.MODE_NIGHT_NO
            )


            dialog.dismiss()
        }


        // =====================================================
        // Dark
        // =====================================================

        cardDark.setOnClickListener {

            radioLight.isChecked =
                false

            radioDark.isChecked =
                true

            radioSystem.isChecked =
                false


            saveTheme(
                THEME_DARK
            )


            AppCompatDelegate.setDefaultNightMode(
                AppCompatDelegate.MODE_NIGHT_YES
            )


            dialog.dismiss()
        }


        // =====================================================
        // System Default
        // =====================================================

        cardSystem.setOnClickListener {

            radioLight.isChecked =
                false

            radioDark.isChecked =
                false

            radioSystem.isChecked =
                true


            saveTheme(
                THEME_SYSTEM
            )


            AppCompatDelegate.setDefaultNightMode(
                AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
            )


            dialog.dismiss()
        }


        dialog.show()
    }


    // =========================================================
    // Save Theme
    // =========================================================

    private fun saveTheme(
        theme: String
    ) {

        requireContext()
            .getSharedPreferences(
                PREFS_NAME,
                Context.MODE_PRIVATE
            )
            .edit()
            .putString(
                KEY_THEME,
                theme
            )
            .apply()
    }
}