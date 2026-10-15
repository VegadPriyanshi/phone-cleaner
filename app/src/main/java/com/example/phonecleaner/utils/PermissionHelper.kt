package com.example.phonecleaner.utils

import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import androidx.core.content.ContextCompat

object PermissionHelper {

    // =========================================================
    // Photos + Videos
    // =========================================================

    fun hasPhotosAndVideosPermission(context: Context): Boolean {

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {

            val imagesAllowed =
                ContextCompat.checkSelfPermission(
                    context,
                    android.Manifest.permission.READ_MEDIA_IMAGES
                ) == PackageManager.PERMISSION_GRANTED

            val videosAllowed =
                ContextCompat.checkSelfPermission(
                    context,
                    android.Manifest.permission.READ_MEDIA_VIDEO
                ) == PackageManager.PERMISSION_GRANTED

            imagesAllowed && videosAllowed

        } else {

            ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

    // =========================================================
    // Photos permission only
    // =========================================================

    fun hasPhotosPermission(context: Context): Boolean {

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {

            ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.READ_MEDIA_IMAGES
            ) == PackageManager.PERMISSION_GRANTED

        } else {

            ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

    // =========================================================
    // Videos permission only
    // =========================================================

    fun hasVideosPermission(context: Context): Boolean {

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {

            ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.READ_MEDIA_VIDEO
            ) == PackageManager.PERMISSION_GRANTED

        } else {

            ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

    // =========================================================
    // Audio
    // =========================================================

    fun hasAudioPermission(context: Context): Boolean {

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {

            ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.READ_MEDIA_AUDIO
            ) == PackageManager.PERMISSION_GRANTED

        } else {

            ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

    // =========================================================
    // All Files Access
    // =========================================================

    fun hasAllFilesAccess(): Boolean {

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {

            Environment.isExternalStorageManager()

        } else {

            true
        }
    }

    // =========================================================
    // Usage Access
    // =========================================================

    fun hasUsageAccess(context: Context): Boolean {

        val appOpsManager =
            context.getSystemService(Context.APP_OPS_SERVICE)
                    as AppOpsManager

        val mode =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {

                appOpsManager.unsafeCheckOpNoThrow(
                    AppOpsManager.OPSTR_GET_USAGE_STATS,
                    android.os.Process.myUid(),
                    context.packageName
                )

            } else {

                @Suppress("DEPRECATION")
                appOpsManager.checkOpNoThrow(
                    AppOpsManager.OPSTR_GET_USAGE_STATS,
                    android.os.Process.myUid(),
                    context.packageName
                )
            }

        return mode == AppOpsManager.MODE_ALLOWED
    }

    // =========================================================
    // Permission arrays
    // =========================================================

    fun getPhotosAndVideosPermissions(): Array<String> {

        return when {

            Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE -> {

                arrayOf(
                    android.Manifest.permission.READ_MEDIA_IMAGES,
                    android.Manifest.permission.READ_MEDIA_VIDEO,
                    android.Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED
                )
            }

            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> {

                arrayOf(
                    android.Manifest.permission.READ_MEDIA_IMAGES,
                    android.Manifest.permission.READ_MEDIA_VIDEO
                )
            }

            else -> {

                arrayOf(
                    android.Manifest.permission.READ_EXTERNAL_STORAGE
                )
            }
        }
    }

    fun getAudioPermission(): String {

        return if (
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
        ) {

            android.Manifest.permission.READ_MEDIA_AUDIO

        } else {

            android.Manifest.permission.READ_EXTERNAL_STORAGE
        }
    }

    // =========================================================
    // Open All Files Access settings
    // =========================================================

    fun openAllFilesAccessSettings(context: Context) {

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            return
        }

        try {

            val intent = Intent(
                Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION
            ).apply {

                data = Uri.parse(
                    "package:${context.packageName}"
                )
            }

            context.startActivity(intent)

        } catch (_: Exception) {

            val intent = Intent(
                Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION
            )

            context.startActivity(intent)
        }
    }

    // =========================================================
    // Open Usage Access settings
    // =========================================================

    fun openUsageAccessSettings(context: Context) {

        try {

            val intent =
                Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)

            context.startActivity(intent)

        } catch (_: Exception) {
            // Settings page unavailable on this device.
        }
    }
}