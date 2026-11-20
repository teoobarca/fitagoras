package dev.hamper.tabula.compat.graphics

import dev.hamper.tabula.platform.fontBytes
import androidx.compose.ui.graphics.nativeCanvas as composeNativeCanvas
import org.jetbrains.skia.Data
import org.jetbrains.skia.PathBuilder
import org.jetbrains.skia.Font
import org.jetbrains.skia.FontEdging
import org.jetbrains.skia.FontHinting
import org.jetbrains.skia.FontMgr
import org.jetbrains.skia.FontVariation
import org.jetbrains.skia.PaintMode
import org.jetbrains.skia.PaintStrokeCap
import org.jetbrains.skia.PaintStrokeJoin
import org.jetbrains.skia.RRect
import org.jetbrains.skia.TextLine
import dev.hamper.tabula.compat.LruCache
import kotlin.math.ceil
import kotlin.math.floor

actual typealias Typeface = org.jetbrains.skia.Typeface
actual typealias Canvas = org.jetbrains.skia.Canvas
actual val androidx.compose.ui.graphics.Canvas.nativeCanvas: Canvas get() = composeNativeCanvas

private val Axis = Regex("""'(\w{4})'\s*(-?[\d.]+)""")

actual fun loadTypeface(file: String, variations: String?): Typeface {
    val base = FontMgr.default.makeFromData(Data.makeFromBytes(fontBytes(file))) ?: error("font $file")
    if (variations == null) return base
    val axes = Axis.findAll(variations).map { FontVariation(it.groupValues[1], it.groupValues[2].toFloat()) }.toList()
    return base.makeClone(axes.toTypedArray())
}

actual class Path actual constructor() {
    private val b = PathBuilder()
    actual fun moveTo(x: Float, y: Float) { b.moveTo(x, y) }
    actual fun lineTo(x: Float, y: Float) { b.lineTo(x, y) }
    actual fun cubicTo(x1: Float, y1: Float, x2: Float, y2: Float, x3: Float, y3: Float) { b.cubicTo(x1, y1, x2, y2, x3, y3) }
    fun path(): org.jetbrains.skia.Path = b.snapshot()
}

/** Skia's Font (face and size) and Paint, set from the Paint before every use, like android.graphics.Paint. */
internal actual class NativePaint actual constructor(private val flags: Int) {
    private val font = Font().apply {
        // ANTI_ALIAS_FLAG | SUBPIXEL_TEXT_FLAG on Android
        edging = if (flags and Paint.ANTI_ALIAS_FLAG != 0) FontEdging.ANTI_ALIAS else FontEdging.ALIAS
        isSubpixel = flags and Paint.SUBPIXEL_TEXT_FLAG != 0
        hinting = FontHinting.SLIGHT
    }
    private val paint = org.jetbrains.skia.Paint().apply { isAntiAlias = flags and Paint.ANTI_ALIAS_FLAG != 0 }

    fun font(p: Paint): Font {
        font.setTypeface(p.typeface) // every time: clones with other variations may compare equal
        font.size = p.textSize
        return font
    }

    /** [text] shaped in [p]'s face and size, shaped once ([Shaped]): the same for measuring and for every frame drawn. */
    fun line(p: Paint, text: String): TextLine = Shaped.line(text, p.typeface, p.textSize, flags) { TextLine.make(text, font(p)) }

    fun paint(p: Paint): org.jetbrains.skia.Paint = paint.apply {
        color = p.color
        mode = when (p.style) { Paint.Style.FILL -> PaintMode.FILL; Paint.Style.STROKE -> PaintMode.STROKE; Paint.Style.FILL_AND_STROKE -> PaintMode.STROKE_AND_FILL }
        strokeWidth = p.strokeWidth
        strokeJoin = when (p.strokeJoin) { Paint.Join.MITER -> PaintStrokeJoin.MITER; Paint.Join.ROUND -> PaintStrokeJoin.ROUND; Paint.Join.BEVEL -> PaintStrokeJoin.BEVEL }
        strokeCap = when (p.strokeCap) { Paint.Cap.BUTT -> PaintStrokeCap.BUTT; Paint.Cap.ROUND -> PaintStrokeCap.ROUND; Paint.Cap.SQUARE -> PaintStrokeCap.SQUARE }
    }

    /** Shaped advance, as Android measures (TextLine shapes the run; also right for surrogate pairs in the browser). */
    actual fun measureText(p: Paint, text: String): Float = line(p, text).width

    /** Integer ink bounds, rounded outwards, as Android's getTextBounds. */
    actual fun getTextBounds(p: Paint, text: String, start: Int, end: Int, bounds: Rect) {
        val r = font(p).measureText(text.substring(start, end))
        bounds.left = floor(r.left).toInt(); bounds.top = floor(r.top).toInt()
        bounds.right = ceil(r.right).toInt(); bounds.bottom = ceil(r.bottom).toInt()
    }

    actual fun fontMetrics(p: Paint): Paint.FontMetrics = font(p).metrics.let { Paint.FontMetrics(it.top, it.ascent, it.descent, it.bottom, it.leading) }
}

actual fun Canvas.drawText(text: String, x: Float, y: Float, paint: Paint) {
    drawTextLine(paint.native.line(paint, text), x, y, paint.native.paint(paint))
}

/**
 * Shaped runs, remembered. Shaping (HarfBuzz) costs far more than drawing, and a formula draws the same glyphs at the
 * same sizes on every frame: before this, shaping took nearly all the time a formula screen spent on the desktop.
 * The key is everything a [TextLine] depends on: the text, the face itself (by identity: clones with other variations
 * may compare equal), the size and the paint's flags (edging, subpixel); colour and stroke are applied when drawing.
 * A [TextLine] is immutable, so a remembered one draws exactly the pixels a fresh one would. Bounded (least recently
 * used out); an evicted line is freed by its cleaner like an unremembered one was. [LruCache] locks itself, so
 * drawing from more than one thread (tests, off-screen scenes) is safe.
 */
internal object Shaped {
    private class Key(val text: String, val face: Typeface?, val size: Float, val flags: Int) {
        override fun equals(other: Any?) = other is Key && other.face === face && other.size == size && other.flags == flags && other.text == text
        override fun hashCode() = (text.hashCode() * 31 + size.hashCode()) * 31 + flags
    }
    private val lines = LruCache<Key, TextLine>(4096)

    fun line(text: String, face: Typeface?, size: Float, flags: Int, shape: () -> TextLine): TextLine {
        val k = Key(text, face, size, flags)
        lines.get(k)?.let { return it }
        // two threads shaping the same run at once both get an identical line
        return shape().also { lines.put(k, it) }
    }
}

actual fun Canvas.drawPath(path: Path, paint: Paint) {
    drawPath(path.path(), paint.native.paint(paint))
}

actual fun Canvas.drawRoundRect(left: Float, top: Float, right: Float, bottom: Float, rx: Float, ry: Float, paint: Paint) {
    drawRRect(RRect.makeLTRB(left, top, right, bottom, rx, ry), paint.native.paint(paint))
}

actual fun Canvas.save(): Int = save()
actual fun Canvas.restore() { restore() }
actual fun Canvas.translate(dx: Float, dy: Float) { translate(dx, dy) }
actual fun Canvas.scale(sx: Float, sy: Float) { scale(sx, sy) }
