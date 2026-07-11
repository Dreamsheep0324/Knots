plugins {
    id("tang.android.feature")
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.tang.prm.feature.subscription"
}

dependencies {
    implementation(project(":core:domain"))
    implementation(project(":core:ui"))

    implementation(libs.kotlinx.serialization.json)
}

tasks.withType<Test> {
    useJUnitPlatform()
}
