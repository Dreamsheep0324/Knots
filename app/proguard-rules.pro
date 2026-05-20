-keepattributes Signature
-keepattributes *Annotation*

# Keep Room entities
-keep class com.tang.prm.data.local.entity.** { *; }

# Keep Hilt generated classes
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-keep class * extends dagger.hilt.android.internal.managers.ComponentSupplier { *; }

# Coil
-dontwarn coil.**
-keep class coil.** { *; }

# Lunar (cn.6tail:lunar)
-keep class com.nlf.calendar.** { *; }
-keepclassmembers class com.nlf.calendar.** { *; }

# DataStore
-keepclassmembers class * extends com.google.protobuf.GeneratedMessageLite { <methods>; }

# EncryptedSharedPreferences (androidx.security)
-keep class androidx.security.crypto.** { *; }
-dontwarn androidx.security.crypto.**

# OkHttp SSE
-dontwarn okhttp3.internal.sse.**

# Kotlinx coroutines
-dontwarn kotlinx.coroutines.**

# Navigation Compose
-keepnames class androidx.navigation.fragment.NavHostFragment
-keep class * extends androidx.navigation.NavController { <init>(...); }

# Compose
-dontwarn androidx.compose.**
