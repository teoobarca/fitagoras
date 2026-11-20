package dev.hamper.tabula.tutor.harness

import dev.hamper.tabula.tutor.GemPrepis
import java.awt.BasicStroke
import java.awt.Color
import java.awt.Font
import java.awt.RenderingHints
import java.awt.geom.AffineTransform
import java.awt.geom.CubicCurve2D
import java.awt.geom.GeneralPath
import java.awt.geom.Point2D
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import java.io.File
import javax.imageio.IIOImage
import javax.imageio.ImageIO
import javax.imageio.ImageWriteParam
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random

/*
 * Synthetic "handwriting" for the reading harness (docs/TUTOR.md, "Harness", Č): a GEM chain from the student simulator
 * drawn as a phone photo of paper. Handwriting fonts (OFL) or real people's glyphs (MathWriting symbols, strokes), a
 * pen colour, slant, wobble, uneven size and baseline, crossed-out tries, squared or plain or crumpled paper, a shadow,
 * perspective, a turned photo, blur, noise and JPEG. The truth is exact (we drew it). How far this stands in for real
 * handwriting is a separate question the report answers honestly: fonts repeat their glyphs, real writing does not.
 */

/** Real handwritten glyphs: per symbol, several writers' strokes (MathWriting `symbols`, InkML). */
class Glyfy(val tahy: Map<String, List<List<List<Point2D.Double>>>>) {
    fun ma(sym: String) = tahy[sym]?.isNotEmpty() == true

    companion object {
        /** Reads the InkML files of the MathWriting `symbols` split; null when the folder isn't there. */
        fun nacitaj(dir: File): Glyfy? {
            if (!dir.isDirectory) return null
            val out = HashMap<String, MutableList<List<List<Point2D.Double>>>>()
            val want = setOf("0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "-", "+", "/", "\\sim", "(", ")", "|")
            dir.walkTopDown().filter { it.extension == "inkml" }.forEach { f ->
                val t = f.readText()
                val label = Regex("<annotation type=\"label\">([^<]*)</annotation>").find(t)?.groupValues?.get(1)?.trim() ?: return@forEach
                if (label !in want) return@forEach
                val traces = Regex("<trace[^>]*>([^<]*)</trace>").findAll(t).map { m ->
                    m.groupValues[1].trim().split(',').mapNotNull { p -> p.trim().split(Regex("\\s+")).takeIf { it.size >= 2 }?.let { Point2D.Double(it[0].toDouble(), it[1].toDouble()) } }
                }.filter { it.isNotEmpty() }.toList()
                if (traces.isNotEmpty()) out.getOrPut(label) { ArrayList() } += traces
            }
            return Glyfy(out).takeIf { out.isNotEmpty() }
        }
    }
}

/** One rendered photo: the JPEG, and where each matrix of the chain is on it (0–1 of width and height, as sent). */
data class Fotka(val jpeg: ByteArray, val oblasti: List<List<Double>>, val otocenie: Int, val styl: Map<String, String>)

class Rukopis(private val fonty: List<File>, private val glyfy: Glyfy?) {

    private class Pero(val farba: Color, val hrubka: Float, val font: Font?, val glyfIdx: Int, val sklon: Double, val velkost: Double)

