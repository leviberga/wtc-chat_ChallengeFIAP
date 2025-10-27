package br.com.wtc_aplicattion.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/**
 * Cores personalizadas do WTC CRM
 */
object WTCColors {
    val Primary = Color(0xFF2563EB)
    val Secondary = Color(0xFF4F46E5)
    val Background = Color(0xFFF9FAFB)
    val Surface = Color.White
    val Success = Color(0xFF10B981)
    val Warning = Color(0xFFF59E0B)
    val Error = Color(0xFFEF4444)
    val OnPrimary = Color.White
    val OnSecondary = Color.White
    val OnBackground = Color(0xFF1F2937)
    val OnSurface = Color(0xFF1F2937)
}

/**
 * Esquema de cores claro do aplicativo
 */
private val LightColorScheme = lightColorScheme(
    primary = WTCColors.Primary,
    secondary = WTCColors.Secondary,
    background = WTCColors.Background,
    surface = WTCColors.Surface,
    error = WTCColors.Error,
    onPrimary = WTCColors.OnPrimary,
    onSecondary = WTCColors.OnSecondary,
    onBackground = WTCColors.OnBackground,
    onSurface = WTCColors.OnSurface,
    onError = Color.White
)

/**
 * Tema principal do WTC Application.
 * Aplica o Material Design 3 com as cores personalizadas.
 */
@Composable
fun WTCApplicationTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = Typography(),
        content = content
    )
}