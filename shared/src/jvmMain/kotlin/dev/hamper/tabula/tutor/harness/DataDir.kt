package dev.hamper.tabula.tutor.harness

import java.io.File

/**
 * The harness's data outside the repository (fonts, MathWriting, sets, runs; docs/TUTOR.md, "Harness"):
 * `$FITAGORAS_TUTOR_DATA`, else `~/.fitagoras/tutor-data`.
 */
internal val tutorData: File = System.getenv("FITAGORAS_TUTOR_DATA")?.let(::File)
    ?: File(System.getProperty("user.home"), ".fitagoras/tutor-data")
