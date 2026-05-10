plugins {
    id("com.android.application")
}

android {
    namespace = "com.example.locationtracker"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.locationtracker"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.10.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")

     implementation("com.android.volley:volley:1.2.1")

    implementation("org.osmdroid:osmdroid-android:6.1.17")
}