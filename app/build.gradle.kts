import java.io.FileInputStream
import java.util.Properties


plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("kotlin-kapt")
    alias(libs.plugins.hilt.android)
}

android {
    namespace = "org.emunix.unipatcher"
    compileSdk = 36

    signingConfigs {
        create("release") { /** see below **/ }
    }

    buildFeatures {
        viewBinding = true
        buildConfig = true
    }

    defaultConfig {
        applicationId = "org.emunix.unipatcher"
        minSdk = 24
        targetSdk = 36
        ndkVersion = "29.0.14206865"
        versionCode = 170300
        versionName = "0.17.3"

        ndk {
            abiFilters.addAll(setOf("arm64-v8a", "x86_64"))
        }

        externalNativeBuild {
            cmake {
                cppFlags("")
                arguments("-DANDROID_PLATFORM=android-24", "-DCMAKE_BUILD_TYPE=Release")
                version = "3.31.6"
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            signingConfig = signingConfigs.getByName("release")
        }
        debug {
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-DEBUG"
        }
    }

    flavorDimensions.add("default")
    productFlavors {
        create("free") {
            dimension = "default"
            buildConfigField("String", "RATE_URL", "\"https://github.com/btimofeev/UniPatcher\"")
            buildConfigField("String", "SHARE_URL", "\"https://github.com/btimofeev/UniPatcher\"")
            buildConfigField("String", "BITCOIN_ADDRESS", "\"16coztryz7xbNNDNhhf98wuHmi3hEintsW\"")
        }
        create("google") {
            dimension = "default"
            buildConfigField("String", "RATE_URL", "\"market://details?id=org.emunix.unipatcher\"")
            buildConfigField("String", "SHARE_URL", "\"https://play.google.com/store/apps/details?id=org.emunix.unipatcher\"")
        }
    }

    externalNativeBuild {
        cmake {
            path = file("src/main/cpp/CMakeLists.txt")
        }
    }

    lint {
        disable.add("MissingTranslation")
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}

val props = Properties()
val propFile = file("../../signing.properties")
if (propFile.canRead()) {
    props.load(FileInputStream(propFile))

    if (props.containsKey("STORE_FILE") && props.containsKey("STORE_PASSWORD") &&
        props.containsKey("KEY_ALIAS") && props.containsKey("KEY_PASSWORD")) {

        println("RELEASE BUILD SIGNING")

        android.signingConfigs.getByName("release").apply {
            storeFile = file(props["STORE_FILE"] as String)
            storePassword = props["STORE_PASSWORD"] as String
            keyAlias = props["KEY_ALIAS"] as String
            keyPassword = props["KEY_PASSWORD"] as String
        }
    } else {
        println("RELEASE BUILD NOT FOUND SIGNING PROPERTIES")
        android.buildTypes.getByName("release").signingConfig = null
    }
} else {
    println("RELEASE BUILD NOT FOUND SIGNING FILE")
    android.buildTypes.getByName("release").signingConfig = null
}

dependencies {
    // Kotlin
    implementation(libs.kotlin.stdlib)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.coroutines.core)

    // AndroidX
    implementation(libs.appcompat)
    implementation(libs.cardview)
    implementation(libs.constraintlayout)
    implementation(libs.core.ktx)
    implementation(libs.documentfile)
    implementation(libs.fragment.ktx)
    implementation(libs.lifecycle.common)
    implementation(libs.lifecycle.livedata.ktx)
    implementation(libs.lifecycle.viewmodel.ktx)
    implementation(libs.preference.ktx)
    implementation(libs.material)

    // Third-party
    implementation(libs.commons.io)

    // DI
    implementation(libs.hilt.android)
    kapt(libs.hilt.android.compiler)

    // Logs
    implementation(libs.timber)

    // Crash reports
    implementation(libs.acra.mail)
    implementation(libs.acra.notification)

    // Testing
    testImplementation(libs.junit)
    testImplementation(libs.mockito.core)
}

val deleteDependencies by tasks.registering(Delete::class) {
    delete("src/main/cpp/xdelta3/xdelta")
    delete("src/main/cpp/xz/xz")
}

tasks.register("downloadDependencies") {
    dependsOn(deleteDependencies)
    val xDelta = "3.1.0"
    val xz = "5.2.4"
    val downloadDir = layout.buildDirectory.get().asFile.path

    doLast {
        val download = extensions.getByType(de.undercouch.gradle.tasks.download.DownloadAction::class.java)

        download.run {
            src("https://github.com/jmacd/xdelta/archive/v${xDelta}.tar.gz")
            dest(File(downloadDir, "xdelta-${xDelta}.tar.gz"))
        }
        copy {
            from(tarTree(resources.gzip("${downloadDir}/xdelta-${xDelta}.tar.gz")))
            into("src/main/cpp/xdelta3/")
        }
        file("src/main/cpp/xdelta3/xdelta-${xDelta}").renameTo(file("src/main/cpp/xdelta3/xdelta"))
        delete("src/main/cpp/xdelta3/pax_global_header")

        download.run {
            src("https://sourceforge.net/projects/lzmautils/files/xz-${xz}.tar.gz/download")
            dest(File(downloadDir, "xz-${xz}.tar.gz"))
        }
        copy {
            from(tarTree(resources.gzip("${downloadDir}/xz-${xz}.tar.gz")))
            into("src/main/cpp/xz/")
        }
        file("src/main/cpp/xz/xz-${xz}").renameTo(file("src/main/cpp/xz/xz"))
    }
}
