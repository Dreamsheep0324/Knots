plugins {
    id("tang.android.feature")
}

android {
    namespace = "com.tang.prm.feature.profile"
}

dependencies {
    implementation(project(":core:domain"))
    implementation(project(":core:ui"))

    implementation(libs.coil.compose)
    implementation(libs.datastore.preferences)
}

tasks.withType<Test> {
    useJUnitPlatform()
}
