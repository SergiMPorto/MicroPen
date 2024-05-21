package com.sergi.micropen

import android.content.Context
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.TranslatorOptions

class LanguageManager(private val context: Context) {

    private val autoDownloadLanguageCodeList = listOf(
        TranslateLanguage.SPANISH,
        TranslateLanguage.ENGLISH
    )

    fun getAutoDownloadLanguages(): List<String> {
        return autoDownloadLanguageCodeList
    }

    fun downloadAllLanguages(
        callback: (success: Boolean, downloadedLanguages: Int) -> Unit,
        progressCallback: (progress: Int) -> Unit
    ) {
        val downloadConditions = DownloadConditions.Builder().requireWifi().build()
        var languagesDownloaded = 0
        val totalLanguages = autoDownloadLanguageCodeList.size

        for (languageCode in autoDownloadLanguageCodeList) {
            val options = TranslatorOptions.Builder()
                .setSourceLanguage(TranslateLanguage.SPANISH)
                .setTargetLanguage(languageCode)
                .build()

            val translator = Translation.getClient(options)

            translator.downloadModelIfNeeded(downloadConditions)
                .addOnSuccessListener {
                    languagesDownloaded++
                    val progress = (languagesDownloaded * 100) / totalLanguages
                    progressCallback(progress)
                    callback(true, languagesDownloaded)
                }
                .addOnFailureListener {
                    callback(false, languagesDownloaded)
                }
        }
    }

    fun downloadLanguage(languageCode: String, callback: (Boolean) -> Unit) {
        val options = TranslatorOptions.Builder()
            .setSourceLanguage(TranslateLanguage.SPANISH)
            .setTargetLanguage(languageCode)
            .build()

        val translator = Translation.getClient(options)

        val downloadConditions = DownloadConditions.Builder()
            .requireWifi()
            .build()

        translator.downloadModelIfNeeded(downloadConditions)
            .addOnSuccessListener {
                callback(true)
            }
            .addOnFailureListener {
                callback(false)
            }
    }
}
