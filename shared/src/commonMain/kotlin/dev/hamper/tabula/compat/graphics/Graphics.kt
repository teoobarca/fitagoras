package dev.hamper.tabula.compat.graphics

/*
 * The android.graphics calls of the formula engine (math/Engine.kt), for common code. Canvas is Compose's native
 * canvas: android.graphics.Canvas on Android, Skia's on the desktop and the web. On Android every call below is the
 * same android.graphics call the engine made before (one Paint with the same flags, set the same way), so formulas
 * render pixel for pixel as they did; Skia gets the same glyphs, metrics and strokes from the same font files.
 */

/** The platform canvas: android.graphics.Canvas / org.jetbrains.skia.Canvas. */
expect class Canvas

/** Compose's nativeCanvas as the platform canvas (common code imports nativeCanvas from here). */
expect val androidx.compose.ui.graphics.Canvas.nativeCanvas: Canvas

/** A loaded font face: android.graphics.Typeface / org.jetbrains.skia.Typeface. */
expect class Typeface

/**
 * A font file from composeResources/font ([file] without .ttf), with OpenType variations in Android's syntax
 * ("'wght' 640, 'opsz' 18"). Android builds it exactly as before (Font.Builder + CustomFallbackBuilder).
 */
expect fun loadTypeface(file: String, variations: String?): Typeface

/** android.graphics.Path: moveTo, lineTo, cubicTo. */
expect class Path() {
    fun moveTo(x: Float, y: Float)
    fun lineTo(x: Float, y: Float)
    fun cubicTo(x1: Float, y1: Float, x2: Float, y2: Float, x3: Float, y3: Float)
}

/** android.graphics.Rect (integer bounds). */
class Rect(var left: Int = 0, var top: Int = 0, var right: Int = 0, var bottom: Int = 0) {
    fun width() = right - left
    fun height() = bottom - top
    override fun toString() = "Rect($left, $top - $right, $bottom)"
}

/** android.graphics.RectF. */
class RectF(var left: Float = 0f, var top: Float = 0f, var right: Float = 0f, var bottom: Float = 0f) {
    fun width() = right - left
    fun height() = bottom - top
}

/** android.graphics.Paint, the parts the engine sets; [native] applies them to the platform paint. */
class Paint(val flags: Int = 0) {
    enum class Style { FILL, STROKE, FILL_AND_STROKE }
    enum class Join { MITER, ROUND, BEVEL }
    enum class Cap { BUTT, ROUND, SQUARE }

    var typeface: Typeface? = null
    var textSize: Float = 12f
    var color: Int = 0xFF000000.toInt()
    var style: Style = Style.FILL
    var strokeWidth: Float = 0f
    var strokeJoin: Join = Join.MITER
    var strokeCap: Cap = Cap.BUTT

    internal val native = NativePaint(flags)

    fun measureText(text: String): Float = native.measureText(this, text)
    fun getTextBounds(text: String, start: Int, end: Int, bounds: Rect) = native.getTextBounds(this, text, start, end, bounds)
    /** android.graphics.Paint.FontMetrics of the current typeface and size (ascent negative, as on Android). */
    val fontMetrics: FontMetrics get() = native.fontMetrics(this)

    class FontMetrics(val top: Float, val ascent: Float, val descent: Float, val bottom: Float, val leading: Float)

    companion object {
        const val ANTI_ALIAS_FLAG = 1
        const val SUBPIXEL_TEXT_FLAG = 128
    }
}

internal expect class NativePaint(flags: Int) {
    fun measureText(p: Paint, text: String): Float
    fun getTextBounds(p: Paint, text: String, start: Int, end: Int, bounds: Rect)
    fun fontMetrics(p: Paint): Paint.FontMetrics
}

expect fun Canvas.drawText(text: String, x: Float, y: Float, paint: Paint)
expect fun Canvas.drawPath(path: Path, paint: Paint)
expect fun Canvas.drawRoundRect(left: Float, top: Float, right: Float, bottom: Float, rx: Float, ry: Float, paint: Paint)
expect fun Canvas.save(): Int
expect fun Canvas.restore()
expect fun Canvas.translate(dx: Float, dy: Float)
expect fun Canvas.scale(sx: Float, sy: Float)
