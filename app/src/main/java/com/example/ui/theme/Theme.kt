package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

private val HarshgamerDarkColorScheme = darkColorScheme(
    primary = ElectricCyan,
    onPrimary = OnCyanDark,
    primaryContainer = CyanContainerDark,
    onPrimaryContainer = ElectricCyan,
    secondary = CrimsonEmber,
    onSecondary = TextPrimaryDark,
    secondaryContainer = CrimsonContainerDark,
    onSecondaryContainer = CrimsonEmber,
    tertiary = AcidLime,
    onTertiary = ObsidianBase,
    background = ObsidianBase,
    onBackground = TextPrimaryDark,
    surface = SlateSurface,
    onSurface = TextPrimaryDark,
    surfaceVariant = SlateElevated,
    onSurfaceVariant = TextSecondaryDark,
    outline = SlateBorder,
    outlineVariant = SlateBorder
)

private val HarshgamerLightColorScheme = lightColorScheme(
    primary = CyanPrimaryLight,
    onPrimary = LightSurface,
    primaryContainer = LightElevated,
    onPrimaryContainer = CyanPrimaryLight,
    secondary = CrimsonSecondaryLight,
    onSecondary = LightSurface,
    secondaryContainer = LightElevated,
    onSecondaryContainer = CrimsonSecondaryLight,
    tertiary = GoldRank,
    onTertiary = TextPrimaryLight,
    background = LightBase,
    onBackground = TextPrimaryLight,
    surface = LightSurface,
    onSurface = TextPrimaryLight,
    surfaceVariant = LightElevated,
    onSurfaceVariant = TextSecondaryLight,
    outline = LightBorder,
    outlineVariant = LightBorder
)

val HarshgamerShapes = Shapes(
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(14.dp),
    large = RoundedCornerShape(20.dp),
    extraLarge = RoundedCornerShape(26.dp)
)

@Composable
fun Harshgamer113Theme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) HarshgamerDarkColorScheme else HarshgamerLightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = HarshgamerTypography,
        shapes = HarshgamerShapes,
        content = content
    )
}
