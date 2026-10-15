package com.example.phonecleaner.ui.results

import android.app.Activity
import android.app.AlertDialog
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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

import com.example.phonecleaner.MainActivity
import com.example.phonecleaner.R
import com.example.phonecleaner.data.model.CleanerFile

import java.util.Locale

import com.example.phonecleaner.utils.ScanSummaryPreferences


class DuplicatePhotosFragment : Fragment() {

    private lateinit var resultsViewModel: ScanResultsViewModel

    private lateinit var duplicatePhotoAdapter: DuplicatePhotoGroupAdapter
    private lateinit var recyclerDuplicatePhotos: RecyclerView

    private lateinit var tvDuplicatePhotosSummary: TextView
    private lateinit var tvDuplicatePhotoSelectionCount: TextView
    private lateinit var tvDuplicatePhotoSelectAll: TextView
    private lateinit var emptyDuplicatePhotosState: View

    private lateinit var cleanSelectedButton:
            com.google.android.material.button.MaterialButton

    private var pendingDeletionPhotos:
            List<CleanerFile> = emptyList()


    private val deleteRequestLauncher =
        registerForActivityResult(
            ActivityResultContracts.StartIntentSenderForResult()
        ) { result ->

            if (
                result.resultCode ==
                Activity.RESULT_OK
            ) {

                handleSuccessfulDeletion()
            }
        }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        return inflater.inflate(
            R.layout.fragment_duplicate_photos,
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

        setupViews(view)
        setupBackButton(view)
        setupRecyclerView()
        setupSelectAll()
        setupCleanSelectedButton()

        displayDuplicatePhotos()
    }


    private fun setupViews(
        view: View
    ) {

        recyclerDuplicatePhotos =
            view.findViewById(
                R.id.recyclerDuplicatePhotos
            )

        tvDuplicatePhotosSummary =
            view.findViewById(
                R.id.tvDuplicatePhotosSummary
            )

        tvDuplicatePhotoSelectionCount =
            view.findViewById(
                R.id.tvDuplicatePhotoSelectionCount
            )

        tvDuplicatePhotoSelectAll =
            view.findViewById(
                R.id.tvDuplicatePhotoSelectAll
            )

        emptyDuplicatePhotosState =
            view.findViewById(
                R.id.emptyDuplicatePhotosState
            )

        cleanSelectedButton =
            view.findViewById(
                R.id.btnCleanSelectedDuplicatePhotos
            )
    }


    private fun setupBackButton(
        view: View
    ) {

        val backButton =
            view.findViewById<ImageButton>(
                R.id.btnDuplicatePhotosBack
            )

        backButton.setOnClickListener {

            parentFragmentManager
                .popBackStack()
        }
    }


    private fun setupRecyclerView() {

        duplicatePhotoAdapter =
            DuplicatePhotoGroupAdapter(
                onSelectionChanged = { selectedCount ->

                    updateSelectionUI(
                        selectedCount
                    )

                    // Sync Duplicate Photos selection
                    // with global selectedCleanFiles
                    syncDuplicatePhotoSelectionWithGlobalList()
                }
            )

        recyclerDuplicatePhotos.layoutManager =
            LinearLayoutManager(
                requireContext()
            )

        recyclerDuplicatePhotos.adapter =
            duplicatePhotoAdapter

        recyclerDuplicatePhotos.setHasFixedSize(
            false
        )
    }


    private fun setupSelectAll() {

        tvDuplicatePhotoSelectAll.setOnClickListener {

            if (
                duplicatePhotoAdapter.areAllSelected()
            ) {

                duplicatePhotoAdapter.clearSelection()

            } else {

                duplicatePhotoAdapter.selectAll()
            }
        }
    }


    private fun setupCleanSelectedButton() {

        cleanSelectedButton.setOnClickListener {

            val selectedPhotos =
                duplicatePhotoAdapter
                    .getSelectedPhotos()

            if (selectedPhotos.isEmpty()) {
                return@setOnClickListener
            }

            showDeleteConfirmation(
                selectedPhotos
            )
        }
    }


