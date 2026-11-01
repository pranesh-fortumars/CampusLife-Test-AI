package com.example.util

import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import androidx.core.content.FileProvider
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL

/**
 * Data class representing remote version metadata hosted on the Vercel deployment.
 */
data class RemoteAppVersion(
    val versionCode: Int,
    val versionName: String,
    val apkUrl: String,
    val downloadUrl: String,
    val releaseNotes: String,
    val publishedDate: String
)

/**
 * AppUpdateManager coordinates in-app Over-The-Air (OTA) updates for the phone.
 * Queries the Vercel deployed link for version.json, downloads the latest APK,
 * and launches the Android package installer.
 */
object AppUpdateManager {

    private const val PREFS_NAME = "campuslife_updater_prefs"
    private const val KEY_VERCEL_URL = "custom_vercel_url"
    const val DEFAULT_VERCEL_URL = "https://campuslife-app.vercel.app"

    private val _isChecking = MutableStateFlow(false)
    val isChecking: StateFlow<Boolean> = _isChecking.asStateFlow()

    private val _isDownloading = MutableStateFlow(false)
    val isDownloading: StateFlow<Boolean> = _isDownloading.asStateFlow()

    private val _downloadProgress = MutableStateFlow(0) // 0 - 100
    val downloadProgress: StateFlow<Int> = _downloadProgress.asStateFlow()

    private val _updateAvailable = MutableStateFlow(false)
    val updateAvailable: StateFlow<Boolean> = _updateAvailable.asStateFlow()

    private val _remoteVersion = MutableStateFlow<RemoteAppVersion?>(null)
    val remoteVersion: StateFlow<RemoteAppVersion?> = _remoteVersion.asStateFlow()

    private val _statusMessage = MutableStateFlow<String?>(null)
    val statusMessage: StateFlow<String?> = _statusMessage.asStateFlow()

