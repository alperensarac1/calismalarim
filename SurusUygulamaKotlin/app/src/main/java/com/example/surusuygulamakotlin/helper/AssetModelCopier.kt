package com.example.surusuygulamakotlin.helper


import android.content.Context
import java.io.File
import java.io.FileOutputStream

object AssetModelCopier {

    fun copyAssetFolder(context: Context, assetFolder: String, outDirName: String): File {
        val outDir = File(context.filesDir, outDirName)
        if (outDir.exists() && outDir.isDirectory && outDir.list()?.isNotEmpty() == true) {
            return outDir
        }
        if (!outDir.exists()) outDir.mkdirs()

        copyAssetsRecursively(context, assetFolder, outDir)
        return outDir
    }

    private fun copyAssetsRecursively(context: Context, assetPath: String, outDir: File) {
        val am = context.assets
        val items = am.list(assetPath) ?: return

        for (name in items) {
            val childAssetPath = if (assetPath.isEmpty()) name else "$assetPath/$name"
            val childItems = am.list(childAssetPath)

            if (childItems != null && childItems.isNotEmpty()) {
                val childOutDir = File(outDir, name)
                if (!childOutDir.exists()) childOutDir.mkdirs()
                copyAssetsRecursively(context, childAssetPath, childOutDir)
            } else {
                val outFile = File(outDir, name)
                if (outFile.exists() && outFile.length() > 0) continue

                am.open(childAssetPath).use { input ->
                    FileOutputStream(outFile).use { output ->
                        input.copyTo(output)
                    }
                }
            }
        }
    }
}
