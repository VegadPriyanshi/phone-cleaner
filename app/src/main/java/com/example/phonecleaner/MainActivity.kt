package com.example.phonecleaner

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge

import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager

import com.example.phonecleaner.core.prefs.AppPrefs.selectedLanguageCode
import com.example.phonecleaner.core.translation.TranslationManager
import com.example.phonecleaner.databinding.ActivityMainBinding
import com.example.phonecleaner.features.notifications.lowstorage.LowStorageScheduler
import com.example.phonecleaner.ui.apps.AppsFragment
import com.example.phonecleaner.ui.clean.CleanFragment
import com.example.phonecleaner.ui.home.HomeFragment
import com.example.phonecleaner.ui.settings.SettingsFragment


class MainActivity : AppCompatActivity() {

    companion object {

        /** Used by notifications to open a specific tab. */
        const val EXTRA_OPEN_TAB =
            "open_tab"

        const val TAB_CLEAN =
            "clean"
    }


    private lateinit var binding:
            ActivityMainBinding


    // =========================================================
    // onCreate
    // =========================================================

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {

        /*
         * Apply saved theme BEFORE creating
         * the Activity UI.
         */
        applySavedTheme()


        super.onCreate(
            savedInstanceState
        )


        /*
         * IMPORTANT:
         *
         * Language and Onboarding are NOT handled
         * from MainActivity anymore.
         *
         * The application flow is:
         *
         * App Launch
         *      ↓
         * LanguageActivity
         *      ↓
         * OnboardingActivity
         *      ↓
         * MainActivity
         *      ↓
         * Home
         *
         * Therefore MainActivity should directly
         * create the Home UI.
         */

        WindowCompat.setDecorFitsSystemWindows(window, false)


        // =====================================================
        // View Binding
        // =====================================================

        binding =
            ActivityMainBinding.inflate(
                layoutInflater
            )


        setContentView(
            binding.root
        )


        // =====================================================
        // Setup
        // =====================================================

        setupAutomaticTranslation()

        setupSystemBarInsets()

        setupHomeScreen(
            savedInstanceState
        )

        setupBottomNavigation()


        // =====================================================
        // Notification Intent
        // =====================================================

        handleNotificationIntent(
            intent
        )


        // =====================================================
        // Low Storage Scheduler
        // =====================================================

        /*
         * Keeps the low storage check scheduled.
         */

        LowStorageScheduler
            .rescheduleIfEnabled(
                this
            )
    }


    // =========================================================
    // Notification Intent
    // =========================================================

    private fun handleNotificationIntent(
        intent: Intent?
    ) {

        if (
            intent?.getStringExtra(
                EXTRA_OPEN_TAB
            ) ==
            TAB_CLEAN
        ) {

            binding.bottomNavigation
                .selectedItemId =
                R.id.navigation_clean
        }
    }


    // =========================================================
    // Home Screen
    // =========================================================

    private fun setupHomeScreen(
        savedInstanceState: Bundle?
    ) {

        /*
         * Clear any old fragment back stack.
         */

        supportFragmentManager.popBackStack(
            null,
            FragmentManager
                .POP_BACK_STACK_INCLUSIVE
        )


        /*
         * Open HomeFragment as the default
         * screen of MainActivity.
         */

        supportFragmentManager
            .beginTransaction()
            .replace(
                R.id.mainFragmentContainer,
                HomeFragment()
            )
            .commit()


        /*
         * Select Home in bottom navigation.
         */

        binding.bottomNavigation
            .selectedItemId =
            R.id.navigation_home
    }


    // =========================================================
    // Automatic Translation
    // =========================================================

    private fun setupAutomaticTranslation() {

        supportFragmentManager
            .registerFragmentLifecycleCallbacks(

                object :
                    FragmentManager
                    .FragmentLifecycleCallbacks() {

                    override fun onFragmentViewCreated(

                        fragmentManager:
                        FragmentManager,

                        fragment:
                        Fragment,

                        view:
                        View,

                        savedInstanceState:
                        Bundle?
                    ) {

                        super.onFragmentViewCreated(

                            fragmentManager,

                            fragment,

                            view,

                            savedInstanceState
                        )


                        applyLanguageToView(
                            view
                        )
                    }
                },

                true
            )
    }


    // =========================================================
    // Apply Language To View
    // =========================================================

    private fun applyLanguageToView(
        view: View
    ) {

        val languageCode =
            applicationContext
                .selectedLanguageCode


        if (
            languageCode.isNullOrBlank()
        ) {

            return
        }


        TranslationManager.translateScreen(

            root =
                view,

            languageCode =
                languageCode
        )
    }


