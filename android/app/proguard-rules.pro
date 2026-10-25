# Grimoire 5E - ProGuard / R8 Consumer Rules

# Keep data models used in JSON serialization (Gson, turn pipeline schemas)
-keepclassmembers class com.grimoire.dnd.dm.models.** {
    <fields>;
    <methods>;
}
-keep class com.grimoire.dnd.dm.models.** { *; }

-keepclassmembers class com.grimoire.dnd.engine.** {
    <fields>;
    <methods>;
}
-keep class com.grimoire.dnd.engine.** { *; }

# Keep Room database entities, DAOs, and Room runtime generated code
-keep class androidx.room.** { *; }
-dontwarn androidx.room.**
-keepclassmembers class * extends androidx.room.RoomDatabase {
    <init>();
}
-keepclassmembers class com.grimoire.dnd.data.db.** {
    <fields>;
    <methods>;
}
-keep class com.grimoire.dnd.data.db.** { *; }

# Gson serialization rules
-keepattributes Signature
-keepattributes *Annotation*
-dontwarn sun.misc.**
-keep class com.google.gson.** { *; }
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# Jetpack Compose and Kotlin coroutines
-dontwarn kotlin.Unit
-keep class androidx.compose.** { *; }
