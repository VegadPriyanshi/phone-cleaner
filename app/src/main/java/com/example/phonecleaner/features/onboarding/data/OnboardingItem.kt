package com.example.phonecleaner.features.onboarding.data

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes


data class OnboardingItem(

    @StringRes
    val title: Int,

    @StringRes
    val description: Int,

    @DrawableRes
    val badgeBackground: Int,

    @DrawableRes
    val icon: Int
)