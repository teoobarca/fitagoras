package dev.hamper.tabula.tutor

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Matrix
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.FocusMeteringAction
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import dev.hamper.tabula.res.Res
import dev.hamper.tabula.res.ic_close
import dev.hamper.tabula.res.ic_flash
import dev.hamper.tabula.ui.Icon24
import dev.hamper.tabula.ui.T
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import kotlin.math.max

/**
 * The in-app camera (Teo, 17. 11.: no system camera): the viewfinder fills the screen, one tap takes a page, the page
 * flies into the strip below, the next tap takes the next one, a tapped thumbnail is removed, "Odoslať" sends; pages
 * can come from the gallery too. No cropping (Teo, 18. 11.: the tutor finds its way on a full page). Tap the viewfinder
 * to focus, the flash icon is the torch. A blurred or dark page is refused at once (it would only waste the plan).
 * Pages stay in memory, never in the gallery; JPEG 85 at most 2048 px, no EXIF (no location).
 */
@Composable
internal fun TutorFotak(maxStran: Int, onHotovo: (List<ByteArray>) -> Unit, onZavri: () -> Unit) {
    val ctx = LocalContext.current
    val owner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()
    var povolene by remember { mutableStateOf(ContextCompat.checkSelfPermission(ctx, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) }
    var odmietnute by remember { mutableStateOf(false) }
    val ziadost = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { ok -> povolene = ok; odmietnute = !ok }
    LaunchedEffect(Unit) { if (!povolene) ziadost.launch(Manifest.permission.CAMERA) }
    val strany = remember { mutableStateListOf<Pair<ByteArray, Bitmap>>() }
    var fotim by remember { mutableStateOf(false) }
    var hlaska by remember { mutableStateOf<String?>(null) }
    var svetlo by remember { mutableStateOf(false) }
    val capture = remember { ImageCapture.Builder().setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY).build() }
    var camera by remember { mutableStateOf<Camera?>(null) }
    val previewView = remember { PreviewView(ctx).apply { scaleType = PreviewView.ScaleType.FILL_CENTER } }

    // the camera is bound only now that the student asked for it (its indicator lights up), and released with the screen
    DisposableEffect(povolene) {
        var provider: ProcessCameraProvider? = null
        var gone = false
        if (povolene) {
            val f = ProcessCameraProvider.getInstance(ctx)
            f.addListener({
                if (gone) return@addListener
                runCatching {
                    val p = f.get(); provider = p
                    val preview = Preview.Builder().build().also { it.surfaceProvider = previewView.surfaceProvider }
                    p.unbindAll()
                    camera = p.bindToLifecycle(owner, CameraSelector.DEFAULT_BACK_CAMERA, preview, capture)
                }.onFailure { hlaska = "Foťák sa nepodarilo spustiť." }
            }, ContextCompat.getMainExecutor(ctx))
        }
        onDispose { gone = true; runCatching { provider?.unbindAll() }; camera = null }
    }

    // pages from the gallery: the same JPEG as a photo (upright, at most 2048 px, no EXIF), checked the same way
    val galeria = rememberLauncherForActivityResult(ActivityResultContracts.PickMultipleVisualMedia(maxOf(2, maxStran))) { uris ->
        scope.launch {
            for (u in uris.take(maxStran - strany.size)) {
                val res = withContext(Dispatchers.IO) { runCatching { zGalerie(ctx, u) } }
                res.onSuccess { (jpg, thumb, problem) -> if (problem != null) hlaska = problem else strany += jpg to thumb }
                    .onFailure { hlaska = "Fotku z galérie sa nepodarilo otvoriť." }
            }
        }
    }

    fun odfot() {
        if (fotim || strany.size >= maxStran) return
        fotim = true; hlaska = null
        capture.takePicture(ContextCompat.getMainExecutor(ctx), object : ImageCapture.OnImageCapturedCallback() {
            override fun onCaptureSuccess(image: ImageProxy) {
                scope.launch {
                    val res = withContext(Dispatchers.Default) { runCatching { spracuj(image) }.also { image.close() } }
                    fotim = false
                    res.onSuccess { (jpg, thumb, problem) -> if (problem != null) { hlaska = problem } else { strany += jpg to thumb } }
                        .onFailure { hlaska = "Fotka sa nepodarila, skús znova." }
                }
            }
            override fun onError(exception: ImageCaptureException) { fotim = false; hlaska = "Fotka sa nepodarila, skús znova." }
        })
    }

    Box(Modifier.fillMaxSize().background(Color.Black)) {
        if (povolene) AndroidView({ previewView }, Modifier.fillMaxSize().semantics { contentDescription = "Hľadáčik. Ťukni, kam zaostriť." }) { v ->
            v.setOnTouchListener { view, e ->
                if (e.action == android.view.MotionEvent.ACTION_UP) {
                    val pt = previewView.meteringPointFactory.createPoint(e.x, e.y)
                    camera?.cameraControl?.startFocusAndMetering(FocusMeteringAction.Builder(pt).build())
                    view.performClick()
                }
                true
            }
        } else Column(Modifier.fillMaxSize().padding(32.dp), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
            Text(if (odmietnute) "Bez povolenia foťáka papier neodfotím. Povoliť ho môžeš v nastaveniach telefónu, alebo vyber fotky z galérie." else "Appka potrebuje foťák, aby odfotila tvoje riešenie.",
                color = Color.White, style = MaterialTheme.typography.bodyLarge)
            Spacer(Modifier.height(16.dp))
            Text("Povoliť", color = Color.White, style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.clip(RoundedCornerShape(20.dp)).clickable(role = Role.Button) { ziadost.launch(Manifest.permission.CAMERA) }.padding(16.dp))
            Text("Vybrať z galérie", color = Color.White, style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.clip(RoundedCornerShape(20.dp)).clickable(role = Role.Button) { galeria.launch(androidx.activity.result.PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }.padding(16.dp))
        }
        // top: close, torch
        Row(Modifier.fillMaxWidth().statusBarsPadding().padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
            Kruh("Zavrieť", onZavri) { Icon24(Res.drawable.ic_close, tint = Color.White) }
            Spacer(Modifier.weight(1f))
            if (camera?.cameraInfo?.hasFlashUnit() == true) Kruh(if (svetlo) "Vypnúť svetlo" else "Zapnúť svetlo", { svetlo = !svetlo; camera?.cameraControl?.enableTorch(svetlo) }) {
                Icon24(Res.drawable.ic_flash, tint = if (svetlo) Color(0xFFFFD54F) else Color.White)
            }
        }
        // bottom: what to do, pages, shutter, send
        Column(Modifier.align(Alignment.BottomCenter).fillMaxWidth().background(Color(0xAA000000)).navigationBarsPadding().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(hlaska ?: if (strany.isEmpty()) "Odfoť svoje riešenie, celý papier ostro. Viac strán môžeš odfotiť po sebe." else "Ďalšia strana, alebo Odoslať. Ťuknutím na náhľad stranu zmažeš.",
                color = if (hlaska != null) Color(0xFFFFB4A9) else Color.White, style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.height(12.dp))
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Row(Modifier.weight(1f).horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    if (strany.isEmpty()) Text("Galéria", color = Color.White, style = MaterialTheme.typography.titleSmall,
                        modifier = Modifier.clip(RoundedCornerShape(20.dp)).border(1.dp, Color.White, RoundedCornerShape(20.dp))
                            .clickable(role = Role.Button) { galeria.launch(androidx.activity.result.PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }
                            .padding(horizontal = 14.dp, vertical = 10.dp))
                    strany.forEachIndexed { i, (_, thumb) ->
                        Image(thumb.asImageBitmap(), "Strana ${i + 1}, ťukni a zmaž ju", Modifier.size(52.dp).clip(RoundedCornerShape(8.dp)).border(1.dp, Color.White, RoundedCornerShape(8.dp))
                            .clickable(role = Role.Button) { strany.removeAt(i) }, contentScale = ContentScale.Crop)
                    }
                }
                // the shutter
                Box(Modifier.size(76.dp).clip(CircleShape).border(4.dp, Color.White, CircleShape).padding(8.dp).clip(CircleShape)
                    .background(if (fotim || strany.size >= maxStran || !povolene) Color.Gray else Color.White)
                    .clickable(enabled = povolene && !fotim && strany.size < maxStran, role = Role.Button) { odfot() }.semantics { contentDescription = "Odfotiť stranu" })
                Box(Modifier.weight(1f), contentAlignment = Alignment.CenterEnd) {
                    if (strany.isNotEmpty()) Text("Odoslať", color = Color.Black, style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.clip(RoundedCornerShape(24.dp)).background(Color.White).clickable(role = Role.Button) { onHotovo(strany.map { it.first }) }.padding(horizontal = 18.dp, vertical = 12.dp))
                }
            }
        }
    }
}

@Composable
private fun Kruh(popis: String, onClick: () -> Unit, content: @Composable () -> Unit) {
    Box(Modifier.size(48.dp).clip(CircleShape).background(Color(0x66000000)).clickable(role = Role.Button, onClick = onClick).semantics { contentDescription = popis }, contentAlignment = Alignment.Center) { content() }
}

/** A picture from the gallery as a page: decoded at most 2048 px, upright by its EXIF, re-encoded without it. */
private fun zGalerie(ctx: android.content.Context, u: android.net.Uri): Triple<ByteArray, Bitmap, String?> {
    val src = android.graphics.ImageDecoder.createSource(ctx.contentResolver, u)
    val bmp = android.graphics.ImageDecoder.decodeBitmap(src) { d, info, _ ->
        val s = minOf(1f, 2048f / max(info.size.width, info.size.height))
        d.setTargetSize(max(1, (info.size.width * s).toInt()), max(1, (info.size.height * s).toInt()))
        d.allocator = android.graphics.ImageDecoder.ALLOCATOR_SOFTWARE
    }
    val jpg = ByteArrayOutputStream().also { bmp.compress(Bitmap.CompressFormat.JPEG, 85, it) }.toByteArray()
    return Triple(jpg, Bitmap.createScaledBitmap(bmp, max(1, bmp.width / 16), max(1, bmp.height / 16), true), kvalita(bmp))
}

/** The captured frame upright, at most 2048 px, JPEG 85 (no EXIF); a thumbnail; or why it can't be used. */
private fun spracuj(image: ImageProxy): Triple<ByteArray, Bitmap, String?> {
    val raw = image.toBitmap()
    val rot = image.imageInfo.rotationDegrees
    val s = minOf(1f, 2048f / max(raw.width, raw.height))
    val m = Matrix().apply { postScale(s, s); postRotate(rot.toFloat()) }
    val bmp = Bitmap.createBitmap(raw, 0, 0, raw.width, raw.height, m, true)
    val jpg = ByteArrayOutputStream().also { bmp.compress(Bitmap.CompressFormat.JPEG, 85, it) }.toByteArray()
    val thumb = Bitmap.createScaledBitmap(bmp, max(1, bmp.width / 16), max(1, bmp.height / 16), true)
    return Triple(jpg, thumb, kvalita(bmp))
}

/** A page too dark or too blurred to read (the variance of the Laplacian of a small grey copy). */
private fun kvalita(b: Bitmap): String? {
    val w = 320; val h = max(1, b.height * w / b.width)
    val small = Bitmap.createScaledBitmap(b, w, h, true)
    val px = IntArray(w * h).also { small.getPixels(it, 0, w, 0, 0, w, h) }
    val g = DoubleArray(px.size) { val c = px[it]; 0.299 * ((c shr 16) and 255) + 0.587 * ((c shr 8) and 255) + 0.114 * (c and 255) }
    if (g.average() < 45) return "Je príliš tma. Rozsviet alebo zapni svetlo vpravo hore."
    var sum = 0.0; var sum2 = 0.0; var n = 0
    for (y in 1 until h - 1) for (x in 1 until w - 1) {
        val l = 4 * g[y * w + x] - g[y * w + x - 1] - g[y * w + x + 1] - g[(y - 1) * w + x] - g[(y + 1) * w + x]
        sum += l; sum2 += l * l; n++
    }
    val variance = sum2 / n - (sum / n) * (sum / n)
    return if (variance < 3) "Fotka je rozmazaná. Podrž telefón chvíľu pokojne a ťukni na papier, nech zaostrí." else null
}