    fun nakresli(gem: GemPrepis, r: Random, realne: Boolean = false, otocenie: Int = 0): Fotka {
        val styl = LinkedHashMap<String, String>()
        val W = 2000; val H = 1500
        val page = BufferedImage(W, H, BufferedImage.TYPE_INT_RGB)
        val g = page.createGraphics()
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON)
        // paper
        val tone = listOf(Color(248, 246, 238), Color(244, 243, 240), Color(250, 249, 244), Color(240, 236, 224)).random(r)
        g.color = tone; g.fillRect(0, 0, W, H)
        val papier = listOf("ciste", "stvorcekovy", "linajkovy").random(r)
        styl["papier"] = papier
        if (papier != "ciste") {
            g.color = if (r.nextBoolean()) Color(150, 175, 210, 110) else Color(140, 140, 140, 90)
            g.stroke = BasicStroke(1.6f)
            val step = r.nextInt(46, 62)
            var y = r.nextInt(step); while (y < H) { g.drawLine(0, y, W, y); y += step }
            if (papier == "stvorcekovy") { var x = r.nextInt(step); while (x < W) { g.drawLine(x, 0, x, H); x += step } }
        }
        // the pen of this sheet: one writer, one pen
        val font = fonty.randomOrNull(r)?.let { runCatching { Font.createFont(Font.TRUETYPE_FONT, it) }.getOrNull() }
        val pero = Pero(
            listOf(Color(28, 52, 160), Color(20, 30, 110), Color(25, 25, 30), Color(70, 70, 75), Color(10, 60, 140)).random(r),
            r.nextDouble(2.2, 4.2).toFloat(), font, r.nextInt(1000), r.nextDouble(-0.28, 0.08), r.nextDouble(52.0, 74.0),
        )
        val pouziGlyfy = realne && glyfy != null
        styl["pismo"] = if (pouziGlyfy) "MathWriting" else font?.fontName ?: "?"
        styl["sklon"] = "%.2f".format(pero.sklon)

        // layout: matrices left to right, wrapping, a "∼" with the note between
        val boxes = ArrayList<List<Double>>()
        var x = r.nextInt(70, 160).toDouble()
        var y = r.nextInt(80, 200).toDouble()
        var lineH = 0.0
        val kept = gem.matice
        for ((mi, m) in kept.withIndex()) {
            val rows = m.riadky.size
            val cols = m.riadky.firstOrNull()?.size ?: 0
            val cw = pero.velkost * r.nextDouble(1.05, 1.5)
            val colW = (0 until cols).map { j -> max(cw, m.riadky.maxOf { it.getOrNull(j)?.v?.length ?: 1 } * pero.velkost * 0.55 + cw * 0.4) }
            val rowH = pero.velkost * r.nextDouble(1.15, 1.45)
            val mw = colW.sum() + pero.velkost * 1.2 + (if (m.ciara != null) pero.velkost * 0.6 else 0.0)
            val mh = rows * rowH + pero.velkost * 0.3
            val noteW = if (mi > 0) max(pero.velkost * 1.6, (gem.operacie.getOrNull(mi - 1)?.length ?: 0) * pero.velkost * 0.24) else 0.0
            if (mi > 0 && x + noteW + mw > W - 60) { x = r.nextInt(60, 140).toDouble(); y += lineH + r.nextDouble(90.0, 160.0); lineH = 0.0 }
            if (y + mh > H - 40) break
            if (mi > 0) {
                // the ∼ and the note above it
                val tx = x + noteW / 2 - pero.velkost * 0.35
                val ty = y + mh / 2
                znak(g, "\\sim", tx, ty + pero.velkost * 0.25, pero, r, pouziGlyfy, scale = 0.9)
                gem.operacie.getOrNull(mi - 1)?.let { note -> text(g, note, x, ty - pero.velkost * r.nextDouble(0.5, 0.9), pero, r, 0.42) }
                x += noteW + pero.velkost * 0.4
            }
            val left = x
            // parentheses
            paren(g, x, y, mh, pero, r, true)
            var cx = x + pero.velkost * 0.6
            for (j in 0 until cols) {
                if (m.ciara == j) {
                    g.color = pero.farba; g.stroke = BasicStroke(pero.hrubka * 0.9f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND)
                    val bx = cx + r.nextDouble(-3.0, 3.0)
                    g.drawLine(bx.roundToInt(), (y + 6).roundToInt(), (bx + r.nextDouble(-6.0, 6.0)).roundToInt(), (y + mh - 6).roundToInt())
                    cx += pero.velkost * 0.6
                }
                for (i in 0 until rows) {
                    val v = m.riadky[i].getOrNull(j)?.v ?: continue
                    val by = y + (i + 0.82) * rowH + r.nextDouble(-4.0, 4.0)
                    val est = v.length * pero.velkost * 0.5
                    text(g, v, cx + colW[j] - est - pero.velkost * 0.25 + r.nextDouble(-6.0, 6.0), by, pero, r, 1.0, pouziGlyfy)
                }
                cx += colW[j]
            }
            paren(g, cx + pero.velkost * 0.1, y, mh, pero, r, false)
            val right = cx + pero.velkost * 0.6
            if (m.preskrtnuta) {
                g.color = pero.farba; g.stroke = BasicStroke(pero.hrubka * 1.1f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND)
                if (r.nextBoolean()) { g.drawLine(left.toInt(), y.toInt(), right.toInt(), (y + mh).toInt()); g.drawLine(left.toInt(), (y + mh).toInt(), right.toInt(), y.toInt()) }
                else { var yy = y + 10; while (yy < y + mh) { g.drawLine(left.toInt(), (yy + r.nextDouble(-8.0, 8.0)).toInt(), right.toInt(), (yy + r.nextDouble(-8.0, 8.0)).toInt()); yy += 18 } }
                styl["preskrtnute"] = "áno"
            }
            boxes += listOf(left / W, y / H, right / W, (y + mh) / H)
            x = right + pero.velkost * 0.5
            lineH = max(lineH, mh)
        }
        g.dispose()
        // the photo: crumples, shadow, perspective, a little turn, blur, noise
        val photo = fotit(page, r, styl)
        val (img, bx) = otoc(photo.first, photo.second(boxes), otocenie)
        styl["otocenie"] = "$otocenie"
        val q = r.nextDouble(0.62, 0.9).toFloat()
        styl["jpeg"] = "%.2f".format(q)
        return Fotka(jpeg(zmensi(img, 2048), q), bx, otocenie, styl)
    }

