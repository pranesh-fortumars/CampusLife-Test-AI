plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.ksp) apply false
}

// Automatically restore debug.keystore from debug.keystore.base64 if missing in CI/CD environments
val keystore = file("debug.keystore")
val keystoreBase64 = file("debug.keystore.base64")
if (!keystore.exists() && keystoreBase64.exists()) {
    try {
        val decoded = java.util.Base64.getDecoder().decode(keystoreBase64.readText().trim())
        keystore.writeBytes(decoded)
    } catch (e: Exception) {
        // Fallback
    }
}
