plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.google.services)

}

android {
    namespace = "com.disha.myapplication"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId = "com.disha.myapplication"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            optimization {
                enable = false
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation("com.google.firebase:firebase-messaging:23.4.1")
    implementation(platform("com.google.firebase:firebase-bom:34.18.0"))
    implementation("com.google.firebase:firebase-analytics")
    implementation("androidx.security:security-crypto:1.1.0-alpha06")
    implementation("com.google.mlkit:text-recognition:16.0.0")
    implementation("androidx.security:security-crypto:1.0.0")
    implementation("com.journeyapps:zxing-android-embedded:4.3.0")
    implementation("com.google.zxing:core:3.5.2")
    implementation(libs.firebase.firestore)
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth)
    implementation(libs.activity.ktx)
    implementation(libs.appcompat)
    implementation(libs.constraintlayout)
    implementation(libs.google.firebase.database)
    implementation(libs.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(libs.ext.junit)
}