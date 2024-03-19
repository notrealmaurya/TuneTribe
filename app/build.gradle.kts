plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.jetbrainsKotlinAndroid)
    id("kotlin-kapt")
    id("dagger.hilt.android.plugin")
    id("com.google.devtools.ksp")
}

android {
    namespace = "com.maurya.dtxloopplayer"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.maurya.dtxloopplayer"
        minSdk = 26
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
        viewBinding = true;
    }
}

dependencies {


    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)


    //hilt injection
    implementation(libs.hilt.android)
    kapt(libs.hilt.android.compiler)
    annotationProcessor(libs.dagger.compiler)


    //lifecycle ViewModel LiveData
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.livedata.ktx)

    //coroutines
    implementation(libs.kotlinx.coroutines.android)

    // Navigation Component
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)

    //lottie
    implementation(libs.lottie)

    //glide
    implementation(libs.glide)


    //notification
    implementation(libs.androidx.media)

    //gson
    implementation(libs.gson)

    //drawer
    implementation(libs.minavdrawer)


//    //room
//    implementation(libs.androidx.room.runtime)
//    annotationProcessor(libs.androidx.room.compiler)
//    // To use Kotlin Symbol Processing (KSP)
//    ksp(libs.androidx.room.room.compiler)
//    // optional - Kotlin Extensions and Coroutines support for Room
//    implementation(libs.androidx.room.ktx)


}