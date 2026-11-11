package vn.quanlychitieu.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import vn.quanlychitieu.app.data.ThemeMode

private val Indigo = Color(0xFF4F46E5)
private val IndigoLight = Color(0xFF818CF8)

private val LightColors = lightColorScheme(
    primary = Indigo,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE0E7FF),
    onPrimaryContainer = Color(0xFF1E1B4B),
    secondary = Color(0xFF7C3AED),
    secondaryContainer = Color(0xFFEDE9FE),
    onSecondaryContainer = Color(0xFF2E1065),
    background = Color(0xFFF6F7FB),
    onBackground = Color(0xFF0F172A),
    surface = Color(0xFFF6F7FB),
    onSurface = Color(0xFF0F172A),
    surfaceVariant = Color(0xFFEEF0F6),
    onSurfaceVariant = Color(0xFF64748B),
    surfaceContainerLowest = Color.White,
    surfaceContainerLow = Color.White,
    surfaceContainer = Color.White,
    surfaceContainerHigh = Color(0xFFF1F3F9),
    surfaceContainerHighest = Color(0xFFE8EBF3),
    outline = Color(0xFFCBD5E1),
    outlineVariant = Color(0xFFE2E8F0),
    error = Color(0xFFE11D48),
)

private val DarkColors = darkColorScheme(
    primary = IndigoLight,
    onPrimary = Color(0xFF1E1B4B),
    primaryContainer = Color(0xFF312E81),
    onPrimaryContainer = Color(0xFFE0E7FF),
    secondary = Color(0xFFA78BFA),
    secondaryContainer = Color(0xFF3B2A6B),
    onSecondaryContainer = Color(0xFFEDE9FE),
    background = Color(0xFF0E0F1A),
    onBackground = Color(0xFFE2E8F0),
    surface = Color(0xFF0E0F1A),
    onSurface = Color(0xFFE2E8F0),
    surfaceVariant = Color(0xFF1E2033),
    onSurfaceVariant = Color(0xFF94A3B8),
    surfaceContainerLowest = Color(0xFF14152A),
    surfaceContainerLow = Color(0xFF17192B),
    surfaceContainer = Color(0xFF181A2E),
    surfaceContainerHigh = Color(0xFF212338),
    surfaceContainerHighest = Color(0xFF2A2D45),
    outline = Color(0xFF475569),
    outlineVariant = Color(0xFF262940),
    error = Color(0xFFFB7185),
)

/** Màu ngữ nghĩa dùng riêng cho tài chính. */
@Immutable
data class FinanceColors(
    val income: Color,
    val expense: Color,
    val incomeContainer: Color,
    val expenseContainer: Color,
    val heroGradient: Brush,
    val card: Color,
)

val LocalFinanceColors = staticCompositionLocalOf {
    FinanceColors(Color.Green, Color.Red, Color.Green, Color.Red, Brush.linearGradient(listOf(Indigo, Indigo)), Color.White)
}

object AppTheme {
    val finance: FinanceColors
        @Composable get() = LocalFinanceColors.current
}

private val AppTypography = Typography().let { t ->
    t.copy(
        displaySmall = t.displaySmall.copy(fontWeight = FontWeight.Bold, letterSpacing = (-0.5).sp),
        headlineMedium = t.headlineMedium.copy(fontWeight = FontWeight.Bold, letterSpacing = (-0.5).sp),
        headlineSmall = t.headlineSmall.copy(fontWeight = FontWeight.Bold),
        titleLarge = t.titleLarge.copy(fontWeight = FontWeight.Bold),
        titleMedium = t.titleMedium.copy(fontWeight = FontWeight.SemiBold),
        titleSmall = t.titleSmall.copy(fontWeight = FontWeight.SemiBold),
        labelLarge = t.labelLarge.copy(fontWeight = FontWeight.SemiBold),
    )
}

private val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(32.dp),
)

val AmountStyle = TextStyle(fontWeight = FontWeight.Bold, letterSpacing = (-0.3).sp)

@Composable
fun QuanLyChiTieuTheme(themeMode: ThemeMode, content: @Composable () -> Unit) {
    val dark = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }
    val finance = if (dark) {
        FinanceColors(
            income = Color(0xFF34D399),
            expense = Color(0xFFFB7185),
            incomeContainer = Color(0xFF0F2E26),
            expenseContainer = Color(0xFF3A1722),
            heroGradient = Brush.linearGradient(listOf(Color(0xFF4338CA), Color(0xFF6D28D9), Color(0xFF9333EA))),
            card = Color(0xFF181A2E),
        )
    } else {
        FinanceColors(
            income = Color(0xFF059669),
            expense = Color(0xFFE11D48),
            incomeContainer = Color(0xFFD1FAE5),
            expenseContainer = Color(0xFFFFE4E6),
            heroGradient = Brush.linearGradient(listOf(Color(0xFF4F46E5), Color(0xFF7C3AED), Color(0xFFA855F7))),
            card = Color.White,
        )
    }
    CompositionLocalProvider(LocalFinanceColors provides finance) {
        MaterialTheme(
            colorScheme = if (dark) DarkColors else LightColors,
            typography = AppTypography,
            shapes = AppShapes,
            content = content,
        )
    }
}
