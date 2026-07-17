plugins {
    id("tang.android.feature")
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.tang.prm.feature.remember"
    defaultConfig {
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
}

dependencies {
    implementation(project(":core:domain"))
    implementation(project(":core:ui"))

    implementation(libs.coil.compose)
    implementation(libs.kotlinx.serialization.json)

    // Android Test
    androidTestImplementation(libs.bundles.android.test)
    androidTestImplementation(platform(libs.compose.bom))
}

tasks.withType<Test> {
    useJUnitPlatform()
}
