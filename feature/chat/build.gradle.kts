plugins {
    id("tang.android.feature")
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.tang.prm.feature.chat"
}

dependencies {
    implementation(project(":core:domain"))
    implementation(project(":core:ui"))

    implementation(libs.coil.compose)
    implementation(libs.kotlinx.serialization.json)
}

tasks.withType<Test> {
    useJUnitPlatform()
}
