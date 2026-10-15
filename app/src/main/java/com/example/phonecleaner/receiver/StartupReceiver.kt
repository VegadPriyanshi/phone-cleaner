package com.example.phonecleaner.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

import com.example.phonecleaner.MainActivity
import com.example.phonecleaner.core.prefs.AppPrefs


class StartupReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "PhoneCleanerStartup"
    }

    override fun onReceive(
        context: Context,
        intent: Intent
    ) {

        if (intent.action != Intent.ACTION_BOOT_COMPLETED) {
            return
        }

        val enabled =
            AppPrefs.isRunOnStartupEnabled(
                context
            )

        Log.d(
            TAG,
            "BOOT_COMPLETED received. Run on startup = $enabled"
        )

        if (!enabled) {
            return
        }

        val launchIntent =
            Intent(
                context,
                MainActivity::class.java
            ).apply {

                flags =
                    Intent.FLAG_ACTIVITY_NEW_TASK or
                            Intent.FLAG_ACTIVITY_CLEAR_TOP or
                            Intent.FLAG_ACTIVITY_SINGLE_TOP
            }

        context.startActivity(
            launchIntent
        )

        Log.d(
            TAG,
            "Phone Cleaner startup launched"
        )
    }
}