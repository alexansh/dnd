package com.grimoire.dnd.data

import android.content.Context
import java.io.File

object AssetBundleManager {
    fun getExpansionDirectory(context: Context): File {
        val extDir = context.getExternalFilesDir("assets") ?: context.filesDir
        if (!extDir.exists()) {
            extDir.mkdirs()
        }
        return extDir
    }

    fun getInstalledAssetPacks(context: Context): List<String> {
        val dir = getExpansionDirectory(context)
        return dir.listFiles()?.map { it.name } ?: emptyList()
    }

    fun getTotalInstalledAssetSizeBytes(context: Context): Long {
        val dir = getExpansionDirectory(context)
        return dir.walkTopDown().filter { it.isFile }.map { it.length() }.sum()
    }
}
