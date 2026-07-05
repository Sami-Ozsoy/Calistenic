# --- Room ---
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-keepclassmembers @androidx.room.Entity class * { *; }
-keep class androidx.room.** { *; }
-dontwarn androidx.room.paging.**

# --- DataStore ---
-keep class androidx.datastore.** { *; }
-keep class **$Companion { *; }
-keepclassmembers class * { @com.google.protobuf.** *; }

# --- Coroutines ---
-keepclassmembers class kotlinx.coroutines.** { volatile <fields>; }
-keep class kotlin.coroutines.** { *; }
-keep class kotlinx.coroutines.android.AndroidExceptionPreHandler { *; }

# --- Compose / Kotlin reflection ---
-keep class androidx.compose.** { *; }
-keep class kotlin.reflect.** { *; }
-dontwarn androidx.compose.**

# --- App model ---
-keep class com.example.calistenic.data.local.** { *; }
-keep class com.example.calistenic.data.repository.** { *; }
-keep class com.example.calistenic.ui.TimerUiState { *; }
-keepclassmembers class com.example.calistenic.** { <init>(...); }

# --- Enums ---
-keepclassmembers enum * { public static **[] values(); public static ** valueOf(java.lang.String); }