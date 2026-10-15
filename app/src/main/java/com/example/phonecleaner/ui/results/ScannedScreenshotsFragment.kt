package com.example.phonecleaner.ui.results

import android.app.Activity
import android.content.res.ColorStateList
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.phonecleaner.MainActivity
import com.example.phonecleaner.R
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.util.Locale

class ScannedScreenshotsFragment : Fragment() {

    private lateinit var resultsViewModel: ScanResultsViewModel

    private lateinit var screenshotAdapter: ScannedPhotoGridAdapter

    private lateinit var cleanSelectedButton: MaterialButton

    private var pendingDeletionScreenshots =
        emptyList<com.example.phonecleaner.data.model.CleanerFile>()

    private val deleteRequestLauncher =
        registerForActivityResult(
            ActivityResultContracts.StartIntentSenderForResult()
        ) { result ->

            if (result.resultCode == Activity.RESULT_OK) {
                handleSuccessfulDeletion()
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        return inflater.inflate(
            R.layout.fragment_scanned_screenshots,
            container,
            false
        )
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(
            view,
            savedInstanceState
        )

        (activity as? MainActivity)
            ?.hideBottomNavigation()

        setupBottomSystemInset(view)

        resultsViewModel =
            ViewModelProvider(requireActivity())
                .get(
                    ScanResultsViewModel::class.java
                )

        cleanSelectedButton =
            view.findViewById(
                R.id.btnCleanSelectedScreenshots
            )

        setupBackButton(view)

        setupScreenshotGrid(view)

        setupSelectAll(view)

        setupCleanSelectedButton()

        displayScreenshotSummary(view)

        updateCleanSelectedButton(0)

        updateEmptyState(
            view,
            resultsViewModel.getScreenshotFiles()
        )
    }

    private fun setupBackButton(
        view: View
    ) {

        val backButton =
            view.findViewById<ImageButton>(
                R.id.btnScannedScreenshotsBack
            )

        backButton.setOnClickListener {

            parentFragmentManager
                .popBackStack()
        }
    }

    private fun setupScreenshotGrid(
        view: View
    ) {

        val recyclerView =
            view.findViewById<RecyclerView>(
                R.id.scannedScreenshotsGrid
            )

        recyclerView.layoutManager =
            GridLayoutManager(
                requireContext(),
                3
            )

        screenshotAdapter =
            ScannedPhotoGridAdapter { selectedCount ->

                val selectedText =
                    view.findViewById<TextView>(
                        R.id.tvScreenshotSelectionCount
                    )

                val selectAll =
                    view.findViewById<TextView>(
                        R.id.tvSelectAllScreenshots
                    )

                selectedText.text =
                    "$selectedCount selected"

                selectAll.text =
                    if (
                        screenshotAdapter.areAllSelected()
                    ) {

                        "Deselect all"

                    } else {

                        "Select all"
                    }

                updateCleanSelectedButton(
                    selectedCount
                )

                /*
                 * IMPORTANT:
                 *
                 * This callback is triggered when the
                 * USER selects/unselects a screenshot.
                 *
                 * Sync that real user change with
                 * the global selectedCleanFiles list.
                 */
                syncScreenshotSelectionWithGlobalList()
            }

        recyclerView.adapter =
            screenshotAdapter

        recyclerView.setHasFixedSize(
            true
        )

        recyclerView.itemAnimator =
            null


        // =========================================================
        // Load Screenshots
        // =========================================================

        val screenshots =
            resultsViewModel
                .getScreenshotFiles()

        screenshotAdapter.submitList(
            screenshots
        )


        // =========================================================
        // Restore Global Selection
        // =========================================================

        val globallySelectedUris =
            resultsViewModel
                .getSelectedCleanFiles()
                .map {
                    it.uri
                }
                .toSet()

        /*
         * Restore previously selected screenshots.
         *
         * This only changes the adapter UI.
         *
         * It does NOT modify the global selection.
         */
        screenshotAdapter.setSelectedUris(
            globallySelectedUris
        )


        // =========================================================
        // Update UI From Restored Selection
        // =========================================================

        val restoredSelectedCount =
            screenshotAdapter
                .getSelectedPhotos()
                .size

        view.findViewById<TextView>(
            R.id.tvScreenshotSelectionCount
        ).text =
            "$restoredSelectedCount selected"

        view.findViewById<TextView>(
            R.id.tvSelectAllScreenshots
        ).text =
            if (
                screenshotAdapter.areAllSelected()
            ) {

                "Deselect all"

            } else {

                "Select all"
            }

        updateCleanSelectedButton(
            restoredSelectedCount
        )
    }




    private fun syncScreenshotSelectionWithGlobalList() {

        val screenshots =
            resultsViewModel
                .getScreenshotFiles()

        val selectedScreenshots =
            screenshotAdapter
                .getSelectedPhotos()

        val selectedUris =
            selectedScreenshots
                .map {
                    it.uri
                }
                .toSet()

        /*
         * Synchronize ONLY screenshot files.
         *
         * Photos, Videos, Audio, etc.
         * remain untouched.
         */
        screenshots.forEach { screenshot ->

            resultsViewModel.setCleanFileSelected(
                file = screenshot,
                selected =
                    screenshot.uri in selectedUris
            )
        }
    }
    private fun setupSelectAll(
        view: View
    ) {

        val selectAll =
            view.findViewById<TextView>(
                R.id.tvSelectAllScreenshots
            )

        selectAll.setOnClickListener {

            if (
                screenshotAdapter.areAllSelected()
            ) {

                screenshotAdapter.clearSelection()

            } else {

                screenshotAdapter.selectAll()
            }
        }
    }

    private fun setupCleanSelectedButton() {

        cleanSelectedButton.setOnClickListener {

            val selectedScreenshots =
                screenshotAdapter
                    .getSelectedPhotos()

            if (
                selectedScreenshots.isEmpty()
            ) {
                return@setOnClickListener
            }

            showDeleteConfirmation(
                selectedScreenshots
            )
        }
    }

    private fun showDeleteConfirmation(
        selectedScreenshots:
        List<com.example.phonecleaner.data.model.CleanerFile>
    ) {

        val selectedCount =
            selectedScreenshots.size

        val selectedSize =
            selectedScreenshots.sumOf {
                it.sizeBytes
            }

        val message =
            "You are about to delete " +
                    "$selectedCount screenshots " +
                    "(${formatDataSize(selectedSize)}).\n\n" +
                    "These screenshots will be permanently " +
                    "deleted from your device."

        MaterialAlertDialogBuilder(
            requireContext()
        )
            .setTitle(
                "Delete selected screenshots?"
            )
            .setMessage(
                message
            )
            .setNegativeButton(
                "Cancel",
                null
            )
            .setPositiveButton(
                "Continue"
            ) { _, _ ->

                requestScreenshotDeletion(
                    selectedScreenshots
                )
            }
            .show()
    }

    private fun requestScreenshotDeletion(
        selectedScreenshots:
        List<com.example.phonecleaner.data.model.CleanerFile>
    ) {

        if (
            Build.VERSION.SDK_INT <
            Build.VERSION_CODES.R
        ) {

            Toast.makeText(
                requireContext(),
                "Deletion confirmation requires Android 11 or newer.",
                Toast.LENGTH_LONG
            ).show()

            return
        }

        pendingDeletionScreenshots =
            selectedScreenshots

        val uris =
            selectedScreenshots.map {
                Uri.parse(it.uri)
            }

        if (uris.isEmpty()) {
            return
        }

        val deleteRequest =
            MediaStore.createDeleteRequest(
                requireContext().contentResolver,
                uris
            )

        val intentSenderRequest =
            IntentSenderRequest.Builder(
                deleteRequest.intentSender
            ).build()

        deleteRequestLauncher.launch(
            intentSenderRequest
        )
    }

    private fun handleSuccessfulDeletion() {

        val deletedScreenshots =
            pendingDeletionScreenshots

        if (
            deletedScreenshots.isEmpty()
        ) {
            return
        }

        val deletedUris =
            deletedScreenshots
                .map {
                    it.uri
                }
                .toSet()

        val remainingScreenshots =
            resultsViewModel
                .getScreenshotFiles()
                .filterNot {
                    it.uri in deletedUris
                }

        resultsViewModel.updateScreenshotFiles(
            remainingScreenshots
        )

        screenshotAdapter.submitList(
            remainingScreenshots
        )

        pendingDeletionScreenshots =
            emptyList()

        displayScreenshotSummary(
            requireView()
        )

        updateEmptyState(
            requireView(),
            remainingScreenshots
        )

        updateCleanSelectedButton(
            0
        )

        requireView()
            .findViewById<TextView>(
                R.id.tvScreenshotSelectionCount
            )
            .text = "0 selected"

        requireView()
            .findViewById<TextView>(
                R.id.tvSelectAllScreenshots
            )
            .text = "Select all"

        Toast.makeText(
            requireContext(),
            "${deletedScreenshots.size} screenshots deleted",
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun updateCleanSelectedButton(
        selectedCount: Int
    ) {

        if (selectedCount > 0) {

            cleanSelectedButton.visibility =
                View.VISIBLE

            cleanSelectedButton.isEnabled =
                true

            cleanSelectedButton.text =
                "Clean $selectedCount Selected"

            cleanSelectedButton.setTextColor(
                resources.getColor(
                    android.R.color.white,
                    requireContext().theme
                )
            )

            cleanSelectedButton.backgroundTintList =
                ColorStateList.valueOf(
                    Color.rgb(
                        118,
                        85,
                        246
                    )
                )

        } else {

            cleanSelectedButton.visibility =
                View.GONE
        }
    }

    private fun displayScreenshotSummary(
        view: View
    ) {

        val screenshots =
            resultsViewModel.getScreenshotFiles()

        val countText =
            view.findViewById<TextView>(
                R.id.tvScannedScreenshotsCount
            )

        countText.text =
            String.format(
                Locale.US,
                "%,d screenshots",
                screenshots.size
            )
    }

    private fun updateEmptyState(
        view: View,
        screenshots:
        List<com.example.phonecleaner.data.model.CleanerFile>
    ) {

        val screenshotGrid =
            view.findViewById<RecyclerView>(
                R.id.scannedScreenshotsGrid
            )

        val emptyState =
            view.findViewById<View>(
                R.id.emptyScreenshotsState
            )

        val selectionBar =
            view.findViewById<View>(
                R.id.screenshotSelectionBar
            )

        val cleanButton =
            view.findViewById<MaterialButton>(
                R.id.btnCleanSelectedScreenshots
            )

        if (screenshots.isEmpty()) {

            screenshotGrid.visibility =
                View.GONE

            emptyState.visibility =
                View.VISIBLE

            selectionBar.visibility =
                View.GONE

            cleanButton.visibility =
                View.GONE

        } else {

            screenshotGrid.visibility =
                View.VISIBLE

            emptyState.visibility =
                View.GONE

            selectionBar.visibility =
                View.VISIBLE

            cleanButton.visibility =
                View.GONE
        }
    }

    private fun setupBottomSystemInset(
        view: View
    ) {

        val initialLeftPadding =
            view.paddingLeft

        val initialTopPadding =
            view.paddingTop

        val initialRightPadding =
            view.paddingRight

        val initialBottomPadding =
            view.paddingBottom

        ViewCompat.setOnApplyWindowInsetsListener(
            view
        ) { v, insets ->

            val systemBars =
                insets.getInsets(
                    WindowInsetsCompat.Type.systemBars()
                )

            v.setPadding(
                initialLeftPadding,
                initialTopPadding +
                        systemBars.top,
                initialRightPadding,
                initialBottomPadding +
                        systemBars.bottom
            )

            insets
        }

        ViewCompat.requestApplyInsets(
            view
        )
    }

    private fun formatDataSize(
        bytes: Long
    ): String {

        val mb =
            bytes / 1_000_000.0

        val gb =
            bytes / 1_000_000_000.0

        return when {

            gb >= 1.0 -> {

                String.format(
                    Locale.US,
                    "%.2f GB",
                    gb
                )
            }

            mb >= 1.0 -> {

                String.format(
                    Locale.US,
                    "%.1f MB",
                    mb
                )
            }

            bytes > 0 -> {

                String.format(
                    Locale.US,
                    "%.0f KB",
                    bytes / 1000.0
                )
            }

            else -> {

                "0 MB"
            }
        }
    }

    override fun onDestroyView() {

        /*
         * Save the latest screenshot selection
         * before leaving the screen.
         */
        if (
            ::screenshotAdapter.isInitialized
        ) {

            syncScreenshotSelectionWithGlobalList()
        }

        (activity as? MainActivity)
            ?.showBottomNavigation()

        super.onDestroyView()
    }
}