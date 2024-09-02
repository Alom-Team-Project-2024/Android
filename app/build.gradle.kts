plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.example.alom_team_project"
    compileSdk = 34

    buildFeatures{
        viewBinding=true
        dataBinding=true
    }

    defaultConfig {
        applicationId = "com.example.alom_team_project"
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
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.4.3"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
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
    //리사이클러뷰
    implementation("androidx.recyclerview:recyclerview:1.2.1")
    //이미지 불러오기
    implementation ("com.github.bumptech.glide:glide:4.15.1")
    implementation("androidx.cardview:cardview:1.0.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.fragment:fragment:1.8.2")
    annotationProcessor ("com.github.bumptech.glide:compiler:4.15.1")
    //constraint 추가
    implementation ("androidx.constraintlayout:constraintlayout:2.1.0")
    //okhttp
    implementation ("com.squareup.okhttp3:okhttp:4.10.0")
    implementation ("com.github.bumptech.glide:okhttp3-integration:4.14.2")

    //retrofit2||api
    implementation ("com.google.code.gson:gson:2.10.1")
    implementation ("com.squareup.retrofit2:retrofit:2.9.0")
    implementation ("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation ("com.squareup.okhttp3:okhttp:4.10.0")
    implementation ("com.squareup.okhttp3:logging-interceptor:4.10.0")
    implementation ("com.github.bumptech.glide:glide:4.16.0")
    // AppCompat 라이브러리 추가
    implementation("androidx.appcompat:appcompat:1.4.0")
    //카드뷰 추가
    implementation("androidx.compose.material3:material3")
    //둥근이미지뷰 나타내기
    implementation ("de.hdodenhof:circleimageview:3.1.0")
    // Websocket
    implementation("org.java-websocket:Java-WebSocket:1.5.2")
    // Jackson
    implementation("com.fasterxml.jackson.core:jackson-databind:2.13.0")
    // RxJava
    implementation("io.reactivex.rxjava2:rxjava:2.2.5")
    implementation("io.reactivex.rxjava2:rxandroid:2.1.0")
    //SVG
    implementation("com.caverock:androidsvg-aar:1.4")

    // Spring
    implementation("org.springframework:spring-messaging:5.3.9")
    implementation("org.springframework:spring-websocket:5.3.9")
    implementation("org.hildan.krossbow:krossbow-stomp-core:7.1.0")
    implementation("org.hildan.krossbow:krossbow-websocket-okhttp:7.1.0")
    implementation("org.hildan.krossbow:krossbow-stomp-moshi:7.1.0")
    implementation("com.squareup.moshi:moshi-kotlin:1.14.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.2")
    //
    implementation("org.jetbrains.kotlin:kotlin-stdlib:1.8.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.0")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.8.0")
    implementation("androidx.appcompat:appcompat:1.6.1")


    implementation("androidx.core:core-ktx:1.9.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
    implementation("androidx.activity:activity-compose:1.8.2")
    implementation(platform("androidx.compose:compose-bom:2023.03.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation(platform("androidx.compose:compose-bom:2023.03.00"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")}