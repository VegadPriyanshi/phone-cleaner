package com.example.phonecleaner.features.onboarding.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import androidx.fragment.app.Fragment

import com.example.phonecleaner.R
import com.example.phonecleaner.core.prefs.AppPrefs.selectedLanguageCode
import com.example.phonecleaner.core.translation.TranslationManager
import com.example.phonecleaner.databinding.FragmentOnboardingGenericBinding
import com.example.phonecleaner.features.onboarding.data.OnboardingItem


class OnboardingPageFragment :
    Fragment() {

    private var _binding:
            FragmentOnboardingGenericBinding? =
        null


    private val binding:
            FragmentOnboardingGenericBinding
        get() = _binding!!


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding =
            FragmentOnboardingGenericBinding.inflate(
                inflater,
                container,
                false
            )


        return binding.root
    }


    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {

        super.onViewCreated(
            view,
            savedInstanceState
        )


        val titleRes =
            arguments?.getInt(
                ARG_TITLE
            ) ?: 0


        val descriptionRes =
            arguments?.getInt(
                ARG_DESCRIPTION
            ) ?: 0


        val badgeBackgroundRes =
            arguments?.getInt(
                ARG_BADGE_BACKGROUND
            ) ?: 0


        val iconRes =
            arguments?.getInt(
                ARG_ICON
            ) ?: 0


        if (titleRes != 0) {

            binding.tvTitle.setText(
                titleRes
            )
        }


        if (descriptionRes != 0) {

            binding.tvDescription.setText(
                descriptionRes
            )
        }


        if (badgeBackgroundRes != 0) {

            binding.badgeContainer
                .setBackgroundResource(
                    badgeBackgroundRes
                )
        }


        if (iconRes != 0) {

            binding.ivOnboardingIcon
                .setImageResource(
                    iconRes
                )
        }


        /*
         * Translate this onboarding page using
         * the language selected on the Language screen.
         *
         * English remains unchanged.
         */
        val languageCode =
            requireContext()
                .selectedLanguageCode


        if (
            !languageCode.isNullOrBlank()
        ) {

            TranslationManager.translateScreen(
                root = view,
                languageCode = languageCode
            )
        }
    }


    override fun onDestroyView() {

        super.onDestroyView()

        _binding = null
    }


    companion object {

        private const val ARG_TITLE =
            "onboarding_title"


        private const val ARG_DESCRIPTION =
            "onboarding_description"


        private const val ARG_BADGE_BACKGROUND =
            "onboarding_badge_background"


        private const val ARG_ICON =
            "onboarding_icon"


        fun newInstance(
            item: OnboardingItem
        ): OnboardingPageFragment {

            return OnboardingPageFragment()
                .apply {

                    arguments =
                        Bundle().apply {

                            putInt(
                                ARG_TITLE,
                                item.title
                            )

                            putInt(
                                ARG_DESCRIPTION,
                                item.description
                            )

                            putInt(
                                ARG_BADGE_BACKGROUND,
                                item.badgeBackground
                            )

                            putInt(
                                ARG_ICON,
                                item.icon
                            )
                        }
                }
        }
    }
}