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
import com.example.phonecleaner.data.model.CleanerFile
import com.example.phonecleaner.utils.ScanSummaryPreferences
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder

import java.util.Locale


class ScannedPhotosFragment : Fragment() {

    private lateinit var resultsViewModel: ScanResultsViewModel

    private lateinit var photoAdapter: ScannedPhotoGridAdapter

    private lateinit var cleanSelectedButton: MaterialButton

    private var pendingDeletionPhotos =
        emptyList<CleanerFile>()


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
            R.layout.fragment_scanned_photos,
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


        /*
         * Hide the main bottom navigation while
         * viewing the individual Photos screen.
         */
        (activity as? MainActivity)
            ?.hideBottomNavigation()


        /*
         * Apply BOTH top and bottom system-bar
         * insets.
         *
         * This fixes the back arrow/title being
         * too close to the status bar.
         */
        setupBottomSystemInset(view)


        cleanSelectedButton =
            view.findViewById(
                R.id.btnCleanSelectedPhotos
            )


        updateCleanSelectedButton(0)

        setupCleanSelectedButton()


        resultsViewModel =
            ViewModelProvider(requireActivity())
                .get(
                    ScanResultsViewModel::class.java
                )


        setupBackButton(view)

        setupPhotoGrid(view)

        setupSelectAll(view)

        setupScanAgain(view)

        displayPhotoSummary(view)


