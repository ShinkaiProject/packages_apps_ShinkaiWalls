package com.shinkai.wallpapers.data.theme

import android.content.Context
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

enum class DarkThemeMode {
    FOLLOW_SYSTEM,
    LIGHT,
    DARK,
}

data class PaletteItem(
    val id: String,
    val name: String,
    val seed: Int,
    val primary: Color,
    val secondary: Color,
    val tertiary: Color,
    val neutral: Color,
)

val PALETTE_PRESETS =
    listOf(
        PaletteItem(
            id = "emerald",
            name = "Emerald",
            seed = 0xFF2E7D32.toInt(),
            primary = Color(0xFF2E7D32),
            secondary = Color(0xFF81C784),
            tertiary = Color(0xFFC8E6C9),
            neutral = Color(0xFFE8F5E9),
        ),
        PaletteItem(
            id = "blue",
            name = "Ocean",
            seed = 0xFF1976D2.toInt(),
            primary = Color(0xFF1976D2),
            secondary = Color(0xFF64B5F6),
            tertiary = Color(0xFFBBDEFB),
            neutral = Color(0xFFE3F2FD),
        ),
        PaletteItem(
            id = "purple",
            name = "Twilight",
            seed = 0xFF7B1FA2.toInt(),
            primary = Color(0xFF7B1FA2),
            secondary = Color(0xFFBA68C8),
            tertiary = Color(0xFFE1BEE7),
            neutral = Color(0xFFF3E5F5),
        ),
        PaletteItem(
            id = "orange",
            name = "Sunset",
            seed = 0xFFE64A19.toInt(),
            primary = Color(0xFFE64A19),
            secondary = Color(0xFFFF8A65),
            tertiary = Color(0xFFFFCCBC),
            neutral = Color(0xFFFBE9E7),
        ),
        PaletteItem(
            id = "rose",
            name = "Sakura",
            seed = 0xFFC2185B.toInt(),
            primary = Color(0xFFC2185B),
            secondary = Color(0xFFF06292),
            tertiary = Color(0xFFF8BBD0),
            neutral = Color(0xFFFCE4EC),
        ),
        PaletteItem(
            id = "amber",
            name = "Amber",
            seed = 0xFFF57C00.toInt(),
            primary = Color(0xFFF57C00),
            secondary = Color(0xFFFFB74D),
            tertiary = Color(0xFFFFE0B2),
            neutral = Color(0xFFFFF3E0),
        ),
        PaletteItem(
            id = "slate",
            name = "Slate",
            seed = 0xFF455A64.toInt(),
            primary = Color(0xFF455A64),
            secondary = Color(0xFF90A4AE),
            tertiary = Color(0xFFCFD8DC),
            neutral = Color(0xFFECEFF1),
        ),
    )

data class ThemePrefs(
    val darkThemeMode: DarkThemeMode = DarkThemeMode.FOLLOW_SYSTEM,
    val useDynamicColor: Boolean = true,
    val seedColor: Int = 0xFF2E7D32.toInt(),
    val isAmoled: Boolean = false,
)

class ThemeRepository(private val context: Context) {
    companion object {
        private const val PREFS_NAME = "shinkai_theme"
        private const val KEY_DARK_MODE = "dark_theme_mode"
        private const val KEY_DYNAMIC = "use_dynamic"
        private const val KEY_SEED = "seed_color"
        private const val KEY_AMOLED = "is_amoled"

        @Volatile
        private var instance: ThemeRepository? = null

        fun getInstance(context: Context): ThemeRepository =
            instance ?: synchronized(this) {
                instance ?: ThemeRepository(context.applicationContext).also { instance = it }
            }
    }

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _themePrefs =
        MutableStateFlow(
            ThemePrefs(
                darkThemeMode =
                    runCatching {
                        DarkThemeMode.valueOf(
                            prefs.getString(KEY_DARK_MODE, DarkThemeMode.FOLLOW_SYSTEM.name)!!
                        )
                    }.getOrDefault(DarkThemeMode.FOLLOW_SYSTEM),
                useDynamicColor = prefs.getBoolean(KEY_DYNAMIC, true),
                seedColor = prefs.getInt(KEY_SEED, 0xFF2E7D32.toInt()),
                isAmoled = prefs.getBoolean(KEY_AMOLED, false),
            )
        )
    val themePrefs: StateFlow<ThemePrefs> = _themePrefs

    fun setDarkThemeMode(mode: DarkThemeMode) {
        prefs.edit().putString(KEY_DARK_MODE, mode.name).apply()
        _themePrefs.value = _themePrefs.value.copy(darkThemeMode = mode)
    }

    fun setUseDynamicColor(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_DYNAMIC, enabled).apply()
        _themePrefs.value = _themePrefs.value.copy(useDynamicColor = enabled)
    }

    fun setSeedColor(seed: Int) {
        prefs.edit().putInt(KEY_SEED, seed).putBoolean(KEY_DYNAMIC, false).apply()
        _themePrefs.value = _themePrefs.value.copy(seedColor = seed, useDynamicColor = false)
    }

    fun setAmoled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_AMOLED, enabled).apply()
        _themePrefs.value = _themePrefs.value.copy(isAmoled = enabled)
    }
}
