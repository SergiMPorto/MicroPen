buildscript {
    repositories {
        mavenCentral()
    }
    dependencies {
        classpath("com.google.gms:google-services:4.4.1")
    }
}

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.gms.google-services")
}

android {
    namespace = "com.sergi.micropen"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.sergi.micropen"
        minSdk = 28
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
        dataBinding
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.13.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("org.jetbrains:annotations:15.0")
    implementation("com.google.android.gms:play-services-mlkit-text-recognition-common:19.0.0")

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

    // Firebase
    implementation(platform("com.google.firebase:firebase-bom:28.3.1"))
    implementation("com.google.firebase:firebase-firestore-ktx")
    implementation("com.google.firebase:firebase-auth-ktx")
    implementation("com.google.android.gms:play-services-auth:21.1.0")

    //Drawelayout
    implementation("de.hdodenhof:circleimageview:3.1.0")
    implementation("androidx.drawerlayout:drawerlayout:1.2.0")
    implementation("androidx.coordinatorlayout:coordinatorlayout:1.2.0")

    //GoogleTranslate
    dependencies {
        implementation ("com.google.mlkit:translate:16.1.2")
      // Asegúrate de usar la versión correcta
        implementation ("androidx.core:core-ktx:1.9.0")
        implementation ("org.jetbrains.kotlin:kotlin-stdlib:1.9.0")
    }

    //Escanear QR
    dependencies {

        implementation ("com.google.mlkit:text-recognition:16.0.0")
        implementation ("com.google.mlkit:common:17.2.0")
    }

    // Usa una versión anterior de play-services-vision
    implementation ("com.google.android.gms:play-services-vision:20.1.3")
    implementation ("com.google.firebase:firebase-ml-vision:24.0.3")
    implementation ("org.jetbrains.kotlin:kotlin-stdlib:1.9.0")

    //Pasar texto escrito en dedo
     implementation("com.google.mlkit:digital-ink-recognition:18.1.0")


    //Facebook
    implementation ("com.facebook.android:facebook-login:latest.release")
    implementation ("com.facebook.android:facebook-android-sdk:[4,5)")

    //Coroutines
    implementation ("androidx.lifecycle:lifecycle-runtime-ktx:2.6.2")


    //Location
    implementation ("com.google.android.gms:play-services-location:21.0.1")

    dependencies {

        implementation("com.google.ai.client.generativeai:generativeai:0.6.0")
    }



}
