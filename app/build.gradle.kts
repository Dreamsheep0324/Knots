import java.util.Properties

plugins {
    id("tang.android.application")
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
    alias(libs.plugins.baselineprofile)
}

val keystorePropertiesFile = rootProject.file("keystore.properties")
val keystoreProperties = Properties()
if (keystorePropertiesFile.exists()) {
    keystoreProperties.load(keystorePropertiesFile.inputStream())
}

android {
    namespace = "com.tang.prm"

    defaultConfig {
        applicationId = "com.tang.prm"
        versionCode = 10601
        versionName = "1.6.1"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    signingConfigs {
        create("release") {
            if (keystorePropertiesFile.exists()) {
                storeFile = file(keystoreProperties.getProperty("storeFile", ""))
                storePassword = keystoreProperties.getProperty("storePassword", "")
                keyAlias = keystoreProperties.getProperty("keyAlias", "")
                keyPassword = keystoreProperties.getProperty("keyPassword", "")
                @Suppress("UnstableApiUsage")
                enableV1Signing = true
                @Suppress("UnstableApiUsage")
                enableV2Signing = true
                @Suppress("UnstableApiUsage")
                enableV3Signing = true
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            signingConfig = signingConfigs.getByName("release")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        // 内部测试构建：启用混淆但保留调试能力，可与正式版共存
        create("staging") {
            initWith(getByName("release"))
            applicationIdSuffix = ".staging"
            versionNameSuffix = "-staging"
            isDebuggable = true
            // Staging 使用与 release 相同的签名，便于在真实环境测试
            signingConfig = signingConfigs.getByName("release")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    // 模块依赖
    implementation(project(":core:domain"))
    implementation(project(":core:data"))   // Hilt @Binds 需要编译时可见
    implementation(project(":core:ui"))
    implementation(project(":engine:divination"))
    implementation(project(":feature:divination"))
    implementation(project(":feature:remember"))
    implementation(project(":feature:people"))
    implementation(project(":feature:events"))
    implementation(project(":feature:chat"))
    implementation(project(":feature:gifts"))
    implementation(project(":feature:reflect"))
    implementation(project(":feature:subscription"))
    implementation(project(":feature:profile"))
    implementation(project(":feature:circle"))
    implementation(project(":feature:home"))
    implementation(project(":feature:recipes"))
    implementation(project(":feature:graph"))

    // Baseline Profile — release 构建自动集成 AOT 优化
    baselineProfile(project(":baselineprofile"))
    implementation(libs.profileinstaller)

    // Coil — app 直接使用 AsyncImage（core:data 不再 api 透传）
    implementation(libs.coil.compose)

    // Compose
    implementation(platform(libs.compose.bom))
    implementation(libs.bundles.compose)

    // AndroidX
    implementation(libs.core.ktx)
    implementation(libs.activity.compose)
    implementation(libs.bundles.lifecycle)
    implementation(libs.lifecycle.process)
    implementation(libs.navigation.compose)

    // Hilt（app 模块需要自己的 Hilt 注入入口）
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.hilt.navigation.compose)

    // Kotlin Serialization（app 模块直接使用）
    implementation(libs.kotlinx.serialization.json)

    // Debug
    debugImplementation(libs.bundles.compose.debug)

    // Test
    testImplementation(libs.junit.jupiter.api)
    testRuntimeOnly(libs.junit.jupiter.engine)
    testImplementation(libs.mockk)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.turbine)
    testImplementation(libs.truth)

    androidTestImplementation(libs.benchmark.macro.junit4)
    androidTestImplementation(libs.test.ext.junit)
    androidTestImplementation(libs.test.runner)
    androidTestImplementation(platform(libs.compose.bom))
    androidTestImplementation(libs.compose.ui.test.junit4)
    androidTestImplementation(libs.truth)
    androidTestImplementation(libs.kotlinx.coroutines.test)
    androidTestImplementation(libs.mockk.android)
    androidTestImplementation(libs.hilt.android.testing)
    kspAndroidTest(libs.hilt.compiler)
}

tasks.withType<Test> {
    useJUnitPlatform()
}
