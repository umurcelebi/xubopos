package com.xubopos.update

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import android.util.Log
import androidx.core.content.FileProvider
import com.google.gson.Gson
import com.google.gson.JsonObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL

data class UpdateInfo(
    val isUpdateAvailable: Boolean,
    val currentVersion: String,
    val latestVersion: String,
    val releaseTitle: String,
    val releaseNotes: String,
    val downloadUrl: String,
    val apkSize: Long
)

sealed class UpdateStatus {
    object Idle : UpdateStatus()
    object Checking : UpdateStatus()
    data class UpdateAvailable(val info: UpdateInfo) : UpdateStatus()
    object UpToDate : UpdateStatus()
    data class Downloading(val progressPercent: Int, val downloadedBytes: Long, val totalBytes: Long) : UpdateStatus()
    data class ReadyToInstall(val apkFile: File) : UpdateStatus()
    data class Error(val message: String) : UpdateStatus()
}

object AppUpdateManager {
    private const val TAG = "AppUpdateManager"
    const val GITHUB_OWNER = "umurcelebi"
    const val GITHUB_REPO = "xubopos"
    private const val GITHUB_API_URL = "https://api.github.com/repos/$GITHUB_OWNER/$GITHUB_REPO/releases/latest"

    /**
     * GitHub Releases üzerinden son sürümü sorgular ve mevcut sürüm ile karşılaştırır.
     */
    suspend fun checkForUpdates(currentVersion: String): Result<UpdateInfo> = withContext(Dispatchers.IO) {
        try {
            val url = URL(GITHUB_API_URL)
            val connection = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 8000
                readTimeout = 8000
                setRequestProperty("User-Agent", "Xubopos-App")
                setRequestProperty("Accept", "application/vnd.github.v3+json")
            }

            if (connection.responseCode != HttpURLConnection.HTTP_OK) {
                return@withContext Result.failure(
                    Exception("GitHub bağlantı hatası: HTTP ${connection.responseCode}")
                )
            }

            val responseText = connection.inputStream.bufferedReader().use { it.readText() }
            val json = Gson().fromJson(responseText, JsonObject::class.java)

            val tagName = json.get("tag_name")?.asString ?: ""
            val releaseTitle = json.get("name")?.asString ?: tagName
            val releaseNotes = json.get("body")?.asString ?: ""

            val assets = json.getAsJsonArray("assets")
            var apkDownloadUrl = ""
            var apkSize = 0L

            if (assets != null && assets.size() > 0) {
                for (i in 0 until assets.size()) {
                    val asset = assets[i].asJsonObject
                    val name = asset.get("name")?.asString ?: ""
                    if (name.endsWith(".apk", ignoreCase = true)) {
                        apkDownloadUrl = asset.get("browser_download_url")?.asString ?: ""
                        apkSize = asset.get("size")?.asLong ?: 0L
                        break
                    }
                }
            }

            if (apkDownloadUrl.isBlank()) {
                return@withContext Result.failure(Exception("Sürümde indirilebilir APK dosyası bulunamadı."))
            }

            val isAvailable = isNewerVersion(remoteTag = tagName, currentVersion = currentVersion)

            val info = UpdateInfo(
                isUpdateAvailable = isAvailable,
                currentVersion = currentVersion,
                latestVersion = tagName,
                releaseTitle = releaseTitle,
                releaseNotes = releaseNotes,
                downloadUrl = apkDownloadUrl,
                apkSize = apkSize
            )

            Result.success(info)
        } catch (e: Exception) {
            Log.e(TAG, "Güncelleme kontrol hatası: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Semantik sürüm karşılaştırması yapar. Örn: "v1.0.1" > "1.0.0" -> true
     */
    fun isNewerVersion(remoteTag: String, currentVersion: String): Boolean {
        val cleanRemote = remoteTag.trim().removePrefix("v").removePrefix("V")
        val cleanCurrent = currentVersion.trim().removePrefix("v").removePrefix("V")

        val remoteParts = cleanRemote.split(".").mapNotNull { it.toIntOrNull() }
        val currentParts = cleanCurrent.split(".").mapNotNull { it.toIntOrNull() }

        if (remoteParts.isEmpty() || currentParts.isEmpty()) {
            return cleanRemote != cleanCurrent
        }

        val maxLen = maxOf(remoteParts.size, currentParts.size)
        for (i in 0 until maxLen) {
            val r = remoteParts.getOrElse(i) { 0 }
            val c = currentParts.getOrElse(i) { 0 }
            if (r > c) return true
            if (r < c) return false
        }
        return false
    }

    /**
     * Yeni APK dosyasını indirir ve anlık indirme yüzdesini bildirir.
     */
    suspend fun downloadApk(
        context: Context,
        downloadUrl: String,
        onProgress: (percent: Int, downloaded: Long, total: Long) -> Unit
    ): Result<File> = withContext(Dispatchers.IO) {
        try {
            var currentUrl = downloadUrl
            var connection: HttpURLConnection
            var redirects = 0

            // GitHub release -> AWS S3 yönlendirmelerini (302/307) takip et
            while (true) {
                val url = URL(currentUrl)
                connection = (url.openConnection() as HttpURLConnection).apply {
                    instanceFollowRedirects = false
                    connectTimeout = 15000
                    readTimeout = 20000
                    setRequestProperty("User-Agent", "Xubopos-App")
                }

                val code = connection.responseCode
                if (code == HttpURLConnection.HTTP_MOVED_PERM ||
                    code == HttpURLConnection.HTTP_MOVED_TEMP ||
                    code == HttpURLConnection.HTTP_SEE_OTHER ||
                    code == 307
                ) {
                    val newLocation = connection.getHeaderField("Location")
                    connection.disconnect()
                    if (newLocation.isNullOrBlank()) {
                        throw Exception("Geçersiz yönlendirme adresi")
                    }
                    currentUrl = newLocation
                    redirects++
                    if (redirects > 6) throw Exception("Çok fazla yönlendirme")
                    continue
                }
                break
            }

            val totalBytes = connection.contentLength.toLong()
            val downloadDir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS) ?: context.cacheDir
            val targetFile = File(downloadDir, "Xubopos-update.apk")

            if (targetFile.exists()) {
                targetFile.delete()
            }

            var downloadedBytes = 0L
            connection.inputStream.use { input ->
                FileOutputStream(targetFile).use { output ->
                    val buffer = ByteArray(8192)
                    var bytesRead: Int
                    while (input.read(buffer).also { bytesRead = it } != -1) {
                        output.write(buffer, 0, bytesRead)
                        downloadedBytes += bytesRead
                        val percent = if (totalBytes > 0) {
                            ((downloadedBytes * 100) / totalBytes).toInt().coerceIn(0, 100)
                        } else {
                            0
                        }
                        onProgress(percent, downloadedBytes, totalBytes)
                    }
                    output.flush()
                }
            }

            Result.success(targetFile)
        } catch (e: Exception) {
            Log.e(TAG, "İndirme hatası: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * İndirilen APK dosyasını FileProvider ile çalıştırarak Android paket yükleyicisini başlatır.
     */
    fun installApk(context: Context, apkFile: File): Boolean {
        return try {
            if (!apkFile.exists()) {
                Log.e(TAG, "Yüklenecek APK bulunamadı: ${apkFile.absolutePath}")
                return false
            }

            // Android 8.0+ bilinmeyen kaynak izni kontrolü
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if (!context.packageManager.canRequestPackageInstalls()) {
                    val manageIntent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES).apply {
                        data = Uri.parse("package:${context.packageName}")
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    context.startActivity(manageIntent)
                    return false
                }
            }

            val apkUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                apkFile
            )

            val installIntent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(apkUri, "application/vnd.android.package-archive")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
            }

            context.startActivity(installIntent)
            true
        } catch (e: Exception) {
            Log.e(TAG, "Yükleme başlatılamadı: ${e.message}", e)
            false
        }
    }
}