    private fun text(g: java.awt.Graphics2D, s: String, x0: Double, y0: Double, p: Pero, r: Random, scale: Double, glyfyOk: Boolean = false) {
        var x = x0
        val drift = r.nextDouble(-3.0, 3.0)
        for ((k, ch) in s.withIndex()) {
            val sym = ch.toString().replace("−", "-")
            if (ch == ' ') { x += p.velkost * scale * 0.35; continue }
            x += znak(g, sym, x, y0 + drift * k * 0.3, p, r, glyfyOk && glyfy!!.ma(sym), scale)
        }
    }

    /** One character at (x, baseline y); returns its advance. */
    private fun znak(g: java.awt.Graphics2D, sym: String, x: Double, y: Double, p: Pero, r: Random, glyf: Boolean, scale: Double = 1.0): Double {
        val size = p.velkost * scale * r.nextDouble(0.88, 1.12)
        val rot = r.nextDouble(-0.09, 0.09)
        g.color = p.farba
        if (glyf && glyfy != null && glyfy.ma(sym)) {
            val samples = glyfy.tahy.getValue(sym)
            val strokes = samples[(p.glyfIdx + r.nextInt(2)) % samples.size]
            val all = strokes.flatten()
            val minX = all.minOf { it.x }; val maxX = all.maxOf { it.x }; val minY = all.minOf { it.y }; val maxY = all.maxOf { it.y }
            val h = max(maxY - minY, (maxX - minX) * 0.6).coerceAtLeast(1e-6)
            val targetH = when (sym) { "-" -> size * 0.12; "\\sim" -> size * 0.3; else -> size * 0.72 }
            val k = targetH / h
            val w = (maxX - minX) * k
            val oy = when (sym) { "-" -> y - size * 0.32; "\\sim" -> y - size * 0.35; else -> y - (maxY - minY) * k }
            val at = AffineTransform().apply { translate(x, oy); rotate(rot); shear(p.sklon, 0.0) }
            g.stroke = BasicStroke(p.hrubka, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND)
            for (s in strokes) {
                val path = GeneralPath()
                s.forEachIndexed { i, pt ->
                    val q = at.transform(Point2D.Double((pt.x - minX) * k, (pt.y - minY) * k), null)
                    if (i == 0) path.moveTo(q.x, q.y) else path.lineTo(q.x, q.y)
                }
                if (s.size == 1) path.lineTo(at.transform(Point2D.Double((s[0].x - minX) * k + 1, (s[0].y - minY) * k), null).x, at.transform(Point2D.Double(0.0, (s[0].y - minY) * k), null).y)
                g.draw(path)
            }
            return max(w, size * 0.3) + size * r.nextDouble(0.08, 0.2)
        }
        val text = if (sym == "\\sim") "~" else sym
        val f = (p.font ?: Font(Font.SERIF, Font.ITALIC, 12)).deriveFont(size.toFloat())
        if (!f.canDisplayUpTo(text).let { it == -1 }) {
            val fb = Font(Font.SANS_SERIF, Font.PLAIN, size.toInt())
            g.font = fb
        } else g.font = f
        val old = g.transform
        g.translate(x, y); g.rotate(rot); g.shear(p.sklon, 0.0)
        // ink: a second pass a hair off, as a ballpoint leaves it
        g.drawString(text, 0f, 0f)
        if (r.nextInt(3) == 0) { g.color = Color(p.farba.red, p.farba.green, p.farba.blue, 90); g.drawString(text, 0.8f, 0.5f) }
        val adv = g.fontMetrics.stringWidth(text).toDouble()
        g.transform = old
        return adv * r.nextDouble(0.95, 1.12)
    }

