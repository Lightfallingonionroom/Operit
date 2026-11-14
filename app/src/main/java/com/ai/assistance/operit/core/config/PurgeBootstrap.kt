package com.ai.assistance.operit.core.config

import android.content.Context
import android.os.Environment
import com.ai.assistance.operit.util.AppLogger
import java.io.File

/**
 * 二改内置资源落地：把 APK assets 里的红队技能与 rt 工具层铺到 Operit 的外部目录。
 *
 *   assets/purge/skills 目录        -> /sdcard/Download/Operit/skills
 *   assets/purge/redteam/bin/rt      -> /sdcard/Download/Operit/redteam/bin/rt
 *   assets/purge/redteam/data 目录   -> /sdcard/Download/Operit/redteam/data
 *
 * 全部 best-effort：没有外部存储权限时静默跳过，不影响应用启动。
 */
object PurgeBootstrap {

    private const val TAG = "PurgeBootstrap"

    fun installAsync(context: Context) {
        val appContext = context.applicationContext
        Thread {
            try {
                install(appContext)
            } catch (t: Throwable) {
                AppLogger.e(TAG, "内置破甲资源落地失败", t)
            }
        }.start()
    }

    private fun install(context: Context) {
        val downloads = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val operitDir = File(downloads, "Operit")
        if (!operitDir.exists() && !operitDir.mkdirs()) {
            AppLogger.w(TAG, "无法创建 Operit 目录，跳过内置资源落地")
            return
        }

        var skills = 0
        copyAssetDir(context, "purge/skills", File(operitDir, "skills")) { _, _ -> skills += 1 }

        val redteamDir = File(operitDir, "redteam")
        copyAssetDir(context, "purge/redteam", redteamDir) { src, dst ->
            if (src.endsWith("rt")) dst.setExecutable(true)
        }

        AppLogger.i(TAG, "内置破甲资源落地完成（技能 $skills 个）")
    }

    private fun copyAssetDir(
        context: Context,
        assetDir: String,
        targetDir: File,
        onFile: (String, File) -> Unit,
    ) {
        val names = context.assets.list(assetDir)?.toList().orEmpty()
        if (names.isEmpty()) return
        for (name in names) {
            val assetPath = "$assetDir/$name"
            val out = File(targetDir, name)
            val children = context.assets.list(assetPath)?.toList().orEmpty()
            if (children.isEmpty()) {
                // 文件
                try {
                    if (out.exists() && out.length() > 0L) {
                        onFile(assetPath, out)
                        continue
                    }
                    out.parentFile?.mkdirs()
                    context.assets.open(assetPath).use { input ->
                        out.outputStream().use { output -> input.copyTo(output) }
                    }
                    onFile(assetPath, out)
                } catch (t: Throwable) {
                    AppLogger.w(TAG, "落地失败: $assetPath -> $out (${t.message})")
                }
            } else {
                copyAssetDir(context, assetPath, out, onFile)
            }
        }
    }
}
