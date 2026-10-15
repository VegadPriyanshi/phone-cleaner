package com.example.phonecleaner.ui.results

import android.app.Activity
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.phonecleaner.MainActivity
import com.example.phonecleaner.R
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.util.Locale


class ScannedDocumentsFragment : Fragment() {

    private lateinit var resultsViewModel: ScanResultsViewModel
    private lateinit var documentAdapter: ScannedDocumentsAdapter

    private lateinit var recyclerDocuments: RecyclerView
    private lateinit var tvDocumentSummary: TextView
    private lateinit var tvDocumentSelectionCount: TextView
    private lateinit var tvDocumentSelectAll: TextView
    private lateinit var emptyDocumentsState: View
    private lateinit var btnCleanSelectedDocuments: MaterialButton


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        return inflater.inflate(
            R.layout.fragment_scanned_documents,
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


        recyclerDocuments =
            view.findViewById(
                R.id.recyclerDocuments
            )


        tvDocumentSummary =
            view.findViewById(
                R.id.tvDocumentSummary
            )


        tvDocumentSelectionCount =
            view.findViewById(
                R.id.tvDocumentSelectionCount
            )


        tvDocumentSelectAll =
            view.findViewById(
                R.id.tvDocumentSelectAll
            )


        emptyDocumentsState =
            view.findViewById(
                R.id.emptyDocumentsState
            )


        btnCleanSelectedDocuments =
            view.findViewById(
                R.id.btnCleanSelectedDocuments
            )


        view.findViewById<ImageButton>(
            R.id.btnScannedDocumentsBack
        ).setOnClickListener {

            parentFragmentManager
                .popBackStack()
        }


        documentAdapter =
            ScannedDocumentsAdapter { selectedCount ->

                updateSelectionUI(
                    selectedCount
                )

                syncDocumentSelectionWithGlobalList()
            }


        recyclerDocuments.layoutManager =
            LinearLayoutManager(
                requireContext()
            )


        recyclerDocuments.adapter =
            documentAdapter


        tvDocumentSelectAll.setOnClickListener {

            if (
                documentAdapter
                    .areAllSelected()
            ) {

                documentAdapter.clearSelection()

            } else {

                documentAdapter.selectAll()
            }
        }


        btnCleanSelectedDocuments.setOnClickListener {

            showDeleteConfirmation()
        }


        setupBottomSystemInset(view)


        /*
         * IMPORTANT:
         *
         * Do not scan Documents again here.
         *
         * Only show the Documents already
         * stored in ScanResultsViewModel.
         */
        displayDocuments()
    }


    private fun syncDocumentSelectionWithGlobalList() {

        val documents =
            resultsViewModel.getDocumentFiles()

        val selectedDocuments =
            documentAdapter.getSelectedDocuments()

        val selectedUris =
            selectedDocuments
                .map {
                    it.uri
                }
                .toSet()


        /*
         * Synchronize only Documents.
         *
         * Other categories already selected by the
         * user remain untouched.
         */
        documents.forEach { document ->

            resultsViewModel.setCleanFileSelected(
                file = document,
                selected = document.uri in selectedUris
            )
        }
    }


    override fun onResume() {

        super.onResume()

        (activity as? MainActivity)
            ?.hideBottomNavigation()
    }


    override fun onPause() {

        super.onPause()

        (activity as? MainActivity)
            ?.showBottomNavigation()
    }


    private fun displayDocuments() {

        val documents =
            resultsViewModel
                .getDocumentFiles()

        val totalSize =
            documents.sumOf {
                it.sizeBytes
            }

        tvDocumentSummary.text =
            String.format(
                Locale.US,
                "%,d documents • %s",
                documents.size,
                formatDataSize(
                    totalSize
                )
            )

        /*
         * Load the current Documents list.
         *
         * This does NOT change global selection.
         */
        documentAdapter.submitList(
            documents
        )

        /*
         * Restore Documents that were already
         * selected globally.
         */
        val globallySelectedUris =
            resultsViewModel
                .getSelectedCleanFiles()
                .map {
                    it.uri
                }
                .toSet()

        documentAdapter.setSelectedUris(
            globallySelectedUris
        )

        /*
         * Get the restored selection count.
         */
        val restoredSelectedCount =
            documentAdapter
                .getSelectedDocuments()
                .size

        val hasDocuments =
            documents.isNotEmpty()

        recyclerDocuments.visibility =
            if (hasDocuments) {
                View.VISIBLE
            } else {
                View.GONE
            }

        emptyDocumentsState.visibility =
            if (hasDocuments) {
                View.GONE
            } else {
                View.VISIBLE
            }

        updateSelectionUI(
            restoredSelectedCount
        )
    }

    private fun updateSelectionUI(
        selectedCount: Int
    ) {

        tvDocumentSelectionCount.text =
            if (selectedCount == 1) {

                "1 selected"

            } else {

                "$selectedCount selected"
            }


        tvDocumentSelectAll.text =
            if (
                documentAdapter
                    .areAllSelected()
            ) {

                "Deselect all"

            } else {

                "Select all"
            }


        btnCleanSelectedDocuments.visibility =
            if (selectedCount > 0) {

                View.VISIBLE

            } else {

                View.GONE
            }


        btnCleanSelectedDocuments.text =
            if (selectedCount > 0) {

                "Clean $selectedCount Selected"

            } else {

                "Clean Selected"
            }
    }


