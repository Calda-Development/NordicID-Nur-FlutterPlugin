plugins {
    id("com.android.application")
    id("kotlin-android")
    // The Flutter Gradle Plugin must be applied after the Android and Kotlin Gradle plugins.
    id("dev.flutter.flutter-gradle-plugin")
}

android {
    namespace = "com.thecalda.nordicidnurplugin_example"
    compileSdk = flutter.compileSdkVersion
    ndkVersion = flutter.ndkVersion

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_11.toString()
    }

    defaultConfig {
        // TODO: Specify your own unique Application ID (https://developer.android.com/studio/build/application-id.html).
        applicationId = "com.thecalda.nordicidnurplugin_example"
        // You can update the following values to match your application needs.
        // For more information, see: https://flutter.dev/to/review-gradle-config.
        minSdk = flutter.minSdkVersion
        targetSdk = flutter.targetSdkVersion
        versionCode = flutter.versionCode
        versionName = flutter.versionName
    }

    buildTypes {
        release {
            // TODO: Add your own signing config for the release build.
            // Signing with the debug keys for now, so `flutter run --release` works.
            signingConfig = signingConfigs.getByName("debug")
        }
    }

    dependencies {
        implementation(files("libs/NurApiAndroid/NurApiAndroid.aar"))
        implementation(files("libs/NurDeviceUpdate/NurDeviceUpdate.aar"))
        implementation(files("libs/NurSmartPair/NurSmartPair.aar"))
        implementation("no.nordicsemi.android.support.v18:scanner:1.4.0")
    }

//    dependencies {
//        implementation files('libs/NurApiAndroid/NurApiAndroid.aar')
//        implementation files('libs/NurDeviceUpdate/NurDeviceUpdate.aar')
//        implementation files('libs/NurSmartPair/NurSmartPair.aar')
//        implementation 'no.nordicsemi.android.support.v18:scanner:1.4.0'
//        implementation 'no.nordicsemi.android:dfu:1.9.0'
//        implementation 'androidx.multidex:multidex:2.0.1'
//
//    }
}

flutter {
    source = "../.."
}
