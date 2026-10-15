package com.example.phonecleaner.ui.results

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.phonecleaner.MainActivity
import com.example.phonecleaner.R
import com.google.android.material.button.MaterialButton
import java.util.Locale
import android.app.Activity
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import com.example.phonecleaner.data.model.CleanerFile

class ScannedJunkFilesFragment : Fragment() {

    private lateinit var resultsViewModel: ScanResultsViewModel

    private lateinit var junkFilesAdapter: ScannedJunkFilesAdapter

    private lateinit var junkCategoryAdapter: JunkCategoryAdapter

    private lateinit var recyclerJunkFiles: RecyclerView

    private lateinit var tvJunkFilesSummary: TextView

    private lateinit var tvJunkFileSelectionCount: TextView

    private lateinit var tvJunkFileSelectAll: TextView

    private lateinit var emptyJunkFilesState: View

    private lateinit var btnCleanSelectedJunkFiles: MaterialButton

    private var showingJunkCategoryFiles = false

    private var pendingDeletionJunkFiles =
        emptyList<CleanerFile>()

    private val deleteRequestLauncher =
        registerForActivityResult(
            ActivityResultContracts.StartIntentSenderForResult()
        ) { result ->

            if (result.resultCode == Activity.RESULT_OK) {

                handleSuccessfulJunkDeletion()

            } else {

                pendingDeletionJunkFiles =
                    emptyList()
            }
        }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        return inflater.inflate(
            R.layout.fragment_scanned_junk_files,
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
         * Hide the main bottom navigation
         * while viewing scanned junk files.
         */
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

        setupBottomSystemInset(view)

        displayJunkFiles()
    }


    private fun setupViews(
        view: View
    ) {

        recyclerJunkFiles =
            view.findViewById(
                R.id.recyclerJunkFiles
            )

        tvJunkFilesSummary =
            view.findViewById(
                R.id.tvJunkFilesSummary
            )

        tvJunkFileSelectionCount =
            view.findViewById(
                R.id.tvJunkFileSelectionCount
            )

        tvJunkFileSelectAll =
            view.findViewById(
                R.id.tvJunkFileSelectAll
            )

        emptyJunkFilesState =
            view.findViewById(
                R.id.emptyJunkFilesState
            )

        btnCleanSelectedJunkFiles =
            view.findViewById(
                R.id.btnCleanSelectedJunkFiles
            )

        btnCleanSelectedJunkFiles.setOnClickListener {
            showCleanSelectedConfirmation()
        }
    }


