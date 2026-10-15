
package com.example.phonecleaner.features.language.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast

import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager

import com.example.phonecleaner.MainActivity
import com.example.phonecleaner.R
import com.example.phonecleaner.core.prefs.AppPrefs.selectedLanguageCode
import com.example.phonecleaner.core.translation.TranslationManager
import com.example.phonecleaner.databinding.ActivityLanguageBinding
import com.example.phonecleaner.features.language.data.Languages
import com.example.phonecleaner.features.onboarding.ui.OnboardingActivity

import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
import com.google.android.gms.ads.nativead.MediaView
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdOptions
import com.google.android.gms.ads.nativead.NativeAdView

class LanguageActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_START_ONBOARDING =
            "extra_start_onboarding"

        const val EXTRA_FROM_SETTINGS =
            "extra_from_settings"

        private const val TAG = "LanguageActivity"
    }

    private lateinit var binding: ActivityLanguageBinding

    private var languageNativeAd: NativeAd? = null
    private var languageNativeAdView: NativeAdView? = null

    private var adRequestVersion = 0
    private var adsInitialized = false

    private var pendingAdUnitIdRes: Int =
        R.string.native_language_ad_id

    private var pickedLanguageCode: String? = null
    private var shouldStartOnboarding = true
    private var openedFromSettings = false

    // ---------------------------------------------------------
    // Activity Creation
    // ---------------------------------------------------------

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()

        super.onCreate(savedInstanceState)

        val splashStartTime = System.currentTimeMillis()

        splashScreen.setKeepOnScreenCondition {
            System.currentTimeMillis() - splashStartTime < 1500L
        }

        WindowCompat.setDecorFitsSystemWindows(window, false)

        binding = ActivityLanguageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        openedFromSettings = intent.getBooleanExtra(
            EXTRA_FROM_SETTINGS,
            false
        )

        shouldStartOnboarding = if (openedFromSettings) {
            false
        } else {
            intent.getBooleanExtra(
                EXTRA_START_ONBOARDING,
                true
            )
        }

        setupSystemInsets()
        setupLanguageList()

        binding.btnDone.setOnClickListener {
            onDoneClicked()
        }

        // First native ad is requested when the screen opens.
        initializeLanguageAds()

        // Long-press the background to open Ad Inspector.
        binding.root.setOnLongClickListener {
            MobileAds.openAdInspector(this) { error ->
                if (error != null) {
                    Log.e(
                        TAG,
                        "Ad Inspector error: ${error.message}"
                    )
                }
            }
            true
        }
    }

    // ---------------------------------------------------------
    // Initialize Ads
    // ---------------------------------------------------------

    private fun initializeLanguageAds() {
        // No shimmer or placeholder.
        binding.nativeAdContainer.visibility = View.GONE

        val configuration = RequestConfiguration.Builder()
            .setTestDeviceIds(
                listOf("49D2FE166B3B09664473CE2BCAFFC7F6")
            )
            .build()

        MobileAds.setRequestConfiguration(configuration)

        MobileAds.initialize(this) {
            runOnUiThread {
                if (isFinishing || isDestroyed) {
                    return@runOnUiThread
                }

                adsInitialized = true

                Log.d(TAG, "Mobile Ads SDK initialized")

                // Use the latest requested placement.
                loadLanguageNativeAd(pendingAdUnitIdRes)
            }
        }
    }

    // ---------------------------------------------------------
    // Load Native Ad
    // ---------------------------------------------------------

    private fun loadLanguageNativeAd(adUnitIdRes: Int) {
        pendingAdUnitIdRes = adUnitIdRes

        if (!adsInitialized) {
            Log.d(
                TAG,
                "Ads SDK is not ready. Ad request queued."
            )
            return
        }

        val requestVersion = ++adRequestVersion

        // Keep the current ad visible while a replacement loads.
        if (languageNativeAdView == null) {
            binding.nativeAdContainer.visibility = View.GONE
        }

        val adLoader = AdLoader.Builder(
            this,
            getString(adUnitIdRes)
        )
            .forNativeAd { nativeAd ->

                runOnUiThread {
                    if (
                        isFinishing ||
                        isDestroyed ||
                        requestVersion != adRequestVersion
                    ) {
                        nativeAd.destroy()
                        return@runOnUiThread
                    }

                    val adView = layoutInflater.inflate(
                        R.layout.item_native_language_ad,
                        binding.nativeAdContainer,
                        false
                    ) as NativeAdView

                    populateNativeAdView(
                        nativeAd,
                        adView
                    )

                    // Remove the old ad view before replacing it.
                    languageNativeAdView?.let { oldView ->
                        binding.nativeAdContainer.removeView(oldView)
                    }

                    languageNativeAd?.destroy()

                    languageNativeAd = nativeAd
                    languageNativeAdView = adView

                    binding.nativeAdContainer.removeAllViews()
                    binding.nativeAdContainer.addView(adView)

                    // Display the container only after successful loading.
                    binding.nativeAdContainer.visibility = View.VISIBLE

                    Log.d(
                        TAG,
                        "Native ad loaded successfully. Resource: $adUnitIdRes"
                    )
                }
            }
            .withAdListener(
                object : AdListener() {

                    override fun onAdFailedToLoad(
                        error: LoadAdError
                    ) {
                        Log.e(
                            TAG,
                            "Native ad failed: ${error.message}; " +
                                    "code=${error.code}; domain=${error.domain}"
                        )

                        runOnUiThread {
                            if (
                                isFinishing ||
                                isDestroyed ||
                                requestVersion != adRequestVersion
                            ) {
                                return@runOnUiThread
                            }

                            // No placeholder. Hide only if no ad is available.
                            binding.nativeAdContainer.visibility =
                                if (languageNativeAdView != null) {
                                    View.VISIBLE
                                } else {
                                    View.GONE
                                }
                        }
                    }
                }
            )
            .withNativeAdOptions(
                NativeAdOptions.Builder().build()
            )
            .build()

        adLoader.loadAd(
            AdRequest.Builder().build()
        )
    }

    // ---------------------------------------------------------
    // Populate Native Ad
    // ---------------------------------------------------------

    private fun populateNativeAdView(
        nativeAd: NativeAd,
        adView: NativeAdView
    ) {
        val headline = adView.findViewById<TextView>(
            R.id.tvAdHeadline
        )

        val body = adView.findViewById<TextView>(
            R.id.tvAdBody
        )

        val icon = adView.findViewById<ImageView>(
            R.id.ivAdIcon
        )

        val action = adView.findViewById<Button>(
            R.id.btnAdAction
        )

        // Headline
        adView.headlineView = headline
        headline.text = nativeAd.headline

        // Body
        adView.bodyView = body

        if (nativeAd.body.isNullOrBlank()) {
            body.visibility = View.GONE
        } else {
            body.visibility = View.VISIBLE
            body.text = nativeAd.body
        }

        // Call to action
        adView.callToActionView = action

        if (nativeAd.callToAction.isNullOrBlank()) {
            action.visibility = View.GONE
        } else {
            action.visibility = View.VISIBLE
            action.text = nativeAd.callToAction
        }

        // Icon
        adView.iconView = icon

        if (nativeAd.icon == null) {
            icon.visibility = View.GONE
        } else {
            icon.setImageDrawable(nativeAd.icon?.drawable)
            icon.visibility = View.VISIBLE
        }

        // Main media, if item_native_language_ad.xml includes MediaView.
        val mediaView = adView.findViewById<MediaView>(
            R.id.adMediaView
        )

        if (mediaView != null) {
            adView.mediaView = mediaView
        }

        // Register the ad assets with the SDK.
        adView.setNativeAd(nativeAd)
    }

    // ---------------------------------------------------------
    // Language List
    // ---------------------------------------------------------

    private fun setupLanguageList() {
        // Normal launch: no language selected initially.
        // Settings: show the currently saved language.
        val savedLanguage =
            if (openedFromSettings) {
                applicationContext.selectedLanguageCode
            } else {
                null
            }

        pickedLanguageCode = null

        binding.rvLanguages.layoutManager =
            LinearLayoutManager(this)

        binding.rvLanguages.adapter = LanguageAdapter(
            languages = Languages.ALL,
            initiallySelectedCode = savedLanguage
        ) { language ->

            pickedLanguageCode = language.translateCode

            binding.btnDone.visibility = View.VISIBLE

            // Second placement: user selects a language.
            loadLanguageNativeAd(
                R.string.native_language_ad_2_id
            )
        }

        binding.btnDone.visibility =
            if (
                openedFromSettings &&
                !savedLanguage.isNullOrBlank()
            ) {
                View.VISIBLE
            } else {
                View.INVISIBLE
            }
    }

    // ---------------------------------------------------------
    // System Bar Insets
    // ---------------------------------------------------------

    private fun setupSystemInsets() {
        val initialRootTopPadding =
            binding.root.paddingTop

        val initialAdBottomMargin =
            (
                    binding.nativeAdContainer.layoutParams
                            as ViewGroup.MarginLayoutParams
                    ).bottomMargin

        ViewCompat.setOnApplyWindowInsetsListener(
            binding.root
        ) { _, insets ->

            val systemBars = insets.getInsets(
                WindowInsetsCompat.Type.systemBars()
            )

            // Keep content below the status bar.
            binding.root.setPadding(
                binding.root.paddingLeft,
                initialRootTopPadding + systemBars.top,
                binding.root.paddingRight,
                binding.root.paddingBottom
            )

            // Keep the ad above the navigation bar.
            val adLayoutParams =
                binding.nativeAdContainer.layoutParams
                        as ViewGroup.MarginLayoutParams

            adLayoutParams.bottomMargin =
                initialAdBottomMargin + systemBars.bottom

            binding.nativeAdContainer.layoutParams =
                adLayoutParams

            insets
        }

        ViewCompat.requestApplyInsets(binding.root)
    }

    // ---------------------------------------------------------
    // Done Button
    // ---------------------------------------------------------

    private fun onDoneClicked() {
        val selectedCode =
            pickedLanguageCode
                ?: applicationContext.selectedLanguageCode
                ?: Languages.ALL.first().translateCode

        binding.downloadOverlay.visibility = View.VISIBLE
        binding.btnDone.isEnabled = false

        TranslationManager.prepare(
            targetLanguageCode = selectedCode,

            onReady = {
                runOnUiThread {
                    if (isFinishing || isDestroyed) {
                        return@runOnUiThread
                    }

                    applicationContext.selectedLanguageCode =
                        selectedCode

                    binding.downloadOverlay.visibility = View.GONE
                    binding.btnDone.isEnabled = true

                    when {
                        openedFromSettings -> {
                            setResult(RESULT_OK)
                            finish()
                        }

                        shouldStartOnboarding -> {
                            startActivity(
                                Intent(
                                    this,
                                    OnboardingActivity::class.java
                                )
                            )
                            finish()
                        }

                        else -> {
                            startActivity(
                                Intent(
                                    this,
                                    MainActivity::class.java
                                )
                            )
                            finish()
                        }
                    }
                }
            },

            onError = { error ->
                runOnUiThread {
                    if (isFinishing || isDestroyed) {
                        return@runOnUiThread
                    }

                    binding.downloadOverlay.visibility = View.GONE
                    binding.btnDone.isEnabled = true

                    Toast.makeText(
                        this,
                        getString(
                            R.string.language_download_failed,
                            error.localizedMessage
                                ?: "Please try again"
                        ),
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        )
    }

    // ---------------------------------------------------------
    // Destroy
    // ---------------------------------------------------------

    override fun onDestroy() {
        // Invalidate any outstanding ad callbacks.
        adRequestVersion++

        languageNativeAd?.destroy()
        languageNativeAd = null
        languageNativeAdView = null

        super.onDestroy()
    }
}
