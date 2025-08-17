import java.io.FileInputStream
import java.util.Properties

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.jetbrainsCompose)
    alias(libs.plugins.kotlin.compose)
    // alias(libs.plugins.sqldelight) // temporary removal
}

// Load local.properties
val localProperties = Properties()
val localPropertiesFile = rootProject.file("local.properties")
if (localPropertiesFile.exists()) {
    localProperties.load(FileInputStream(localPropertiesFile))
}

kotlin {
    androidTarget {
        compilations.all {
            kotlinOptions {
                jvmTarget = "1.8"
            }
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(compose.components.resources)
            implementation(libs.androidx.appcompat)
            implementation(libs.accompanist.permissions)

            // Navigation
            implementation(libs.androidx.lifecycle.viewmodel.compose)
            implementation(libs.navigation.compose)

            // SQLDelight - temporarily removed
            // implementation(libs.sqldelight.coroutines)
            
            // Koin
            implementation(libs.koin.core)

            // 共通ロジック
            implementation(projects.shared)
        }

        androidMain.dependencies {
            implementation(libs.androidx.activity.compose)
            implementation(libs.compose.ui.tooling.preview)
            implementation(libs.compose.ui.tooling)

            implementation(libs.play.services.maps)
            implementation(libs.maps.compose)

            implementation(libs.play.services.location)
            
            // SQLDelight Android driver - temporarily removed
            // implementation(libs.sqldelight.driver.android)
            
            // Koin Android
            implementation(libs.koin.android)
            implementation(libs.koin.compose)
        }
    }
}

android {
    namespace = "xyz.moroku0519.shoppinghelper"
    compileSdk = 35

    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    sourceSets["main"].res.srcDirs("src/androidMain/res")
    sourceSets["main"].resources.srcDirs("src/commonMain/resources")

    defaultConfig {
        applicationId = "xyz.moroku0519.shoppinghelper"
        minSdk = 29
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
        // Load API keys from local.properties, fallback to gradle.properties, then default
        val mapsApiKey = localProperties.getProperty("MAPS_API_KEY") 
            ?: project.findProperty("MAPS_API_KEY") as String? 
            ?: "YOUR_API_KEY_HERE"
        manifestPlaceholders["MAPS_API_KEY"] = mapsApiKey
        
        // Supabase configuration for Phase 2
        val supabaseUrl = localProperties.getProperty("SUPABASE_URL")
            ?: project.findProperty("SUPABASE_URL") as String?
            ?: "https://your-project.supabase.co"
        buildConfigField("String", "SUPABASE_URL", "\"$supabaseUrl\"")
        
        val supabasePublishableKey = localProperties.getProperty("SUPABASE_PUBLISHABLE_KEY")
            ?: localProperties.getProperty("SUPABASE_ANON_KEY")  // Fallback for migration
            ?: project.findProperty("SUPABASE_PUBLISHABLE_KEY") as String?
            ?: project.findProperty("SUPABASE_ANON_KEY") as String?  // Legacy fallback
            ?: "your-supabase-publishable-key-here"
        buildConfigField("String", "SUPABASE_PUBLISHABLE_KEY", "\"$supabasePublishableKey\"")
        
        // Legacy support (deprecated)
        buildConfigField("String", "SUPABASE_ANON_KEY", "\"$supabasePublishableKey\"")
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    buildTypes {
        getByName("debug") {
            isDebuggable = true
            applicationIdSuffix = ".debug"
            buildConfigField("boolean", "DEBUG", "true")
        }
        getByName("release") {
            isMinifyEnabled = false
            buildConfigField("boolean", "DEBUG", "false")
        }
    }
    
    buildFeatures {
        buildConfig = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.4"
    }
}