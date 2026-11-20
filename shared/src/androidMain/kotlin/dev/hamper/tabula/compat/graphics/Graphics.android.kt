package dev.hamper.tabula.compat.graphics

import android.graphics.fonts.Font
import android.graphics.fonts.FontFamily
import dev.hamper.tabula.platform.AndroidContext
import androidx.compose.ui.graphics.nativeCanvas as composeNativeCanvas

actual typealias Typeface = android.graphics.Typeface
actual typealias Canvas = android.graphics.Canvas
actual val androidx.compose.ui.graphics.Canvas.nativeCanvas: Canvas get() = composeNativeCanvas

/** As the engine built its faces before: Font.Builder (from the APK's assets now) and CustomFallbackBuilder. */
actual fun loadTypeface(file: String, variations: String?): Typeface {
    val b = Font.Builder(AndroidContext.assets, AndroidContext.assetPath("font/$file.ttf"))
    if (variations != null) b.setFontVariationSettings(variations)
    return android.graphics.Typeface.CustomFallbackBuilder(FontFamily.Builder(b.build()).build()).build()
}

actual typealias Path = android.graphics.Path

/** One android.graphics.Paint per Paint, with the same flags, set from the Paint before every use. */
internal actual class NativePaint actual constructor(flags: Int) {
    val p = android.graphics.Paint(flags)

    fun sync(from: Paint): android.graphics.Paint {
        p.typeface = from.typeface
        p.textSize = from.textSize
        p.color = from.color
        p.style = when (from.style) {
            Paint.Style.FILL -> android.graphics.Paint.Style.FILL
            Paint.Style.STROKE -> android.graphics.Paint.Style.STROKE
            Paint.Style.FILL_AND_STROKE -> android.graphics.Paint.Style.FILL_AND_STROKE
        }
        p.strokeWidth = from.strokeWidth
        p.strokeJoin = when (from.strokeJoin) {
            Paint.Join.MITER -> android.graphics.Paint.Join.MITER
            Paint.Join.ROUND -> android.graphics.Paint.Join.ROUND
            Paint.Join.BEVEL -> android.graphics.Paint.Join.BEVEL
        }
        p.strokeCap = when (from.strokeCap) {
            Paint.Cap.BUTT -> android.graphics.Paint.Cap.BUTT
            Paint.Cap.ROUND -> android.graphics.Paint.Cap.ROUND
            Paint.Cap.SQUARE -> android.graphics.Paint.Cap.SQUARE
        }
        return p
    }

    private val r = android.graphics.Rect()

    actual fun measureText(p: Paint, text: String): Float = sync(p).measureText(text)

    actual fun getTextBounds(p: Paint, text: String, start: Int, end: Int, bounds: Rect) {
        sync(p).getTextBounds(text, start, end, r)
        bounds.left = r.left; bounds.top = r.top; bounds.right = r.right; bounds.bottom = r.bottom
    }

    actual fun fontMetrics(p: Paint): Paint.FontMetrics = sync(p).fontMetrics.let { Paint.FontMetrics(it.top, it.ascent, it.descent, it.bottom, it.leading) }
}

actual fun Canvas.drawText(text: String, x: Float, y: Float, paint: Paint) = drawText(text, x, y, paint.native.sync(paint))
actual fun Canvas.drawPath(path: Path, paint: Paint) = drawPath(path, paint.native.sync(paint))
actual fun Canvas.drawRoundRect(left: Float, top: Float, right: Float, bottom: Float, rx: Float, ry: Float, paint: Paint) =
    drawRoundRect(left, top, right, bottom, rx, ry, paint.native.sync(paint))

actual fun Canvas.save(): Int = save()
actual fun Canvas.restore() = restore()
actual fun Canvas.translate(dx: Float, dy: Float) = translate(dx, dy)
actual fun Canvas.scale(sx: Float, sy: Float) = scale(sx, sy)
