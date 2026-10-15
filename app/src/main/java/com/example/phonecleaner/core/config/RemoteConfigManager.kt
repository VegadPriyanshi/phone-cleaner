
package com.example.phonecleaner.core.config

import android.util.Log
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.remoteConfigSettings

object RemoteConfigManager {

    private const val TAG = "RemoteConfigManager"

    private const val KEY_ADS_ENABLED = "ads_enabled"
    private const val KEY_LANGUAGE_AD_1_ENABLED = "language_ad_1_enabled"
    private const val KEY_LANGUAGE_AD_2_ENABLED = "language_ad_2_enabled"
    private const val KEY_ONBOARDING_FIRST_ENABLED = "onboarding_first_enabled"
    private const val KEY_ONBOARDING_FULL_ENABLED = "onboarding_full_enabled"
    private const val KEY_ONBOARDING_LAST_ENABLED = "onboarding_last_enabled"

    private val remoteConfig: FirebaseRemoteConfig =
        FirebaseRemoteConfig.getInstance()

    @Volatile
    private var isReady = false

    private val pendingCallbacks = mutableListOf<() -> Unit>()

    private val defaultValues = mapOf(
        KEY_ADS_ENABLED to true,
        KEY_LANGUAGE_AD_1_ENABLED to true,
        KEY_LANGUAGE_AD_2_ENABLED to true,
        KEY_ONBOARDING_FIRST_ENABLED to true,
        KEY_ONBOARDING_FULL_ENABLED to true,
        KEY_ONBOARDING_LAST_ENABLED to true
    )

    fun initialize() {
        val settings = remoteConfigSettings {
            // Development: fetch every time while testing.
            // Before production, change this to 3600 or higher.
            minimumFetchIntervalInSeconds = 0
        }

        remoteConfig.setConfigSettingsAsync(settings)
        remoteConfig.setDefaultsAsync(defaultValues)
    }

    fun fetchAndActivate(onComplete: (Boolean) -> Unit = {}) {
        remoteConfig.fetchAndActivate()
            .addOnCompleteListener { task ->
                isReady = true

                if (task.isSuccessful) {
                    Log.d(TAG, "Remote Config fetch succeeded")
                } else {
                    Log.w(
                        TAG,
                        "Remote Config fetch failed; using defaults",
                        task.exception
                    )
                }

                onComplete(task.isSuccessful)
                notifyReadyCallbacks()
            }
    }

    fun whenReady(callback: () -> Unit) {
        val runImmediately: Boolean

        synchronized(pendingCallbacks) {
            runImmediately = isReady
            if (!runImmediately) {
                pendingCallbacks.add(callback)
            }
        }

        if (runImmediately) {
            callback()
        }
    }

    private fun notifyReadyCallbacks() {
        val callbacks: List<() -> Unit>

        synchronized(pendingCallbacks) {
            callbacks = pendingCallbacks.toList()
            pendingCallbacks.clear()
        }

        callbacks.forEach { callback ->
            runCatching { callback() }
                .onFailure {
                    Log.e(TAG, "Remote Config callback failed", it)
                }
        }
    }

    fun areAdsEnabled(): Boolean =
        remoteConfig.getBoolean(KEY_ADS_ENABLED)

    fun isLanguageAd1Enabled(): Boolean =
        areAdsEnabled() && remoteConfig.getBoolean(KEY_LANGUAGE_AD_1_ENABLED)

    fun isLanguageAd2Enabled(): Boolean =
        areAdsEnabled() && remoteConfig.getBoolean(KEY_LANGUAGE_AD_2_ENABLED)

    fun isOnboardingFirstEnabled(): Boolean =
        areAdsEnabled() && remoteConfig.getBoolean(KEY_ONBOARDING_FIRST_ENABLED)

    fun isOnboardingFullEnabled(): Boolean =
        areAdsEnabled() && remoteConfig.getBoolean(KEY_ONBOARDING_FULL_ENABLED)

    fun isOnboardingLastEnabled(): Boolean =
        areAdsEnabled() && remoteConfig.getBoolean(KEY_ONBOARDING_LAST_ENABLED)
}
