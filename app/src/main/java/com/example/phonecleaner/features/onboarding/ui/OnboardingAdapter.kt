package com.example.phonecleaner.features.onboarding.ui

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

import com.example.phonecleaner.features.onboarding.data.OnboardingItem


class OnboardingAdapter(
    activity: FragmentActivity,
    private val items: List<OnboardingItem>
) : FragmentStateAdapter(
    activity
) {

    override fun getItemCount(): Int =
        items.size


    override fun createFragment(
        position: Int
    ): Fragment {

        return OnboardingPageFragment
            .newInstance(
                items[position]
            )
    }
}