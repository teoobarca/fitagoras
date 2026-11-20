package dev.hamper.tabula.platform

/** The bytes of composeResources/font/[file].ttf: the JVM reads them from the classpath, the browser preloads them. */
internal expect fun fontBytes(file: String): ByteArray
