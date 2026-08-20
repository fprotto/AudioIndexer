package com.unitn.audioindexer.ui.viewmodels

import android.app.LocaleConfig
import android.app.LocaleManager
import android.content.Context
import android.os.Build
import android.os.LocaleList
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.unitn.audioindexer.data.components.ExportConfig
import com.unitn.audioindexer.data.database.entities.MusicSourceEntity
import com.unitn.audioindexer.data.repository.MusicRepository
import com.unitn.audioindexer.data.repository.SettingsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.xmlpull.v1.XmlPullParser
import java.io.InputStream
import java.io.OutputStream
import java.util.Locale

class SettingsViewModel(
    private val repository: MusicRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {
    val isDarkTheme: StateFlow<Boolean?> = settingsRepository.isDarkTheme

    val allSources: StateFlow<List<MusicSourceEntity>?> = repository.allSources
        .stateIn(viewModelScope, SharingStarted.Lazily, null)

    val activeSourceId: StateFlow<Int?> = repository.activeSourceId

    fun setActiveSource(id: Int) {
        repository.setActiveSource(id)
    }

    fun updateSource(updatedSource: MusicSourceEntity, oldSource: MusicSourceEntity, onConfirmResync: () -> Unit) {
        viewModelScope.launch {
            repository.updateSource(updatedSource)
            if (updatedSource.type == "REMOTE" && (updatedSource.path != oldSource.path || updatedSource.port != oldSource.port)) {
                onConfirmResync()
            } else if (updatedSource.type == "LOCAL" && updatedSource.path != oldSource.path) {
                onConfirmResync()
            }
        }
    }

    fun clearSongsForSource(sourceId: Int) {
        viewModelScope.launch {
            repository.clearSongsForSource(sourceId)
            repository.syncSource(sourceId)
        }
    }

    fun deleteSource(source: MusicSourceEntity) {
        viewModelScope.launch {
            val all = allSources.value ?: return@launch
            val activeId = activeSourceId.value
            
            repository.deleteSource(source)
            
            if (activeId == source.id) {
                val remaining = all.filter { it.id != source.id }
                if (remaining.isNotEmpty()) {
                    repository.setActiveSource(remaining.first().id)
                } else {
                    repository.setActiveSource(null)
                }
            }
        }
    }

    fun syncSource(sourceId: Int) {
        repository.syncSource(sourceId)
    }

    fun toggleTheme(systemInDarkTheme: Boolean) {
        settingsRepository.setDarkTheme(!(isDarkTheme.value ?: systemInDarkTheme))
    }

    fun exportConfiguration(outputStream: OutputStream, onComplete: () -> Unit) {
        viewModelScope.launch {
            val config = repository.exportActiveSourceConfiguration()
            if (config != null) {
                try {
                    val json = Gson().toJson(config)
                    outputStream.use { it.write(json.toByteArray()) }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            onComplete()
        }
    }

    fun importConfiguration(inputStream: InputStream, onResult: (Result<Unit>) -> Unit) {
        viewModelScope.launch {
            try {
                val json = inputStream.use { it.bufferedReader().readText() }
                val config = Gson().fromJson(json, ExportConfig::class.java)
                val result = repository.importConfiguration(config)
                onResult(result)
            } catch (e: Exception) {
                onResult(Result.failure(e))
            }
        }
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