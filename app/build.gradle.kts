// ─────────────────────────────────────────────────────────────────────────────
// Imports
// ─────────────────────────────────────────────────────────────────────────────
import org.gradle.kotlin.dsl.androidTestImplementation
import org.gradle.kotlin.dsl.testImplementation
import org.gradle.testing.jacoco.plugins.JacocoTaskExtension
import org.gradle.testing.jacoco.tasks.JacocoReport
import org.gradle.api.tasks.testing.Test
import java.io.File
import java.util.Properties

// ─────────────────────────────────────────────────────────────────────────────
// Plugins
// - JaCoCo is a core Gradle plugin: apply with id("jacoco") (no version).
// ─────────────────────────────────────────────────────────────────────────────
plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.jetbrainsKotlinAndroid)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.ktfmt)
    alias(libs.plugins.sonar)
    alias(libs.plugins.gms)
    id("jacoco")
}

// ─────────────────────────────────────────────────────────────────────────────
// Versions & constants (from Version Catalog)
// - Single source of truth for JaCoCo engine version.
// ─────────────────────────────────────────────────────────────────────────────
val jacocoVer = libs.versions.jacoco.get()
// Keep the Gradle JaCoCo plugin aligned with the catalog engine version
jacoco {
    toolVersion = jacocoVer
}

// ─────────────────────────────────────────────────────────────────────────────
// Load local properties (for TomTom API key)
// ─────────────────────────────────────────────────────────────────────────────
val localPropertiesFile = rootProject.file("local.properties")
val localProperties = Properties()

if (localPropertiesFile.exists()) {
    localProperties.load(localPropertiesFile.inputStream())
}

val tomtomApiKey: String = System.getenv("TOMTOM_API_KEY")
    ?: localProperties.getProperty("TOMTOM_API_KEY")
    ?: throw GradleException("TOMTOM_API_KEY not found in environment or local.properties")

