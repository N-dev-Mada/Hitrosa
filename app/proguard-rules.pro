# ==============================================================================
# ProGuard / R8 Configuration for CarnetPro
# Optimized for Jetpack Compose, Room SQLite, DataStore & Biometrics
# ==============================================================================

# Preserve line numbers and source files for readable stack traces in crash logs
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Preserve Annotations & Generics
-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod

# ------------------------------------------------------------------------------
# 1. Room Database & SQLite Immutability Protection
# ------------------------------------------------------------------------------
-keep class * extends androidx.room.RoomDatabase {
    public <init>();
}

# Keep all Room Entities, DAOs and their generated implementations
-keep @androidx.room.Entity class * { *; }
-keep @androidx.room.Dao interface * { *; }
-keep class * extends androidx.room.RoomDatabase
-keep class **_Impl { *; }

# Keep data models
-keep class com.example.data.model.** { *; }
-keepclassmembers class com.example.data.model.** { *; }

# Prevent SQLite support classes and triggers from being stripped
-keep class androidx.sqlite.db.** { *; }
-keep class androidx.room.** { *; }

# ------------------------------------------------------------------------------
# 2. Jetpack Compose & Kotlin Coroutines
# ------------------------------------------------------------------------------
-keepclassmembers class * {
    @androidx.compose.runtime.Composable *;
}

-dontwarn kotlinx.coroutines.**
-keepnames class kotlinx.coroutines.** { *; }

# ------------------------------------------------------------------------------
# 3. AndroidX DataStore Preferences & Protobuf
# ------------------------------------------------------------------------------
-keepclassmembers class * extends androidx.datastore.preferences.core.Preferences { *; }
-dontwarn androidx.datastore.**

# ------------------------------------------------------------------------------
# 4. Biometric Authentication & Crypto
# ------------------------------------------------------------------------------
-keep class androidx.biometric.** { *; }
-keepclassmembers class androidx.biometric.** { *; }
-keep class java.security.MessageDigest { *; }

# ------------------------------------------------------------------------------
# 5. Coil Image Loading
# ------------------------------------------------------------------------------
-keep class coil.** { *; }
-dontwarn coil.**

# ------------------------------------------------------------------------------
# 6. JSON Export & Backup Models
# ------------------------------------------------------------------------------
-keepclassmembers class org.json.** { *; }
