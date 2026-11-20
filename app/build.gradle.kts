import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

// The release key lives outside every checkout, by default in ~/.fitest (keystore.properties names the .jks by absolute
// path). Never in git: a key inside the repo was once deleted by a checkout.
// Every update must be signed with it. -PfitestSigning=<file> points elsewhere.
val signingFile = (findProperty("fitestSigning") as String?)?.let { File(it) } ?: File(System.getProperty("user.home"), ".fitest/keystore.properties")
val signing = Properties().apply { signingFile.takeIf { it.exists() }?.reader()?.use { load(it) } }
// The guest key for lecture clips (the app's server holds only its hash): secrets.properties, never in git.
// Without the file the app builds and reads clips as captions; a release refuses to build without it.
val clipKey: String = Properties().apply { rootProject.file("secrets.properties").takeIf { it.exists() }?.reader()?.use { load(it) } }
    .getProperty("clipsGuestToken", "").trim()

android {
    namespace = "dev.hamper.tabula"
    compileSdk = 37

    defaultConfig {
        // FITest since 1.3.0: a new id and key (the 1.2.x key was lost); 1.2.x (dev.hamper.tabula) can stay installed next to it
        applicationId = "dev.hamper.fitest"
        minSdk = 33
        targetSdk = 36
        versionCode = 48
        versionName = "1.6.9"
        buildConfigField("String", "CLIPS_GUEST_TOKEN", "\"$clipKey\"")
        // the update check (system/Updater.kt); -PfitestUpdateUrl=… points a test build at a local server
        buildConfigField("String", "UPDATE_URL", "\"${providers.gradleProperty("fitestUpdateUrl").getOrElse("https://fitagoras.hamper.dev/api/fitest/latest")}\"")
        ndk { abiFilters += setOf("arm64-v8a", "x86_64") }
    }
    signingConfigs {
        if (signing.isNotEmpty()) create("release") {
            storeFile = File(signing.getProperty("storeFile"))
            storePassword = signing.getProperty("storePassword")
            keyAlias = signing.getProperty("keyAlias")
            keyPassword = signing.getProperty("keyPassword")
        }
    }
    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            signingConfigs.findByName("release")?.let { signingConfig = it }
        }
    }
    buildFeatures { compose = true; buildConfig = true }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    androidResources {
        noCompress += listOf("mp4", "wav")
        // the sounds' provenance note stays in the repository, not in the APK
        ignoreAssetsPattern = "!POVOD.md:!.svn:!.git:!.ds_store:!*.scc:.*:!CVS:!thumbs.db:!picasa.ini:!*~"
    }
    // JVM screenshots (Robolectric) read the real resources: fonts, drawables, strings
    testOptions { unitTests.isIncludeAndroidResources = true }
}

kotlin {
    compilerOptions {
        optIn.addAll(
            "androidx.compose.material3.ExperimentalMaterial3Api",
            "androidx.compose.material3.ExperimentalMaterial3ExpressiveApi",
            "androidx.compose.animation.ExperimentalSharedTransitionApi",
            "androidx.compose.foundation.ExperimentalFoundationApi",
            "androidx.compose.foundation.layout.ExperimentalLayoutApi",
            "androidx.compose.ui.text.ExperimentalTextApi",
            "androidx.media3.common.util.UnstableApi",
        )
    }
}

dependencies {
    // Content, maths and every screen (Compose Multiplatform). The androidx versions below stay the app's own: Gradle
    // picks the higher of these and what shared/ asks for, so Android runs the same Compose, Material 3 and media3.
    implementation(project(":shared"))
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.core.splashscreen)
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.foundation)
    implementation(libs.compose.material3)
    implementation(libs.navigation3.ui)
    implementation(libs.navigation3.runtime)
    implementation(libs.lifecycle.runtime.compose)
    implementation(libs.lifecycle.service)
    implementation(libs.media3.exoplayer)
    implementation(libs.media3.ui.compose)
    implementation(libs.glance.appwidget)
    implementation(libs.glance.material3)
    implementation(libs.coroutines.android)
    implementation(libs.media3.datasource)
    implementation(libs.datastore.preferences)
    implementation(libs.work.runtime)
    testImplementation(libs.junit)
    testImplementation("org.json:json:20250517")
    testImplementation(libs.robolectric)
    testImplementation(libs.roborazzi)
    testImplementation(libs.roborazzi.compose)
    testImplementation(libs.compose.ui.test.junit4)
    debugImplementation(libs.compose.ui.test.manifest) // ComponentActivity in the debug manifest, for FoldTest (Robolectric reads that manifest)
    testImplementation(libs.espresso.core) // 3.5.1 from ui-test cannot inject input on API 37
    debugImplementation(libs.compose.ui.test.manifest) // createComposeRule needs its activity (GemStageLayoutTest)
}

