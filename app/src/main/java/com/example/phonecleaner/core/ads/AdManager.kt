package com.example.phonecleaner.core.ads

import android.content.Context
import android.util.Log
import com.example.phonecleaner.R
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdOptions

enum class NativePlacement {
    LANGUAGE_AD_1,
    LANGUAGE_AD_2,
    ONBOARDING_FIRST,
    ONBOARDING_FULL,
    ONBOARDING_LAST
}

object AdManager {

    private const val TAG = "AdManager"

    private val nativeAdCache = mutableMapOf<NativePlacement, NativeAd>()
    private val loadingPlacements = mutableSetOf<NativePlacement>()

    private fun getNativeAdUnitId(
        context: Context,
        placement: NativePlacement
    ): String {
        val resourceId = when (placement) {
            NativePlacement.LANGUAGE_AD_1 -> R.string.native_language_ad_id
            NativePlacement.LANGUAGE_AD_2 -> R.string.native_language_ad_2_id
            NativePlacement.ONBOARDING_FIRST -> R.string.native_onboarding_ad_id
            NativePlacement.ONBOARDING_FULL -> R.string.native_onboarding_full_ad_id
            NativePlacement.ONBOARDING_LAST -> R.string.native_onboarding_last_ad_id
        }

        val adUnitId = context.getString(resourceId)
        Log.d(TAG, "Ad Unit ID for $placement: $adUnitId")
        return adUnitId
    }

    fun preloadNativeAd(context: Context, placement: NativePlacement) {
        synchronized(this) {
            if (nativeAdCache.containsKey(placement) || loadingPlacements.contains(placement)) {
                return
            }
            loadingPlacements.add(placement)
        }

        val appContext = context.applicationContext
        val adUnitId = getNativeAdUnitId(appContext, placement)

        val loader = AdLoader.Builder(appContext, adUnitId)
            .forNativeAd { nativeAd ->
                synchronized(this) {
                    loadingPlacements.remove(placement)
                    nativeAdCache.remove(placement)?.destroy()
                    nativeAdCache[placement] = nativeAd
                }
                Log.d(TAG, "Native ad preloaded: $placement")
            }
            .withAdListener(object : AdListener() {
                override fun onAdFailedToLoad(error: LoadAdError) {
                    synchronized(this@AdManager) {
                        loadingPlacements.remove(placement)
                    }
                    Log.w(TAG, "Native ad preload failed ($placement): ${error.message} (code: ${error.code})")
                }
            })
            .withNativeAdOptions(NativeAdOptions.Builder().build())
            .build()

        loader.loadAd(AdRequest.Builder().build())
    }

    fun consumeNativeAd(placement: NativePlacement): NativeAd? {
        val ad = synchronized(this) {
            nativeAdCache.remove(placement)
        }
        Log.d(TAG, "Native ad consumed ($placement): ${if (ad != null) "READY" else "NOT READY"}")
        return ad
    }

    fun loadNativeAd(
        context: Context,
        placement: NativePlacement,
        onLoaded: (NativeAd) -> Unit,
        onFailed: (LoadAdError) -> Unit
    ) {
        val adUnitId = getNativeAdUnitId(context, placement)

        val loader = AdLoader.Builder(context.applicationContext, adUnitId)
            .forNativeAd { nativeAd ->
                onLoaded(nativeAd)
            }
            .withAdListener(object : AdListener() {
                override fun onAdFailedToLoad(error: LoadAdError) {
                    Log.e(TAG, "Native ad load failed ($placement): ${error.message} (code: ${error.code}, domain: ${error.domain})")
                    onFailed(error)
                }
            })
            .withNativeAdOptions(NativeAdOptions.Builder()
                .setAdChoicesPlacement(NativeAdOptions.ADCHOICES_TOP_RIGHT)
                .setRequestMultipleImages(false)
                .build())
            .build()

        loader.loadAd(AdRequest.Builder().build())
    }

    fun clearCachedAds() {
        synchronized(this) {
            nativeAdCache.values.forEach { it.destroy() }
            nativeAdCache.clear()
            loadingPlacements.clear()
        }
    }
}
