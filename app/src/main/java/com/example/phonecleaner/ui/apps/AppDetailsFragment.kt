package com.example.phonecleaner.ui.apps

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.phonecleaner.R
import com.example.phonecleaner.utils.StorageUtils
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.activity.result.contract.ActivityResultContracts

class AppDetailsFragment : Fragment(R.layout.fragment_app_details) {


    private val uninstallLauncher =
        registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->

            if (result.resultCode == android.app.Activity.RESULT_OK) {

                /*
                 * RESULT_OK only means the user confirmed the
                 * uninstall in the system dialog. The actual
                 * package removal happens asynchronously, so we
                 * must wait until PackageManager no longer knows
                 * about it before refreshing the apps list.
                 * Refreshing immediately here was the cause of
                 * the uninstalled app still showing up and the
                 * apps count not decreasing.
                 */

                val packageName =
                    arguments?.getString(ARG_PACKAGE_NAME)

                if (packageName.isNullOrEmpty()) {

                    parentFragmentManager.setFragmentResult(
                        "refresh_apps",
                        Bundle()
                    )

                    parentFragmentManager.popBackStack()

                } else {

                    waitForUninstallToComplete(packageName)
                }
            }
        }

    // ---------------------------------------------------------
    // Wait for the package to actually be removed before
    // refreshing the apps list, instead of refreshing on the
    // same frame as RESULT_OK (which races the real uninstall).
    // ---------------------------------------------------------

    private fun waitForUninstallToComplete(
        packageName: String,
        attemptsLeft: Int = 10
    ) {

        view?.postDelayed({

            if (!isAdded) {
                return@postDelayed
            }

            val stillInstalled = try {

                requireContext()
                    .packageManager
                    .getApplicationInfo(packageName, 0)

                true

            } catch (_: Exception) {

                false
            }

            if (!stillInstalled || attemptsLeft <= 0) {

                parentFragmentManager.setFragmentResult(
                    "refresh_apps",
                    Bundle()
                )

                parentFragmentManager.popBackStack()

            } else {

                waitForUninstallToComplete(
                    packageName,
                    attemptsLeft - 1
                )
            }

        }, 300)
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)

        val packageName =
            arguments?.getString(ARG_PACKAGE_NAME)

        if (packageName.isNullOrEmpty()) {
            parentFragmentManager.popBackStack()
            return
        }

        // Load the selected app's actual information.
        loadAppDetails(
            view = view,
            packageName = packageName
        )

        setupBackButton(view)

        setupOpenAppButton(
            view = view,
            packageName = packageName
        )

        setupAppInfoButton(
            view = view,
            packageName = packageName
        )

        setupUninstallButton(
            view = view,
            packageName = packageName
        )
    }


    override fun onResume() {
        super.onResume()

        val packageName =
            arguments?.getString(ARG_PACKAGE_NAME)

        if (packageName.isNullOrEmpty()) {
            return
        }

        view?.postDelayed({

            if (!isAdded) {
                return@postDelayed
            }

            try {
                requireContext()
                    .packageManager
                    .getApplicationInfo(packageName, 0)

            } catch (_: Exception) {

                parentFragmentManager.setFragmentResult(
                    "refresh_apps",
                    Bundle()
                )

                parentFragmentManager.popBackStack()
            }

        }, 1000)
    }

    // ---------------------------------------------------------
    // Back Button
    // ---------------------------------------------------------

    private fun setupBackButton(view: View) {

        view.findViewById<View>(
            R.id.btnBack
        ).setOnClickListener {

            /*
             * Just navigating back should NOT trigger a full
             * rescan of installed apps. A rescan is only needed
             * when something actually changed (an app was
             * uninstalled), which is already handled separately
             * via waitForUninstallToComplete() and the onResume()
             * check below. Sending "refresh_apps" here was
             * forcing an unnecessary rescan on every back press.
             */

            parentFragmentManager.popBackStack()
        }
    }

    // ---------------------------------------------------------
    // Open App
    // ---------------------------------------------------------

    private fun setupOpenAppButton(
        view: View,
        packageName: String
    ) {

        val openAppButton =
            view.findViewById<View>(
                R.id.btnOpenApp
            )

        openAppButton.setOnClickListener {

            openSelectedApp(packageName)
        }
    }

    private fun openSelectedApp(
        packageName: String
    ) {

        val packageManager =
            requireContext().packageManager

        val launchIntent =
            packageManager.getLaunchIntentForPackage(
                packageName
            )

        if (launchIntent != null) {

            launchIntent.addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK
            )

            startActivity(launchIntent)

        } else {

            Toast.makeText(
                requireContext(),
                "This app cannot be opened.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    // ---------------------------------------------------------
    // App Info
    // ---------------------------------------------------------

    private fun setupAppInfoButton(
        view: View,
        packageName: String
    ) {

        val appInfoButton =
            view.findViewById<View>(
                R.id.btnAppInfo
            )

        appInfoButton.setOnClickListener {

            openAppInfo(packageName)
        }
    }

    private fun openAppInfo(
        packageName: String
    ) {

        val intent = Intent(
            Settings.ACTION_APPLICATION_DETAILS_SETTINGS
        ).apply {

            data = Uri.parse(
                "package:$packageName"
            )
        }

        startActivity(intent)
    }

    // ---------------------------------------------------------
    // Uninstall
    // ---------------------------------------------------------

    private fun setupUninstallButton(
        view: View,
        packageName: String
    ) {

        val uninstallButton =
            view.findViewById<View>(
                R.id.btnUninstallApp
            )

        uninstallButton.setOnClickListener {

            uninstallApp(packageName)
        }
    }

    private fun uninstallApp(packageName: String) {

        val intent = Intent(
            Intent.ACTION_UNINSTALL_PACKAGE
        ).apply {
            data = Uri.parse("package:$packageName")
            putExtra(
                Intent.EXTRA_RETURN_RESULT,
                true
            )
        }

        uninstallLauncher.launch(intent)
    }

    // ---------------------------------------------------------
    // Load App Details
    // ---------------------------------------------------------

    private fun loadAppDetails(
        view: View,
        packageName: String
    ) {

        val packageManager =
            requireContext().packageManager

        val applicationInfo = try {

            packageManager.getApplicationInfo(
                packageName,
                0
            )

        } catch (_: Exception) {

            return
        }

        // Get actual app name
        val appName =
            packageManager
                .getApplicationLabel(applicationInfo)
                .toString()

        // Get actual app icon
        val appIcon =
            packageManager.getApplicationIcon(
                applicationInfo
            )

        // Get actual values passed from InstalledApp
        val sizeBytes =
            arguments?.getLong(
                ARG_SIZE_BYTES,
                0L
            ) ?: 0L

        val lastUsedTime =
            arguments?.getLong(
                ARG_LAST_USED_TIME,
                0L
            ) ?: 0L

        val isSystemApp =
            arguments?.getBoolean(
                ARG_IS_SYSTEM_APP,
                false
            ) ?: false

        // -----------------------------------------------------
        // Update UI
        // -----------------------------------------------------

        view.findViewById<ImageView>(
            R.id.ivAppIcon
        ).setImageDrawable(
            appIcon
        )

        view.findViewById<TextView>(
            R.id.tvAppName
        ).text =
            appName

        view.findViewById<TextView>(
            R.id.tvPackageName
        ).text =
            packageName

        view.findViewById<TextView>(
            R.id.tvAppSize
        ).text =
            StorageUtils.formatStorageSize(
                sizeBytes
            )

        view.findViewById<TextView>(
            R.id.tvLastUsed
        ).text =
            formatLastUsed(
                lastUsedTime
            )

        view.findViewById<TextView>(
            R.id.tvAppType
        ).text =
            if (isSystemApp) {
                "System App"
            } else {
                "User App"
            }
    }

    // ---------------------------------------------------------
    // Last Used
    // ---------------------------------------------------------

    private fun formatLastUsed(
        timestamp: Long
    ): String {

        if (timestamp <= 0L) {
            return "Never"
        }

        val dateFormat =
            SimpleDateFormat(
                "dd MMM yyyy",
                Locale.getDefault()
            )

        return dateFormat.format(
            Date(timestamp)
        )
    }

    // ---------------------------------------------------------
    // Arguments
    // ---------------------------------------------------------

    companion object {

        private const val ARG_PACKAGE_NAME =
            "package_name"

        private const val ARG_SIZE_BYTES =
            "size_bytes"

        private const val ARG_LAST_USED_TIME =
            "last_used_time"

        private const val ARG_IS_SYSTEM_APP =
            "is_system_app"

        fun newInstance(
            packageName: String,
            sizeBytes: Long,
            lastUsedTime: Long,
            isSystemApp: Boolean
        ): AppDetailsFragment {

            return AppDetailsFragment().apply {

                arguments = Bundle().apply {

                    putString(
                        ARG_PACKAGE_NAME,
                        packageName
                    )

                    putLong(
                        ARG_SIZE_BYTES,
                        sizeBytes
                    )

                    putLong(
                        ARG_LAST_USED_TIME,
                        lastUsedTime
                    )

                    putBoolean(
                        ARG_IS_SYSTEM_APP,
                        isSystemApp
                    )
                }
            }
        }
    }
}