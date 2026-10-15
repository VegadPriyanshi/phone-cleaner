package com.example.phonecleaner.ui.permission

import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.view.View
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.phonecleaner.R
import com.google.android.material.switchmaterial.SwitchMaterial

class AppPermissionsActivity : AppCompatActivity() {

    // =========================================================
    // Switches
    // =========================================================

    private lateinit var switchPhotosVideos: SwitchMaterial
    private lateinit var switchAudio: SwitchMaterial
    private lateinit var switchAllFiles: SwitchMaterial
    private lateinit var switchUsage: SwitchMaterial

    // =========================================================
    // Status TextViews
    // =========================================================

    private lateinit var valuePhotosVideos: TextView
    private lateinit var valueAudio: TextView
    private lateinit var valueAllFiles: TextView
    private lateinit var valueUsage: TextView

    // =========================================================
    // Media permission launcher
    // =========================================================

    private val mediaPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) {
            // Android has returned from the permission dialog.
            // Read the real state again.
            updatePermissionStatus()
        }

    // =========================================================
    // Audio permission launcher
    // =========================================================

    private val audioPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) {
            updatePermissionStatus()
        }

    // =========================================================
    // Activity
    // =========================================================

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_app_permissions)

        // =========================================================
        // Back button
        // =========================================================

        findViewById<View>(R.id.btnBack).setOnClickListener {
            finish()
        }

        // =========================================================
        // Root
        // =========================================================

        val root =
            findViewById<View>(R.id.appPermissionsRoot)

        // =========================================================
        // System bar insets
        // =========================================================

        root.setOnApplyWindowInsetsListener { view, insets ->

            val topInset =
                insets.getSystemWindowInsetTop()

            val bottomInset =
                insets.getSystemWindowInsetBottom()

            view.setPadding(
                view.paddingLeft,
                topInset + dpToPx(8),
                view.paddingRight,
                bottomInset + dpToPx(8)
            )

            insets
        }

        root.requestApplyInsets()

        // =========================================================
        // Find views
        // =========================================================

        switchPhotosVideos =
            findViewById(R.id.switchPhotosVideosPermission)

        switchAudio =
            findViewById(R.id.switchAudioPermission)

        switchAllFiles =
            findViewById(R.id.switchAllFilesPermission)

        switchUsage =
            findViewById(R.id.switchUsagePermission)

        valuePhotosVideos =
            findViewById(R.id.valuePhotosVideosPermission)

        valueAudio =
            findViewById(R.id.valueAudioPermission)

        valueAllFiles =
            findViewById(R.id.valueAllFilesPermission)

        valueUsage =
            findViewById(R.id.valueUsagePermission)

        // =========================================================
        // Switches are status indicators only
        // =========================================================

        switchPhotosVideos.isClickable = false
        switchPhotosVideos.isFocusable = false

        switchAudio.isClickable = false
        switchAudio.isFocusable = false

        switchAllFiles.isClickable = false
        switchAllFiles.isFocusable = false

        switchUsage.isClickable = false
        switchUsage.isFocusable = false

        // =========================================================
        // Permission row clicks
        // =========================================================

        findViewById<View>(
            R.id.rowPhotosVideosPermission
        ).apply {

            isClickable = true
            isFocusable = true

            setOnClickListener {
                requestPhotosAndVideosPermission()
            }
        }

        findViewById<View>(
            R.id.rowAudioPermission
        ).apply {

            isClickable = true
            isFocusable = true

            setOnClickListener {
                requestAudioPermission()
            }
        }

        findViewById<View>(
            R.id.rowAllFilesPermission
        ).setOnClickListener {

            openAllFilesAccessSettings()
        }

        findViewById<View>(
            R.id.rowUsagePermission
        ).setOnClickListener {

            openUsageAccessSettings()
        }
    }

    // =============================================================
    // Refresh whenever screen becomes visible
    // =============================================================

    override fun onResume() {
        super.onResume()

        updatePermissionStatus()
    }

    // =============================================================
    // Update all permission states
    // =============================================================

    private fun updatePermissionStatus() {

        updatePhotosAndVideosPermission()

        updateAudioPermission()

        updateAllFilesPermission()

        updateUsagePermission()
    }

    // =============================================================
    // Photos + Videos status
    // =============================================================

    private fun updatePhotosAndVideosPermission() {

        when (getPhotosVideosAccessState()) {

            MediaAccessState.ALLOWED -> {

                switchPhotosVideos.isChecked = true

                valuePhotosVideos.text = "Allowed"

                valuePhotosVideos.setTextColor(
                    ContextCompat.getColor(
                        this,
                        android.R.color.holo_green_light
                    )
                )
            }

            MediaAccessState.LIMITED -> {

                switchPhotosVideos.isChecked = true

                valuePhotosVideos.text = "Limited access"

                valuePhotosVideos.setTextColor(
                    ContextCompat.getColor(
                        this,
                        android.R.color.holo_orange_light
                    )
                )
            }

            MediaAccessState.NOT_ALLOWED -> {

                switchPhotosVideos.isChecked = false

                valuePhotosVideos.text = "Not allowed"

                valuePhotosVideos.setTextColor(
                    ContextCompat.getColor(
                        this,
                        android.R.color.holo_orange_light
                    )
                )
            }
        }
    }

    // =============================================================
    // Audio status
    // =============================================================

    private fun updateAudioPermission() {

        val allowed =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {

                ContextCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.READ_MEDIA_AUDIO
                ) == PackageManager.PERMISSION_GRANTED

            } else {

                ContextCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.READ_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED
            }

        switchAudio.isChecked = allowed

        if (allowed) {

            valueAudio.text = "Allowed"

            valueAudio.setTextColor(
                ContextCompat.getColor(
                    this,
                    android.R.color.holo_green_light
                )
            )

        } else {

            valueAudio.text = "Not allowed"

            valueAudio.setTextColor(
                ContextCompat.getColor(
                    this,
                    android.R.color.holo_orange_light
                )
            )
        }
    }

    // =============================================================
    // All Files Access status
    // =============================================================

    private fun updateAllFilesPermission() {

        val allowed =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {

                Environment.isExternalStorageManager()

            } else {

                ContextCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.READ_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED
            }

        switchAllFiles.isChecked = allowed

        if (allowed) {

            valueAllFiles.text = "Allowed"

            valueAllFiles.setTextColor(
                ContextCompat.getColor(
                    this,
                    android.R.color.holo_green_light
                )
            )

        } else {

            valueAllFiles.text = "Not allowed"

            valueAllFiles.setTextColor(
                ContextCompat.getColor(
                    this,
                    android.R.color.holo_orange_light
                )
            )
        }
    }

    // =============================================================
    // Usage Access status
    // =============================================================

    private fun updateUsagePermission() {

        val allowed = hasUsageAccess()

        switchUsage.isChecked = allowed

        if (allowed) {

            valueUsage.text = "Allowed"

            valueUsage.setTextColor(
                ContextCompat.getColor(
                    this,
                    android.R.color.holo_green_light
                )
            )

        } else {

            valueUsage.text = "Not allowed"

            valueUsage.setTextColor(
                ContextCompat.getColor(
                    this,
                    android.R.color.holo_orange_light
                )
            )
        }
    }

    // =============================================================
    // Request Photos + Videos permission
    // =============================================================
    private fun openAppPermissionSettings() {

        try {

            val intent = Intent(
                Settings.ACTION_APPLICATION_DETAILS_SETTINGS
            ).apply {

                data = Uri.parse(
                    "package:$packageName"
                )
            }

            startActivity(intent)

        } catch (_: Exception) {

            // Fallback: open general app settings
            try {

                startActivity(
                    Intent(
                        Settings.ACTION_SETTINGS
                    )
                )

            } catch (_: Exception) {
            }
        }
    }

    private fun requestPhotosAndVideosPermission() {

        val state = getPhotosVideosAccessState()

        if (state == MediaAccessState.ALLOWED ||
            state == MediaAccessState.LIMITED
        ) {
            openAppPermissionSettings()
            return
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {

                mediaPermissionLauncher.launch(
                    arrayOf(
                        android.Manifest.permission.READ_MEDIA_IMAGES,
                        android.Manifest.permission.READ_MEDIA_VIDEO,
                        android.Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED
                    )
                )

            } else {

                mediaPermissionLauncher.launch(
                    arrayOf(
                        android.Manifest.permission.READ_MEDIA_IMAGES,
                        android.Manifest.permission.READ_MEDIA_VIDEO
                    )
                )
            }

        } else {

            mediaPermissionLauncher.launch(
                arrayOf(
                    android.Manifest.permission.READ_EXTERNAL_STORAGE
                )
            )
        }
    }
    // =============================================================
    // Old Android storage permission
    // =============================================================

    private fun audioOrStoragePermissionForOldAndroid() {

        if (
            ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {

            mediaPermissionLauncher.launch(
                arrayOf(
                    android.Manifest.permission.READ_EXTERNAL_STORAGE
                )
            )

        } else {

            updatePermissionStatus()
        }
    }

    // =============================================================
    // Request Audio permission
    // =============================================================

    private fun requestAudioPermission() {

        val allowed =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {

                ContextCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.READ_MEDIA_AUDIO
                ) == PackageManager.PERMISSION_GRANTED

            } else {

                ContextCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.READ_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED
            }

        if (allowed) {

            openAppPermissionSettings()
            return
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {

            audioPermissionLauncher.launch(
                android.Manifest.permission.READ_MEDIA_AUDIO
            )

        } else {

            audioPermissionLauncher.launch(
                android.Manifest.permission.READ_EXTERNAL_STORAGE
            )
        }
    }
    // =============================================================
    // Open All Files Access settings
    // =============================================================

    private fun openAllFilesAccessSettings() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {

            try {

                val intent = Intent(
                    Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION
                ).apply {

                    data = Uri.parse(
                        "package:$packageName"
                    )
                }

                startActivity(intent)

            } catch (_: Exception) {

                val intent =
                    Intent(
                        Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION
                    )

                startActivity(intent)
            }

        } else {

            requestPhotosAndVideosPermission()
        }
    }

    // =============================================================
    // Open Usage Access settings
    // =============================================================

    private fun openUsageAccessSettings() {

        try {

            val intent =
                Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)

            startActivity(intent)

        } catch (_: Exception) {
            // Some devices may not expose this settings page.
        }
    }

    // =============================================================
    // Check Photos + Videos
    // =============================================================

    private fun getPhotosVideosAccessState(): MediaAccessState {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {

            val imagesAllowed =
                ContextCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.READ_MEDIA_IMAGES
                ) == PackageManager.PERMISSION_GRANTED

            val videosAllowed =
                ContextCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.READ_MEDIA_VIDEO
                ) == PackageManager.PERMISSION_GRANTED

            if (imagesAllowed && videosAllowed) {

                return MediaAccessState.ALLOWED
            }

            /*
             * Android 14+
             *
             * User may have granted selected/limited media access.
             */

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {

                val selectedAccess =
                    ContextCompat.checkSelfPermission(
                        this,
                        android.Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED
                    ) == PackageManager.PERMISSION_GRANTED

                if (selectedAccess) {

                    return MediaAccessState.LIMITED
                }
            }

            return MediaAccessState.NOT_ALLOWED
        }

        val storageAllowed =
            ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED

        return if (storageAllowed) {

            MediaAccessState.ALLOWED

        } else {

            MediaAccessState.NOT_ALLOWED
        }
    }

    // =============================================================
    // Check Usage Access
    // =============================================================

    private fun hasUsageAccess(): Boolean {

        val appOpsManager =
            getSystemService(Context.APP_OPS_SERVICE)
                    as AppOpsManager

        val mode =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {

                appOpsManager.unsafeCheckOpNoThrow(
                    AppOpsManager.OPSTR_GET_USAGE_STATS,
                    android.os.Process.myUid(),
                    packageName
                )

            } else {

                @Suppress("DEPRECATION")
                appOpsManager.checkOpNoThrow(
                    AppOpsManager.OPSTR_GET_USAGE_STATS,
                    android.os.Process.myUid(),
                    packageName
                )
            }

        return mode == AppOpsManager.MODE_ALLOWED
    }

    // =============================================================
    // dp helper
    // =============================================================

    private fun dpToPx(dp: Int): Int {

        return (
                dp * resources.displayMetrics.density
                ).toInt()
    }

    // =============================================================
    // Media permission state
    // =============================================================

    private enum class MediaAccessState {

        ALLOWED,

        LIMITED,

        NOT_ALLOWED
    }
}