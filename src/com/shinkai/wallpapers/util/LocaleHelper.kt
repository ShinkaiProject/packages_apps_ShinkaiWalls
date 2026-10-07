package com.shinkai.wallpapers.util

import android.app.LocaleManager
import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Build
import android.os.LocaleList
import com.shinkai.wallpapers.R
import java.util.Locale
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.xmlpull.v1.XmlPullParser

data class SupportedLanguage(
    val tag: String,
    val nativeName: String,
    val localizedName: String,
)

object LocaleHelper {
    const val SYSTEM = "system"

    private const val PREFS_NAME = "shinkai_locale_pref"
    private const val KEY_SELECTED_LANGUAGE = "selected_language"

    private val _currentLanguage = MutableStateFlow(SYSTEM)
    val currentLanguage: StateFlow<String> = _currentLanguage

    val SUPPORTED_TAGS = listOf("en", "id", "ja")

    /**
     * Dynamically reads supported language tags from res/xml/locales_config.xml.
     * Zero-hardcode: adding a new language is strictly XML-driven.
     */
    fun parseLocalesFromConfig(context: Context): List<String> {
        val tags = mutableListOf<String>()
        try {
            val parser = context.resources.getXml(R.xml.locales_config)
            var eventType = parser.eventType
            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_TAG && parser.name == "locale") {
                    val name =
                        parser.getAttributeValue("http://schemas.android.com/apk/res/android", "name")
                            ?: parser.getAttributeValue(null, "name")
                    if (!name.isNullOrBlank() && !tags.contains(name)) {
                        tags.add(name)
                    }
                }
                eventType = parser.next()
            }
        } catch (_: Exception) {
            tags.addAll(SUPPORTED_TAGS)
        }
        return if (tags.isNotEmpty()) tags else SUPPORTED_TAGS
    }

    fun getSystemLocale(): Locale =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Resources.getSystem().configuration.locales.get(0) ?: Locale.getDefault()
        } else {
            @Suppress("DEPRECATION")
            Resources.getSystem().configuration.locale ?: Locale.getDefault()
        }

    fun getSystemLocaleList(): LocaleList =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Resources.getSystem().configuration.locales
        } else {
            LocaleList.getDefault()
        }

    fun getSupportedLanguages(context: Context): List<SupportedLanguage> {
        val list = mutableListOf<SupportedLanguage>()

        // 1. Follow System option
        list.add(
            SupportedLanguage(
                tag = SYSTEM,
                nativeName = context.getString(R.string.settings_language_system),
                localizedName = context.getString(R.string.settings_language_system_desc),
            )
        )

        // 2. Supported languages
        val tags = parseLocalesFromConfig(context)
        for (tag in tags) {
            val nameResId =
                context.resources.getIdentifier(
                    "lang_name_${tag.lowercase()}",
                    "string",
                    context.packageName,
                )
            val subResId =
                context.resources.getIdentifier(
                    "lang_sub_${tag.lowercase()}",
                    "string",
                    context.packageName,
                )

            val nativeName =
                if (nameResId != 0) {
                    context.getString(nameResId)
                } else {
                    when (tag.lowercase()) {
                        "en" -> "English"
                        "id", "in" -> "Bahasa Indonesia"
                        "ja" -> "日本語"
                        else -> {
                            val loc = parseLocale(tag)
                            loc.getDisplayName(loc).substringBefore(" (").replaceFirstChar {
                                if (it.isLowerCase()) it.titlecase(loc) else it.toString()
                            }
                        }
                    }
                }

            val localizedName =
                if (subResId != 0) {
                    context.getString(subResId)
                } else {
                    parseLocale(tag).getDisplayName(Locale.ENGLISH)
                }

            list.add(
                SupportedLanguage(
                    tag = tag,
                    nativeName = nativeName,
                    localizedName = localizedName,
                )
            )
        }
        return list
    }

    fun setLocale(context: Context, languageCode: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_SELECTED_LANGUAGE, languageCode).apply()

        val isSystem = languageCode == SYSTEM || languageCode.isEmpty()
        val targetTag = if (isSystem) "" else languageCode

        val targetLocale = if (isSystem) getSystemLocale() else parseLocale(targetTag)
        Locale.setDefault(targetLocale)

        val resources = context.resources
        val config = Configuration(resources.configuration)
        config.setLocale(targetLocale)
        config.setLayoutDirection(targetLocale)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val localeList = if (isSystem) getSystemLocaleList() else createLocaleList(targetTag)
            config.setLocales(localeList)
        }
        @Suppress("DEPRECATION")
        resources.updateConfiguration(config, resources.displayMetrics)

        val appRes = context.applicationContext?.resources
        if (appRes != null && appRes !== resources) {
            @Suppress("DEPRECATION")
            appRes.updateConfiguration(config, appRes.displayMetrics)
        }

        _currentLanguage.value = languageCode
    }

    fun wrapContext(context: Context, languageCode: String? = null): Context {
        val savedLang = languageCode ?: getCurrentLanguage(context)
        val isSystem = savedLang == SYSTEM || savedLang.isEmpty()
        val targetLocale = if (isSystem) getSystemLocale() else parseLocale(savedLang)
        Locale.setDefault(targetLocale)

        val config = Configuration(context.resources.configuration)
        config.setLocale(targetLocale)
        config.setLayoutDirection(targetLocale)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val localeList = if (isSystem) getSystemLocaleList() else createLocaleList(savedLang)
            config.setLocales(localeList)
        }
        @Suppress("DEPRECATION")
        context.resources.updateConfiguration(config, context.resources.displayMetrics)
        return context.createConfigurationContext(config)
    }

    fun applySavedLocale(context: Context) {
        val saved = getCurrentLanguage(context)
        _currentLanguage.value = saved
        val isSystem = saved == SYSTEM || saved.isEmpty()
        val targetLocale = if (isSystem) getSystemLocale() else parseLocale(saved)
        Locale.setDefault(targetLocale)

        val resources = context.resources
        val config = Configuration(resources.configuration)
        config.setLocale(targetLocale)
        config.setLayoutDirection(targetLocale)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val localeList = if (isSystem) getSystemLocaleList() else createLocaleList(saved)
            config.setLocales(localeList)
        }
        @Suppress("DEPRECATION")
        resources.updateConfiguration(config, resources.displayMetrics)

        val appRes = context.applicationContext?.resources
        if (appRes != null && appRes !== resources) {
            @Suppress("DEPRECATION")
            appRes.updateConfiguration(config, appRes.displayMetrics)
        }
    }

    fun getCurrentLanguage(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val saved = prefs.getString(KEY_SELECTED_LANGUAGE, null)
        if (!saved.isNullOrEmpty()) {
            return saved
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val locales = context.getSystemService(LocaleManager::class.java)?.applicationLocales
            if (locales != null && !locales.isEmpty) {
                val tags = parseLocalesFromConfig(context)
                return tags.firstOrNull { locales.getFirstMatch(arrayOf(it)) != null } ?: SYSTEM
            }
        }
        return SYSTEM
    }

    fun parseLocale(tag: String): Locale =
        when (tag.lowercase()) {
            "id", "in" -> Locale.forLanguageTag("in-ID")
            "ja" -> Locale.forLanguageTag("ja-JP")
            "en" -> Locale.forLanguageTag("en-US")
            else -> Locale.forLanguageTag(tag)
        }

    fun createLocaleList(tag: String): LocaleList =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            when (tag.lowercase()) {
                "id", "in" -> {
                    LocaleList(Locale.forLanguageTag("in-ID"), Locale.forLanguageTag("id-ID"))
                }
                "ja" -> {
                    LocaleList(Locale.forLanguageTag("ja-JP"), Locale.forLanguageTag("ja"))
                }
                "en" -> {
                    LocaleList(Locale.forLanguageTag("en-US"), Locale.forLanguageTag("en"))
                }
                else -> {
                    LocaleList(Locale.forLanguageTag(tag))
                }
            }
        } else {
            LocaleList.getDefault()
        }

    fun syncSystemLocale(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val languageCode = getCurrentLanguage(context)
            val isSystem = languageCode == SYSTEM || languageCode.isEmpty()
            val targetTag = if (isSystem) "" else languageCode
            val localeManager = context.getSystemService(LocaleManager::class.java) ?: return
            val targetLocales = if (isSystem) LocaleList.getEmptyLocaleList() else createLocaleList(targetTag)
            if (localeManager.applicationLocales != targetLocales) {
                localeManager.applicationLocales = targetLocales
            }
        }
    }
}
