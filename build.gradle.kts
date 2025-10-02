// Top-level build file (or equivalent)
plugins {
    // 💡 FIX: Re-add the application plugin alias (this was missing/unclear in the screenshot)
    alias(libs.plugins.android.application) apply false

    // Google services plugin definition
    id("com.google.gms.google-services") version "4.4.3" apply false

}
// NOTE: Make sure this is the ONLY 'plugins' block in this file.