tasks.matching { it.name == "preReleaseBuild" }.configureEach { // before packaging: no keyless APK is left behind
    val missing = clipKey.isEmpty() // a plain local: the configuration cache can't keep a reference to the script
    val unsigned = signing.isEmpty()
    val where = signingFile.path
    val testServer = providers.gradleProperty("fitestUpdateUrl").orNull // a local test server must never reach a release
    doFirst {
        if (missing) throw GradleException("secrets.properties with clipsGuestToken is missing: a release without it plays no clips")
        if (unsigned) throw GradleException("$where is missing: a release must be signed with the app's release key (docs/VYVOJ.md, \"Kľúče a vydanie\")")
        if (testServer != null) throw GradleException("-PfitestUpdateUrl=$testServer is for debug builds: a release asks the app's server for updates; build the release in a separate run")
    }
}

// JVM screenshots (docs/SYSTEM.md, "Screenshots without the emulator"). The ordinary unit tests skip them; three tasks
// run them on the same classes and classpath as testDebugUnitTest:
//   snap           a route or your own shots, -Psnap.route=… -Psnap.out=… (writes, never compares)
//   snapCatalogue  every step type and the first steps of every lesson, both courses, both themes
//   snapGolden     compares a small stable set with app/src/test/snapshots (-Psnap.record=true rewrites it)
val snapProps = listOf("route", "course", "theme", "width", "height", "font", "out", "only", "steps", "seeds", "smer", "sitting", "look")
    .associateWith { providers.gradleProperty("snap.$it") }
val snapRecord = providers.gradleProperty("snap.record").map { it == "true" }.orElse(false)
val snapTest = providers.gradleProperty("snap.test")
tasks.withType<Test>().configureEach {
    if (!name.startsWith("snap")) {
        exclude("dev/hamper/tabula/snap/**")
        // Robolectric classes split over forks, 2 GB each like the snap tasks (docs/SYSTEM.md, "Speed")
        maxParallelForks = (findProperty("test.forks") as String?)?.toInt() ?: 2
        maxHeapSize = "2g"
    }
}
afterEvaluate {
    val unit = tasks.named<Test>("testDebugUnitTest")
    fun snapTask(name: String, desc: String, defaultClass: String, golden: Boolean = false) = tasks.register<Test>(name) {
        group = "verification"; description = desc
        testClassesDirs = unit.get().testClassesDirs
        classpath = unit.get().classpath
        // -Psnap.test=A,B*: several classes in one run (one turn of the shared Gradle lock)
        (if (name == "snap") snapTest.getOrElse(defaultClass) else defaultClass).split(",").forEach { filter.includeTestsMatching("dev.hamper.tabula.snap.${it.trim()}") }
        systemProperty("robolectric.pixelCopyRenderMode", "hardware")
        snapProps.forEach { (k, v) -> v.orNull?.let { systemProperty("snap.$k", it) } }
        if (golden) {
            systemProperty("snap.golden", layout.projectDirectory.dir("src/test/snapshots").asFile.path)
            systemProperty("snap.record", snapRecord.get().toString())
        }
        // Always render and compare: the PNGs, the references and res/ (fonts) are not inputs this task could track.
        // Never from the build cache either: a cached result would skip the rendering (no PNGs) and the comparison.
        outputs.upToDateWhen { false }
        outputs.cacheIf { false }
        // 3 GB: the statement scan renders ~220 cards in one Robolectric run; at 2 GB the later captures came out blank
        maxHeapSize = "3g"
        // a fresh JVM per test class: Robolectric captures degrade after several hundred in one process (OverflowScan*)
        forkEvery = 1
        testLogging { events("failed"); showStandardStreams = true; exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL }
    }
    snapTask("snap", "Renders -Psnap.route (or -Psnap.test) to PNGs in -Psnap.out", "SnapTest")
    snapTask("snapCatalogue", "One PNG per step type and per lesson start, both courses and themes", "CatalogueTest")
    snapTask("snapGolden", "Compares a stable set of screens with app/src/test/snapshots", "GoldenTest", golden = true)
    snapTask("snapFold", "Fails when a question step's first answer is below the fold on Teo's phone (411 dp, 100 %)", "FoldTest")
}
