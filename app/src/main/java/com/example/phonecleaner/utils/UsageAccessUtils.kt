package com.example.phonecleaner.utils

import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.os.Process
import android.provider.Settings

object UsageAccessUtils {

    fun hasUsageAccess(context: Context): Boolean {

        val appOpsManager =
            context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager

        val mode = appOpsManager.checkOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            Process.myUid(),
            context.packageName
        )

        return mode == AppOpsManager.MODE_ALLOWED
    }

    fun openUsageAccessSettings(context: Context) {

        val intent = Intent(
            Settings.ACTION_USAGE_ACCESS_SETTINGS
        )

        context.startActivity(intent)
    }
}