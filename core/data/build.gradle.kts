plugins {
    id("tang.android.library")
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.tang.prm.data"

    defaultConfig {
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    ksp {
        arg("room.schemaLocation", "$projectDir/schemas")
    }
}

dependencies {
    implementation(project(":core:domain"))

    // Room
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)

    // kotlinx-serialization
    implementation(libs.kotlinx.serialization.json)

    // Hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    // DataStore + security
    implementation(libs.datastore.preferences)
    implementation(libs.security.crypto)

    // OkHTTP
    implementation(libs.okhttp)

    // Core
    implementation(libs.core.ktx)

    // Coroutines
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)

    testImplementation(libs.json)
    // A-2 修复：kxml2 用于 DavXmlParser 纯 JVM 单测。
    // compileOnly：编译时需要 KXmlParser 类型（Android 运行时内置但不暴露给编译器）；
    // testImplementation：单元测试需要实际实现（运行时无 Android 框架）。
    compileOnly("net.sf.kxml:kxml2:2.3.0")
    testImplementation("net.sf.kxml:kxml2:2.3.0")
    androidTestImplementation(libs.room.testing)
}