    private fun requestJunkFileDeletion(
        selectedFiles: List<com.example.phonecleaner.data.model.CleanerFile>
    ) {

        if (selectedFiles.isEmpty()) {
            return
        }

        /*
         * Thumbnail caches / Leftover app data
         * ---------------------------------------------
         * These come from direct filesystem scanning
         * (ThumbnailCacheScanner / LeftoverAppDataScanner)
         * rather than MediaStore, so their uri uses the
         * file:// scheme instead of content://. They can't
         * go through MediaStore.createDeleteRequest - they
         * need direct File deletion instead, and leftover
         * app data folders need a recursive delete since
         * they are directories, not single files.
         */
        val fileSchemeItems =
            selectedFiles.filter {
                Uri.parse(it.uri).scheme == "file"
            }

        /*
         * APK files
         * ---------------------------------------------
         * Keep the APK deletion flow that is already
         * working perfectly.
         */
        val oldApkFiles =
            selectedFiles.filter {
                it !in fileSchemeItems &&
                        it.name.endsWith(
                            ".apk",
                            ignoreCase = true
                        )
            }

        /*
         * Empty files
         * ---------------------------------------------
         * Empty files are 0-byte files. These need the
         * direct File.delete() flow instead of
         * MediaStore.createDeleteRequest().
         */
        val emptyFiles =
            selectedFiles.filter {
                it !in fileSchemeItems &&
                        it.sizeBytes == 0L &&
                        !it.name.endsWith(
                            ".apk",
                            ignoreCase = true
                        )
            }

        /*
         * Normal junk files
         * ---------------------------------------------
         * Keep the existing MediaStore deletion flow
         * for Temporary Files / other normal junk.
         */
        val normalJunkFiles =
            selectedFiles.filter {
                it !in fileSchemeItems &&
                        !it.name.endsWith(
                            ".apk",
                            ignoreCase = true
                        ) &&
                        it.sizeBytes != 0L
            }

        /*
         * file:// deletion
         * (thumbnail caches, leftover app data)
         */
        if (fileSchemeItems.isNotEmpty()) {
            deleteFileSchemeItems(
                fileSchemeItems
            )
        }

        /*
         * APK deletion
         */
        if (oldApkFiles.isNotEmpty()) {
            deleteOldApkFiles(
                oldApkFiles
            )
        }

        /*
         * Empty-file deletion
         */
        if (emptyFiles.isNotEmpty()) {
            deleteEmptyFiles(
                emptyFiles
            )
        }

        /*
         * Nothing left for MediaStore deletion.
         */
        if (normalJunkFiles.isEmpty()) {
            return
        }

        /*
         * Existing normal junk deletion flow.
         * DO NOT CHANGE this logic.
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

        pendingDeletionJunkFiles =
            normalJunkFiles.toList()

        val uris =
            normalJunkFiles.map {
                Uri.parse(it.uri)
            }

        if (uris.isEmpty()) {

            pendingDeletionJunkFiles =
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

            pendingDeletionJunkFiles =
                emptyList()

            Toast.makeText(
                requireContext(),
                "Permission is required to delete these files.",
                Toast.LENGTH_LONG
            ).show()

        } catch (
            exception: Exception
        ) {

            pendingDeletionJunkFiles =
                emptyList()

            Toast.makeText(
                requireContext(),
                "Unable to start deletion.",
                Toast.LENGTH_LONG
            ).show()
        }
    }


    private fun deleteFileSchemeItems(
        selectedFiles: List<com.example.phonecleaner.data.model.CleanerFile>
    ) {

        if (selectedFiles.isEmpty()) {
            return
        }

        val deletedFiles =
            mutableListOf<CleanerFile>()

        val failedFiles =
            mutableListOf<CleanerFile>()

        selectedFiles.forEach { cleanerFile ->

            try {

                val uri =
                    Uri.parse(
                        cleanerFile.uri
                    )

                val path =
                    uri.path

                if (path.isNullOrBlank()) {

                    failedFiles.add(
                        cleanerFile
                    )

                    return@forEach
                }

                val target =
                    java.io.File(path)

                if (!target.exists()) {

                    /*
                     * Already gone - treat as deleted so it
                     * doesn't linger in the UI.
                     */
                    deletedFiles.add(
                        cleanerFile
                    )

                    return@forEach
                }

                /*
                 * Leftover app data entries are folders;
                 * thumbnail cache entries are individual files.
                 */
                val succeeded =
                    if (target.isDirectory) {

                        target.deleteRecursively()

                    } else {

                        target.delete()
                    }