    private fun showDeleteConfirmation(
        selectedPhotos: List<CleanerFile>
    ) {

        val selectedCount =
            selectedPhotos.size

        AlertDialog.Builder(
            requireContext()
        )
            .setTitle(
                "Delete selected photos?"
            )
            .setMessage(
                "You have selected $selectedCount photos. " +
                        "These photos will be permanently deleted from your device."
            )
            .setNegativeButton(
                "Cancel",
                null
            )
            .setPositiveButton(
                "Delete"
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
            selectedPhotos
                .map {
                    Uri.parse(it.uri)
                }

        if (uris.isEmpty()) {
            return
        }

        try {

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

        } catch (_: SecurityException) {

            pendingDeletionPhotos =
                emptyList()

            Toast.makeText(
                requireContext(),
                "Unable to delete the selected photos.",
                Toast.LENGTH_LONG
            ).show()
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

        resultsViewModel.removeSelectedCleanFiles(
            deletedUris
        )

        val currentGroups =
            resultsViewModel
                .getDuplicatePhotoGroups()

        val updatedGroups =
            currentGroups
                .mapNotNull { group ->

                    val remainingFiles =
                        group.files
                            .filterNot {
                                it.uri in deletedUris
                            }

                    if (remainingFiles.isNotEmpty()) {

                        com.example.phonecleaner
                            .data.model
                            .DuplicatePhotoGroup(
                                files =
                                    remainingFiles
                            )

                    } else {

                        null
                    }
                }

        resultsViewModel
            .setDuplicatePhotoGroups(
                updatedGroups
            )

        val updatedDuplicatePhotoCount =
            updatedGroups.sumOf {
                it.files.size
            }

        ScanSummaryPreferences.saveDuplicateSummary(
            context = requireContext(),
            groupCount = updatedGroups.size,
            photoCount = updatedDuplicatePhotoCount
        )

        pendingDeletionPhotos =
            emptyList()

        duplicatePhotoAdapter
            .submitList(
                updatedGroups
            )

        displayDuplicatePhotos()

        Toast.makeText(
            requireContext(),
            "${deletedPhotos.size} photos deleted",
            Toast.LENGTH_SHORT
        ).show()
    }


    private fun displayDuplicatePhotos() {

        val duplicateGroups =
            resultsViewModel
                .getDuplicatePhotoGroups()

        val groupCount =
            duplicateGroups.size

        val duplicatePhotoCount =
            duplicateGroups.sumOf {
                it.files.size
            }

        val totalSize =
            duplicateGroups.sumOf { group ->

                group.files.sumOf {
                    it.sizeBytes
                }
            }

        tvDuplicatePhotosSummary.text =
            String.format(
                Locale.US,
                "%,d groups • %,d photos • %s",
                groupCount,
                duplicatePhotoCount,
                formatDataSize(totalSize)
            )

        /*
         * Load the duplicate groups.
         *
         * IMPORTANT:
         * submitList() does NOT modify global
         * selection anymore.
         */
        duplicatePhotoAdapter.submitList(
            duplicateGroups
        )

        /*
         * Read the selections that already exist
         * globally across all Clean categories.
         */
        val globallySelectedUris =
            resultsViewModel
                .getSelectedCleanFiles()
                .map {
                    it.uri
                }
                .toSet()

        /*
         * Restore those selections into the
         * Duplicate Photos adapter.
         *
         * This does NOT sync back to the global
         * list because these selections already
         * exist globally.
         */
        duplicatePhotoAdapter.setSelectedUris(
            globallySelectedUris
        )

        /*
         * Get the actual restored selection count.
         */
        val restoredSelectedCount =
            duplicatePhotoAdapter
                .getSelectedPhotos()
                .size

        if (
            duplicateGroups.isEmpty()
        ) {

            recyclerDuplicatePhotos.visibility =
                View.GONE

            emptyDuplicatePhotosState.visibility =
                View.VISIBLE

            cleanSelectedButton.visibility =
                View.GONE

            tvDuplicatePhotoSelectionCount.text =
                "0 selected"

            tvDuplicatePhotoSelectAll.text =
                "Select all"

        } else {

            recyclerDuplicatePhotos.visibility =
                View.VISIBLE

            emptyDuplicatePhotosState.visibility =
                View.GONE

            /*
             * Show the restored global selection.
             */
            updateSelectionUI(
                restoredSelectedCount
            )
        }
    }


    private fun updateSelectionUI(
        selectedCount: Int
    ) {

        tvDuplicatePhotoSelectionCount.text =
            if (selectedCount == 1) {
                "1 selected"
            } else {
                "$selectedCount selected"
            }

        tvDuplicatePhotoSelectAll.text =
            if (
                duplicatePhotoAdapter.areAllSelected()
            ) {
                "Deselect all"
            } else {
                "Select all"
            }

        /*
         * IMPORTANT:
         * Update the button text according to
         * the number of selected photos.
         */
        cleanSelectedButton.text =
            if (selectedCount == 1) {
                "Clean 1 Selected"
            } else {
                "Clean $selectedCount Selected"
            }

        if (selectedCount > 0) {

            cleanSelectedButton.visibility =
                View.VISIBLE

        } else {

            cleanSelectedButton.visibility =
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

            gb >= 1.0 ->
                String.format(
                    Locale.US,
                    "%.2f GB",
                    gb
                )

            mb >= 1.0 ->
                String.format(
                    Locale.US,
                    "%.1f MB",
                    mb
                )

            else ->
                String.format(
                    Locale.US,
                    "%.0f KB",
                    bytes / 1000.0
                )
        }
    }


    private fun syncDuplicatePhotoSelectionWithGlobalList() {

        val duplicateGroups =
            resultsViewModel
                .getDuplicatePhotoGroups()

        val selectedPhotos =
            duplicatePhotoAdapter
                .getSelectedPhotos()

        val selectedUris =
            selectedPhotos
                .map {
                    it.uri
                }
                .toSet()

        val allDuplicatePhotos =
            duplicateGroups
                .flatMap { group ->
                    group.files
                }

        allDuplicatePhotos.forEach { photo ->

            resultsViewModel.setCleanFileSelected(
                file = photo,
                selected = photo.uri in selectedUris
            )
        }
    }


    override fun onResume() {

        super.onResume()

        if (
            ::resultsViewModel.isInitialized &&
            ::duplicatePhotoAdapter.isInitialized
        ) {

            displayDuplicatePhotos()
        }
    }


    override fun onDestroyView() {

        /*
         * Save the current Duplicate Photos
         * selection before this Fragment view
         * is destroyed.
         */
        if (
            ::duplicatePhotoAdapter.isInitialized
        ) {

            syncDuplicatePhotoSelectionWithGlobalList()
        }

        (activity as? MainActivity)
            ?.showBottomNavigation()

        super.onDestroyView()
    }
}