    private fun showDeleteConfirmation() {

        val selected =
            documentAdapter
                .getSelectedDocuments()


        if (selected.isEmpty()) {
            return
        }


        val count =
            selected.size


        val size =
            formatDataSize(
                selected.sumOf {
                    it.sizeBytes
                }
            )


        val message =
            if (count == 1) {

                "You selected 1 document ($size)." +
                        "\n\n" +
                        "Are you sure you want to delete it?"

            } else {

                "You selected $count documents ($size)." +
                        "\n\n" +
                        "Are you sure you want to delete them?"
            }


        MaterialAlertDialogBuilder(
            requireContext()
        )
            .setTitle(
                "Delete selected documents?"
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

                deleteSelectedDocuments(
                    selected
                )
            }
            .show()
    }


    /*
     * ============================================================
     * DOCUMENT DELETION
     * ============================================================
     *
     * Same deletion approach already used by
     * the working Junk / Empty / APK deletion flow.
     *
     * URI
     *   ↓
     * Real filesystem path
     *   ↓
     * File.delete()
     *   ↓
     * Remove from ViewModel
     *   ↓
     * Refresh screen
     */
    private fun deleteSelectedDocuments(
        selectedFiles:
        List<com.example.phonecleaner.data.model.CleanerFile>
    ) {

        if (selectedFiles.isEmpty()) {
            return
        }


        val deletedFiles =
            mutableListOf<
                    com.example.phonecleaner.data.model.CleanerFile
                    >()


        val failedFiles =
            mutableListOf<
                    com.example.phonecleaner.data.model.CleanerFile
                    >()


        selectedFiles.forEach { cleanerFile ->

            try {

                val uri =
                    Uri.parse(
                        cleanerFile.uri
                    )


                /*
                 * Get the actual filesystem path
                 * from the content URI.
                 */
                val path =
                    getFilePathFromUri(
                        uri
                    )


                if (!path.isNullOrBlank()) {

                    val file =
                        java.io.File(
                            path
                        )


                    if (!file.exists()) {

                        /*
                         * File is already gone.
                         *
                         * Remove it from the cleaner
                         * result anyway.
                         */
                        deletedFiles.add(
                            cleanerFile
                        )

                    } else {

                        /*
                         * Delete the actual file.
                         */
                        if (file.delete()) {

                            /*
                             * Best-effort cleanup of
                             * MediaStore entry.
                             */
                            try {

                                requireContext()
                                    .contentResolver
                                    .delete(
                                        uri,
                                        null,
                                        null
                                    )

                            } catch (_: Exception) {

                                /*
                                 * Physical file has already
                                 * been deleted, so this is not
                                 * considered a failure.
                                 */
                            }


                            deletedFiles.add(
                                cleanerFile
                            )

                        } else {

                            failedFiles.add(
                                cleanerFile
                            )
                        }
                    }

                } else {

                    failedFiles.add(
                        cleanerFile
                    )
                }

            } catch (exception: Exception) {

                Log.e(
                    "PhoneCleaner",
                    "Failed to delete document: ${cleanerFile.name}",
                    exception
                )

                failedFiles.add(
                    cleanerFile
                )
            }
        }


        /*
         * Remove successfully deleted Documents
         * from the current ViewModel list.
         */
        if (deletedFiles.isNotEmpty()) {

            val deletedUris =
                deletedFiles
                    .map {
                        it.uri
                    }
                    .toSet()

            resultsViewModel.removeSelectedCleanFiles(
                deletedUris
            )


            val remainingFiles =
                resultsViewModel
                    .getDocumentFiles()
                    .filterNot {
                        it.uri in deletedUris
                    }


            resultsViewModel.updateDocumentFiles(
                remainingFiles
            )
        }


        /*
         * Clear current selection.
         */
        documentAdapter.clearSelectionSilently()

        /*
         * Refresh the Documents screen.
         */
        displayDocuments()


        /*
         * Show deletion result.
         */
        when {

            deletedFiles.isNotEmpty() &&
                    failedFiles.isEmpty() -> {

                Toast.makeText(
                    requireContext(),
                    if (deletedFiles.size == 1) {

                        "1 document deleted"

                    } else {

                        "${deletedFiles.size} documents deleted"
                    },
                    Toast.LENGTH_SHORT
                ).show()
            }


            deletedFiles.isNotEmpty() &&
                    failedFiles.isNotEmpty() -> {

                Toast.makeText(
                    requireContext(),
                    "${deletedFiles.size} deleted, " +
                            "${failedFiles.size} could not be deleted.",
                    Toast.LENGTH_LONG
                ).show()
            }


            failedFiles.isNotEmpty() -> {

                Toast.makeText(
                    requireContext(),
                    "Unable to delete selected document(s).",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }


    /*
     * Get the real filesystem path from the
     * MediaStore content URI.
     */
    private fun getFilePathFromUri(
        uri: Uri
    ): String? {

        /*
         * Support file:// URI.
         */
        if (
            uri.scheme.equals(
                "file",
                ignoreCase = true
            )
        ) {

            return uri.path
        }


        val projection =
            arrayOf(
                MediaStore.MediaColumns.DATA
            )


        return try {

            requireContext()
                .contentResolver
                .query(
                    uri,
                    projection,
                    null,
                    null,
                    null
                )
                ?.use { cursor ->

                    if (
                        cursor.moveToFirst()
                    ) {

                        val columnIndex =
                            cursor.getColumnIndex(
                                MediaStore.MediaColumns.DATA
                            )


                        if (
                            columnIndex >= 0 &&
                            !cursor.isNull(
                                columnIndex
                            )
                        ) {

                            cursor.getString(
                                columnIndex
                            )

                        } else {

                            null
                        }

                    } else {

                        null
                    }
                }

        } catch (exception: Exception) {

            Log.e(
                "PhoneCleaner",
                "Unable to resolve document path: $uri",
                exception
            )

            null
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

                "0 KB"
            }
        }
    }
}