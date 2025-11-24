// ─────────────────────────────────────────────────────────────────────────────
// Imports
// ─────────────────────────────────────────────────────────────────────────────
import java.util.Properties
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

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
  alias(libs.plugins.kotlin.serialization)
  jacoco
}

// ─────────────────────────────────────────────────────────────────────────────
// Kotlin configuration
// ─────────────────────────────────────────────────────────────────────────────
kotlin { compilerOptions { jvmTarget = JvmTarget.fromTarget("17") } }

// ─────────────────────────────────────────────────────────────────────────────
// Versions & constants (from Version Catalog)
// - Single source of truth for JaCoCo engine version.
// ─────────────────────────────────────────────────────────────────────────────
val jacocoVer = libs.versions.jacoco.get()

// Keep the Gradle JaCoCo plugin aligned with the catalog engine version
jacoco { toolVersion = jacocoVer }

// ─────────────────────────────────────────────────────────────────────────────
// Load local properties (for API keys)
// ─────────────────────────────────────────────────────────────────────────────
val localPropertiesFile = rootProject.file("local.properties")
val localProperties = Properties()

if (localPropertiesFile.exists()) {
  localProperties.load(localPropertiesFile.inputStream())
}

val tomtomApiKey: String =
    System.getenv("TOMTOM_API_KEY")
        ?: localProperties.getProperty("TOMTOM_API_KEY")
        ?: throw GradleException("TOMTOM_API_KEY not found in environment or local.properties")

val openaiApiKey: String =
    System.getenv("OPENAI_API_KEY")
        ?: localProperties.getProperty("OPENAI_API_KEY")
        ?: throw GradleException("OPENAI_API_KEY not found in environment or local.properties")

