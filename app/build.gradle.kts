plugins {
    alias(libs.plugins.android.application)
    id("com.google.gms.google-services")
}

android {
    namespace = "com.master.verificamtc"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.master.verificamtc"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            vectorDrawables.useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    // Seguridad
    implementation(libs.jbcrypt) // Encriptación de contraseñas

    // UI Android
    implementation(libs.appcompat) // Compatibilidad
    implementation(libs.material) // Diseño Material
    implementation(libs.constraintlayout) // Layouts

    // Firebase
    implementation(libs.firebase.analytics) // Métricas
    implementation(libs.firebase.authentication) // Login
    implementation(platform(libs.firebase.bom)) // Gestión de versiones
    implementation(libs.firebase.database) // Base de datos

    // Reconocimiento Facial
    implementation(libs.firebase.ml.vision) // Procesamiento de imágenes
    implementation(libs.firebase.ml.face.model) // Modelo facial

    // Cámara
    implementation(libs.camera.camera2) // Núcleo cámara
    implementation(libs.camera.lifecycle) // Ciclo de vida
    implementation(libs.camera.view) // Vista previa

    // Testing
    testImplementation(libs.junit) // Pruebas unitarias
    androidTestImplementation(libs.ext.junit) // Pruebas Android
    androidTestImplementation(libs.espresso.core) // Pruebas UI
}