                if (succeeded) {

                    deletedFiles.add(
                        cleanerFile
                    )

                } else {

                    failedFiles.add(
                        cleanerFile
                    )
                }

            } catch (
                exception: Exception
            ) {

                Log.e(
                    "PhoneCleaner",
                    "Failed to delete: ${cleanerFile.name}",
                    exception
                )

                failedFiles.add(
                    cleanerFile
                )
            }
        }

        if (deletedFiles.isNotEmpty()) {

            val deletedUris =
                deletedFiles
                    .map {
                        it.uri
                    }
                    .toSet()

            /*
             * Remove deleted files from the
             * global Clean selection.
             */
            resultsViewModel.removeSelectedCleanFiles(
                deletedUris
            )

            val remainingFiles =
                resultsViewModel
                    .getJunkFiles()
                    .filterNot {
                        it.uri in deletedUris
                    }

            resultsViewModel.setJunkFiles(
                remainingFiles
            )
        }

        /*
         * Do not trigger global synchronization here.
         */
        junkFilesAdapter.clearSelectionSilently()

        pendingDeletionJunkFiles =
            emptyList()

        displayJunkFiles()

        when {

            deletedFiles.isNotEmpty() &&
                    failedFiles.isEmpty() -> {

                Toast.makeText(
                    requireContext(),
                    "${deletedFiles.size} item(s) deleted.",
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
                    "Unable to delete selected item(s).",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }


    private fun deleteEmptyFiles(
        selectedFiles: List<com.example.phonecleaner.data.model.CleanerFile>
    ) {

        if (selectedFiles.isEmpty()) {
            return
        }

        val deletedFiles =
            mutableListOf<CleanerFile>()

        val failedFiles =
            mutableListOf<CleanerFile>()

        selectedFiles.forEach { cleanerFile ->

            try {

                val uri =
                    Uri.parse(
                        cleanerFile.uri
                    )

                val path =
                    getFilePathFromUri(uri)

                if (!path.isNullOrBlank()) {

                    val file =
                        java.io.File(path)

                    if (file.exists()) {

                        /*
                         * Empty files should be 0 bytes.
                         */
                        if (
                            file.length() == 0L &&
                            file.delete()
                        ) {

                            /*
                             * Try to remove the corresponding
                             * MediaStore entry as well.
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

                                // Physical file is already deleted.
                            }

                            deletedFiles.add(
                                cleanerFile
                            )

                        } else {

                            failedFiles.add(
                                cleanerFile
                            )
                        }

                    } else {

                        /*
                         * File is already missing.
                         * Treat as deleted from the cleaner
                         * results so it doesn't remain in the UI.
                         */
                        deletedFiles.add(
                            cleanerFile
                        )
                    }

                } else {

                    failedFiles.add(
                        cleanerFile
                    )
                }

            } catch (
                exception: Exception
            ) {

                Log.e(
                    "PhoneCleaner",
                    "Failed to delete empty file: ${cleanerFile.name}",
                    exception
                )

                failedFiles.add(
                    cleanerFile
                )
            }
        }

        /*
         * Remove successfully deleted files
         * from the current Junk Files result.
         */
        if (deletedFiles.isNotEmpty()) {

            val deletedUris =
                deletedFiles
                    .map {
                        it.uri
                    }
                    .toSet()

            /*
             * Remove deleted files from global
             * Clean selection.
             */
            resultsViewModel.removeSelectedCleanFiles(
                deletedUris
            )

            val remainingFiles =
                resultsViewModel
                    .getJunkFiles()
                    .filterNot {
                        it.uri in deletedUris
                    }

            resultsViewModel.setJunkFiles(
                remainingFiles
            )
        }

        /*
         * Clear selection without triggering
         * global selection synchronization.
         */
        junkFilesAdapter.clearSelectionSilently()

        pendingDeletionJunkFiles =
            emptyList()

        /*
         * Refresh the screen.
         */
        displayJunkFiles()

        /*
         * Result message.
         */
        when {

            deletedFiles.isNotEmpty() &&
                    failedFiles.isEmpty() -> {

                Toast.makeText(
                    requireContext(),
                    "${deletedFiles.size} empty file(s) deleted.",
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
                    "Unable to delete selected empty file(s).",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }


    private fun deleteOldApkFiles(
        selectedFiles: List<com.example.phonecleaner.data.model.CleanerFile>
    ) {

        if (selectedFiles.isEmpty()) {
            return
        }

        val deletedFiles =
            mutableListOf<com.example.phonecleaner.data.model.CleanerFile>()

        val failedFiles =
            mutableListOf<com.example.phonecleaner.data.model.CleanerFile>()

        selectedFiles.forEach { cleanerFile ->

            try {

                val uri =
                    Uri.parse(
                        cleanerFile.uri
                    )

                /*
                 * Try to get the real filesystem path
                 * from the MediaStore URI.
                 */
                val path =
                    getFilePathFromUri(uri)

                if (!path.isNullOrBlank()) {

                    val file =
                        java.io.File(path)

                    if (
                        file.exists() &&
                        file.delete()
                    ) {

                        deletedFiles.add(
                            cleanerFile
                        )

                    } else {

                        failedFiles.add(
                            cleanerFile
                        )
                    }

                } else {

                    failedFiles.add(
                        cleanerFile
                    )
                }

            } catch (
                exception: Exception
            ) {

                android.util.Log.e(
                    "PhoneCleaner",
                    "Failed to delete APK: ${cleanerFile.name}",
                    exception
                )

                failedFiles.add(
                    cleanerFile
                )
            }
        }

        /*
         * Remove successfully deleted files
         * from the current Junk Files result.
         */
        if (deletedFiles.isNotEmpty()) {

            val deletedUris =
                deletedFiles
                    .map {
                        it.uri
                    }
                    .toSet()

            /*
             * Remove deleted APKs from the
             * global Clean selection.
             */
            resultsViewModel.removeSelectedCleanFiles(
                deletedUris
            )

            val remainingFiles =
                resultsViewModel
                    .getJunkFiles()
                    .filterNot {
                        it.uri in deletedUris
                    }

            resultsViewModel.setJunkFiles(
                remainingFiles
            )
        }

        /*
         * Clear current adapter selection silently.
         */
        junkFilesAdapter.clearSelectionSilently()

        pendingDeletionJunkFiles =
            emptyList()

        /*
         * Refresh Junk Files screen.
         */
        displayJunkFiles()

        /*
         * Show result.
         */
        when {

            deletedFiles.isNotEmpty() &&
                    failedFiles.isEmpty() -> {

                val count =
                    deletedFiles.size

                Toast.makeText(
                    requireContext(),
                    if (count == 1) {
                        "1 APK file deleted"
                    } else {
                        "$count APK files deleted"
                    },
                    Toast.LENGTH_SHORT
                ).show()
            }

            deletedFiles.isNotEmpty() -> {

                Toast.makeText(
                    requireContext(),
                    "${deletedFiles.size} APK files deleted. " +
                            "${failedFiles.size} could not be deleted.",
                    Toast.LENGTH_LONG
                ).show()
            }

            else -> {

                Toast.makeText(
                    requireContext(),
                    "Unable to delete the selected APK files.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }


    private fun getFilePathFromUri(
        uri: Uri
    ): String? {

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

                    if (cursor.moveToFirst()) {

                        val columnIndex =
                            cursor.getColumnIndex(
                                MediaStore.MediaColumns.DATA
                            )

                        if (columnIndex >= 0) {

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

        } catch (
            exception: Exception
        ) {

            Log.e(
                "PhoneCleaner",
                "Unable to resolve file path: $uri",
                exception
            )

            null
        }
    }


    private fun handleSuccessfulJunkDeletion() {

        val deletedFiles =
            pendingDeletionJunkFiles

        if (deletedFiles.isEmpty()) {
            return
        }

        val deletedUris =
            deletedFiles
                .map {
                    it.uri
                }
                .toSet()

        /*
         * Remove deleted files from the
         * global Clean selection.
         */
        resultsViewModel.removeSelectedCleanFiles(
            deletedUris
        )

        val remainingFiles =
            resultsViewModel
                .getJunkFiles()
                .filterNot { file ->
                    file.uri in deletedUris
                }

        resultsViewModel.updateJunkFiles(
            remainingFiles
        )

        pendingDeletionJunkFiles =
            emptyList()

        /*
         * Clear only the adapter selection.
         * Do not trigger global synchronization.
         */
        junkFilesAdapter.clearSelectionSilently()

        displayJunkFiles()

        val deletedCount =
            deletedFiles.size

        val message =
            if (deletedCount == 1) {

                "1 junk file deleted"

            } else {

                "$deletedCount junk files deleted"
            }

        Toast.makeText(
            requireContext(),
            message,
            Toast.LENGTH_SHORT
        ).show()
    }


    private fun showCleanSelectedConfirmation() {

        val selectedFiles =
            junkFilesAdapter
                .getSelectedJunkFiles()

        if (selectedFiles.isEmpty()) {
            return
        }

        com.google.android.material.dialog.MaterialAlertDialogBuilder(
            requireContext()
        )
            .setTitle(
                "Clean selected files?"
            )
            .setMessage(
                String.format(
                    Locale.US,
                    "%,d selected files will be removed.",
                    selectedFiles.size
                )
            )
            .setNegativeButton(
                "Cancel",
                null
            )
            .setPositiveButton(
                "Clean"
            ) { _, _ ->

                requestJunkFileDeletion(
                    selectedFiles
                )
            }
            .show()
    }


    private fun setupBackButton(
        view: View
    ) {

        val backButton =
            view.findViewById<ImageButton>(
                R.id.btnScannedJunkFilesBack
            )

        backButton.setOnClickListener {

            if (showingJunkCategoryFiles) {

                showJunkCategories()

            } else {

                parentFragmentManager
                    .popBackStack()
            }
        }
    }


    private fun getAllJunkCategoryFiles(): List<CleanerFile> {

        val categories =
            resultsViewModel
                .getJunkCategories()

        categories.forEach { category ->

            Log.d(
                "JunkCategoryDebug",
                "${category.title}: " +
                        "itemCount=${category.itemCount}, " +
                        "files=${category.files.size}, " +
                        "size=${category.sizeBytes}"
            )
        }

        val allFiles =
            categories
                .flatMap { category ->
                    category.files
                }
                .distinctBy { file ->
                    file.uri
                }

        Log.d(
            "JunkCategoryDebug",
            "TOTAL UNIQUE FILES = ${allFiles.size}"
        )

        return allFiles
    }


    private fun showJunkCategories() {

        showingJunkCategoryFiles = false

        recyclerJunkFiles.adapter =
            junkCategoryAdapter

        val junkCategories =
            resultsViewModel
                .getJunkCategories()

        junkCategoryAdapter.submitList(
            junkCategories
        )

        /*
         * Build the complete Junk Files list from
         * all visible categories.
         *
         * distinctBy(uri) prevents the same file
         * from being counted twice.
         */
        val allCategoryFiles =
            getAllJunkCategoryFiles()

        val totalCount =
            allCategoryFiles.size

        val totalSize =
            allCategoryFiles.sumOf {
                it.sizeBytes
            }

        tvJunkFilesSummary.text =
            String.format(
                Locale.US,
                "%,d files • %s",
                totalCount,
                formatDataSize(totalSize)
            )

        /*
         * Clear only the local adapter selection.
         *
         * IMPORTANT:
         * Do not synchronize this empty adapter
         * selection with the global list.
         *
         * The actual selected files are already
         * stored in the global ViewModel.
         */
        junkFilesAdapter.clearSelectionSilently()

        updateSelectionUI(
            0
        )
    }


    private fun setupRecyclerView() {

        junkFilesAdapter =
            ScannedJunkFilesAdapter(
                onSelectionChanged = { selectedCount ->

                    updateSelectionUI(
                        selectedCount
                    )

                    /*
                     * This callback occurs only when
                     * the user actually changes selection.
                     */
                    syncJunkFileSelectionWithGlobalList()
                }
            )

        junkCategoryAdapter =
            JunkCategoryAdapter(
                onCategoryClick = { category ->

                    openJunkCategory(
                        category
                    )
                }
            )

        recyclerJunkFiles.layoutManager =
            LinearLayoutManager(
                requireContext()
            )

        recyclerJunkFiles.adapter =
            junkCategoryAdapter

        recyclerJunkFiles.setHasFixedSize(
            false
        )
    }


    private fun syncJunkFileSelectionWithGlobalList() {

        val currentJunkFiles =
            resultsViewModel
                .getJunkFiles()

        val selectedJunkFiles =
            junkFilesAdapter
                .getSelectedJunkFiles()

        val selectedUris =
            selectedJunkFiles
                .map {
                    it.uri
                }
                .toSet()

        /*
         * Synchronize only the currently displayed
         * Junk Files.
         *
         * Selections from Photos, Videos, Audio,
         * Large Files, etc. remain untouched.
         */
        currentJunkFiles.forEach { junkFile ->

            resultsViewModel.setCleanFileSelected(
                file = junkFile,
                selected =
                    junkFile.uri in selectedUris
            )
        }
    }


    private fun openJunkCategory(
        category: com.example.phonecleaner.data.model.JunkCategory
    ) {

        showingJunkCategoryFiles = true

        /*
         * Set the current category files.
         */
        resultsViewModel.setJunkFiles(
            category.files
        )

        recyclerJunkFiles.adapter =
            junkFilesAdapter

        /*
         * IMPORTANT:
         *
         * Do not trigger the selection callback here.
         *
         * Otherwise the current category's global
         * selection can be cleared before we restore it.
         */
        junkFilesAdapter.submitListSilently(
            category.files
        )

        /*
         * Read the global selections.
         */
        val globallySelectedFiles =
            resultsViewModel
                .getSelectedCleanFiles()

        val globallySelectedUris =
            globallySelectedFiles
                .map {
                    it.uri
                }
                .toSet()

        /*
         * Restore selections belonging to
         * this category.
         */
        junkFilesAdapter.setSelectedUris(
            globallySelectedUris
        )

        /*
         * Category summary.
         */
        tvJunkFilesSummary.text =
            String.format(
                Locale.US,
                "%,d files • %s",
                category.itemCount,
                formatDataSize(
                    category.sizeBytes
                )
            )

        /*
         * Show the number selected in this
         * particular category.
         */
        updateSelectionUI(
            junkFilesAdapter
                .getSelectedJunkFiles()
                .size
        )
    }


    private fun setupSelectAllButton() {

        tvJunkFileSelectAll.setOnClickListener {

            /*
             * MAIN JUNK FILES CATEGORY SCREEN
             */
            if (!showingJunkCategoryFiles) {

                val allCategoryFiles =
                    getAllJunkCategoryFiles()

                if (allCategoryFiles.isEmpty()) {
                    return@setOnClickListener
                }

                /*
                 * IMPORTANT:
                 *
                 * Do NOT call submitList() here.
                 *
                 * The complete category list has already
                 * been submitted by displayJunkFiles().
                 */
                val selectedCount =
                    junkFilesAdapter
                        .getSelectedJunkFiles()
                        .size

                val allSelected =
                    selectedCount ==
                            allCategoryFiles.size

                if (allSelected) {

                    /*
                     * Everything is currently selected.
                     * Deselect everything.
                     */
                    junkFilesAdapter.clearSelection()

                } else {

                    /*
                     * Not everything is selected.
                     * Select everything.
                     */
                    junkFilesAdapter.selectAll()
                }

                return@setOnClickListener
            }

            /*
             * CATEGORY FILE SCREEN
             *
             * Example:
             * Temporary Files
             * Old APK Files
             * Empty Files
             * Thumbnail Caches
             */
            if (
                junkFilesAdapter.areAllSelected()
            ) {

                junkFilesAdapter.clearSelection()

            } else {

                junkFilesAdapter.selectAll()
            }
        }
    }


    private fun displayJunkFiles() {

        val junkCategories =
            resultsViewModel
                .getJunkCategories()

        junkCategoryAdapter.submitList(
            junkCategories
        )

        /*
         * Calculate the complete Junk Files
         * summary from all categories.
         */
        val allCategoryFiles =
            getAllJunkCategoryFiles()

        val count =
            allCategoryFiles.size

        val totalSize =
            allCategoryFiles.sumOf {
                it.sizeBytes
            }

        tvJunkFilesSummary.text =
            String.format(
                Locale.US,
                "%,d files • %s",
                count,
                formatDataSize(
                    totalSize
                )
            )

        /*
         * Keep the complete list available for
         * Select All / Clean Selected.
         */
        junkFilesAdapter.submitListSilently(
            allCategoryFiles
        )

        /*
         * Restore files that were already selected
         * globally.
         */
        val globallySelectedUris =
            resultsViewModel
                .getSelectedCleanFiles()
                .map {
                    it.uri
                }
                .toSet()

        junkFilesAdapter.setSelectedUris(
            globallySelectedUris
        )

        /*
         * Use the restored selection count.
         */
        val restoredSelectedCount =
            junkFilesAdapter
                .getSelectedJunkFiles()
                .size

        if (junkCategories.isEmpty()) {

            recyclerJunkFiles.visibility =
                View.GONE

            emptyJunkFilesState.visibility =
                View.VISIBLE

        } else {

            recyclerJunkFiles.visibility =
                View.VISIBLE

            emptyJunkFilesState.visibility =
                View.GONE
        }

        updateSelectionUI(
            restoredSelectedCount
        )
    }


    private fun updateSelectionUI(
        selectedCount: Int
    ) {

        tvJunkFileSelectionCount.text =
            if (selectedCount == 1) {

                "1 selected"

            } else {

                "$selectedCount selected"
            }

        tvJunkFileSelectAll.text =
            if (
                junkFilesAdapter.areAllSelected()
            ) {

                "Deselect all"

            } else {

                "Select all"
            }

        val hasSelection =
            selectedCount > 0

        btnCleanSelectedJunkFiles.isEnabled =
            hasSelection

        if (hasSelection) {

            btnCleanSelectedJunkFiles.visibility =
                View.VISIBLE

            btnCleanSelectedJunkFiles.text =
                "Clean $selectedCount Selected"

        } else {

            btnCleanSelectedJunkFiles.visibility =
                View.GONE
        }
    }


    private fun formatDataSize(
        bytes: Long
    ): String {

        if (bytes < 1024) {

            return "$bytes B"
        }

        val kb =
            bytes / 1024.0

        if (kb < 1024) {

            return String.format(
                Locale.US,
                "%.1f KB",
                kb
            )
        }

        val mb =
            kb / 1024.0

        if (mb < 1024) {

            return String.format(
                Locale.US,
                "%.1f MB",
                mb
            )
        }

        val gb =
            mb / 1024.0

        return String.format(
            Locale.US,
            "%.2f GB",
            gb
        )
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
}