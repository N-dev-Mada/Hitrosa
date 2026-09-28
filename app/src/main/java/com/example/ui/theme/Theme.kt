package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

// ==============================================================================
// Material 3 Color Schemes - Dark & Light
// ==============================================================================

private val DarkColorScheme = darkColorScheme(
    primary = BrandTealLight,
    onPrimary = Color(0xFF003831),
    primaryContainer = BrandTealDark,
    onPrimaryContainer = BrandTealContainerLight,
    secondary = BrandSkyLight,
    onSecondary = Color(0xFF00354D),
    secondaryContainer = Color(0xFF075985),
    onSecondaryContainer = BrandSkyContainer,
    tertiary = WarningAmber,
    onTertiary = Color.Black,
    tertiaryContainer = WarningAmberDark,
    onTertiaryContainer = WarningAmberLight,
    background = Color(0xFF0B0F19),
    onBackground = Color(0xFFF1F5F9),
    surface = Color(0xFF131C2E),
    onSurface = Color(0xFFF8FAFC),
    surfaceVariant = Color(0xFF1E293B),
    onSurfaceVariant = Color(0xFF94A3B8),
    surfaceContainer = Color(0xFF182236),
    surfaceContainerHigh = Color(0xFF223048),
    outline = Color(0xFF334155),
    outlineVariant = Color(0xFF1E293B),
    error = CreditRed,
    onError = Color.White,
    errorContainer = Color(0xFF4C0519),
    onErrorContainer = CreditRedLight
)

private val LightColorScheme = lightColorScheme(
    primary = BrandTealDark,
    onPrimary = Color.White,
    primaryContainer = BrandTealContainerLight,
    onPrimaryContainer = Color(0xFF042F2C),
    secondary = BrandSky,
    onSecondary = Color.White,
    secondaryContainer = BrandSkyContainer,
    onSecondaryContainer = Color(0xFF0C4A6E),
    tertiary = WarningAmber,
    onTertiary = Color.White,
    tertiaryContainer = WarningAmberLight,
    onTertiaryContainer = WarningAmberDark,
    background = Color(0xFFF8FAFC), // Modern clean off-white / light slate
    onBackground = Color(0xFF0F172A),
    surface = Color.White,
    onSurface = Color(0xFF0F172A),
    surfaceVariant = Color(0xFFF1F5F9),
    onSurfaceVariant = Color(0xFF475569),
    surfaceContainer = Color(0xFFF8FAFC),
    surfaceContainerHigh = Color(0xFFFFFFFF),
    outline = Color(0xFFE2E8F0),
    outlineVariant = Color(0xFFF1F5F9),
    error = CreditRed,
    onError = Color.White,
    errorContainer = CreditRedContainer,
    onErrorContainer = CreditRedDark
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Keep intentional financial palette by default
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
