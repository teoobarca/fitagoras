package dev.hamper.tabula.ui

import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Density
import dev.hamper.tabula.platform.fontBytes

/**
 * Skia applies variations when it loads the face, without a density, so an axis given in sp (the optical size) is
 * resolved here as Android does at font scale 1.0 (sp × font scale).
 */
actual fun fontFile(file: String, weight: FontWeight, style: FontStyle, variationSettings: FontVariation.Settings): Font {
    val settings = variationSettings.settings.map { if (it.needsDensity) FontVariation.Setting(it.axisName, it.toVariationValue(Density(1f, 1f))) else it }
    val axes = settings.joinToString(",") { "${it.axisName}=${it.toVariationValue(null)}" }
    return androidx.compose.ui.text.platform.Font("$file/${weight.weight}/$style/$axes", { fontBytes(file) }, weight, style, FontVariation.Settings(*settings.toTypedArray()))
}

actual fun noFontPadding(): PlatformTextStyle? = null
