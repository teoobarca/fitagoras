package dev.hamper.tabula.platform

import dev.hamper.tabula.res.Res
import kotlinx.coroutines.runBlocking
import org.jetbrains.compose.resources.ExperimentalResourceApi

private val fonts = HashMap<String, ByteArray>()

@OptIn(ExperimentalResourceApi::class)
internal actual fun fontBytes(file: String): ByteArray = synchronized(fonts) {
    fonts.getOrPut(file) { runBlocking { Res.readBytes("font/$file.ttf") } }
}

/** Files from the classpath (the JVM build of composeResources/files). */
@OptIn(ExperimentalResourceApi::class)
fun installJvmAssets() {
    Assets.read = { path -> runBlocking { Res.readBytes("files/$path") }.decodeToString() }
}