    private fun paren(g: java.awt.Graphics2D, x: Double, y: Double, h: Double, p: Pero, r: Random, left: Boolean) {
        g.color = p.farba; g.stroke = BasicStroke(p.hrubka * 1.1f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND)
        val bow = p.velkost * r.nextDouble(0.25, 0.45) * (if (left) -1 else 1)
        val x0 = x + (if (left) p.velkost * 0.3 else 0.0)
        g.draw(CubicCurve2D.Double(x0 + r.nextDouble(-3.0, 3.0), y, x0 + bow, y + h * 0.3, x0 + bow, y + h * 0.7, x0 + r.nextDouble(-3.0, 3.0), y + h))
    }

    /** Paper to phone photo; the second value maps page boxes to photo boxes. */
    private fun fotit(page: BufferedImage, r: Random, styl: MutableMap<String, String>): Pair<BufferedImage, (List<List<Double>>) -> List<List<Double>>> {
        val W = page.width; val H = page.height
        // the page lies on a table, seen from a little off: a homography of the four corners
        val table = listOf(Color(120, 90, 60), Color(60, 60, 64), Color(200, 200, 205), Color(150, 120, 90)).random(r)
        val ow = (W * 1.08).toInt(); val oh = (H * 1.1).toInt()
        val persp = r.nextDouble(0.0, 0.07)
        styl["perspektiva"] = "%.2f".format(persp)
        fun jit(a: Double) = a + r.nextDouble(-persp, persp) * W
        val dst = listOf(Point2D.Double(jit(W * 0.04), jit(H * 0.05)), Point2D.Double(jit(W * 1.04), jit(H * 0.04)), Point2D.Double(jit(W * 1.03), jit(H * 1.05)), Point2D.Double(jit(W * 0.05), jit(H * 1.04)))
        val src = listOf(Point2D.Double(0.0, 0.0), Point2D.Double(W.toDouble(), 0.0), Point2D.Double(W.toDouble(), H.toDouble()), Point2D.Double(0.0, H.toDouble()))
        val hInv = homography(dst, src)
        val hFwd = homography(src, dst)
        // crumples: smooth bumps of light and dark; a shadow across one side
        val bumps = List(if (r.nextInt(3) == 0) r.nextInt(6, 14) else r.nextInt(0, 3)) { doubleArrayOf(r.nextDouble(W.toDouble()), r.nextDouble(H.toDouble()), r.nextDouble(80.0, 400.0), r.nextDouble(-0.10, 0.08)) }
        if (bumps.size > 5) styl["pokrcene"] = "áno"
        val shadow = r.nextInt(3) != 0
        val sAng = r.nextDouble(0.0, 2 * PI); val sStr = r.nextDouble(0.15, 0.45); val sPos = r.nextDouble(0.2, 0.8)
        if (shadow) styl["tien"] = "%.2f".format(sStr)
        val out = BufferedImage(ow, oh, BufferedImage.TYPE_INT_RGB)
        val noise = r.nextDouble(3.0, 11.0)
        val light = r.nextDouble(0.82, 1.05)
        val rgbPage = page.getRGB(0, 0, W, H, null, 0, W)
        val tr = table.red; val tg = table.green; val tb = table.blue
        for (py in 0 until oh) for (px in 0 until ow) {
            val s = apply(hInv, px.toDouble(), py.toDouble())
            var rr: Double; var gg: Double; var bb: Double
            if (s.x < 0 || s.y < 0 || s.x >= W - 1 || s.y >= H - 1) { rr = tr.toDouble(); gg = tg.toDouble(); bb = tb.toDouble() }
            else {
                val x0 = s.x.toInt(); val y0 = s.y.toInt(); val fx = s.x - x0; val fy = s.y - y0
                fun c(ix: Int, iy: Int, sh: Int) = ((rgbPage[iy * W + ix] shr sh) and 255).toDouble()
                fun bil(sh: Int) = c(x0, y0, sh) * (1 - fx) * (1 - fy) + c(x0 + 1, y0, sh) * fx * (1 - fy) + c(x0, y0 + 1, sh) * (1 - fx) * fy + c(x0 + 1, y0 + 1, sh) * fx * fy
                rr = bil(16); gg = bil(8); bb = bil(0)
                var k = light
                for (b in bumps) { val dx = s.x - b[0]; val dy = s.y - b[1]; k += b[3] * exp(-(dx * dx + dy * dy) / (2 * b[2] * b[2])) }
                if (shadow) { val t = (cos(sAng) * s.x / W + sin(sAng) * s.y / H) - sPos; if (t > 0) k -= sStr * min(1.0, t * 4) }
                rr *= k; gg *= k; bb *= k * 0.98
            }
            val n = r.nextDouble(-noise, noise)
            out.setRGB(px, py, (cl(rr + n) shl 16) or (cl(gg + n) shl 8) or cl(bb + n))
        }
        val blurred = if (r.nextInt(3) == 0) { styl["rozmazane"] = "áno"; blur(out) } else out
        val map: (List<List<Double>>) -> List<List<Double>> = { bs -> bs.map { b ->
            val pts = listOf(b[0] to b[1], b[2] to b[1], b[2] to b[3], b[0] to b[3]).map { (u, v) -> apply(hFwd, u * W, v * H) }
            listOf(pts.minOf { it.x } / ow, pts.minOf { it.y } / oh, pts.maxOf { it.x } / ow, pts.maxOf { it.y } / oh)
        } }
        return blurred to map
    }