    // =========================================================
    // Apply Current Language
    // =========================================================

    fun applyCurrentLanguage() {

        val languageCode =
            applicationContext
                .selectedLanguageCode


        if (
            languageCode.isNullOrBlank()
        ) {

            return
        }


        TranslationManager.prepare(

            targetLanguageCode =
                languageCode,

            onReady = {

                runOnUiThread {

                    TranslationManager
                        .translateAllViews(
                            binding.root
                        )
                }
            },

            onError = {

                /*
                 * Keep English if translation fails.
                 */
            }
        )
    }


    // =========================================================
    // Bottom Navigation
    // =========================================================

    private fun setupBottomNavigation() {

        binding.bottomNavigation
            .setOnItemSelectedListener { item ->

                when (item.itemId) {

                    // -------------------------------------------------
                    // HOME
                    // -------------------------------------------------

                    R.id.navigation_home -> {

                        supportFragmentManager
                            .beginTransaction()
                            .replace(
                                R.id.mainFragmentContainer,
                                HomeFragment()
                            )
                            .commit()

                        true
                    }


                    // -------------------------------------------------
                    // CLEAN
                    // -------------------------------------------------

                    R.id.navigation_clean -> {

                        supportFragmentManager
                            .beginTransaction()
                            .replace(
                                R.id.mainFragmentContainer,
                                CleanFragment()
                            )
                            .commit()

                        true
                    }


                    // -------------------------------------------------
                    // APPS
                    // -------------------------------------------------

                    R.id.navigation_apps -> {

                        supportFragmentManager
                            .beginTransaction()
                            .replace(
                                R.id.mainFragmentContainer,
                                AppsFragment()
                            )
                            .commit()

                        true
                    }


                    // -------------------------------------------------
                    // SETTINGS
                    // -------------------------------------------------

                    R.id.navigation_settings -> {

                        supportFragmentManager
                            .beginTransaction()
                            .replace(
                                R.id.mainFragmentContainer,
                                SettingsFragment()
                            )
                            .commit()

                        true
                    }


                    // -------------------------------------------------
                    // UNKNOWN
                    // -------------------------------------------------

                    else -> {

                        false
                    }
                }
            }
    }


    // =========================================================
    // Saved Theme
    // =========================================================

    private fun applySavedTheme() {

        val preferences =
            getSharedPreferences(
                "phone_cleaner_preferences",
                MODE_PRIVATE
            )


        when (
            preferences.getString(
                "theme_mode",
                "system"
            )
        ) {

            // -------------------------------------------------
            // LIGHT
            // -------------------------------------------------

            "light" -> {

                androidx.appcompat.app
                    .AppCompatDelegate
                    .setDefaultNightMode(

                        androidx.appcompat.app
                            .AppCompatDelegate
                            .MODE_NIGHT_NO
                    )
            }


            // -------------------------------------------------
            // DARK
            // -------------------------------------------------

            "dark" -> {

                androidx.appcompat.app
                    .AppCompatDelegate
                    .setDefaultNightMode(

                        androidx.appcompat.app
                            .AppCompatDelegate
                            .MODE_NIGHT_YES
                    )
            }


            // -------------------------------------------------
            // SYSTEM
            // -------------------------------------------------

            else -> {

                androidx.appcompat.app
                    .AppCompatDelegate
                    .setDefaultNightMode(

                        androidx.appcompat.app
                            .AppCompatDelegate
                            .MODE_NIGHT_FOLLOW_SYSTEM
                    )
            }
        }
    }


    // =========================================================
    // System Bar Insets
    // =========================================================

    private fun setupSystemBarInsets() {

        ViewCompat.setOnApplyWindowInsetsListener(

            binding.root

        ) { _, insets ->

            val systemBars =
                insets.getInsets(
                    WindowInsetsCompat
                        .Type.systemBars()
                )


            // -------------------------------------------------
            // Bottom Navigation
            // -------------------------------------------------

            binding.bottomNavigation.setPadding(

                binding.bottomNavigation.paddingLeft,

                binding.bottomNavigation.paddingTop,

                binding.bottomNavigation.paddingRight,

                systemBars.bottom
            )


            insets
        }
    }


    // =========================================================
    // Bottom Navigation Visibility
    // =========================================================

    fun showBottomNavigation() {

        binding.bottomNavigation.visibility =
            View.VISIBLE
    }


    fun hideBottomNavigation() {

        binding.bottomNavigation.visibility =
            View.GONE
    }
}