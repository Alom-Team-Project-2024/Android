plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
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

    packaging {
        resources.pickFirsts.add("META-INF/spring.schemas")
        resources.pickFirsts.add("META-INF/spring.handlers")
        resources.pickFirsts.add("META-INF/spring.factories")
        resources.pickFirsts.add("META-INF/spring.tooling")
        resources.pickFirsts.add("META-INF/license.txt")
        resources.pickFirsts.add("META-INF/notice.txt")
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.9.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

    implementation ("com.github.bumptech.glide:glide:4.12.0")
    implementation("androidx.cardview:cardview:1.0.0")
    implementation ("com.github.bumptech.glide:glide:4.16.0")

    // OkHttp
    implementation("com.squareup.okhttp3:okhttp:4.2.1")
    implementation("com.google.code.gson:gson:2.9.0")

    // Retrofit
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")

    // WebSocket
    implementation("org.java-websocket:Java-WebSocket:1.5.2")

    // Jackson
    implementation("com.fasterxml.jackson.core:jackson-databind:2.13.0")

    // RxJava
    implementation("io.reactivex.rxjava2:rxjava:2.2.5")
    implementation("io.reactivex.rxjava2:rxandroid:2.1.0")

    // Spring
    implementation("org.springframework:spring-messaging:5.3.9")
    implementation("org.springframework:spring-websocket:5.3.9")

    implementation("org.hildan.krossbow:krossbow-stomp-core:7.2.0")
    implementation("org.hildan.krossbow:krossbow-websocket-okhttp:7.2.0")
    implementation("org.hildan.krossbow:krossbow-stomp-moshi:7.2.0")
    implementation("com.squareup.moshi:moshi-kotlin:1.14.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.2")

    //
    implementation("org.jetbrains.kotlin:kotlin-stdlib:1.8.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.0")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.8.0")

}
