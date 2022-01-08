package ui

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.platform.Font
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import utils.EnglishStringRes
import utils.StringRes

@Composable
fun AppTheme(isLightTheme: Boolean = true, content: @Composable () -> Unit) =
    CustomTheme(isLightTheme, content = content)

@Immutable
data class CustomColors(
    val background: Color = Color.Unspecified,
    val componentBackground: Color = Color.Unspecified,
    val componentBackground2: Color = Color.Unspecified,
    val highContrast: Color = Color.Unspecified,
    val mediumContrast: Color = Color.Unspecified,
    val lowContrast: Color = Color.Unspecified,
    val componentOutline: Color = Color.Unspecified,
    val accent: Color = Color.Unspecified,
    val alertColors: CustomAlertColors = CustomAlertColors()
)

@Immutable
data class CustomAlertColors(
    val danger: Color = Color.Unspecified,
    val success: Color = Color.Unspecified,
)

@Immutable
data class CustomTypography(
    val body: TextStyle,
    val bodySmall: TextStyle,
    val title: TextStyle,
    val headings: CustomHeading
)

@Immutable
data class CustomElevation(
    val default: Dp,
    val pressed: Dp
)

@Immutable
data class CustomResources(
    val strings: StringRes
)

val LocalCustomColors = staticCompositionLocalOf {
    CustomColors()
}
val LocalCustomTypography = staticCompositionLocalOf {
    CustomTypography(
        body = TextStyle.Default,
        bodySmall = TextStyle.Default,
        title = TextStyle.Default,
        headings = CustomHeading(null)
    )
}
val LocalCustomElevation = staticCompositionLocalOf {
    CustomElevation(
        default = Dp.Unspecified,
        pressed = Dp.Unspecified
    )
}
val LocalCustomShape = staticCompositionLocalOf {
    Shapes()
}
val LocalCustomResources = staticCompositionLocalOf {
    CustomResources(EnglishStringRes())
}

@Composable
fun CustomTheme(
    isLightTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val fontFamily = FontFamily(
        Font("WorkSans-Bold.ttf", FontWeight.Bold),
        Font("WorkSans-Regular.ttf", FontWeight.Normal),
        Font("WorkSans-SemiBold.ttf", FontWeight.SemiBold),
        Font("WorkSans-Medium.ttf", FontWeight.Medium),
    )
    val alertColors = CustomAlertColors(
        danger = Color(0xFFDC3545),
        success = Color(0xFF28A745)
    )
    val customColors = customColors(isLightTheme, alertColors)
    val customTypography = CustomTypography(
        body = TextStyle(fontSize = 16.sp, fontFamily = fontFamily),
        bodySmall = TextStyle(fontSize = 12.sp, fontFamily = fontFamily),
        title = TextStyle(fontSize = 32.sp, fontFamily = fontFamily),
        headings = CustomHeading(fontFamily)
    )
    val customElevation = CustomElevation(
        default = 4.dp,
        pressed = 8.dp
    )
    val customShapes = Shapes(
        small = RoundedCornerShape(8.dp), medium = RoundedCornerShape(8.dp),
        large = RoundedCornerShape(8.dp)
    )
    val customRes = CustomResources(EnglishStringRes())
    val materialColors = Colors(
        primary = customColors.accent,
        background = customColors.background,
        surface = customColors.componentBackground,
        onPrimary = customColors.componentBackground,
        primaryVariant = customColors.accent.copy(alpha = 0.7f),
        secondary = customColors.highContrast, secondaryVariant = customColors.mediumContrast,
        error = customColors.alertColors.danger,
        onSecondary = customColors.componentBackground,
        onBackground = customColors.highContrast,
        onError = customColors.componentBackground,
        onSurface = customColors.highContrast, isLight = isLightTheme
    )
    MaterialTheme(
        typography = Typography(defaultFontFamily = fontFamily), shapes = customShapes,
        colors = materialColors
    ) {
        CompositionLocalProvider(
            LocalCustomColors provides customColors,
            LocalCustomTypography provides customTypography,
            LocalCustomElevation provides customElevation,
            LocalCustomShape provides customShapes,
            LocalContentColor provides customColors.highContrast,
            LocalCustomResources provides customRes
        ) {
            content()
        }
    }
}

private fun customColors(isLightTheme: Boolean, alertColors: CustomAlertColors) = if (isLightTheme) {
    CustomColors(
        background = Color(0xFFF5F5F5),
        componentBackground = Color.White,
        componentBackground2 = Color(0xFFF9F9F9),
        highContrast = Color(0xFF364A59),
        mediumContrast = Color(0xFF566976),
        lowContrast = Color(0xFFACBAC3),
        accent = Color(0xFF31AAB7),
        alertColors = alertColors,
        componentOutline = Color(0xFFDAE2E8)
    )
} else {
    CustomColors(
        background = Color(0xFF2E3438),
        componentBackground = Color(0xFF111B22),
        componentBackground2 = Color(0xFF383E42),
        highContrast = Color.White,
        mediumContrast = Color(0xFFACBAC3),
        lowContrast = Color(0xFF566976),
        accent = Color(0xFF31AAB7),
        alertColors = alertColors,
        componentOutline = Color(0xFF3A4349)
    )
}

// Use with eg. CustomTheme.elevation.small
object CustomTheme {
    val colors: CustomColors
        @Composable
        get() = LocalCustomColors.current
    val typography: CustomTypography
        @Composable
        get() = LocalCustomTypography.current
    val elevation: CustomElevation
        @Composable
        get() = LocalCustomElevation.current
    val shapes: Shapes
        @Composable
        get() = LocalCustomShape.current
    val strings: StringRes
        @Composable
        get() = LocalCustomResources.current.strings
    val resources: CustomResources
        @Composable
        get() = LocalCustomResources.current
}
