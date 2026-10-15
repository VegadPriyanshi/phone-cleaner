package com.example.phonecleaner

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import com.example.phonecleaner.databinding.ActivitySplashBinding
import com.example.phonecleaner.features.language.ui.LanguageActivity
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds

class SplashActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "SplashActivity"
        private const val MIN_SPLASH_DISPLAY_TIME = 2500L
        private const val MAX_SPLASH_WAIT_TIME = 6000L
        private const val POST_AD_DISPLAY_TIME = 1500L
    }

    private lateinit var binding: ActivitySplashBinding
    private val handler = Handler(Looper.getMainLooper())

    private var navigationStarted = false
    private var splashStartTime = 0L
    private var shimmerAnimator: ObjectAnimator? = null

    private val navigateRunnable = Runnable {
        openLanguageScreen()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        splashStartTime = System.currentTimeMillis()

        WindowCompat.setDecorFitsSystemWindows(window, false)

        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupInsets()

        // Show the shimmer placeholder and start animation
        binding.bannerAdView.visibility = View.INVISIBLE
        binding.bannerShimmer.visibility = View.VISIBLE
        startShimmer()

        // Maximum wait time for splash screen in case ad takes too long
        handler.postDelayed(navigateRunnable, MAX_SPLASH_WAIT_TIME)

        // Initialize Google Mobile Ads and request the banner
        MobileAds.initialize(this) {
            runOnUiThread {
                if (!isFinishing && !isDestroyed && !navigationStarted) {
                    loadBannerAd()
                }
            }
        }
    }

    private fun startShimmer() {
        stopShimmer()

        binding.bannerShimmer.visibility = View.VISIBLE
        binding.shimmerHighlight.translationX = -250f

        shimmerAnimator = ObjectAnimator.ofFloat(
            binding.shimmerHighlight,
            View.TRANSLATION_X,
            -250f,
            800f
        ).apply {
            duration = 1000L
            repeatCount = ValueAnimator.INFINITE
            repeatMode = ValueAnimator.RESTART
            start()
        }
    }

    private fun stopShimmer() {
        shimmerAnimator?.cancel()
        shimmerAnimator = null
    }

    private fun loadBannerAd() {
        if (isFinishing || isDestroyed || navigationStarted) return

        binding.bannerAdView.adListener = object : AdListener() {

            override fun onAdLoaded() {
                if (isFinishing || isDestroyed || navigationStarted) return

                Log.d(TAG, "Banner ad loaded successfully on splash screen")

                stopShimmer()
                binding.bannerShimmer.visibility = View.GONE
                binding.bannerAdView.visibility = View.VISIBLE

                handler.removeCallbacks(navigateRunnable)

                val elapsed = System.currentTimeMillis() - splashStartTime
                val minimumRemaining = (MIN_SPLASH_DISPLAY_TIME - elapsed).coerceAtLeast(0L)

                handler.postDelayed(
                    navigateRunnable,
                    maxOf(POST_AD_DISPLAY_TIME, minimumRemaining)
                )
            }

            override fun onAdFailedToLoad(error: LoadAdError) {
                Log.e(TAG, "Banner ad failed to load: code=${error.code}, message=${error.message}")

                if (isFinishing || isDestroyed || navigationStarted) return

                stopShimmer()
                binding.bannerShimmer.visibility = View.GONE
                binding.bannerAdContainer.visibility = View.GONE

                val elapsed = System.currentTimeMillis() - splashStartTime
                val remaining = (MIN_SPLASH_DISPLAY_TIME - elapsed).coerceAtLeast(0L)

                handler.removeCallbacks(navigateRunnable)
                handler.postDelayed(navigateRunnable, remaining)
            }
        }

        Log.d(TAG, "Requesting banner ad for splash screen")
        binding.bannerAdView.loadAd(AdRequest.Builder().build())
    }

    private fun setupInsets() {
        val initialLeft = binding.root.paddingLeft
        val initialTop = binding.root.paddingTop
        val initialRight = binding.root.paddingRight
        val initialBottom = binding.root.paddingBottom

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())

            view.setPadding(
                initialLeft + systemBars.left,
                initialTop + systemBars.top,
                initialRight + systemBars.right,
                initialBottom + systemBars.bottom
            )

            insets
        }

        ViewCompat.requestApplyInsets(binding.root)
    }

    private fun openLanguageScreen() {
        if (navigationStarted || isFinishing || isDestroyed) return

        navigationStarted = true
        handler.removeCallbacks(navigateRunnable)
        stopShimmer()

        startActivity(
            Intent(this, LanguageActivity::class.java).apply {
                putExtra(LanguageActivity.EXTRA_START_ONBOARDING, true)
            }
        )

        finish()
    }

    override fun onPause() {
        binding.bannerAdView.pause()
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
        binding.bannerAdView.resume()
    }

    override fun onDestroy() {
        handler.removeCallbacks(navigateRunnable)
        stopShimmer()
        binding.bannerAdView.destroy()
        super.onDestroy()
    }
}