        updateEmptyState(
            view,
            resultsViewModel.getScannedFiles()
        )
    }


    private fun setupBackButton(
        view: View
    ) {

        val backButton =
            view.findViewById<ImageButton>(
                R.id.btnScannedPhotosBack
            )


        backButton.setOnClickListener {

            parentFragmentManager
                .popBackStack()
        }
    }


    private fun handleSuccessfulDeletion() {

        val deletedPhotos =
            pendingDeletionPhotos


        if (deletedPhotos.isEmpty()) {
            return
        }


        val deletedUris =
            deletedPhotos
                .map {
                    it.uri
                }
                .toSet()


        val remainingPhotos =
            resultsViewModel
                .getScannedFiles()
                .filterNot {
                    it.uri in deletedUris
                }


        resultsViewModel.updateScannedFiles(
            remainingPhotos
        )


        ScanSummaryPreferences.savePhotoSummary(
            context = requireContext(),
            count = remainingPhotos.size,
            sizeBytes = remainingPhotos.sumOf {
                it.sizeBytes
            }
        )


        photoAdapter.submitList(
            remainingPhotos
        )


        updateEmptyState(
            requireView(),
            remainingPhotos
        )


        pendingDeletionPhotos =
            emptyList()


        displayPhotoSummary(
            requireView()
        )


        updateCleanSelectedButton(0)


        requireView()
            .findViewById<TextView>(
                R.id.tvPhotoSelectionCount
            )
            .text = "0 selected"


        requireView()
            .findViewById<TextView>(
                R.id.tvSelectAll
            )
            .text = "Select all"


        Toast.makeText(
            requireContext(),
            "${deletedPhotos.size} photos deleted",
            Toast.LENGTH_SHORT
        ).show()
    }


    private fun setupCleanSelectedButton() {

        cleanSelectedButton.setOnClickListener {

            val selectedPhotos =
                photoAdapter.getSelectedPhotos()


            if (selectedPhotos.isEmpty()) {
                return@setOnClickListener
            }


            showDeleteConfirmation(
                selectedPhotos
            )
        }
    }


    private fun setupScanAgain(
        view: View
    ) {

        val scanAgainButton =
            view.findViewById<MaterialButton>(
                R.id.btnScanAgain
            )


        scanAgainButton.setOnClickListener {

            parentFragmentManager
                .beginTransaction()
                .replace(
                    R.id.mainFragmentContainer,
                    com.example.phonecleaner.ui.scanning.ScanningFragment()
                )
                .addToBackStack(null)
                .commit()
        }
    }


    private fun updateEmptyState(
        view: View,
        photos: List<CleanerFile>
    ) {

        val photoGrid =
            view.findViewById<RecyclerView>(
                R.id.scannedPhotosGrid
            )


        val emptyState =
            view.findViewById<View>(
                R.id.emptyPhotosState
            )


        val selectionBar =
            view.findViewById<View>(
                R.id.photoSelectionBar
            )


        val cleanButton =
            view.findViewById<MaterialButton>(
                R.id.btnCleanSelectedPhotos
            )


        if (photos.isEmpty()) {

            photoGrid.visibility =
                View.GONE

            emptyState.visibility =
                View.VISIBLE

            selectionBar.visibility =
                View.GONE

            cleanButton.visibility =
                View.GONE

        } else {

            photoGrid.visibility =
                View.VISIBLE

            emptyState.visibility =
                View.GONE

            selectionBar.visibility =
                View.VISIBLE

            cleanButton.visibility =
                View.GONE
        }
    }


    private fun showDeleteConfirmation(
        selectedPhotos: List<CleanerFile>
    ) {

        val selectedCount =
            selectedPhotos.size


        val selectedSize =
            selectedPhotos.sumOf {
                it.sizeBytes
            }


        val message =
            "You are about to delete " +
                    "$selectedCount photos " +
                    "(${formatDataSize(selectedSize)}).\n\n" +
                    "These photos will be permanently " +
                    "deleted from your device."


        MaterialAlertDialogBuilder(
            requireContext()
        )
            .setTitle(
                "Delete selected photos?"
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

                requestPhotoDeletion(
                    selectedPhotos
                )
            }
            .show()
    }


    private fun requestPhotoDeletion(
        selectedPhotos: List<CleanerFile>
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


        pendingDeletionPhotos =
            selectedPhotos


        val uris =
            selectedPhotos.map {
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


    private fun setupPhotoGrid(
        view: View
    ) {

        val recyclerView =
            view.findViewById<RecyclerView>(
                R.id.scannedPhotosGrid
            )


        recyclerView.layoutManager =
            GridLayoutManager(
                requireContext(),
                3
            )


        photoAdapter =
            ScannedPhotoGridAdapter { selectedCount ->

                val selectedText =
                    view.findViewById<TextView>(
                        R.id.tvPhotoSelectionCount
                    )


                val selectAll =
                    view.findViewById<TextView>(
                        R.id.tvSelectAll
                    )


                selectedText.text =
                    "$selectedCount selected"


                selectAll.text =
                    if (
                        photoAdapter.areAllSelected()
                    ) {

                        "Deselect all"

                    } else {

                        "Select all"
                    }


                updateCleanSelectedButton(
                    selectedCount
                )


                /*
                 * Synchronize the Photos selection
                 * with the global selected-file list.
                 */
                syncPhotoSelectionWithGlobalList()
            }


        recyclerView.adapter =
            photoAdapter


        recyclerView.setHasFixedSize(
            true
        )


        recyclerView.itemAnimator =
            null


        // =========================================================
        // Load Photos
        // =========================================================

        val photos =
            resultsViewModel
                .getScannedFiles()


        photoAdapter.submitList(
            photos
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
         * Restore the selected photos visually.
         *
         * This does NOT modify the global list.
         */
        photoAdapter.setSelectedUris(
            globallySelectedUris
        )


        // =========================================================
        // Update UI From Restored Selection
        // =========================================================

        val restoredSelectedCount =
            photoAdapter
                .getSelectedPhotos()
                .size


        view.findViewById<TextView>(
            R.id.tvPhotoSelectionCount
        ).text =
            "$restoredSelectedCount selected"


        view.findViewById<TextView>(
            R.id.tvSelectAll
        ).text =
            if (
                photoAdapter.areAllSelected()
            ) {

                "Deselect all"

            } else {

                "Select all"
            }


        updateCleanSelectedButton(
            restoredSelectedCount
        )
    }


    private fun syncPhotoSelectionWithGlobalList() {

        val photos =
            resultsViewModel
                .getScannedFiles()


        val selectedPhotos =
            photoAdapter
                .getSelectedPhotos()


        val selectedUris =
            selectedPhotos
                .map {
                    it.uri
                }
                .toSet()


        /*
         * Synchronize ONLY Photos.
         *
         * Other categories remain untouched:
         *
         * Screenshots
         * Videos
         * Audio
         * Large Files
         * Junk
         * Documents
         * Duplicate Photos
         */
        photos.forEach { photo ->

            resultsViewModel.setCleanFileSelected(
                file = photo,
                selected =
                    photo.uri in selectedUris
            )
        }
    }


    private fun updateCleanSelectedButton(
        selectedCount: Int
    ) {

        cleanSelectedButton.visibility =
            if (selectedCount > 0) {
                View.VISIBLE
            } else {
                View.GONE
            }


        if (selectedCount > 0) {

            cleanSelectedButton.text =
                "Clean $selectedCount Selected"


            cleanSelectedButton.isEnabled =
                true


            cleanSelectedButton.setTextColor(
                Color.WHITE
            )


            cleanSelectedButton.backgroundTintList =
                ColorStateList.valueOf(
                    Color.rgb(
                        118,
                        85,
                        246
                    )
                )
        }
    }


    /*
     * ============================================================
     * SYSTEM BAR INSETS
     * ============================================================
     *
     * IMPORTANT:
     *
     * The old code only added:
     *
     *     systemBars.bottom
     *
     * Therefore the top header could be drawn underneath
     * the Android status bar.
     *
     * This version adds BOTH:
     *
     *     systemBars.top
     *     systemBars.bottom
     *
     * This moves:
     *
     *     ← Back
     *     Scanned Photos
     *     photo count
     *
     * safely below the status bar.
     */
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


    private fun displayPhotoSummary(
        view: View
    ) {

        val files =
            resultsViewModel.getScannedFiles()


        val totalCount =
            files.size


        val totalSize =
            files.sumOf {
                it.sizeBytes
            }


        val countText =
            view.findViewById<TextView>(
                R.id.tvScannedPhotosCount
            )


        countText.text =
            String.format(
                Locale.US,
                "%,d photos • %s",
                totalCount,
                formatDataSize(totalSize)
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


    private fun setupSelectAll(
        view: View
    ) {

        val selectAll =
            view.findViewById<TextView>(
                R.id.tvSelectAll
            )


        selectAll.setOnClickListener {

            if (
                photoAdapter.areAllSelected()
            ) {

                photoAdapter.clearSelection()

            } else {

                photoAdapter.selectAll()
            }
        }
    }


    override fun onDestroyView() {

        /*
         * Make sure the latest Photos selection
         * is stored globally before leaving
         * the Photos screen.
         */
        if (
            ::photoAdapter.isInitialized
        ) {

            syncPhotoSelectionWithGlobalList()
        }


        /*
         * Restore the main bottom navigation
         * when leaving the Photos detail screen.
         */
        (activity as? MainActivity)
            ?.showBottomNavigation()


        super.onDestroyView()
    }
}