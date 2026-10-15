package com.example.phonecleaner.features.language.data

import com.google.mlkit.nl.translate.TranslateLanguage

data class Language(
    val displayName: String,
    val nativeName: String,
    val translateCode: String
)

object Languages {

    val ALL = listOf(

        Language(
            displayName = "English",
            nativeName = "English",
            translateCode = TranslateLanguage.ENGLISH
        ),

        Language(
            displayName = "Hindi",
            nativeName = "हिन्दी",
            translateCode = TranslateLanguage.HINDI
        ),

        Language(
            displayName = "Spanish",
            nativeName = "Español",
            translateCode = TranslateLanguage.SPANISH
        ),

        Language(
            displayName = "French",
            nativeName = "Français",
            translateCode = TranslateLanguage.FRENCH
        ),

        Language(
            displayName = "German",
            nativeName = "Deutsch",
            translateCode = TranslateLanguage.GERMAN
        ),

        Language(
            displayName = "Portuguese",
            nativeName = "Português",
            translateCode = TranslateLanguage.PORTUGUESE
        ),

        Language(
            displayName = "Russian",
            nativeName = "Русский",
            translateCode = TranslateLanguage.RUSSIAN
        ),

        Language(
            displayName = "Arabic",
            nativeName = "العربية",
            translateCode = TranslateLanguage.ARABIC
        ),

        Language(
            displayName = "Chinese",
            nativeName = "中文",
            translateCode = TranslateLanguage.CHINESE
        ),

        Language(
            displayName = "Japanese",
            nativeName = "日本語",
            translateCode = TranslateLanguage.JAPANESE
        )
    )
}