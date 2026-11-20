pluginManagement { repositories { google(); mavenCentral(); gradlePluginPortal() } }
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google(); mavenCentral()
        // Kotlin/Wasm (shared/) fetches Node.js, Yarn and Binaryen from these; projects may not add repositories
        // themselves, so build.gradle.kts tells the Kotlin plugin not to (downloadBaseUrl = null)
        exclusiveContent {
            forRepository { ivy("https://nodejs.org/dist") { name = "Node.js"; patternLayout { artifact("v[revision]/[artifact](-v[revision]-[classifier]).[ext]") }; metadataSources { artifact() } } }
            filter { includeModule("org.nodejs", "node") }
        }
        exclusiveContent {
            forRepository { ivy("https://github.com/yarnpkg/yarn/releases/download") { name = "Yarn"; patternLayout { artifact("v[revision]/[artifact](-v[revision]).[ext]") }; metadataSources { artifact() } } }
            filter { includeModule("com.yarnpkg", "yarn") }
        }
        exclusiveContent {
            forRepository { ivy("https://github.com/WebAssembly/binaryen/releases/download") { name = "Binaryen"; patternLayout { artifact("version_[revision]/[module]-version_[revision]-[classifier].[ext]") }; metadataSources { artifact() } } }
            filter { includeModule("com.github.webassembly", "binaryen") }
        }
    }
}
rootProject.name = "la1"
include(":app")
include(":shared")