// ─────────────────────────────────────────────────────────────────────────────
// Android configuration
// ─────────────────────────────────────────────────────────────────────────────
android {
  namespace = "com.android.universe"
  compileSdk = 36

  // BuildConfig is required for injecting TOMTOM_API_KEY
  buildFeatures {
    buildConfig = true
    compose = true
  }

  // Expose TOMTOM_API_KEY as BuildConfig.TOMTOM_API_KEY
  // Expose OPENAI_API_KEY as BuildConfig.OPENAI_API_KEY
  buildTypes.configureEach {
    buildConfigField("String", "TOMTOM_API_KEY", "\"$tomtomApiKey\"")
    buildConfigField("String", "OPENAI_API_KEY", "\"$openaiApiKey\"")
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
    multiDexEnabled = true

    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    vectorDrawables { useSupportLibrary = true }

    // TomTom SDK ABIs
    ndk { abiFilters += listOf("arm64-v8a", "x86_64") }
  }

  val hasReleaseKeys = System.getenv("SIGNING_STORE_PASSWORD") != null
  buildTypes {
    release {
      isMinifyEnabled = true
      proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")

      signingConfig =
          if (hasReleaseKeys) signingConfigs.getByName("release")
          else signingConfigs.getByName("debug")
    }

    debug {
      enableUnitTestCoverage = true
      enableAndroidTestCoverage = true
      signingConfig = signingConfigs.getByName("debug")
    }
  }

  // Ensure AGP-managed instrumentation (androidTest) uses the same agent
  testCoverage { jacocoVersion = jacocoVer }

  // Bytecode level for the app; host JDK for tests can be newer
  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
  }

  packaging {
    resources {
      excludes +=
          setOf(
              "/META-INF/{AL2.0,LGPL2.1}",
              "META-INF/LICENSE",
              "META-INF/LICENSE.md",
              "META-INF/LICENSE-notice.md",
              "META-INF/NOTICE",
              "META-INF/NOTICE.txt")
    }
  }

  testFixtures { enable = true }

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
    property("sonar.sourceEnconding", "UTF-8")
    property("sonar.language", "kotlin")

    property(
        "sonar.tests",
        "src/test/java" + "," + "src/androidTest/java" + "," + "src/testFixtures/java")
    // Comma-separated paths to the various directories containing the *.xml JUnit report files.
    // Each path may be absolute or relative to the project base directory.
    property(
        "sonar.junit.reportPaths",
        "${project.layout.buildDirectory.get()}/test-results/testDebugUnitTest/" +
            "," +
            "${project.layout.buildDirectory.get()}/outputs/androidTest-results/connected/debug/")

    // Paths to xml files with Android Lint issues. If the main flavor is changed, this file will
    // have to be changed too.
    property(
        "sonar.androidLint.reportPaths",
        "${project.layout.buildDirectory.get()}/reports/lint-results-debug.xml")
    // Paths to JaCoCo XML coverage report files.
    property(
        "sonar.coverage.jacoco.xmlReportPaths",
        "${project.layout.buildDirectory.get()}/reports/jacoco/jacocoTestReport/jacocoTestReport.xml")
    // Exclusions
    property("sonar.coverage.exclusions", "**/MainActivity.kt")
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
  val composeBom = enforcedPlatform(libs.androidx.compose.bom)
  val firebaseBom = enforcedPlatform(libs.firebase.bom)
  implementation(composeBom)
  globalTestImplementation(composeBom)
  implementation(firebaseBom)

  // --------------------- Core & Runtime ---------------------
  implementation(libs.androidx.multidex)
  implementation(libs.androidx.appcompat)
  implementation(libs.material)

  // --------------------- Auth ---------------------
  implementation(libs.google.credentials)
  implementation(libs.google.id)
  implementation(libs.google.play.auth)

  // ------------------- Firebase -------------------
  // Version is controlled by the firebase-bom
  implementation(libs.firebase.auth)
  implementation(libs.firebase.firestore)

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
  implementation(libs.io.github.backdrop)
  // ------------------- Navigation -------------------
  implementation(libs.androidx.navigation.compose)
  implementation(libs.androidx.navigation.ui)
  implementation(libs.androidx.navigation.fragment)

  // ----------------- TomTom SDK -----------------
  implementation(libs.tomtom.maps) {
    exclude(group = "com.google.protobuf", module = "protobuf-java")
    exclude(group = "com.google.protobuf", module = "protobuf-kotlin")
  }
  implementation(libs.tomtom.location) {
    exclude(group = "com.google.protobuf", module = "protobuf-java")
    exclude(group = "com.google.protobuf", module = "protobuf-kotlin")
  }
  implementation(libs.tomtom.search) {
    exclude(group = "com.google.protobuf", module = "protobuf-java")
    exclude(group = "com.google.protobuf", module = "protobuf-kotlin")
  }
  implementation(libs.tomtom.orbis) {
    exclude(group = "com.google.protobuf", module = "protobuf-java")
    exclude(group = "com.google.protobuf", module = "protobuf-kotlin")
  }

  // ==========================================================================
  // TESTING
  // ==========================================================================

  globalTestImplementation(testFixtures(project(path)))
  testFixturesImplementation(kotlin("stdlib"))
  testFixturesImplementation(firebaseBom)
  testFixturesImplementation(libs.firebase.auth)
  testFixturesImplementation(libs.firebase.firestore)
  testFixturesImplementation(libs.kotlin.coroutines.test)
  testFixturesImplementation(libs.junit4)
  testFixturesImplementation(composeBom)
  testFixturesImplementation(libs.androidx.compose.runtime)
  testFixturesImplementation(libs.androidx.test.core)
  testFixturesImplementation(libs.google.credentials)
  testFixturesImplementation(libs.google.id)
  testFixturesImplementation(libs.mockk.android)
  testFixturesImplementation(libs.mockk.agent)
  testFixturesImplementation(libs.androidx.compose.ui)
  testFixturesImplementation(libs.androidx.compose.ui.test.junit4)
  testFixturesImplementation(libs.io.github.backdrop)

  // ----------------- Unit Testing (test/) -----------------
  testImplementation(libs.junit4)
  globalTestImplementation(libs.androidx.compose.ui.test.junit4)
  testImplementation(libs.kotlin.coroutines.test)
  testImplementation(libs.turbine)
  globalTestImplementation(libs.mockk.android) // Use mockk-android for Android-specific APIs
  globalTestImplementation(libs.mockk.agent)
  globalTestImplementation(libs.io.github.backdrop)
  testImplementation(libs.robolectric)
  // WARNING: logback can only be used in local tests, not instrumented tests.
  testImplementation(libs.logback)

  // ----------------- PlaceHolder -----------------
  // This fixes import issue in the second screen test, imo the test class should be moved to
  // a different package (androidTest). If it should not maybe debugImplementation is the way
  testImplementation(libs.io.github.kakaocup)

  // ----------------- Instrumented Testing (androidTest/) -----------------
  androidTestImplementation(libs.androidx.test.core)
  androidTestImplementation(libs.androidx.test.rules)
  androidTestImplementation(libs.androidx.test.junit)
  androidTestImplementation(libs.androidx.test.espresso.core)
  // Compose UI Tests (versions managed by compose-bom)
  debugImplementation(libs.androidx.compose.ui.test.manifest)

  // ----------------- Kaspresso (UI Automation) -----------------
  androidTestImplementation(libs.kaspresso.compose)

  // TODO! CHECK VERSIONS & ADD TO LIBS.VERSIONS
  // ----------------- NETWORK & JSON SERIALIZATION -–---------------
  // Retrofit + OkHttp stack for all HTTP communication (OpenAI, backend APIs, etc.)
  implementation("com.squareup.retrofit2:retrofit:2.11.0")
  implementation(
      "com.squareup.retrofit2:converter-gson:2.11.0") // Legacy Gson support (kept only for existing
                                                      // endpoints)

  // Primary JSON library in 2025 – type-safe, null-safe, Kotlin-first
  implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")
  implementation(
      "com.jakewharton.retrofit:retrofit2-kotlinx-serialization-converter:1.0.0") // Preferred
                                                                                  // converter for
                                                                                  // new APIs

  // OkHttp – explicit version to guarantee logging-interceptor compatibility
  implementation("com.squareup.okhttp3:okhttp:4.12.0")
  implementation(
      "com.squareup.okhttp3:logging-interceptor:4.12.0") // Debug logging (automatically disabled in
                                                         // release via ProGuard/R8)
}

