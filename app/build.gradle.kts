// ─────────────────────────────────────────────────────────────────────────────
// Imports
// ─────────────────────────────────────────────────────────────────────────────
import org.gradle.kotlin.dsl.androidTestImplementation
import org.gradle.kotlin.dsl.testImplementation
import org.gradle.testing.jacoco.plugins.JacocoTaskExtension
import org.gradle.testing.jacoco.tasks.JacocoReport
import org.gradle.api.tasks.testing.Test
import java.io.File

// ─────────────────────────────────────────────────────────────────────────────
// Plugins
// - JaCoCo is a core Gradle plugin: apply with id("jacoco") (no version).
// ─────────────────────────────────────────────────────────────────────────────
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ktfmt)
    alias(libs.plugins.sonarqube)
    alias(libs.plugins.google.services)
    jacoco
}

// ─────────────────────────────────────────────────────────────────────────────
// Versions & constants (from Version Catalog)
// - Single source of truth for JaCoCo engine version.
// ─────────────────────────────────────────────────────────────────────────────
val jacocoVer = libs.versions.jacoco.get()
val tomtomApiKey: String by project

// Keep the Gradle JaCoCo plugin aligned with the catalog engine version
jacoco {
    toolVersion = jacocoVer
}

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
        }

        debug {
            // Turn coverage on in debug for both unit and connected tests
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
// - Aliases are defined in /gradle/libs.versions.toml
// ─────────────────────────────────────────────────────────────────────────────
dependencies {
    // Import the Bill of Materials (BOMs) to manage library versions.
    // This removes the need to specify versions for individual Compose and Firebase libraries.
    implementation(platform(libs.androidx.compose.bom))
    implementation(platform(libs.firebase.bom))
    androidTestImplementation(platform(libs.androidx.compose.bom)) // Make BOM available for Android tests

    // --------------------- Core & Runtime ---------------------
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.lifecycle.runtime.ktx)

    // --------------------- Auth ---------------------
    implementation(libs.google.credentials)
    implementation(libs.google.id)

    // ------------------- Firebase -------------------
    // Version is controlled by the firebase-bom
    implementation(libs.firebase.auth)

    // ----------------- Jetpack Compose ------------------
    // Versions are controlled by the androidx-compose-bom
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    // Android Studio Preview support
    debugImplementation(libs.androidx.compose.ui.tooling)
    implementation(libs.androidx.compose.ui.tooling.preview)

    // ------------------- Navigation -------------------
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)

    // ----------------- TomTom SDK -----------------
    implementation(libs.tomtom.maps)
    implementation(libs.tomtom.location)
    implementation(libs.tomtom.search)

    // ==========================================================================
    // TESTING
    // ==========================================================================

    // ----------------- Unit Testing (test/) -----------------
    globalTestImplementation(libs.kotlin.test)
    testImplementation(libs.androidx.test.core)
    testImplementation(libs.junit4)
    testImplementation(libs.kotlin.test.junit)
    testImplementation(libs.kotlin.coroutines.test)
    testImplementation(libs.turbine)
    testImplementation(libs.mockk.android) // Use mockk-android for Android-specific APIs
    testImplementation(libs.mockk.agent)
    testImplementation(libs.robolectric)
    // WARNING: logback can only be used in local tests, not instrumented tests.
    testImplementation(libs.logback)

    // ----------------- PlaceHolder -----------------
    // This fixes import issue in the second screen test, imo the test class should be moved to
    // a different package (androidTest). If it should not maybe debugImplementation is the way
    testImplementation(libs.androidx.compose.ui.test)
    testImplementation(libs.io.github.kakaocup)

    // ----------------- Instrumented Testing (androidTest/) -----------------
    androidTestImplementation(libs.androidx.test.junit)
    androidTestImplementation(libs.androidx.test.espresso.core)
    androidTestImplementation(libs.androidx.test.core)
    // Compose UI Tests (versions managed by compose-bom)
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    // ----------------- Kaspresso (UI Automation) -----------------
    androidTestImplementation(libs.kaspresso)
    androidTestImplementation(libs.kaspresso.compose)
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