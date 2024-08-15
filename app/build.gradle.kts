plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")

    //kapt
    id ("kotlin-kapt")
}

android {
    namespace = "com.example.setong_alom"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.setong_alom"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        viewBinding = true
    }
}

dependencies {

//    implementation("androidx.core:core-ktx:1.9.0")
//    implementation("androidx.appcompat:appcompat:1.6.1")
//    implementation("com.google.android.material:material:1.11.0")
//    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
//    implementation("org.chromium.net:cronet-embedded:119.6045.31")
//    testImplementation("junit:junit:4.13.2")
//    androidTestImplementation("androidx.test.ext:junit:1.1.5")
//    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
//
//    // Gson 라이브러리
//    implementation("com.google.code.gson:gson:2.9.0")
//
//    // Retrofit
//    implementation ("com.squareup.retrofit2:retrofit:2.9.0")
//    implementation ("com.squareup.retrofit2:converter-gson:2.9.0")
//
//    //okhttp
//    implementation ("com.squareup.okhttp3:okhttp:4.9.3")
//
//    //glide
//    implementation ("com.github.bumptech.glide:glide:4.12.0")
//    kapt ("com.github.bumptech.glide:compiler:4.12.0")

    // Core KTX
    implementation("androidx.core:core-ktx:1.9.0")

    // AppCompat
    implementation("androidx.appcompat:appcompat:1.6.1")

    // Material Components
    implementation("com.google.android.material:material:1.12.0")

    // ConstraintLayout
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")

    // Cronet (Chromium Network Stack)
    implementation("org.chromium.net:cronet-embedded:119.6045.31")

    // Testing dependencies
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

    // Gson
    implementation("com.google.code.gson:gson:2.10.1")

    // Retrofit
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")

    // OkHttp
    implementation("com.squareup.okhttp3:okhttp:4.10.0")

    // Glide
    implementation("com.github.bumptech.glide:glide:4.16.0")
    kapt("com.github.bumptech.glide:compiler:4.16.0")



}