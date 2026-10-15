package com.example.phonecleaner.ui.settings

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment

import com.example.phonecleaner.MainActivity
import com.example.phonecleaner.R


class AppearanceFragment : Fragment() {

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
            R.layout.fragment_appearance,
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


        val lightOption =
            view.findViewById<View>(
                R.id.cardThemeLight
            )

        val darkOption =
            view.findViewById<View>(
                R.id.cardThemeDark
            )

        val systemOption =
            view.findViewById<View>(
                R.id.cardThemeSystem
            )


        updateSelectedThemeUI(
            view
        )


        lightOption.setOnClickListener {

            saveTheme(
                THEME_LIGHT
            )

            AppCompatDelegate.setDefaultNightMode(
                AppCompatDelegate.MODE_NIGHT_NO
            )

            updateSelectedThemeUI(
                view
            )
        }


        darkOption.setOnClickListener {

            saveTheme(
                THEME_DARK
            )

            AppCompatDelegate.setDefaultNightMode(
                AppCompatDelegate.MODE_NIGHT_YES
            )

            updateSelectedThemeUI(
                view
            )
        }


        systemOption.setOnClickListener {

            saveTheme(
                THEME_SYSTEM
            )

            AppCompatDelegate.setDefaultNightMode(
                AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
            )

            updateSelectedThemeUI(
                view
            )
        }


        view.findViewById<View>(
            R.id.btnAppearanceBack
        ).setOnClickListener {

            parentFragmentManager.popBackStack()
        }
    }


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


    private fun getSavedTheme(): String {

        return requireContext()
            .getSharedPreferences(
                PREFS_NAME,
                Context.MODE_PRIVATE
            )
            .getString(
                KEY_THEME,
                THEME_SYSTEM
            )
            ?: THEME_SYSTEM
    }


    private fun updateSelectedThemeUI(
        view: View
    ) {

        val selectedTheme =
            getSavedTheme()


        val lightCheck =
            view.findViewById<View>(
                R.id.ivThemeLightSelected
            )

        val darkCheck =
            view.findViewById<View>(
                R.id.ivThemeDarkSelected
            )

        val systemCheck =
            view.findViewById<View>(
                R.id.ivThemeSystemSelected
            )


        lightCheck.visibility =
            if (
                selectedTheme ==
                THEME_LIGHT
            ) {
                View.VISIBLE
            } else {
                View.GONE
            }


        darkCheck.visibility =
            if (
                selectedTheme ==
                THEME_DARK
            ) {
                View.VISIBLE
            } else {
                View.GONE
            }


        systemCheck.visibility =
            if (
                selectedTheme ==
                THEME_SYSTEM
            ) {
                View.VISIBLE
            } else {
                View.GONE
            }
    }
}