    private fun cl(v: Double) = v.roundToInt().coerceIn(0, 255)

    private fun blur(img: BufferedImage): BufferedImage {
        val k = floatArrayOf(1f, 2f, 1f, 2f, 4f, 2f, 1f, 2f, 1f).map { it / 16f }.toFloatArray()
        return java.awt.image.ConvolveOp(java.awt.image.Kernel(3, 3, k), java.awt.image.ConvolveOp.EDGE_NO_OP, null).filter(img, null)
    }

    private fun otoc(img: BufferedImage, boxes: List<List<Double>>, deg: Int): Pair<BufferedImage, List<List<Double>>> {
        if (deg == 0) return img to boxes
        val w = img.width; val h = img.height
        val out = if (deg == 180) BufferedImage(w, h, img.type) else BufferedImage(h, w, img.type)
        // the photo is turned by -deg: turning it back by +deg (clockwise) makes it upright
        for (y in 0 until h) for (x in 0 until w) {
            val (nx, ny) = when (deg) { 90 -> y to (w - 1 - x); 180 -> (w - 1 - x) to (h - 1 - y); else -> (h - 1 - y) to x }
            out.setRGB(nx, ny, img.getRGB(x, y))
        }
        val bx = boxes.map { b ->
            when (deg) {
                90 -> listOf(b[1], 1 - b[2], b[3], 1 - b[0])
                180 -> listOf(1 - b[2], 1 - b[3], 1 - b[0], 1 - b[1])
                else -> listOf(1 - b[3], b[0], 1 - b[1], b[2])
            }
        }
        return out to bx
    }

