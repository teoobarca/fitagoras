import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

// FITagoras's content, maths and every screen, for Android (the app module), the JVM (unit tests, screenshots without
// an emulator) and the browser (wasmJs). Layout and the compat layer: docs/KMP.md.
plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.kmp.library)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.kotlin.compose)
}

kotlin {
    android {
        namespace = "dev.hamper.tabula.shared"
        compileSdk = 37
        minSdk = 33
        androidResources { enable = true } // composeResources (fonts, drawables, files) ship as the APK's assets
        compilerOptions { jvmTarget.set(JvmTarget.JVM_17) }
    }
    jvm { compilerOptions { jvmTarget.set(JvmTarget.JVM_17) } }
    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        outputModuleName.set("fitest")
        browser { commonWebpackConfig { outputFileName = "fitest.js" } }
        binaries.executable()
    }

    compilerOptions {
        freeCompilerArgs.add("-Xexpect-actual-classes")
        optIn.addAll(
            "androidx.compose.material3.ExperimentalMaterial3Api",
            "androidx.compose.material3.ExperimentalMaterial3ExpressiveApi",
            "androidx.compose.animation.ExperimentalSharedTransitionApi",
            "androidx.compose.foundation.ExperimentalFoundationApi",
            "androidx.compose.foundation.layout.ExperimentalLayoutApi",
            "androidx.compose.ui.text.ExperimentalTextApi",
            "kotlin.time.ExperimentalTime",
        )
    }

    sourceSets {
        // commonMain ─┬─ jvmCommonMain (java.io: Android + JVM) ─┬─ androidMain
        //             │                                         └─ jvmMain
        //             └─ skikoMain (Skia: JVM + browser) ────────┬─ jvmMain
        //                                                        └─ wasmJsMain
        val commonMain by getting
        val jvmCommonMain by creating { dependsOn(commonMain) }
        val skikoMain by creating { dependsOn(commonMain) }
        val androidMain by getting { dependsOn(jvmCommonMain) }
        val jvmMain by getting { dependsOn(jvmCommonMain); dependsOn(skikoMain) }
        val wasmJsMain by getting { dependsOn(skikoMain) }
        val commonTest by getting
        val jvmTest by getting { dependsOn(commonTest) }

        commonMain.dependencies {
            implementation(libs.cmp.runtime)
            implementation(libs.cmp.foundation)
            implementation(libs.cmp.ui)
            implementation(libs.cmp.material3)
            implementation(libs.cmp.resources)
            implementation(libs.cmp.navigation3.ui)
            implementation(libs.cmp.lifecycle.runtime.compose)
            implementation(libs.kotlinx.coroutines.core)
            api(libs.kotlinx.datetime)
        }
        androidMain.dependencies {
            implementation(libs.androidx.activity.compose)
            implementation(libs.media3.exoplayer)
            implementation(libs.media3.datasource)
            implementation(libs.media3.ui.compose)
            implementation(libs.work.runtime)
            implementation(libs.datastore.preferences)
            // the tutor's in-app camera (tutor/TutorFotak.kt)
            implementation(libs.camera.camera2)
            implementation(libs.camera.lifecycle)
            implementation(libs.camera.view)
        }
        jvmMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(libs.kotlinx.coroutines.swing)
        }
        jvmTest.dependencies {
            implementation(libs.junit)
            // taps on the desktop JVM, headless (runComposeUiTest): lesson navigation without an emulator
            implementation(libs.cmp.ui.test)
        }
    }
}

// The course packs the tutor reads (docs/TUTOR.md, "Kontext"): kurz/**/*.md and docs/ZASADY.md copied at build time
// into files/kontext, never by hand into the repo. A custom directory replaces commonMain's own, so it holds both.
val kontextKurzu = tasks.register<Sync>("kontextKurzu") {
    from(layout.projectDirectory.dir("src/commonMain/composeResources"))
    from(rootProject.layout.projectDirectory.dir("kurz")) { include("**/*.md"); into("files/kontext") }
    from(rootProject.layout.projectDirectory.file("docs/ZASADY.md")) { into("files/kontext") }
    into(layout.buildDirectory.dir("generated/kontext"))
    // the list of packs, for the tutor's "kurz" tool (common code cannot list a directory)
    doLast {
        val d = destinationDir.resolve("files/kontext")
        d.resolve("index.txt").writeText(d.walkTopDown().filter { it.isFile && it.extension == "md" }.map { it.relativeTo(d).invariantSeparatorsPath }.sorted().joinToString("\n"))
    }
}

compose.resources {
    packageOfResClass = "dev.hamper.tabula.res"
    publicResClass = true
    generateResClass = always
    customDirectory(sourceSetName = "commonMain", directoryProvider = layout.dir(kontextKurzu.map { it.destinationDir }))
}

