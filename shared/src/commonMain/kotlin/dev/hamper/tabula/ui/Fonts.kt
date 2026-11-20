package dev.hamper.tabula.ui

import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight

/**
 * A font file from composeResources/font ([file] without .ttf) for Compose text. Android loads it from the APK's
 * assets with the same variation settings as before; Skia (desktop, web) from the same bytes.
 */
expect fun fontFile(file: String, weight: FontWeight, style: FontStyle, variationSettings: FontVariation.Settings): Font

/** Android's includeFontPadding = false; other platforms have no font padding to turn off. */
expect fun noFontPadding(): PlatformTextStyle?
