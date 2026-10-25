// Top-level build file: plugin versions are declared once here and applied in :app.
plugins {
    id("com.android.application") version "8.7.3" apply false
    id("org.jetbrains.kotlin.android") version "2.0.21" apply false
    // Kotlin 2.0+ ships the Compose compiler as a Gradle plugin.
    id("org.jetbrains.kotlin.plugin.compose") version "2.0.21" apply false
}
