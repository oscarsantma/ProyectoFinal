plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}
android {
    namespace = "com.example.resiplus"
    compileSdk = 35
    defaultConfig {
        applicationId = "com.example.resiplus"
        minSdk = 26; targetSdk = 35
        versionCode = 1; versionName = "1.0"
        // 10.0.2.2 = localhost del PC cuando usas el emulador de Android Studio
        // Si usas movil fisico: cambia por la IP local de tu PC (ej: 192.168.1.X)
        buildConfigField("String", "API_BASE_URL", "\"http://10.0.2.2/resiplus/api/index.php\"")
    }
    buildTypes { release { isMinifyEnabled = false } }
    buildFeatures { buildConfig = true }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }
}
dependencies {
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.recyclerview:recyclerview:1.3.2")
}