    fun getVercelUrl(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_VERCEL_URL, DEFAULT_VERCEL_URL) ?: DEFAULT_VERCEL_URL
    }

    fun setVercelUrl(context: Context, url: String) {
        val cleanUrl = url.trim().trimEnd('/')
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_VERCEL_URL, cleanUrl).apply()
        _statusMessage.value = "Updated Vercel URL to $cleanUrl"
    }

    fun clearStatusMessage() {
        _statusMessage.value = null
    }

    /**
     * Checks the Vercel deployed link for the latest APK version.json
     */
    suspend fun checkForUpdates(context: Context, customUrl: String? = null): Boolean {
        _isChecking.value = true
        _statusMessage.value = "Checking Vercel for updates..."

        return withContext(Dispatchers.IO) {
            try {
                val baseUrl = (customUrl ?: getVercelUrl(context)).trim().trimEnd('/')
                val versionJsonUrl = "$baseUrl/version.json"

                val url = URL(versionJsonUrl)
                val connection = (url.openConnection() as HttpURLConnection).apply {
                    connectTimeout = 8000
                    readTimeout = 8000
                    requestMethod = "GET"
                    setRequestProperty("Accept", "application/json")
                    setRequestProperty("Cache-Control", "no-cache")
                }

                if (connection.responseCode in 200..299) {
                    val jsonString = connection.inputStream.bufferedReader().use { it.readText() }
                    val json = JSONObject(jsonString)

                    val remoteCode = json.optInt("versionCode", 1)
                    val remoteName = json.optString("versionName", "1.0")
                    var apkPath = json.optString("apkUrl", "/CampusLife.apk")
                    if (!apkPath.startsWith("http")) {
                        apkPath = "$baseUrl/${apkPath.trimStart('/')}"
                    }
                    val notes = json.optString("releaseNotes", "New stability and feature updates available.")
                    val pubDate = json.optString("publishedDate", "Recently")

                    val versionInfo = RemoteAppVersion(
                        versionCode = remoteCode,
                        versionName = remoteName,
                        apkUrl = apkPath,
                        downloadUrl = apkPath,
                        releaseNotes = notes,
                        publishedDate = pubDate
                    )

                    _remoteVersion.value = versionInfo

                    // Compare with current running app versionCode
                    val currentVersionCode = BuildConfig.VERSION_CODE
                    val hasNewVersion = remoteCode > currentVersionCode

                    _updateAvailable.value = hasNewVersion
                    _isChecking.value = false

                    _statusMessage.value = if (hasNewVersion) {
                        "New version $remoteName (build $remoteCode) available on Vercel! 🎉"
                    } else {
                        "CampusLife is up to date (v${BuildConfig.VERSION_NAME})"
                    }

                    hasNewVersion
                } else {
                    _isChecking.value = false
                    _statusMessage.value = "Could not fetch updates from Vercel (HTTP ${connection.responseCode})"
                    false
                }
            } catch (e: Exception) {
                _isChecking.value = false
                _statusMessage.value = "Vercel update check note: ${e.localizedMessage ?: "Verify network or Vercel link"}"
                false
            }
        }
    }

    /**
     * Downloads the APK from the Vercel deployed URL and triggers the package installer.
     */
    suspend fun downloadAndInstall(context: Context, downloadUrl: String? = null) {
        val targetUrl = downloadUrl ?: _remoteVersion.value?.apkUrl ?: "${getVercelUrl(context)}/CampusLife.apk"
        _isDownloading.value = true
        _downloadProgress.value = 10
        _statusMessage.value = "Downloading update from Vercel..."

        withContext(Dispatchers.IO) {
            try {
                val url = URL(targetUrl)
                val connection = (url.openConnection() as HttpURLConnection).apply {
                    connectTimeout = 15000
                    readTimeout = 30000
                    requestMethod = "GET"
                }

                val contentLength = connection.contentLength
                val cacheDir = File(context.cacheDir, "updates")
                if (!cacheDir.exists()) cacheDir.mkdirs()

                val apkFile = File(cacheDir, "CampusLife-update.apk")
                if (apkFile.exists()) apkFile.delete()

                connection.inputStream.use { input ->
                    FileOutputStream(apkFile).use { output ->
                        val buffer = ByteArray(8192)
                        var bytesRead: Int
                        var totalRead = 0L

                        while (input.read(buffer).also { bytesRead = it } != -1) {
                            output.write(buffer, 0, bytesRead)
                            totalRead += bytesRead
                            if (contentLength > 0) {
                                val progress = ((totalRead * 100) / contentLength).toInt().coerceIn(10, 95)
                                _downloadProgress.value = progress
                            }
                        }
                    }
                }

                _downloadProgress.value = 100
                _isDownloading.value = false
                _statusMessage.value = "Download complete! Opening installer..."

                withContext(Dispatchers.Main) {
                    launchPackageInstaller(context, apkFile)
                }
            } catch (e: Exception) {
                _isDownloading.value = false
                _statusMessage.value = "Download failed: ${e.localizedMessage}. Opening browser instead..."
                withContext(Dispatchers.Main) {
                    openVercelPageInBrowser(context)
                }
            }
        }
    }

    /**
     * Launches the system package installer intent for the downloaded APK using FileProvider.
     */
    fun launchPackageInstaller(context: Context, apkFile: File) {
        try {
            val apkUri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                apkFile
            )

            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(apkUri, "application/vnd.android.package-archive")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            // Fallback: Open Vercel web page in browser so student can install
            openVercelPageInBrowser(context)
        }
    }

    /**
     * Opens the Vercel deployed link directly in Chrome / system browser.
     */
    fun openVercelPageInBrowser(context: Context) {
        val url = getVercelUrl(context)
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
            _statusMessage.value = "Opened Vercel download page in browser"
        } catch (e: Exception) {
            _statusMessage.value = "Cannot open browser: ${e.localizedMessage}"
        }
    }
}
