# ============================================================
# 结绳 (Tang) — ProGuard / R8 规则
# ============================================================

# ---- 通用保留 ----
-keepattributes Signature
-keepattributes *Annotation*
-keepattributes SourceFile,LineNumberTable        # 崩溃堆栈可读
-keepattributes RuntimeVisibleAnnotations
-keepattributes EnclosingMethod

# ---- Kotlin ----
-dontwarn kotlin.**
-keepclassmembers class kotlin.Metadata { *; }
-keep class kotlin.reflect.** { *; }

# ---- Kotlinx Serialization ----
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keepclassmembers class kotlinx.serialization.json.** { *** Companion; }
-keepclasseswithmembers class kotlinx.serialization.json.** { kotlinx.serialization.KSerializer serializer(...); }
-keepclassmembers class * {
    ** Companion;
    kotlinx.serialization.KSerializer serializer(...);
}
-keep class *$$serializer { *; }
-keepclassmembers class *$$serializer {
    kotlinx.serialization.KSerializer serializer(...);
}

# ---- Room ----
-keep class com.tang.prm.data.local.entity.** { *; }
-keep class com.tang.prm.data.local.dao.** { *; }
-keep @androidx.room.Entity class * { *; }
-dontwarn androidx.room.paging.**

# ---- Hilt / Dagger ----
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-keep class * extends dagger.hilt.android.internal.managers.ComponentSupplier { *; }
-dontwarn dagger.hilt.**

# ---- Retrofit ----
-keepattributes Signature, Exceptions
-keepclassmembers,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}
-dontwarn retrofit2.**

# ---- OkHttp SSE ----
-dontwarn okhttp3.internal.sse.**
-dontwarn okio.**

# ---- Gson ----
-keepattributes Signature
-keepattributes *Annotation*
-keep class com.google.gson.** { *; }
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer

# ---- Coil ----
-dontwarn coil.**
-keep class coil.** { *; }

# ---- Lunar (cn.6tail:lunar) ----
-keep class com.nlf.calendar.** { *; }
-keepclassmembers class com.nlf.calendar.** { *; }

# ---- DataStore ----
-keepclassmembers class * extends com.google.protobuf.GeneratedMessageLite { <methods>; }
-dontwarn com.google.protobuf.**

# ---- EncryptedSharedPreferences ----
-keep class androidx.security.crypto.** { *; }
-dontwarn androidx.security.crypto.**

# ---- Kotlinx Coroutines ----
-dontwarn kotlinx.coroutines.**

# ---- Navigation Compose ----
-keepnames class androidx.navigation.fragment.NavHostFragment
-keep class * extends androidx.navigation.NavController { <init>(...); }

# ---- Compose ----
-dontwarn androidx.compose.**

# ---- AndroidX Security / Biometric ----
-keep class androidx.biometric.** { *; }
-dontwarn androidx.biometric.**

# ---- 应用自身 Model ----
-keep class com.tang.prm.domain.model.** { *; }
-keep class com.tang.prm.domain.divination.model.** { *; }
