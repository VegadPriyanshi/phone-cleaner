package com.example.phonecleaner.utils

import android.content.Context
import com.example.phonecleaner.data.model.AutoCleanResult
import org.json.JSONObject

object AutoCleanResultStorage {

    private const val PREFS_NAME = "auto_clean_result"
    private const val KEY_RESULT = "last_result"

    fun saveResult(
        context: Context,
        result: AutoCleanResult
    ) {
        val json = JSONObject().apply {
            put("timestamp", result.timestamp)
            put("deletedCount", result.deletedCount)
            put("failedCount", result.failedCount)
            put("spaceFreedBytes", result.spaceFreedBytes)
            put("temporaryFilesDeleted", result.temporaryFilesDeleted)
            put("emptyFilesDeleted", result.emptyFilesDeleted)
            put("oldApkFilesDeleted", result.oldApkFilesDeleted)
            put("thumbnailCacheFilesDeleted", result.thumbnailCacheFilesDeleted)
            put("leftoverAppDataDeleted", result.leftoverAppDataDeleted)
        }

        context
            .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_RESULT, json.toString())
            .apply()
    }

    fun getLastResult(
        context: Context
    ): AutoCleanResult? {

        val jsonString = context
            .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_RESULT, null)
            ?: return null

        return try {
            val json = JSONObject(jsonString)

            AutoCleanResult(
                timestamp = json.getLong("timestamp"),
                deletedCount = json.getInt("deletedCount"),
                failedCount = json.getInt("failedCount"),
                spaceFreedBytes = json.getLong("spaceFreedBytes"),
                temporaryFilesDeleted =
                    json.getInt("temporaryFilesDeleted"),
                emptyFilesDeleted =
                    json.getInt("emptyFilesDeleted"),
                oldApkFilesDeleted =
                    json.getInt("oldApkFilesDeleted"),
                thumbnailCacheFilesDeleted =
                    json.getInt("thumbnailCacheFilesDeleted"),
                leftoverAppDataDeleted =
                    json.getInt("leftoverAppDataDeleted")
            )
        } catch (_: Exception) {
            null
        }
    }

    fun clearResult(context: Context) {
        context
            .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .remove(KEY_RESULT)
            .apply()
    }
}