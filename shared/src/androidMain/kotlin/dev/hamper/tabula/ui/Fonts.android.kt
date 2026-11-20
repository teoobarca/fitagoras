package dev.hamper.tabula.ui

import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import dev.hamper.tabula.platform.AndroidContext

actual fun fontFile(file: String, weight: FontWeight, style: FontStyle, variationSettings: FontVariation.Settings): Font =
    Font(AndroidContext.assetPath("font/$file.ttf"), AndroidContext.assets, weight, style, variationSettings)

actual fun noFontPadding(): PlatformTextStyle? = PlatformTextStyle(includeFontPadding = false)
