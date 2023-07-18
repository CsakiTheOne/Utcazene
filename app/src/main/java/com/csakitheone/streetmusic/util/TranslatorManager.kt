package com.csakitheone.streetmusic.util

import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.TranslatorOptions

class TranslatorManager {
    companion object {

        fun translateDescription(
            description: String,
            callback: (String?) -> Unit,
        ) {
            val options = TranslatorOptions.Builder()
                .setSourceLanguage(TranslateLanguage.HUNGARIAN)
                .setTargetLanguage(TranslateLanguage.ENGLISH)
                .build()

            val descriptionTranslator = Translation.getClient(options)

            descriptionTranslator.downloadModelIfNeeded(
                DownloadConditions.Builder()
                    .requireWifi()
                    .build()
            )
                .addOnFailureListener { callback("Can't translate text. Please try again later!") }
                .addOnSuccessListener {
                    descriptionTranslator.translate(description)
                        .addOnFailureListener { callback("Can't translate text. Please try again later!") }
                        .addOnSuccessListener { callback(it) }
                }
        }

    }
}