// ─────────────────────────────────────────────────────────────────────────────
// Android configuration
// ─────────────────────────────────────────────────────────────────────────────
android {
    namespace = "com.android.universe"
    compileSdk = 34

    // BuildConfig is required for injecting TOMTOM_API_KEY
    buildFeatures {
        buildConfig = true
        compose = true
    }

    // Expose TOMTOM_API_KEY as BuildConfig.TOMTOM_API_KEY
    buildTypes.configureEach {
        buildConfigField("String", "TOMTOM_API_KEY", "\"$tomtomApiKey\"")
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Release signing configuration (for CI / manual release builds)
    // Reads from environment variables defined in GitHub Actions or locally
    // ─────────────────────────────────────────────────────────────────────────
    signingConfigs {
        create("release") {
            val keystorePath = System.getenv("SIGNING_KEYSTORE_FILE") ?: "release-keystore.jks"
            val file = file(keystorePath)
            if (file.exists()) {
                storeFile = file
                storePassword = System.getenv("SIGNING_STORE_PASSWORD")
                keyAlias = System.getenv("SIGNING_KEY_ALIAS")
                keyPassword = System.getenv("SIGNING_KEY_PASSWORD")
            } else {
                println("⚠️ Warning: release-keystore.jks not found, skipping signing setup for release.")
            }
        }
    }

    defaultConfig {
        applicationId = "com.android.universe"
        minSdk = 34
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables { useSupportLibrary = true }

        // TomTom SDK ABIs
        ndk { abiFilters += listOf("arm64-v8a", "x86_64") }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )

            signingConfig = signingConfigs.getByName("release")
        }

        debug {
            enableUnitTestCoverage = true
            enableAndroidTestCoverage = true
        }
    }

    // Ensure AGP-managed instrumentation (androidTest) uses the same agent
    testCoverage {
        jacocoVersion = jacocoVer
    }

    composeOptions { kotlinCompilerExtensionVersion = "1.4.2" }

    // Bytecode level for the app; host JDK for tests can be newer
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }

    packaging {
        resources { excludes += "/META-INF/{AL2.0,LGPL2.1}" }
    }

    testOptions {
        unitTests {
            // Robolectric
            isIncludeAndroidResources = true
            isReturnDefaultValues = true
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Source sets tweak for Robolectric in debug
    // - Move shared tests into testDebug to avoid duplication issues.
    // ─────────────────────────────────────────────────────────────────────────
    sourceSets.getByName("testDebug") {
        val test = sourceSets.getByName("test")
        java.setSrcDirs(test.java.srcDirs)
        res.setSrcDirs(test.res.srcDirs)
        resources.setSrcDirs(test.resources.srcDirs)
    }
    sourceSets.getByName("test") {
        java.setSrcDirs(emptyList<File>())
        res.setSrcDirs(emptyList<File>())
        resources.setSrcDirs(emptyList<File>())
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// SonarCloud configuration
// - Note the corrected path casing: testDebugUnitTest
// ─────────────────────────────────────────────────────────────────────────────
sonar {
    properties {
        property("sonar.projectKey", "SwEnt-Universe_UniVERSE")
        property("sonar.projectName", "UniVERSE")
        property("sonar.organization", "swent-universe")
        property("sonar.host.url", "https://sonarcloud.io")
        // Comma-separated paths to the various directories containing the *.xml JUnit report files. Each path may be absolute or relative to the project base directory.
        property("sonar.junit.reportPaths", "${project.layout.buildDirectory.get()}/test-results/testDebugunitTest/")
        // Paths to xml files with Android Lint issues. If the main flavor is changed, this file will have to be changed too.
        property("sonar.androidLint.reportPaths", "${project.layout.buildDirectory.get()}/reports/lint-results-debug.xml")
        // Paths to JaCoCo XML coverage report files.
        property("sonar.coverage.jacoco.xmlReportPaths", "${project.layout.buildDirectory.get()}/reports/jacoco/jacocoTestReport/jacocoTestReport.xml")
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Dependency sugar: add to both unit & androidTest
// ─────────────────────────────────────────────────────────────────────────────
fun DependencyHandlerScope.globalTestImplementation(dep: Any) {
    androidTestImplementation(dep)
    testImplementation(dep)
}

// ─────────────────────────────────────────────────────────────────────────────
// Dependencies
// - use /gradle/wrapper/libs.versions.tomtom when available
// ─────────────────────────────────────────────────────────────────────────────
dependencies {
    // Runtime
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    globalTestImplementation(libs.androidx.junit)

    // --------------------- Auth ---------------------
    implementation(libs.credentials)
    implementation(libs.googleid)

    // ------------------- Firebase -------------------
    val firebaseBom = platform(libs.firebase.bom)
    implementation(firebaseBom)
    globalTestImplementation(firebaseBom)

    implementation(libs.firebase.auth)

    // ------------- Jetpack Compose ------------------
    val composeBom = platform(libs.compose.bom)
    implementation(composeBom)
    globalTestImplementation(composeBom)

    implementation(libs.compose.ui)
    implementation(libs.compose.ui.graphics)
    // Material Design 3
    implementation(libs.compose.material3)
    implementation(libs.androidx.material.icons.extended)
    // Integration with activities
    implementation(libs.compose.activity)
    // Integration with ViewModels
    implementation(libs.compose.viewmodel)
    // Android Studio Preview support
    implementation(libs.compose.preview)
    debugImplementation(libs.compose.tooling)
    // Navigation
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    // UI Tests
    globalTestImplementation(libs.compose.test.junit)
    debugImplementation(libs.compose.test.manifest)

    // Testing
    testImplementation(libs.logback) // logback for logging mockK
    testImplementation(libs.junit)
    globalTestImplementation(libs.androidx.junit)
    globalTestImplementation(libs.androidx.espresso.core)
    // Mockito for JVM unit tests (needed to mock FirebaseAuth.getInstance())
    testImplementation("org.mockito:mockito-core:5.12.0")
    testImplementation("org.mockito:mockito-inline:5.2.0")   // enables mockStatic(...)
    testImplementation("org.mockito.kotlin:mockito-kotlin:5.2.1")

    // Coroutines test (pick ONE version; 1.8.1 is current)
    testImplementation(libs.jetbrains.kotlinx.coroutines.test)
    // Turbine for Flow testing
    testImplementation(libs.turbine)
    // MockK
    testImplementation(libs.mockk)
    testImplementation(libs.mockk.agent)
    testImplementation(libs.mockk.android)
    // Kotlin test bridge
    testImplementation(libs.kotlin.test.junit)
    androidTestImplementation(libs.jetbrains.kotlin.test.junit)

    // AndroidX test core (explicit if needed)
    androidTestImplementation(libs.androidx.core)

    // Kaspresso
    globalTestImplementation(libs.kaspresso)
    globalTestImplementation(libs.kaspresso.compose)

    // Robolectric (from catalog)
    testImplementation(libs.robolectric)

    // TomTom SDK
    implementation(libs.tomtomMap)
    implementation(libs.tomtomLocation)
    implementation(libs.tomtomSearch)
}

// ─────────────────────────────────────────────────────────────────────────────
// JaCoCo agent tuning for ALL test tasks
// - include no-location classes
// - exclude JDK internals to reduce noise
// ─────────────────────────────────────────────────────────────────────────────
tasks.withType<Test>().configureEach {
    extensions.configure(JacocoTaskExtension::class.java) {
        isIncludeNoLocationClasses = true
        excludes = listOf("jdk.internal.*", "jdk.proxy*", "java.*", "javax.*")
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Pin all org.jacoco artifacts to the same engine version (catalog-driven)
// ─────────────────────────────────────────────────────────────────────────────
configurations.configureEach {
    resolutionStrategy.eachDependency {
        if (requested.group == "org.jacoco") {
            useVersion(jacocoVer)
        }
    }
    resolutionStrategy.force(
        "org.jacoco:org.jacoco.agent:$jacocoVer",
        "org.jacoco:org.jacoco.ant:$jacocoVer",
        "org.jacoco:org.jacoco.core:$jacocoVer",
        "org.jacoco:org.jacoco.report:$jacocoVer",
    )
}

// ─────────────────────────────────────────────────────────────────────────────
// Coverage report task (unit + connected)
// - Produces XML for Sonar + HTML for humans
// - Must run after test tasks
// ─────────────────────────────────────────────────────────────────────────────
tasks.register("jacocoTestReport", JacocoReport::class) {
    mustRunAfter("testDebugUnitTest", "connectedDebugAndroidTest")

    reports {
        xml.required.set(true)   // for Sonar
        html.required.set(true)  // for humans
    }

    val fileFilter = listOf(
        "**/R.class", "**/R$*.class",
        "**/BuildConfig.*",
        "**/Manifest*.*",
        "**/*Test*.*",
        "android/**/*.*",
    )

    val debugTree = fileTree("${project.layout.buildDirectory.get()}/tmp/kotlin-classes/debug") {
        exclude(fileFilter)
    }

    val mainSrc = files(
        "${project.layout.projectDirectory}/src/main/java",
        "${project.layout.projectDirectory}/src/main/kotlin",
    )

    sourceDirectories.setFrom(mainSrc)
    classDirectories.setFrom(files(debugTree))
    executionData.setFrom(
        fileTree(project.layout.buildDirectory.get()) {
            include(
                "outputs/unit_test_code_coverage/debugUnitTest/testDebugUnitTest.exec",
                "outputs/code_coverage/debugAndroidTest/connected/*/coverage.ec"
            )
        }
    )
}