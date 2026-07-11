plugins {
    id("tang.android.feature")
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.tang.prm.feature.divination"
}

dependencies {
    implementation(project(":core:domain"))
    implementation(project(":core:ui"))
    implementation(project(":engine:divination"))

    debugImplementation(libs.compose.ui.tooling)
    implementation(libs.kotlinx.serialization.json)

    // Android Test
    androidTestImplementation(libs.bundles.android.test)
    androidTestImplementation(platform(libs.compose.bom))
}

tasks.withType<Test> {
    useJUnitPlatform()
}
