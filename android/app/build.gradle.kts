plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.readyrecipe.android"
    compileSdk = 36

    val baseUrl = (project.findProperty("READYRECIPE_BASE_URL") as String?)
        ?.trim()
        ?.let { if (it.endsWith("/")) it else "$it/" }
        ?: "http://10.0.2.2:8080/"

    defaultConfig {
        multiDexEnabled = true
        applicationId = "com.readyrecipe.android"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
        buildConfigField("String", "BASE_URL", "\"$baseUrl\"")

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        debug {
            ndk {
                abiFilters += "x86"
                abiFilters += "x86_64"
                abiFilters += "arm64-v8a"
                abiFilters += "armeabi-v7a"
            }
        }
        release {
            isMinifyEnabled = false
            ndk {
                abiFilters += "arm64-v8a"
                abiFilters += "armeabi-v7a"
            }
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        buildConfig = true
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.composeCompiler.get()
    }
    kotlin {
        jvmToolchain(17)
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
        }
    }

    packaging {
        jniLibs {
            useLegacyPackaging = false
        }
    }
}

dependencies {
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.multidex)

    // Compose UI
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.tooling.preview)
    debugImplementation(libs.androidx.compose.ui.tooling)

    // Networking (Backend API)
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.gson)
    implementation(libs.okhttp.logging.interceptor)
    implementation(libs.gson)

    implementation(libs.androidx.recyclerview)
    implementation(libs.androidx.swiperefreshlayout)
    implementation(libs.androidx.cardview)
    implementation("androidx.navigation:navigation-fragment-ktx:2.9.7")
    implementation("androidx.navigation:navigation-ui-ktx:2.9.7")

    // TensorFlow Lite (Object Detection) - Updated for 16KB page size support
    implementation(libs.tensorflow.lite)
    implementation(libs.tensorflow.lite.support)

    // CameraX (Camera preview & capture)
    implementation(libs.androidx.camera.core)
    implementation(libs.androidx.camera.camera2)
    implementation(libs.androidx.camera.lifecycle)
    implementation(libs.androidx.camera.view)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
