package com.example.phonecleaner.ui.home

import android.Manifest
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast

import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment

import com.example.phonecleaner.R
import com.example.phonecleaner.ui.scanning.ScanningFragment
import com.example.phonecleaner.utils.PermissionHelper
import com.example.phonecleaner.utils.StoragePermissionHelper
import com.example.phonecleaner.utils.StorageUtils
import com.google.android.material.button.MaterialButton
import com.google.android.material.progressindicator.LinearProgressIndicator

import java.util.Locale


class HomeFragment : Fragment() {

    private var waitingForAllFilesAccess = false


    // =========================================================
    // MEDIA PERMISSION RESULT
    // =========================================================

    private val mediaPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) {

            if (!isAdded) {
                return@registerForActivityResult
            }


            if (hasAllMediaPermissions()) {

                continuePermissionFlow()

            } else {

                showPermissionMessage()
            }
        }


    // =========================================================
    // CREATE VIEW
    // =========================================================

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        return inflater.inflate(
            R.layout.fragment_home,
            container,
            false
        )
    }


    // =========================================================
    // VIEW CREATED
    // =========================================================

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {

        super.onViewCreated(
            view,
            savedInstanceState
        )


        setupScanButton(
            view
        )


        updateStorageInformation(
            view
        )
    }


    // =========================================================
    // SCAN BUTTON
    // =========================================================

    private fun setupScanButton(
        view: View
    ) {

        val btnScanNow =
            view.findViewById<MaterialButton>(
                R.id.btnScanNow
            )


        btnScanNow.setOnClickListener {

            startPermissionFlow()
        }
    }


    // =========================================================
    // START PERMISSION FLOW
    // =========================================================

    private fun startPermissionFlow() {

        if (!hasAllMediaPermissions()) {

            requestMissingMediaPermissions()

            return
        }


        continuePermissionFlow()
    }


    // =========================================================
    // CONTINUE PERMISSION FLOW
    // =========================================================

    private fun continuePermissionFlow() {

        if (!isAdded) {
            return
        }


        // -----------------------------------------------------
        // ALL FILES ACCESS
        // -----------------------------------------------------

        if (
            Build.VERSION.SDK_INT >=
            Build.VERSION_CODES.R &&
            !StoragePermissionHelper.hasAllFilesAccess()
        ) {

            waitingForAllFilesAccess = true


            StoragePermissionHelper
                .openAllFilesAccessSettings(
                    requireContext()
                )

            return
        }


        // -----------------------------------------------------
        // ALL PERMISSIONS READY
        // -----------------------------------------------------

        openScanningScreen()
    }


    // =========================================================
    // REQUEST MEDIA PERMISSIONS
    // =========================================================

    private fun requestMissingMediaPermissions() {

        val permissions =
            mutableListOf<String>()


        if (
            Build.VERSION.SDK_INT >=
            Build.VERSION_CODES.TIRAMISU
        ) {

            // -------------------------------------------------
            // PHOTOS
            // -------------------------------------------------

            if (
                !PermissionHelper.hasPhotosPermission(
                    requireContext()
                )
            ) {

                permissions.add(
                    Manifest.permission.READ_MEDIA_IMAGES
                )
            }


            // -------------------------------------------------
            // VIDEOS
            // -------------------------------------------------

            if (
                !PermissionHelper.hasVideosPermission(
                    requireContext()
                )
            ) {

                permissions.add(
                    Manifest.permission.READ_MEDIA_VIDEO
                )
            }


            // -------------------------------------------------
            // AUDIO
            // -------------------------------------------------

            if (
                !PermissionHelper.hasAudioPermission(
                    requireContext()
                )
            ) {

                permissions.add(
                    Manifest.permission.READ_MEDIA_AUDIO
                )
            }

        } else {

            // -------------------------------------------------
            // ANDROID 12 AND BELOW
            // -------------------------------------------------

            if (
                !PermissionHelper.hasPhotosPermission(
                    requireContext()
                ) ||
                !PermissionHelper.hasVideosPermission(
                    requireContext()
                ) ||
                !PermissionHelper.hasAudioPermission(
                    requireContext()
                )
            ) {

                permissions.add(
                    Manifest.permission.READ_EXTERNAL_STORAGE
                )
            }
        }


        if (permissions.isNotEmpty()) {

            mediaPermissionLauncher.launch(
                permissions.toTypedArray()
            )

        } else {

            continuePermissionFlow()
        }
    }


    // =========================================================
    // CHECK MEDIA PERMISSIONS
    // =========================================================

    private fun hasAllMediaPermissions(): Boolean {

        val context =
            requireContext()


        return PermissionHelper.hasPhotosPermission(
            context
        ) &&
                PermissionHelper.hasVideosPermission(
                    context
                ) &&
                PermissionHelper.hasAudioPermission(
                    context
                )
    }


    // =========================================================
    // PERMISSION MESSAGE
    // =========================================================

    private fun showPermissionMessage() {

        Toast.makeText(
            requireContext(),
            "All required media permissions are needed to scan your phone.",
            Toast.LENGTH_LONG
        ).show()
    }


    // =========================================================
    // OPEN SCANNING SCREEN
    // =========================================================

    private fun openScanningScreen() {

        waitingForAllFilesAccess = false


        parentFragmentManager
            .beginTransaction()
            .replace(
                R.id.mainFragmentContainer,
                ScanningFragment()
            )
            .addToBackStack(null)
            .commit()
    }


    // =========================================================
    // STORAGE INFORMATION
    // =========================================================

    private fun updateStorageInformation(
        view: View
    ) {

        val storageInfo =
            StorageUtils.getStorageInfo(
                requireContext()
            )


        val totalStorage =
            StorageUtils.formatStorageSize(
                storageInfo.totalBytes
            )


        val usedStorage =
            StorageUtils.formatStorageSize(
                storageInfo.usedBytes
            )


        val freeStorage =
            StorageUtils.formatStorageSize(
                storageInfo.freeBytes
            )


        val storagePercentage =
            storageInfo.usedPercentage


        val tvStoragePercentage =
            view.findViewById<TextView>(
                R.id.tvStoragePercentage
            )


        val tvStorageUsed =
            view.findViewById<TextView>(
                R.id.tvStorageUsed
            )


        val tvStorageInfo =
            view.findViewById<TextView>(
                R.id.tvStorageInfo
            )


        val tvUsedStorage =
            view.findViewById<TextView>(
                R.id.tvUsedStorage
            )


        val tvFreeStorage =
            view.findViewById<TextView>(
                R.id.tvFreeStorage
            )


        val storageProgress =
            view.findViewById<LinearProgressIndicator>(
                R.id.storageProgress
            )


        tvStoragePercentage.text =
            "$storagePercentage%"


        tvStorageUsed.text =
            "$usedStorage used"


        tvStorageInfo.text =
            "$totalStorage total"


        tvUsedStorage.text =
            usedStorage


        tvFreeStorage.text =
            freeStorage


        storageProgress.progress =
            storagePercentage
    }


    // =========================================================
    // RESUME
    // =========================================================

    override fun onResume() {

        super.onResume()


        // -----------------------------------------------------
        // User returned from All Files Access settings
        // -----------------------------------------------------

        if (
            waitingForAllFilesAccess &&
            Build.VERSION.SDK_INT >=
            Build.VERSION_CODES.R &&
            StoragePermissionHelper.hasAllFilesAccess()
        ) {

            waitingForAllFilesAccess = false


            openScanningScreen()

            return
        }


        // -----------------------------------------------------
        // Refresh storage information
        // -----------------------------------------------------

        view?.let { currentView ->

            updateStorageInformation(
                currentView
            )
        }
    }
}