// ─────────────────────────────────────────────────────────────────────────────
// JaCoCo agent tuning for ALL test tasks
// - include no-location classes
// - exclude JDK internals to reduce noise
// ─────────────────────────────────────────────────────────────────────────────
tasks.withType<Test>().configureEach {
  extensions.configure(JacocoTaskExtension::class.java) {
    isIncludeNoLocationClasses = true
    excludes =
        listOf(
            "jdk.internal.*",
            "jdk.proxy*",
            "java.*",
            "javax.*",
            "**/MainActivity*.*",
        )
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

configurations.forEach { configuration ->
  // Exclude protobuf-lite from all configurations
  // This fixes a fatal exception for tests interacting with Cloud Firestore
  configuration.exclude("com.google.protobuf", "protobuf-lite")
}

// ─────────────────────────────────────────────────────────────────────────────
// Coverage report task (unit + connected)
// - Produces XML for Sonar + HTML for humans
// - Must run after test tasks
// ─────────────────────────────────────────────────────────────────────────────
tasks.register("jacocoTestReport", JacocoReport::class) {
  mustRunAfter("testDebugUnitTest", "connectedDebugAndroidTest")

  reports {
    xml.required.set(true) // for Sonar
    html.required.set(true) // for humans
  }

  val fileFilter =
      listOf(
          "**/R.class",
          "**/R$*.class",
          "**/BuildConfig.*",
          "**/Manifest*.*",
          "**/*Test*.*",
          "android/**/*.*",
      )

  val debugTree =
      fileTree("${project.layout.buildDirectory.get()}/tmp/kotlin-classes/debug") {
        exclude(fileFilter)
      }

  val mainSrc =
      files(
          "${project.layout.projectDirectory}/src/main/java",
          "${project.layout.projectDirectory}/src/main/kotlin",
      )

  sourceDirectories.setFrom(mainSrc)
  classDirectories.setFrom(files(debugTree))
  executionData.setFrom(
      fileTree(project.layout.buildDirectory.get()) {
        include(
            "outputs/unit_test_code_coverage/debugUnitTest/testDebugUnitTest.exec",
            "outputs/code_coverage/debugAndroidTest/connected/*/coverage.ec")
      })
}

tasks.named("sonar") {
  mustRunAfter("lintDebug", "testDebugUnitTest", "connectedDebugAndroidTest", "jacocoTestReport")
}