// The files under composeResources/files, as a list common code can read (the browser cannot list a directory;
// ClipLibrary loads every clips-*.json).
val filesDir = layout.projectDirectory.dir("src/commonMain/composeResources/files")
val resourceIndex = tasks.register("resourceIndex") {
    val out = layout.buildDirectory.dir("generated/resourceIndex/kotlin")
    inputs.dir(filesDir).withPathSensitivity(PathSensitivity.RELATIVE)
    outputs.dir(out)
    val root = filesDir.asFile
    doLast {
        val names = root.walkTopDown().filter { it.isFile }.map { it.relativeTo(root).invariantSeparatorsPath }.sorted().toList()
        val f = out.get().file("dev/hamper/tabula/platform/ResourceIndex.kt").asFile
        f.parentFile.mkdirs()
        f.writeText(buildString {
            append("package dev.hamper.tabula.platform\n\n/** Generated by :shared:resourceIndex from composeResources/files. */\nobject ResourceIndex {\n    val files = listOf(\n")
            names.forEach { append("        \"").append(it).append("\",\n") }
            append("    )\n}\n")
        })
    }
}
kotlin.sourceSets.commonMain { kotlin.srcDir(resourceIndex) }

// The app's sounds (app/src/main/assets/zvuky) also next to the web app's index.html, played through Web Audio
// (wasmJsMain/resources/web.js); one source of the files for both.
kotlin.sourceSets.named("wasmJsMain") { resources.srcDir(rootProject.layout.projectDirectory.dir("app/src/main/assets")) }

// Unit tests read the shipped files from disk (clips, the coverage register): a changed file must rerun them.
// The repo checks read the checkout too (OpenQuizTest: docs/, kurz/, kontroly/; RepoLintTest and NoSecretsTrackedTest:
// every tracked file; AppIdTest: the app's build file and shortcuts): with the build cache on, a change there must rerun
// them, never replay a cached result. kotlin-js-store/ is left out: the wasm build writes it (kotlinWasmStoreYarnLock).
val repoFiles = rootProject.fileTree(rootDir) {
    exclude("**/build/**", "kotlin-js-store/**", "**/.gradle/**", "**/.kotlin/**", "**/node_modules/**", ".git", ".idea/**", "**/.DS_Store", "**/__pycache__/**", "local.properties")
}
tasks.withType<Test>().configureEach {
    inputs.dir(filesDir).withPropertyName("files").withPathSensitivity(PathSensitivity.RELATIVE)
    inputs.files(repoFiles).withPropertyName("repo").withPathSensitivity(PathSensitivity.RELATIVE)
    maxHeapSize = "2g"
    // test classes split over forks (-Ptest.forks=1 when memory is short); 2 x 2 GB next to the daemons fits the 16 GB machine (docs/SYSTEM.md, "Speed")
    maxParallelForks = (findProperty("test.forks") as String?)?.toInt() ?: 2
    // the tutor's motion frames (screens/TutorPohybTest): -Ppohyb.out=<folder>, skipped without it
    providers.gradleProperty("pohyb.out").orNull?.let { systemProperty("pohyb.out", it); outputs.upToDateWhen { false } }
}

// Screenshots on the desktop JVM (Skia), no emulator: -Psnap.route=…, -Psnap.out=…, theme, width, font, height,
// density, course (shared/src/jvmMain/.../desktop/Snap.kt). Always renders.
val snapProps = listOf("route", "course", "theme", "width", "height", "font", "out", "density").associateWith { providers.gradleProperty("snap.$it") }
tasks.register<JavaExec>("snapDesktop") {
    group = "verification"; description = "Renders -Psnap.route to PNGs in -Psnap.out on the desktop JVM"
    val main = kotlin.jvm().compilations["main"]
    classpath = files(main.output.allOutputs, main.runtimeDependencyFiles)
    mainClass.set("dev.hamper.tabula.desktop.SnapKt")
    jvmArgs("-Djava.awt.headless=true", "-Xmx2g")
    snapProps.forEach { (k, v) -> v.orNull?.let { systemProperty("snap.$k", it) } }
    outputs.upToDateWhen { false }
}

// The text of every lesson, card, proof and a few exercises of each generator as markdown, one file per chapter, for
// the LLM checks (kontroly/, scripts/llm-kontrola.sh): -Pexport.out=<folder> [-Pexport.seeds=2]. The exporter is test
// code (shared/src/jvmTest/.../data/ContentExport.kt) so it reuses the tests' helpers and never ships.
tasks.register<JavaExec>("exportContent") {
    group = "verification"; description = "Writes the text of all LA1 and DML content to -Pexport.out as markdown"
    val test = kotlin.jvm().compilations["test"]
    val main = kotlin.jvm().compilations["main"]
    classpath = files(test.output.allOutputs, main.output.allOutputs, test.runtimeDependencyFiles)
    mainClass.set("dev.hamper.tabula.data.ContentExportKt")
    workingDir = projectDir
    jvmArgs("-Djava.awt.headless=true", "-Xmx2g")
    val out = providers.gradleProperty("export.out")
    val seeds = providers.gradleProperty("export.seeds")
    argumentProviders.add(CommandLineArgumentProvider { listOf(out.orNull ?: error("-Pexport.out=<folder>"), seeds.orNull ?: "2") })
    outputs.upToDateWhen { false }
}

// The tutor's harness (docs/TUTOR.md, "Harness"): the jvm jar and its runtime classpath in build/tutor-harness/lib, so
// scripts/tutor.sh runs it with plain java: no Gradle and no build lock while it waits for a sign-in or the model.
tasks.register<Sync>("tutorHarness") {
    group = "verification"; description = "Builds the tutor's harness into build/tutor-harness (run it with scripts/tutor.sh)"
    val main = kotlin.jvm().compilations["main"]
    from(tasks.named("jvmJar"))
    from(main.runtimeDependencyFiles)
    into(layout.buildDirectory.dir("tutor-harness/lib"))
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}
