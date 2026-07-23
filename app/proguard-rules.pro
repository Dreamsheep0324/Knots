# ============================================================
# 结绳 (Tang) — ProGuard / R8 规则
# ============================================================

# ---- 通用保留 ----
-keepattributes Signature
-keepattributes *Annotation*
-keepattributes SourceFile,LineNumberTable        # 崩溃堆栈可读
-keepattributes RuntimeVisibleAnnotations
-keepattributes EnclosingMethod

# 保留 @Keep 注解标记的类/成员
-keep,allowobfuscation @androidx.annotation.Keep class *
-keep @androidx.annotation.Keep class * { *; }
-keepclassmembers class * {
    @androidx.annotation.Keep *;
}

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

# ---- OkHttp SSE ----
-dontwarn okhttp3.internal.sse.**
-dontwarn okio.**

# ---- kXML2 (DavXmlParser 使用, Android 平台内部库) ----
-dontwarn org.kxml2.**
-keep class org.kxml2.** { *; }

# ---- Gson ----
-keepattributes Signature
-keepattributes *Annotation*
-keep class com.google.gson.** { *; }
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer
# 保留使用 @SerializedName 的 DTO 类（Gson 反射字段名）
-keep class com.tang.prm.data.remote.** { *; }
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}

# ---- Coil ----
-dontwarn coil.**
-keep class coil.** { *; }

# ---- Lunar (cn.6tail:lunar) ----
-keep class com.nlf.calendar.** { *; }
-keepclassmembers class com.nlf.calendar.** { *; }

# ---- DataStore Preferences ----
# (DataStore Preferences does not use Protobuf; no keep rules needed)

# ---- EncryptedSharedPreferences ----
-keep class androidx.security.crypto.** { *; }
-dontwarn androidx.security.crypto.**

# ---- Tink (EncryptedSharedPreferences 依赖) ----
# Tink 引用 errorprone 注解做编译期检查，运行时无需该依赖
-dontwarn com.google.errorprone.annotations.**

# ---- Kotlinx Coroutines ----
-dontwarn kotlinx.coroutines.**

# ---- Navigation Compose ----
# 保留所有路由对象（@Serializable object/data class），Navigation Compose
# 类型安全 API 依赖 kotlinx.serialization 反射路由参数
-keep class com.tang.prm.ui.navigation.** { *; }
-keep class * extends androidx.navigation.NavController { <init>(...); }
# 保留 kotlinx.serialization 生成的 $$serializer 供 Navigation 使用
-keepclassmembers class com.tang.prm.ui.navigation.** {
    *** Companion;
    kotlinx.serialization.KSerializer serializer(...);
}

# ---- Compose ----
-dontwarn androidx.compose.**

# ---- 应用自身 Model ----
-keep class com.tang.prm.domain.model.** { *; }
-keep class com.tang.prm.domain.divination.model.** { *; }
