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
    aaptOptions {
        noCompress += listOf("tflite", "lite") // Asegúrate de incluir las extensiones necesarias
    }
    buildFeatures {
        dataBinding = true
        viewBinding = true  // Añade esta línea

    }

}

dependencies {
    // Seguridad
    implementation(libs.jbcrypt) // Encriptación de contraseñas

    // UI Android
    implementation(libs.appcompat) // Compatibilidad
    implementation(libs.material) // Diseño Material
    implementation(libs.constraintlayout) // Layouts
    implementation(libs.exifinterface)

    // ML Kit
    implementation(libs.mlkit.image.labeling)
    implementation(libs.mlkit.image.labeling.custom)
    implementation(libs.mlkit.objec.detection)
    implementation(libs.mlkit.face.detection)

    // TensorFlow Lite
    implementation(libs.tensorflow.lite)
    implementation(libs.tensorflow.lite.gpu)
    implementation(libs.tensorflow.lite.support)
    implementation(libs.tensorflow.lite.task.audio)
    implementation(libs.tensorflow.lite.task.text)

    // CameraX
    implementation(libs.camera.core)
    implementation(libs.camera.view)
    implementation(libs.camera.camera2)
    implementation(libs.camera.lifecycle)

    // Utilidades
    implementation(libs.gson)
    implementation(libs.guava.android)

    // Firebase
    implementation(libs.firebase.analytics) // Métricas
    implementation(libs.firebase.authentication) // Login
    implementation(platform(libs.firebase.bom)) // Gestión de versiones
    implementation(libs.firebase.database)
    implementation(libs.play.services.mlkit.face.detection)
    implementation(libs.tensorflow.lite.metadata)
    implementation(libs.firebase.firestore) // Base de datos



    // Testing
    testImplementation(libs.junit) // Pruebas unitarias
    androidTestImplementation(libs.ext.junit) // Pruebas Android
    androidTestImplementation(libs.espresso.core) // Pruebas UI
    implementation (libs.retrofit)
    implementation (libs.retrofit.converter.gson)
    implementation (libs.okhttp.logging.interceptor)


}
