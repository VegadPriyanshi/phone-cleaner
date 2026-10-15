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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.phonecleaner.MainActivity
import com.example.phonecleaner.R
import com.example.phonecleaner.data.model.CleanerFile
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.util.Locale
import com.example.phonecleaner.utils.ScanSummaryPreferences

class ScannedLargeFilesFragment : Fragment() {

    private lateinit var resultsViewModel: ScanResultsViewModel

    private lateinit var largeFilesAdapter: ScannedLargeFilesAdapter
    private lateinit var recyclerLargeFiles: RecyclerView

    private lateinit var tvLargeFilesSummary: TextView
    private lateinit var tvLargeFileSelectionCount: TextView
    private lateinit var tvLargeFileSelectAll: TextView

    private lateinit var emptyLargeFilesState: View
    private lateinit var btnCleanSelectedLargeFiles: MaterialButton

    /*
     * Files waiting for Android deletion confirmation.
     */
    private var pendingDeletionLargeFiles =
        emptyList<CleanerFile>()

    /*
     * Android deletion confirmation launcher.
     *
     * This reuses the same MediaStore deletion
     * mechanism used by the other media screens.
     */
    private val deleteRequestLauncher =
        registerForActivityResult(
            ActivityResultContracts.StartIntentSenderForResult()
        ) { result ->

            if (result.resultCode == Activity.RESULT_OK) {

                handleSuccessfulDeletion()

            } else {

                /*
                 * User cancelled Android's
                 * deletion confirmation.
                 */
                pendingDeletionLargeFiles =
                    emptyList()
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        return inflater.inflate(
            R.layout.fragment_scanned_large_files,
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

        resultsViewModel =
            ViewModelProvider(requireActivity())[
                ScanResultsViewModel::class.java
            ]

        setupViews(view)

        setupBackButton(view)

        setupRecyclerView()

        setupSelectAllButton()

        setupCleanButton()

        setupBottomSystemInset(view)

        displayLargeFiles()
    }

    private fun setupViews(
        view: View
    ) {

        recyclerLargeFiles =
            view.findViewById(
                R.id.recyclerLargeFiles
            )

        tvLargeFilesSummary =
            view.findViewById(
                R.id.tvLargeFilesSummary
            )

        tvLargeFileSelectionCount =
            view.findViewById(
                R.id.tvLargeFileSelectionCount
            )

        tvLargeFileSelectAll =
            view.findViewById(
                R.id.tvLargeFileSelectAll
            )

        emptyLargeFilesState =
            view.findViewById(
                R.id.emptyLargeFilesState
            )

        btnCleanSelectedLargeFiles =
            view.findViewById(
                R.id.btnCleanSelectedLargeFiles
            )
    }

    private fun setupBackButton(
        view: View
    ) {

        val backButton =
            view.findViewById<ImageButton>(
                R.id.btnScannedLargeFilesBack
            )

        backButton.setOnClickListener {

            parentFragmentManager
                .popBackStack()
        }
    }

    private fun setupRecyclerView() {

        largeFilesAdapter =
            ScannedLargeFilesAdapter(
                onSelectionChanged = { selectedCount ->

                    updateSelectionUI(
                        selectedCount
                    )

                    /*
                     * Sync only when the user actually
                     * changes Large File selection.
                     */
                    syncLargeFileSelectionWithGlobalList()
                }
            )

        recyclerLargeFiles.layoutManager =
            LinearLayoutManager(
                requireContext()
            )

        recyclerLargeFiles.adapter =
            largeFilesAdapter

        recyclerLargeFiles.setHasFixedSize(
            false
        )
    }
    private fun syncLargeFileSelectionWithGlobalList() {

        val largeFiles =
            resultsViewModel.getLargeFiles()

        val selectedLargeFiles =
            largeFilesAdapter.getSelectedLargeFiles()

        val selectedUris =
            selectedLargeFiles
                .map {
                    it.uri
                }
                .toSet()


        /*
         * Synchronize only Large Files.
         *
         * Selections from Documents, Photos,
         * Videos, Audio, etc. remain untouched.
         */
        largeFiles.forEach { largeFile ->

            resultsViewModel.setCleanFileSelected(
                file = largeFile,
                selected = largeFile.uri in selectedUris
            )
        }
    }

    private fun setupSelectAllButton() {

        tvLargeFileSelectAll.setOnClickListener {

            if (
                largeFilesAdapter.areAllSelected()
            ) {

                largeFilesAdapter.clearSelection()

            } else {

                largeFilesAdapter.selectAll()
            }
        }
    }

    private fun setupCleanButton() {

        btnCleanSelectedLargeFiles.setOnClickListener {

            val selectedFiles =
                largeFilesAdapter
                    .getSelectedLargeFiles()

            if (selectedFiles.isEmpty()) {
                return@setOnClickListener
            }

            /*
             * Do NOT show the old
             * "large file(s) selected" Toast.
             *
             * Start the deletion confirmation flow.
             */
            showDeleteConfirmation(
                selectedFiles
            )
        }
    }

    private fun showDeleteConfirmation(
        selectedFiles: List<CleanerFile>
    ) {

        val selectedCount =
            selectedFiles.size

        val selectedSize =
            selectedFiles.sumOf {
                it.sizeBytes
            }

        val sizeText =
            formatDataSize(
                selectedSize
            )

        val message =
            if (selectedCount == 1) {

                "You selected 1 large file ($sizeText)." +
                        "\n\n" +
                        "Are you sure you want to delete it?"

            } else {

                "You selected $selectedCount large files ($sizeText)." +
                        "\n\n" +
                        "Are you sure you want to delete them?"
            }

        MaterialAlertDialogBuilder(
            requireContext()
        )
            .setTitle(
                "Delete selected large files?"
            )
            .setMessage(
                message
            )
            .setNegativeButton(
                "Cancel",
                null
            )
            .setPositiveButton(
                "Delete"
            ) { _, _ ->

                requestLargeFileDeletion(
                    selectedFiles
                )
            }
            .show()
    }

    private fun requestLargeFileDeletion(
        selectedFiles: List<CleanerFile>
    ) {

        /*
         * MediaStore.createDeleteRequest()
         * is available from Android 11 / API 30.
         */
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

        if (selectedFiles.isEmpty()) {
            return
        }

        pendingDeletionLargeFiles =
            selectedFiles.toList()

        val uris =
            selectedFiles.map {
                Uri.parse(it.uri)
            }

        if (uris.isEmpty()) {

            pendingDeletionLargeFiles =
                emptyList()

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

        } catch (
            securityException: SecurityException
        ) {

            pendingDeletionLargeFiles =
                emptyList()

            Toast.makeText(
                requireContext(),
                "Permission is required to delete these files.",
                Toast.LENGTH_LONG
            ).show()

        } catch (
            exception: Exception
        ) {

            pendingDeletionLargeFiles =
                emptyList()

            Toast.makeText(
                requireContext(),
                "Unable to start deletion.",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun handleSuccessfulDeletion() {

        val deletedFiles =
            pendingDeletionLargeFiles

        if (deletedFiles.isEmpty()) {
            return
        }

        /*
         * Store the URIs of files that Android
         * confirmed for deletion.
         */
        val deletedUris =
            deletedFiles
                .map {
                    it.uri
                }
                .toSet()


        /*
 * Remove deleted files from the global
 * Clean selection as well.
 */
        resultsViewModel.removeSelectedCleanFiles(
            deletedUris
        )

        /*
         * Remove only the deleted files from
         * the current Large Files results.
         */
        val remainingFiles =
            resultsViewModel
                .getLargeFiles()
                .filterNot { file ->

                    file.uri in deletedUris
                }

        /*
         * Update the ViewModel.
         */
        resultsViewModel.updateLargeFiles(
            remainingFiles
        )

        ScanSummaryPreferences.saveLargeFileSummary(
            context = requireContext(),
            count = remainingFiles.size,
            sizeBytes = remainingFiles.sumOf { it.sizeBytes }
        )

        /*
         * Clear pending deletion.
         */
        pendingDeletionLargeFiles =
            emptyList()

        /*
         * Clear adapter selection.
         */
        largeFilesAdapter.clearSelection()

        /*
         * Refresh the screen.
         */
        displayLargeFiles()

        val deletedCount =
            deletedFiles.size

        val message =
            if (deletedCount == 1) {

                "1 large file deleted"

            } else {

                "$deletedCount large files deleted"
            }

        Toast.makeText(
            requireContext(),
            message,
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun displayLargeFiles() {

        val largeFiles =
            resultsViewModel.getLargeFiles()

        val count =
            largeFiles.size

        val totalSize =
            largeFiles.sumOf {
                it.sizeBytes
            }

        tvLargeFilesSummary.text =
            String.format(
                Locale.US,
                "%,d files • %s",
                count,
                formatDataSize(
                    totalSize
                )
            )

        /*
         * Load the current Large Files list.
         *
         * IMPORTANT:
         * submitList() must NOT modify the
         * global Clean selection.
         */
        largeFilesAdapter.submitList(
            largeFiles
        )

        /*
         * Restore Large File selections that
         * were already selected globally.
         */
        val globallySelectedUris =
            resultsViewModel
                .getSelectedCleanFiles()
                .map {
                    it.uri
                }
                .toSet()

        largeFilesAdapter.setSelectedUris(
            globallySelectedUris
        )

        /*
         * Get the restored selection count
         * directly from the adapter.
         */
        val restoredSelectedCount =
            largeFilesAdapter
                .getSelectedLargeFiles()
                .size

        if (largeFiles.isEmpty()) {

            recyclerLargeFiles.visibility =
                View.GONE

            emptyLargeFilesState.visibility =
                View.VISIBLE

            btnCleanSelectedLargeFiles.visibility =
                View.GONE

            tvLargeFileSelectionCount.text =
                "0 selected"

            tvLargeFileSelectAll.text =
                "Select all"

        } else {

            recyclerLargeFiles.visibility =
                View.VISIBLE

            emptyLargeFilesState.visibility =
                View.GONE

            updateSelectionUI(
                restoredSelectedCount
            )
        }
    }

    private fun updateSelectionUI(
        selectedCount: Int
    ) {

        tvLargeFileSelectionCount.text =
            if (selectedCount == 1) {

                "1 selected"

            } else {

                "$selectedCount selected"
            }

        tvLargeFileSelectAll.text =
            if (
                largeFilesAdapter.areAllSelected()
            ) {

                "Deselect all"

            } else {

                "Select all"
            }

        val hasSelection =
            selectedCount > 0

        btnCleanSelectedLargeFiles.isEnabled =
            hasSelection

        if (hasSelection) {

            btnCleanSelectedLargeFiles.visibility =
                View.VISIBLE

            btnCleanSelectedLargeFiles.text =
                "Clean $selectedCount Selected"

            btnCleanSelectedLargeFiles.setTextColor(
                Color.WHITE
            )

            btnCleanSelectedLargeFiles.backgroundTintList =
                ColorStateList.valueOf(
                    Color.rgb(
                        118,
                        85,
                        246
                    )
                )

        } else {

            btnCleanSelectedLargeFiles.visibility =
                View.GONE

            btnCleanSelectedLargeFiles.text =
                "Clean Selected"

            btnCleanSelectedLargeFiles.setTextColor(
                Color.rgb(
                    138,
                    123,
                    175
                )
            )

            btnCleanSelectedLargeFiles.backgroundTintList =
                ColorStateList.valueOf(
                    Color.rgb(
                        233,
                        226,
                        255
                    )
                )
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

    override fun onResume() {
        super.onResume()

        if (
            ::resultsViewModel.isInitialized &&
            ::largeFilesAdapter.isInitialized
        ) {

            displayLargeFiles()
        }
    }

    override fun onDestroyView() {

        /*
         * Save the current Large Files
         * selection before this Fragment view
         * is destroyed.
         */
        if (
            ::largeFilesAdapter.isInitialized
        ) {

            syncLargeFileSelectionWithGlobalList()
        }

        (activity as? MainActivity)
            ?.showBottomNavigation()

        super.onDestroyView()
    }
}