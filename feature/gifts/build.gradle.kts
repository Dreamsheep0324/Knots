plugins {
    id("tang.android.feature")
}

android {
    namespace = "com.tang.prm.feature.gifts"
}

dependencies {
    implementation(project(":core:domain"))
    implementation(project(":core:ui"))

    implementation(libs.coil.compose)
}

tasks.withType<Test> {
    useJUnitPlatform()
}
