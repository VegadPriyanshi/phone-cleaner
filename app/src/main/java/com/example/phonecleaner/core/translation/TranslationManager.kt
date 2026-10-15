package com.example.phonecleaner.core.translation

import android.view.View
import android.view.ViewGroup
import android.widget.TextView

import com.example.phonecleaner.R
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.Translator
import com.google.mlkit.nl.translate.TranslatorOptions

object TranslationManager {

    private var activeTranslator: Translator? = null

    private var activeLanguageCode: String? = null

    private var isDownloading = false

    private val pendingTranslations =
        mutableListOf<() -> Unit>()


    // =========================================================
    // Prepare Translator
    // =========================================================

    fun prepare(
        targetLanguageCode: String,
        onReady: () -> Unit,
        onError: (Exception) -> Unit
    ) {

        // English does not need ML Kit.
        if (
            targetLanguageCode ==
            TranslateLanguage.ENGLISH
        ) {

            activeTranslator?.close()

            activeTranslator = null

            activeLanguageCode =
                TranslateLanguage.ENGLISH

            isDownloading = false

            onReady()

            return
        }


        // Translator is already ready.
        if (
            targetLanguageCode ==
            activeLanguageCode &&
            activeTranslator != null &&
            !isDownloading
        ) {

            onReady()

            return
        }


        isDownloading = true

        activeLanguageCode =
            targetLanguageCode


        activeTranslator?.close()


        val options =
            TranslatorOptions.Builder()
                .setSourceLanguage(
                    TranslateLanguage.ENGLISH
                )
                .setTargetLanguage(
                    targetLanguageCode
                )
                .build()


        val translator =
            Translation.getClient(options)


        activeTranslator =
            translator


        val conditions =
            DownloadConditions.Builder()
                .build()


        translator
            .downloadModelIfNeeded(conditions)

            .addOnSuccessListener {

                isDownloading = false

                onReady()

                flushPending()
            }

            .addOnFailureListener { error ->

                isDownloading = false

                activeTranslator = null

                activeLanguageCode = null

                onError(error)

                flushPending()
            }
    }


    // =========================================================
    // Translate Text
    // =========================================================

    fun translate(
        text: String,
        onResult: (String) -> Unit
    ) {

        if (
            text.isBlank()
        ) {

            onResult(text)

            return
        }


        if (
            activeLanguageCode.isNullOrBlank() ||
            activeLanguageCode ==
            TranslateLanguage.ENGLISH
        ) {

            onResult(text)

            return
        }


        if (isDownloading) {

            pendingTranslations.add {

                translate(
                    text,
                    onResult
                )
            }

            return
        }


        val translator =
            activeTranslator


        if (translator == null) {

            onResult(text)

            return
        }


        translator
            .translate(text)

            .addOnSuccessListener { translatedText ->

                onResult(translatedText)
            }

            .addOnFailureListener {

                // Keep original English text
                // if translation fails.
                onResult(text)
            }
    }


    // =========================================================
    // Translate One TextView
    // =========================================================

    fun translateView(
        view: TextView
    ) {

        var originalText =
            view.getTag(
                R.id.translation_original_text
            ) as? String


        if (originalText == null) {

            originalText =
                view.text
                    ?.toString()
                    ?: ""


            if (originalText.isNotBlank()) {

                view.setTag(
                    R.id.translation_original_text,
                    originalText
                )
            }
        }


        if (originalText.isBlank()) {
            return
        }


        translate(
            originalText
        ) { translatedText ->

            view.post {

                view.text =
                    translatedText
            }
        }
    }


    // =========================================================
    // Translate Complete Screen
    // =========================================================

    fun translateAllViews(
        root: View
    ) {

        if (root is TextView) {

            translateView(root)

            return
        }


        if (root is ViewGroup) {

            for (
            index in 0 until root.childCount
            ) {

                translateAllViews(
                    root.getChildAt(index)
                )
            }
        }
    }


    // =========================================================
    // Translate Screen After Translator Is Ready
    // =========================================================

    fun translateScreen(
        root: View,
        languageCode: String,
        onError: ((Exception) -> Unit)? = null
    ) {

        prepare(

            targetLanguageCode = languageCode,

            onReady = {

                root.post {

                    translateAllViews(root)
                }
            },

            onError = { error ->

                onError?.invoke(error)
            }
        )
    }


    // =========================================================
    // Pending Translation
    // =========================================================

    private fun flushPending() {

        val pending =
            ArrayList(
                pendingTranslations
            )

        pendingTranslations.clear()


        pending.forEach { action ->

            action()
        }
    }


    // =========================================================
    // Release
    // =========================================================

    fun release() {

        activeTranslator?.close()

        activeTranslator = null

        activeLanguageCode = null

        isDownloading = false

        pendingTranslations.clear()
    }
}