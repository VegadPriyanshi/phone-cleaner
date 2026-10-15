package com.example.phonecleaner.features.onboarding.ui

import android.content.Intent
import android.os.Bundle

import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.viewpager2.widget.ViewPager2

import com.example.phonecleaner.MainActivity
import com.example.phonecleaner.R
import com.example.phonecleaner.core.prefs.AppPrefs
import com.example.phonecleaner.databinding.ActivityOnboardingBinding
import com.example.phonecleaner.features.onboarding.data.OnboardingItem


class OnboardingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOnboardingBinding


    // =========================================================
    // ONBOARDING ITEMS
    // =========================================================

    private val onboardingItems = listOf(

        // =====================================================
        // 1. JUNK FILES
        // =====================================================

        OnboardingItem(
            title = R.string.onboarding_junk_title,
            description = R.string.onboarding_junk_description,
            badgeBackground = R.drawable.bg_onboarding_junk,
            icon = R.drawable.ic_onboarding_junk
        ),


        // =====================================================
        // 2. LARGE FILES
        // =====================================================

        OnboardingItem(
            title = R.string.onboarding_large_title,
            description = R.string.onboarding_large_description,
            badgeBackground = R.drawable.bg_onboarding_large,
            icon = R.drawable.ic_onboarding_large
        ),


        // =====================================================
        // 3. DUPLICATE PHOTOS
        // =====================================================

        OnboardingItem(
            title = R.string.onboarding_duplicate_title,
            description = R.string.onboarding_duplicate_description,
            badgeBackground = R.drawable.bg_onboarding_duplicate,
            icon = R.drawable.ic_onboarding_duplicate
        ),


        // =====================================================
        // 4. UNUSED APPS
        // =====================================================

        OnboardingItem(
            title = R.string.onboarding_apps_title,
            description = R.string.onboarding_apps_description,
            badgeBackground = R.drawable.bg_onboarding_apps,
            icon = R.drawable.ic_onboarding_apps
        ),


        // =====================================================
        // 5. YOUR CONTROL
        // =====================================================

        OnboardingItem(
            title = R.string.onboarding_control_title,
            description = R.string.onboarding_control_description,
            badgeBackground = R.drawable.bg_onboarding_control,
            icon = R.drawable.ic_onboarding_control
        )
    )


    // =========================================================
    // ON CREATE
    // =========================================================

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)
        binding =
            ActivityOnboardingBinding.inflate(
                layoutInflater
            )

        setContentView(binding.root)

        setupSystemInsets()

        setupViewPager()

        setupNextButton()

        setupBackButton()
    }


    // =========================================================
    // VIEW PAGER
    // =========================================================

    private fun setupViewPager() {

        binding.viewPager.adapter =
            OnboardingAdapter(
                activity = this,
                items = onboardingItems
            )

        updateNextButton(0)
        updatePageIndicator(0)

        binding.viewPager.registerOnPageChangeCallback(
            object : ViewPager2.OnPageChangeCallback() {

                override fun onPageSelected(
                    position: Int
                ) {

                    super.onPageSelected(position)

                    updateNextButton(position)
                    updatePageIndicator(position)
                }
            }
        )
    }


    private fun updatePageIndicator(
        position: Int
    ) {

        val dots = listOf(
            binding.dot1,
            binding.dot2,
            binding.dot3,
            binding.dot4,
            binding.dot5
        )

        dots.forEachIndexed { index, dot ->

            dot.setBackgroundResource(
                if (index == position) {
                    R.drawable.bg_onboarding_dot_active
                } else {
                    R.drawable.bg_onboarding_dot_inactive
                }
            )
        }
    }


    // =========================================================
    // NEXT BUTTON
    // =========================================================

    private fun setupNextButton() {

        binding.tvNext.setOnClickListener {

            val current =
                binding.viewPager.currentItem

            if (current < onboardingItems.lastIndex) {

                binding.viewPager.currentItem =
                    current + 1

            } else {

                openHome()
            }
        }
    }


    // =========================================================
    // BUTTON TEXT
    // =========================================================

    private fun updateNextButton(
        position: Int
    ) {

        binding.tvNext.text =

            if (
                position ==
                onboardingItems.lastIndex
            ) {

                getString(
                    R.string.onboarding_get_started
                )

            } else {

                getString(
                    R.string.onboarding_next
                )
            }
    }


    // =========================================================
    // OPEN HOME
    // =========================================================

    // =========================================================
// OPEN HOME
// =========================================================

    private fun openHome() {

        val intent =
            Intent(
                this,
                MainActivity::class.java
            )


        /*
         * Open MainActivity normally.
         *
         * Do NOT use:
         *
         * FLAG_ACTIVITY_NEW_TASK
         * FLAG_ACTIVITY_CLEAR_TASK
         *
         * Those flags can cause a temporary blank/white
         * window while the task is being recreated.
         */

        startActivity(
            intent
        )


        /*
         * Remove the onboarding screen after
         * MainActivity has been launched.
         */

        finish()


        /*
         * Remove the default Activity transition.
         * This makes the change from onboarding → Home
         * visually immediate.
         */

        overridePendingTransition(
            0,
            0
        )
    }

    // =========================================================
    // SYSTEM BAR INSETS
    // =========================================================

    private fun setupSystemInsets() {

        ViewCompat.setOnApplyWindowInsetsListener(
            binding.root
        ) { _, insets ->

            val bars = insets.getInsets(
                WindowInsetsCompat.Type.systemBars()
            )

            // Keep the onboarding content below the status bar.
            binding.viewPager.setPadding(
                0,
                bars.top,
                0,
                0
            )

            // Move the bottom controls above the navigation bar.
            binding.bottomContainer.translationY =
                -bars.bottom.toFloat()

            insets
        }

        ViewCompat.requestApplyInsets(
            binding.root
        )
    }

    // =========================================================
    // BACK BUTTON
    // =========================================================

    private fun setupBackButton() {

        onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(true) {

                override fun handleOnBackPressed() {

                    if (
                        binding.viewPager.currentItem > 0
                    ) {

                        binding.viewPager.currentItem =
                            binding.viewPager.currentItem - 1

                    } else {

                        /*
                         * At the first onboarding page,
                         * do not open Home.
                         *
                         * Close the onboarding activity.
                         */

                        finish()
                    }
                }
            }
        )
    }
}