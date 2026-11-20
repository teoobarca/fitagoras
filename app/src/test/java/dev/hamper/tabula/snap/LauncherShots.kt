package dev.hamper.tabula.snap

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.toBitmap
import dev.hamper.tabula.R
import dev.hamper.tabula.ui.T
import org.junit.Test

/**
 * The launcher icon as the phone shows it: `-Psnap.test=LauncherShots`. The adaptive icon in a circle, a squircle and a
 * rounded square mask, its themed (monochrome) layer, and the name under it.
 */
class LauncherShots : Shots() {
    @Test fun icon() = shoot("ikona", Snap.looks(theme = "light,dark", width = "360").map { it.copy(heightDp = 260) }) {
        val ctx = LocalContext.current
        val px = (108 * ctx.resources.displayMetrics.density).toInt()
        fun layer(id: Int) = ResourcesCompat.getDrawable(ctx.resources, id, ctx.theme)!!.toBitmap(px, px).asImageBitmap()
        val bg = layer(R.drawable.ic_launcher_background); val fg = layer(R.drawable.ic_launcher_foreground); val mono = layer(R.drawable.ic_launcher_monochrome)
        Column(Modifier.fillMaxSize().background(T.c.bg).padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                for ((shape, themed) in listOf(CircleShape to false, Squircle to false, RoundedCornerShape(22.dp) to false, CircleShape to true)) Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    // the system crops the 108 dp layers to the middle 72 dp
                    Box(Modifier.size(72.dp).clip(shape).background(if (themed) Color(0xFFD8E6DD) else Color.Transparent), contentAlignment = Alignment.Center) {
                        if (themed) Image(mono, null, Modifier.requiredSize(108.dp), colorFilter = ColorFilter.tint(Color(0xFF1F4F43)))
                        else { Image(bg, null, Modifier.requiredSize(108.dp)); Image(fg, null, Modifier.requiredSize(108.dp)) }
                    }
                    Spacer(Modifier.height(6.dp))
                    Text(if (themed) "podľa témy" else "FITagoras", style = MaterialTheme.typography.labelMedium, color = T.c.ink)
                }
            }
        }
    }
}

/** The squircle of Pixel launchers: a superellipse |x|⁴ + |y|⁴ = 1. */
private val Squircle = androidx.compose.foundation.shape.GenericShape { size, _ ->
    val n = 96
    for (i in 0..n) {
        val t = 2 * Math.PI * i / n
        val c = Math.cos(t); val s = Math.sin(t)
        val x = Math.signum(c) * Math.sqrt(Math.abs(c)); val y = Math.signum(s) * Math.sqrt(Math.abs(s))
        val px = (size.width / 2 * (1 + x)).toFloat(); val py = (size.height / 2 * (1 + y)).toFloat()
        if (i == 0) moveTo(px, py) else lineTo(px, py)
    }
    close()
}
