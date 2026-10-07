package com.shinkai.wallpapers.ui.screens.settings

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import com.shinkai.wallpapers.data.theme.DarkThemeMode
import com.shinkai.wallpapers.data.theme.ThemePrefs
import com.shinkai.wallpapers.data.theme.ThemeRepository
import com.shinkai.wallpapers.util.LocaleHelper
import kotlinx.coroutines.flow.StateFlow

class SettingsViewModel(application: Application) : AndroidViewModel(application) {
    private val themeRepository = ThemeRepository.getInstance(application)

    val themePrefs: StateFlow<ThemePrefs> = themeRepository.themePrefs
    val currentLanguage: StateFlow<String> = LocaleHelper.currentLanguage

    fun setDarkThemeMode(mode: DarkThemeMode) {
        themeRepository.setDarkThemeMode(mode)
    }

    fun setUseDynamicColor(enabled: Boolean) {
        themeRepository.setUseDynamicColor(enabled)
    }

    fun setSeedColor(seed: Int) {
        themeRepository.setSeedColor(seed)
    }

    fun setAmoled(enabled: Boolean) {
        themeRepository.setAmoled(enabled)
    }

    fun setLanguage(context: Context, tag: String) {
        LocaleHelper.setLocale(context, tag)
    }
}