    companion object {
        fun zmensi(img: BufferedImage, maxSide: Int): BufferedImage {
            val s = maxSide.toDouble() / max(img.width, img.height)
            if (s >= 1) return img
            val w = (img.width * s).roundToInt(); val h = (img.height * s).roundToInt()
            val out = BufferedImage(w, h, BufferedImage.TYPE_INT_RGB)
            val g = out.createGraphics()
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC)
            g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY)
            g.drawImage(img, 0, 0, w, h, null); g.dispose()
            return out
        }

        fun jpeg(img: BufferedImage, q: Float): ByteArray {
            val w = ImageIO.getImageWritersByFormatName("jpeg").next()
            val p = w.defaultWriteParam.apply { compressionMode = ImageWriteParam.MODE_EXPLICIT; compressionQuality = q }
            val bos = ByteArrayOutputStream()
            ImageIO.createImageOutputStream(bos).use { out -> w.output = out; w.write(null, IIOImage(img, null, null), p) }
            w.dispose()
            return bos.toByteArray()
        }

        /** A photo from disk as the app sends it: longer side [maxSide], JPEG 85. */
        fun zoSuboru(f: File, maxSide: Int = 2048, otoc: Int = 0): ByteArray {
            var img = ImageIO.read(f)
            if (img.type != BufferedImage.TYPE_INT_RGB) { val c = BufferedImage(img.width, img.height, BufferedImage.TYPE_INT_RGB); c.createGraphics().apply { drawImage(img, 0, 0, null); dispose() }; img = c }
            return jpeg(zmensi(img, maxSide), 0.85f)
        }

        /** The 3×3 homography taking 4 points [a] to [b]. */
        fun homography(a: List<Point2D.Double>, b: List<Point2D.Double>): DoubleArray {
            val m = Array(8) { DoubleArray(9) }
            for (i in 0 until 4) {
                val (x, y) = a[i].x to a[i].y; val (u, v) = b[i].x to b[i].y
                m[2 * i] = doubleArrayOf(x, y, 1.0, 0.0, 0.0, 0.0, -u * x, -u * y, u)
                m[2 * i + 1] = doubleArrayOf(0.0, 0.0, 0.0, x, y, 1.0, -v * x, -v * y, v)
            }
            for (c in 0 until 8) {
                val p = (c until 8).maxBy { abs(m[it][c]) }
                val t = m[c]; m[c] = m[p]; m[p] = t
                for (rr in 0 until 8) if (rr != c) { val f = m[rr][c] / m[c][c]; for (k in c until 9) m[rr][k] -= f * m[c][k] }
            }
            return DoubleArray(9) { if (it == 8) 1.0 else m[it][8] / m[it][it] }
        }

        fun apply(h: DoubleArray, x: Double, y: Double): Point2D.Double {
            val w = h[6] * x + h[7] * y + h[8]
            return Point2D.Double((h[0] * x + h[1] * y + h[2]) / w, (h[3] * x + h[4] * y + h[5]) / w)
        }

        @Suppress("unused") private fun dist(a: Point2D.Double, b: Point2D.Double) = sqrt((a.x - b.x) * (a.x - b.x) + (a.y - b.y) * (a.y - b.y))
    }
}
