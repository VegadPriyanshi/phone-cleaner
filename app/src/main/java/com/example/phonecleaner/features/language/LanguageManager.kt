package com.example.phonecleaner.features.language


import android.content.Context

import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat

import com.example.phonecleaner.core.prefs.AppPrefs.selectedLanguageCode


object LanguageManager {


    // =========================================================
    // Supported Languages
    // =========================================================

    const val ENGLISH = "en"

    const val HINDI = "hi"


    // =========================================================
    // Apply Saved Language
    // =========================================================

    fun applySavedLanguage(
        context: Context
    ) {

        val languageCode =
            context.selectedLanguageCode
                ?: ENGLISH

        applyLanguage(
            languageCode
        )
    }


    // =========================================================
    // Apply Language
    // =========================================================

    fun applyLanguage(
        languageCode: String?
    ) {

        val code =
            languageCode
                ?.takeIf {
                    it.isNotBlank()
                }
                ?: ENGLISH


        val localeList =
            LocaleListCompat.forLanguageTags(
                code
            )


        /*
         * AppCompat applies the selected locale
         * to the application.
         *
         * This causes the affected Activity to
         * recreate so the current screen can
         * immediately use the new language.
         */
        AppCompatDelegate.setApplicationLocales(
            localeList
        )
    }


    // =========================================================
    // Save + Apply Language
    // =========================================================

    fun setLanguage(
        context: Context,
        languageCode: String
    ) {

        /*
         * Save the selected language.
         */
        context.selectedLanguageCode =
            languageCode


        /*
         * Apply it immediately.
         */
        applyLanguage(
            languageCode
        )
    }


    // =========================================================
    // Reset To English
    // =========================================================

    fun resetToEnglish(
        context: Context
    ) {

        /*
         * Save English as the default language.
         */
        context.selectedLanguageCode =
            ENGLISH


        /*
         * Apply English immediately.
         */
        applyLanguage(
            ENGLISH
        )
    }


    // =========================================================
    // Get Current Language
    // =========================================================

    fun getCurrentLanguageCode(
        context: Context
    ): String {

        return context.selectedLanguageCode
            ?.takeIf {
                it.isNotBlank()
            }
            ?: ENGLISH
    }


    // =========================================================
    // Get Display Language Name
    // =========================================================

    fun getLanguageDisplayName(
        context: Context
    ): String {

        return when (
            getCurrentLanguageCode(
                context
            )
        ) {

            HINDI ->
                "हिन्दी"

            else ->
                "English"
        }
    }
}