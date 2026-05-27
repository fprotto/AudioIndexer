package com.unitn.audioindexer.ui

import android.app.LocaleConfig
import android.app.LocaleManager
import android.content.Context
import android.os.Build
import android.os.LocaleList
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import org.xmlpull.v1.XmlPullParser
import java.util.Locale

class SettingsViewModel : ViewModel() {
    var isDarkTheme by mutableStateOf<Boolean?>(null)
        private set

    fun toggleTheme(systemInDarkTheme: Boolean) {
        isDarkTheme = !(isDarkTheme ?: systemInDarkTheme)
    }

    fun setLanguage(context: Context, languageCode: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.getSystemService(LocaleManager::class.java).applicationLocales =
                LocaleList.forLanguageTags(languageCode)
        }
    }

    fun getSupportedLanguages(context: Context): List<Pair<String, String>> {
        val locales = mutableListOf<Locale>()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            try {
                val localeConfig = LocaleConfig(context)
                val localeList = localeConfig.supportedLocales
                if (localeList != null) {
                    for (i in 0 until localeList.size()) {
                        localeList.get(i)?.let { locales.add(it) }
                    }
                }
            } catch (e: Exception) {
                // LocaleConfig might not be found if not generated yet
            }
        }

        if (locales.isEmpty()) {
            // Fallback for older versions or if LocaleConfig failed: parse the generated locales_config.xml
            // Note: AGP generates it as _generated_res_locale_config
            try {
                val names = listOf("_generated_res_locale_config", "locales_config")
                var id = 0
                for (name in names) {
                    id = context.resources.getIdentifier(name, "xml", context.packageName)
                    if (id != 0) break
                }

                if (id != 0) {
                    val xpp = context.resources.getXml(id)
                    var eventType = xpp.eventType
                    while (eventType != XmlPullParser.END_DOCUMENT) {
                        if (eventType == XmlPullParser.START_TAG && xpp.name == "locale") {
                            val name = xpp.getAttributeValue("http://schemas.android.com/apk/res/android", "name")
                            if (name != null) {
                                locales.add(Locale.forLanguageTag(name))
                            }
                        }
                        eventType = xpp.next()
                    }
                }
            } catch (e: Exception) {
                // If it fails, we'll just have the default locale
            }
        }

        // If still empty (e.g. no translations yet), add English and Italian as defaults
        // or just the current locale.
        if (locales.isEmpty()) {
            locales.add(Locale.ENGLISH)
            locales.add(Locale.ITALIAN)
        }

        return locales.map { locale ->
            locale.language to locale.getDisplayName(locale).replaceFirstChar { it.uppercase() }
        }.distinctBy { it.first }
    }
}
