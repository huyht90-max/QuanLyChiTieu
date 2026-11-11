package vn.quanlychitieu.app.data

import android.content.Context
import androidx.core.content.edit
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class ThemeMode(val label: String) {
    SYSTEM("Theo hệ thống"),
    LIGHT("Sáng"),
    DARK("Tối"),
}

data class AppSettings(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val monthlyBudget: Long = 0,
    val hideAmounts: Boolean = false,
    val userName: String = "",
)

class SettingsStore(context: Context) {
    private val prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE)
    private val _state = MutableStateFlow(read())
    val state: StateFlow<AppSettings> = _state.asStateFlow()

    private fun read() = AppSettings(
        themeMode = ThemeMode.entries.firstOrNull { it.name == prefs.getString("theme", null) }
            ?: ThemeMode.SYSTEM,
        monthlyBudget = prefs.getLong("budget", 0),
        hideAmounts = prefs.getBoolean("hide", false),
        userName = prefs.getString("name", "") ?: "",
    )

    fun update(transform: (AppSettings) -> AppSettings) {
        val s = transform(_state.value)
        prefs.edit {
            putString("theme", s.themeMode.name)
            putLong("budget", s.monthlyBudget)
            putBoolean("hide", s.hideAmounts)
            putString("name", s.userName)
        }
        _state.value = s
    }
}
