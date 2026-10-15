package com.example.phonecleaner.ui.clean

import android.app.Activity
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.TextView
import android.widget.Toast

import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

import java.io.File

import com.example.phonecleaner.R
import com.example.phonecleaner.data.model.CleanCategory
import com.example.phonecleaner.ui.results.DuplicatePhotosFragment
import com.example.phonecleaner.ui.results.ScanResultsViewModel
import com.example.phonecleaner.ui.results.ScannedAudioFragment
import com.example.phonecleaner.ui.results.ScannedDocumentsFragment
import com.example.phonecleaner.ui.results.ScannedJunkFilesFragment
import com.example.phonecleaner.ui.results.ScannedLargeFilesFragment
import com.example.phonecleaner.ui.results.ScannedPhotosFragment
import com.example.phonecleaner.ui.results.ScannedScreenshotsFragment
import com.example.phonecleaner.ui.results.ScannedVideosFragment
import com.example.phonecleaner.ui.scanning.ScanningFragment
import com.example.phonecleaner.utils.ScanSummaryPreferences
import com.example.phonecleaner.utils.StorageUtils
import com.google.android.material.dialog.MaterialAlertDialogBuilder


class CleanFragment : Fragment(
    R.layout.fragment_clean
) {

    private lateinit var cleanCategoryAdapter: CleanCategoryAdapter

    private val resultsViewModel:
            ScanResultsViewModel by activityViewModels()

    private var categories =
        emptyList<CleanCategory>()


    /*
     * Files waiting for Android's deletion confirmation.
     */
    private var pendingGlobalDeletionFiles =
        emptyList<com.example.phonecleaner.data.model.CleanerFile>()


    /*
     * Files that were already deleted directly
     * before the Android MediaStore confirmation.
     *
     * Example:
     * - Thumbnail cache
     * - APK
     * - Empty file
     */
    private var pendingGlobalDirectlyDeletedFiles =
        emptyList<com.example.phonecleaner.data.model.CleanerFile>()


    /*
     * Android deletion confirmation launcher.
     *
     * This is used for MediaStore files such as:
     * Photos
     * Screenshots
     * Videos
     * Audio
     * and other MediaStore-backed files.
     */
    private val globalDeleteRequestLauncher =
        registerForActivityResult(
            ActivityResultContracts.StartIntentSenderForResult()
        ) { result ->

            if (result.resultCode == Activity.RESULT_OK) {

                handleGlobalMediaStoreDeletionSuccess()

            } else {

                /*
                 * User cancelled the Android
                 * deletion confirmation.
                 *
                 * Direct files may already have been
                 * deleted before this confirmation.
                 */
                finishGlobalClean(
                    pendingGlobalDirectlyDeletedFiles
                )

                pendingGlobalDeletionFiles =
                    emptyList()

                pendingGlobalDirectlyDeletedFiles =
                    emptyList()
            }
        }


    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {

        super.onViewCreated(
            view,
            savedInstanceState
        )

        setupRecyclerView(view)

        loadCategories(view)

        setupScanButton(view)
        setupCleanSelectedButton(view)

        /*
         * Global Deselect All button.
         *
         * This clears selections from every Clean
         * category through the shared ViewModel.
         */
        setupDeselectAllButton(view)
    }


    override fun onResume() {

        super.onResume()

        /*
         * Refresh the Clean screen whenever
         * we return from a result screen.
         *
         * This keeps the counts and sizes
         * updated after deletion.
         */
        view?.let { currentView ->

            loadCategories(
                currentView
            )
        }
    }


    // =========================================================
    // RecyclerView
    // =========================================================

    private fun setupRecyclerView(
        view: View
    ) {

        val recyclerView =
            view.findViewById<RecyclerView>(
                R.id.rvCleanCategories
            )


        cleanCategoryAdapter =
            CleanCategoryAdapter(

                onSelectionChanged = {
                        category,
                        isSelected ->

                    updateCategorySelection(
                        category,
                        isSelected,
                        view
                    )
                },


                onCategoryClick = { category ->

                    when (category.id) {


                        // =================================================
                        // PHOTOS
                        // =================================================

                        "photos" -> {

                            parentFragmentManager
                                .beginTransaction()
                                .replace(
                                    R.id.mainFragmentContainer,
                                    ScannedPhotosFragment()
                                )
                                .addToBackStack(null)
                                .commit()
                        }


                        // =================================================
                        // SCREENSHOTS
                        // =================================================

                        "screenshots" -> {

                            parentFragmentManager
                                .beginTransaction()
                                .replace(
                                    R.id.mainFragmentContainer,
                                    ScannedScreenshotsFragment()
                                )
                                .addToBackStack(null)
                                .commit()
                        }


                        // =================================================
                        // DUPLICATE PHOTOS
                        // =================================================

                        "duplicates" -> {

                            parentFragmentManager
                                .beginTransaction()
                                .replace(
                                    R.id.mainFragmentContainer,
                                    DuplicatePhotosFragment()
                                )
                                .addToBackStack(null)
                                .commit()
                        }


                        // =================================================
                        // VIDEOS
                        // =================================================

                        "videos" -> {

                            parentFragmentManager
                                .beginTransaction()
                                .replace(
                                    R.id.mainFragmentContainer,
                                    ScannedVideosFragment()
                                )
                                .addToBackStack(null)
                                .commit()
                        }


                        // =================================================
                        // AUDIO
                        // =================================================

                        "audio" -> {

                            parentFragmentManager
                                .beginTransaction()
                                .replace(
                                    R.id.mainFragmentContainer,
                                    ScannedAudioFragment()
                                )
                                .addToBackStack(null)
                                .commit()
                        }


                        // =================================================
                        // LARGE FILES
                        // =================================================

                        "large_files" -> {

                            parentFragmentManager
                                .beginTransaction()
                                .replace(
                                    R.id.mainFragmentContainer,
                                    ScannedLargeFilesFragment()
                                )
                                .addToBackStack(null)
                                .commit()
                        }


                        // =================================================
                        // JUNK
                        // =================================================

                        "junk" -> {

                            parentFragmentManager
                                .beginTransaction()
                                .replace(
                                    R.id.mainFragmentContainer,
                                    ScannedJunkFilesFragment()
                                )
                                .addToBackStack(null)
                                .commit()
                        }


                        // =================================================
                        // DOCUMENTS
                        // =================================================

                        "documents" -> {

                            parentFragmentManager
                                .beginTransaction()
                                .replace(
                                    R.id.mainFragmentContainer,
                                    ScannedDocumentsFragment()
                                )
                                .addToBackStack(null)
                                .commit()
                        }
                    }
                }
            )


        recyclerView.apply {

            layoutManager =
                LinearLayoutManager(
                    requireContext()
                )

            adapter =
                cleanCategoryAdapter

            isNestedScrollingEnabled =
                false
        }
    }


    // =========================================================
    // Clean Selected Button
    // =========================================================

    private fun setupCleanSelectedButton(
        view: View
    ) {

        val cleanButton =
            view.findViewById<
                    com.google.android.material.button.MaterialButton
                    >(
                R.id.btnCleanSelected
            )


        cleanButton.setOnClickListener {

            val selectedFiles =
                resultsViewModel.getSelectedCleanFiles()

            if (selectedFiles.isEmpty()) {

                MaterialAlertDialogBuilder(
                    requireContext()
                )
                    .setTitle(
                        "Nothing selected"
                    )
                    .setMessage(
                        "Please select files from one or more categories first."
                    )
                    .setPositiveButton(
                        "OK",
                        null
                    )
                    .show()

                return@setOnClickListener
            }

            val selectedCount =
                selectedFiles.size

            val selectedSize =
                resultsViewModel
                    .getSelectedCleanFileTotalSize()

            /*
             * IMPORTANT:
             *
             * Use the same formatter as the Clean card.
             *
             * This prevents the Clean card showing for example
             * "1.8 MB" while the confirmation dialog shows
             * "0.00 GB".
             */
            val sizeText =
                formatSelectedCleanSize(
                    selectedSize
                )

            val message =
                if (selectedCount == 1) {

                    "You selected 1 file ($sizeText)."

                } else {

                    "You selected $selectedCount files ($sizeText)."
                }

            MaterialAlertDialogBuilder(
                requireContext()
            )
                .setTitle(
                    "Clean selected files?"
                )
                .setMessage(
                    "$message\n\n" +
                            "The selected files are ready to be cleaned."
                )
                .setNegativeButton(
                    "Cancel",
                    null
                )
                .setPositiveButton(
                    "Continue"
                ) { _, _ ->

                    startGlobalCleanDeletion(
                        selectedFiles
                    )
                }
                .show()
        }
    }


    // =========================================================
    // Deselect All
    // =========================================================

    private fun setupDeselectAllButton(
        view: View
    ) {

        val deselectAll =
            view.findViewById<TextView>(
                R.id.tvDeselectAll
            )

        deselectAll.setOnClickListener {

            /*
             * Clear ALL selected files from the
             * shared global selection.
             *
             * This affects selections made from:
             * Photos
             * Screenshots
             * Duplicate Photos
             * Videos
             * Audio
             * Large Files
             * Junk
             * Documents
             */
            resultsViewModel.clearSelectedCleanFiles()

            /*
             * Refresh the Clean summary immediately.
             */
            updateSummary(view)
        }
    }


    // =========================================================
    // Global Clean Selected Deletion
    // =========================================================

    private fun startGlobalCleanDeletion(
        selectedFiles:
        List<com.example.phonecleaner.data.model.CleanerFile>
    ) {

        if (selectedFiles.isEmpty()) {
            return
        }


        // =========================================================
        // Direct filesystem files
        // =========================================================

        val directDeleteFiles =
            selectedFiles.filter { file ->

                file.name.endsWith(
                    ".apk",
                    ignoreCase = true
                ) ||
                        file.uri.startsWith(
                            "file://",
                            ignoreCase = true
                        )
            }


        // =========================================================
        // MediaStore files
        // =========================================================

        val mediaStoreFiles =
            selectedFiles.filterNot { file ->

                file.name.endsWith(
                    ".apk",
                    ignoreCase = true
                ) ||
                        file.uri.startsWith(
                            "file://",
                            ignoreCase = true
                        )
            }


        // =========================================================
        // Delete direct filesystem files first
        // =========================================================

        val directDeleteResult =
            deleteGlobalDirectFiles(
                directDeleteFiles
            )


        /*
         * IMPORTANT:
         *
         * Remember successfully deleted direct files.
         *
         * If MediaStore files also exist, these files
         * must be removed from global selection after
         * the Android confirmation finishes.
         */
        pendingGlobalDirectlyDeletedFiles =
            directDeleteResult


        // =========================================================
        // No MediaStore files
        // =========================================================

        if (mediaStoreFiles.isEmpty()) {

            finishGlobalClean(
                directDeleteResult
            )

            pendingGlobalDirectlyDeletedFiles =
                emptyList()

            return
        }


        // =========================================================
        // Android 11+
        // =========================================================

        if (
            Build.VERSION.SDK_INT <
            Build.VERSION_CODES.R
        ) {

            Toast.makeText(
                requireContext(),
                "MediaStore deletion confirmation requires Android 11 or newer.",
                Toast.LENGTH_LONG
            ).show()

            /*
             * The direct files were already deleted,
             * so remove those from global selection.
             */
            finishGlobalClean(
                directDeleteResult
            )

            pendingGlobalDirectlyDeletedFiles =
                emptyList()

            return
        }


        // =========================================================
        // Save MediaStore files waiting for confirmation
        // =========================================================

        pendingGlobalDeletionFiles =
            mediaStoreFiles


        val uris =
            mediaStoreFiles.mapNotNull { file ->

                try {

                    Uri.parse(
                        file.uri
                    )

                } catch (_: Exception) {

                    null
                }
            }


        if (uris.isEmpty()) {

            /*
             * MediaStore deletion could not be started,
             * but direct files were already deleted.
             */
            finishGlobalClean(
                directDeleteResult
            )

            pendingGlobalDeletionFiles =
                emptyList()

            pendingGlobalDirectlyDeletedFiles =
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


            globalDeleteRequestLauncher.launch(
                intentSenderRequest
            )

        } catch (securityException: SecurityException) {

            /*
             * Direct files were already deleted.
             * Remove those from global selection.
             */
            finishGlobalClean(
                directDeleteResult
            )

            pendingGlobalDeletionFiles =
                emptyList()

            pendingGlobalDirectlyDeletedFiles =
                emptyList()

            Toast.makeText(
                requireContext(),
                "Permission is required to delete some selected files.",
                Toast.LENGTH_LONG
            ).show()

        } catch (exception: Exception) {

            /*
             * Direct files were already deleted.
             * Remove those from global selection.
             */
            finishGlobalClean(
                directDeleteResult
            )

            pendingGlobalDeletionFiles =
                emptyList()

            pendingGlobalDirectlyDeletedFiles =
                emptyList()

            Toast.makeText(
                requireContext(),
                "Unable to start file deletion.",
                Toast.LENGTH_LONG
            ).show()
        }
    }


    private fun deleteGlobalDirectFiles(
        files:
        List<com.example.phonecleaner.data.model.CleanerFile>
    ): List<com.example.phonecleaner.data.model.CleanerFile> {

        if (files.isEmpty()) {
            return emptyList()
        }

        val deletedFiles =
            mutableListOf<
                    com.example.phonecleaner.data.model.CleanerFile
                    >()

        files.forEach { cleanerFile ->

            try {

                val uri =
                    Uri.parse(
                        cleanerFile.uri
                    )

                val path =
                    getGlobalFilePathFromUri(
                        uri
                    )

                if (!path.isNullOrBlank()) {

                    val file =
                        File(path)

                    /*
                     * If the file is already gone,
                     * treat it as successfully cleaned.
                     */
                    if (!file.exists()) {

                        deletedFiles.add(
                            cleanerFile
                        )

                    } else if (file.delete()) {

                        /*
                         * Best-effort MediaStore cleanup.
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
                    }
                }

            } catch (_: Exception) {
                // Keep failed files selected.
            }
        }

        return deletedFiles
    }


    private fun getGlobalFilePathFromUri(
        uri: Uri
    ): String? {

        /*
         * file:// URI
         */
        if (
            uri.scheme.equals(
                "file",
                ignoreCase = true
            )
        ) {

            return uri.path
        }

        /*
         * MediaStore URI.
         */
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

        } catch (_: Exception) {

            null
        }
    }


    private fun handleGlobalMediaStoreDeletionSuccess() {

        val deletedMediaStoreFiles =
            pendingGlobalDeletionFiles

        val deletedDirectFiles =
            pendingGlobalDirectlyDeletedFiles


        /*
         * Combine BOTH types:
         *
         * Direct filesystem deletions
         * +
         * MediaStore deletions
         */
        val allDeletedFiles =
            (
                    deletedDirectFiles +
                            deletedMediaStoreFiles
                    )
                .distinctBy {
                    it.uri
                }


        if (allDeletedFiles.isEmpty()) {

            pendingGlobalDeletionFiles =
                emptyList()

            pendingGlobalDirectlyDeletedFiles =
                emptyList()

            return
        }


        /*
         * Remove ALL successfully deleted files
         * from global selectedCleanFiles.
         */
        finishGlobalClean(
            allDeletedFiles
        )


        /*
         * Clear pending state.
         */
        pendingGlobalDeletionFiles =
            emptyList()

        pendingGlobalDirectlyDeletedFiles =
            emptyList()
    }


    private fun finishGlobalClean(
        deletedFiles:
        List<com.example.phonecleaner.data.model.CleanerFile>
    ) {

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
         * Remove these files from the
         * global selected list.
         */
        resultsViewModel
            .removeSelectedCleanFiles(
                deletedUris
            )

        /*
         * Refresh the Clean screen.
         *
         * onResume() will also reload
         * the category counts.
         */
        view?.let { currentView ->

            loadCategories(
                currentView
            )

            updateSummary(
                currentView
            )
        }

        Toast.makeText(
            requireContext(),
            if (deletedFiles.size == 1) {

                "1 file cleaned successfully."

            } else {

                "${deletedFiles.size} files cleaned successfully."
            },
            Toast.LENGTH_SHORT
        ).show()
    }


    // =========================================================
    // Scan Button
    // =========================================================

    private fun setupScanButton(
        view: View
    ) {

        val scanButton =
            view.findViewById<
                    com.google.android.material.button.MaterialButton
                    >(
                R.id.btnCleanScanNow
            )


        scanButton.setOnClickListener {

            parentFragmentManager
                .beginTransaction()
                .replace(
                    R.id.mainFragmentContainer,
                    ScanningFragment()
                )
                .addToBackStack(null)
                .commit()
        }
    }


    // =========================================================
    // Junk Files
    // =========================================================

    private fun getTotalJunkCategoryFiles():
            List<com.example.phonecleaner.data.model.CleanerFile> {

        return resultsViewModel
            .getJunkCategories()
            .flatMap { category ->

                category.files
            }
            .distinctBy { file ->

                file.uri
            }
    }


    // =========================================================
    // Load Categories
    // =========================================================

    private fun loadCategories(
        view: View
    ) {

        // ---------------------------------------------------------
        // Photos
        // ---------------------------------------------------------

        val photoCount =
            resultsViewModel
                .getPhotoCount()

        val photoSize =
            resultsViewModel
                .getTotalSize()


        // ---------------------------------------------------------
        // Screenshots
        // ---------------------------------------------------------

        val screenshotCount =
            resultsViewModel
                .getScreenshotCount()

        val screenshotSize =
            resultsViewModel
                .getScreenshotTotalSize()


        // ---------------------------------------------------------
        // Duplicate Photos
        // ---------------------------------------------------------

        val duplicatePhotoCount =
            resultsViewModel
                .getDuplicatePhotoCount()

        val duplicatePhotoSize =
            resultsViewModel
                .getDuplicateTotalSize()


        // ---------------------------------------------------------
        // Videos
        // ---------------------------------------------------------

        val videoCount =
            resultsViewModel
                .getVideoCount()

        val videoSize =
            resultsViewModel
                .getVideoTotalSize()


        // ---------------------------------------------------------
        // Audio
        // ---------------------------------------------------------

        val audioCount =
            resultsViewModel
                .getAudioCount()

        val audioSize =
            resultsViewModel
                .getAudioTotalSize()


        // ---------------------------------------------------------
        // Large Files
        // ---------------------------------------------------------

        val largeFileCount =
            resultsViewModel
                .getLargeFileCount()

        val largeFileSize =
            resultsViewModel
                .getLargeFileTotalSize()


        // ---------------------------------------------------------
        // Junk
        // ---------------------------------------------------------

        val junkCategoryFiles =
            getTotalJunkCategoryFiles()

        val junkCategoryCount =
            junkCategoryFiles.size

        val junkCategorySize =
            junkCategoryFiles.sumOf {
                it.sizeBytes
            }


        // ---------------------------------------------------------
        // Documents
        // ---------------------------------------------------------

        /*
         * Use the current ViewModel result first.
         *
         * If the current ViewModel result is empty,
         * use the last saved Documents summary.
         *
         * ---------------------------------------------------------
         */

        val documentCount =
            resultsViewModel
                .getDocumentCount()

        val documentSize =
            resultsViewModel
                .getDocumentTotalSize()


        // =========================================================
        // Build Categories
        // =========================================================

        categories =
            listOf(

                // -------------------------------------------------
                // Photos
                // -------------------------------------------------

                CleanCategory(
                    id = "photos",
                    title = "Photos",
                    itemCount = photoCount,
                    sizeBytes = photoSize,
                    iconResId =
                        android.R.drawable
                            .ic_menu_gallery
                ),


                // -------------------------------------------------
                // Screenshots
                // -------------------------------------------------

                CleanCategory(
                    id = "screenshots",
                    title = "Screenshots",
                    itemCount = screenshotCount,
                    sizeBytes = screenshotSize,
                    iconResId =
                        android.R.drawable
                            .ic_menu_camera
                ),


                // -------------------------------------------------
                // Duplicate Photos
                // -------------------------------------------------

                CleanCategory(
                    id = "duplicates",
                    title = "Duplicate photos",
                    itemCount = duplicatePhotoCount,
                    sizeBytes = duplicatePhotoSize,
                    iconResId =
                        android.R.drawable
                            .ic_menu_gallery
                ),


                // -------------------------------------------------
                // Videos
                // -------------------------------------------------

                CleanCategory(
                    id = "videos",
                    title = "Videos",
                    itemCount = videoCount,
                    sizeBytes = videoSize,
                    iconResId =
                        android.R.drawable
                            .ic_media_play
                ),


                // -------------------------------------------------
                // Audio
                // -------------------------------------------------

                CleanCategory(
                    id = "audio",
                    title = "Audio",
                    itemCount = audioCount,
                    sizeBytes = audioSize,
                    iconResId =
                        android.R.drawable
                            .ic_lock_silent_mode_off
                ),


                // -------------------------------------------------
                // Large Files
                // -------------------------------------------------

                CleanCategory(
                    id = "large_files",
                    title = "Large files",
                    itemCount = largeFileCount,
                    sizeBytes = largeFileSize,
                    iconResId =
                        android.R.drawable
                            .ic_menu_save
                ),


                // -------------------------------------------------
                // Junk
                // -------------------------------------------------

                CleanCategory(
                    id = "junk",
                    title = "Junk and cache",
                    itemCount = junkCategoryCount,
                    sizeBytes = junkCategorySize,
                    iconResId =
                        android.R.drawable
                            .ic_menu_delete
                ),


                // -------------------------------------------------
                // Documents
                // -------------------------------------------------

                CleanCategory(
                    id = "documents",
                    title = "Documents",
                    itemCount = documentCount,
                    sizeBytes = documentSize,
                    iconResId =
                        android.R.drawable
                            .ic_menu_edit
                )
            )


        cleanCategoryAdapter.submitList(
            categories
        )

        updateSummary(view)
    }


    // =========================================================
    // Category Selection
    // =========================================================

    private fun updateCategorySelection(
        category: CleanCategory,
        isSelected: Boolean,
        view: View
    ) {

        categories =
            categories.map {

                if (it.id == category.id) {

                    it.copy(
                        isSelected = isSelected
                    )

                } else {

                    it
                }
            }


        cleanCategoryAdapter.submitList(
            categories
        )

        updateSummary(view)
    }


    // =========================================================
    // Clean Summary
    // =========================================================

    private fun updateSummary(
        view: View
    ) {

        val selectedFiles =
            resultsViewModel
                .getSelectedCleanFiles()

        val selectedCount =
            resultsViewModel
                .getSelectedCleanFileCount()

        val totalSize =
            resultsViewModel
                .getSelectedCleanFileTotalSize()


        /*
         * Show the actual selected size.
         *
         * GB  -> 2 decimal places
         * MB  -> 1 decimal place
         * KB  -> whole number
         * B   -> bytes
         */
        val sizeText =
            formatSelectedCleanSize(
                totalSize
            )


        view.findViewById<TextView>(
            R.id.tvCleanableSize
        ).text =
            sizeText


        view.findViewById<TextView>(
            R.id.tvCleanableDescription
        ).text =
            if (selectedCount == 0) {

                "No files selected"

            } else if (selectedCount == 1) {

                "1 file selected"

            } else {

                "$selectedCount files selected"
            }


        val cleanButton =
            view.findViewById<
                    com.google.android.material.button.MaterialButton
                    >(
                R.id.btnCleanSelected
            )


        cleanButton.isEnabled =
            selectedFiles.isNotEmpty()


        cleanButton.alpha =
            if (selectedFiles.isNotEmpty()) {

                1.0f

            } else {

                0.5f
            }


        /*
         * =========================================================
         * Deselect All visibility
         * =========================================================
         *
         * Show the button only when at least one file
         * is selected globally.
         */
        val deselectAll =
            view.findViewById<TextView>(
                R.id.tvDeselectAll
            )

        deselectAll.visibility =
            if (selectedCount > 0) {
                View.VISIBLE
            } else {
                View.GONE
            }
    }


    // =========================================================
    // Selected Clean Size Formatter
    // =========================================================

    private fun formatSelectedCleanSize(
        bytes: Long
    ): String {

        if (bytes <= 0L) {
            return "0 MB"
        }

        val kb =
            bytes / 1000.0

        val mb =
            bytes / 1_000_000.0

        val gb =
            bytes / 1_000_000_000.0

        return when {

            gb >= 1.0 -> {

                String.format(
                    java.util.Locale.US,
                    "%.2f GB",
                    gb
                )
            }


            mb >= 1.0 -> {

                String.format(
                    java.util.Locale.US,
                    "%.1f MB",
                    mb
                )
            }


            kb >= 1.0 -> {

                String.format(
                    java.util.Locale.US,
                    "%.0f KB",
                    kb
                )
            }


            else -> {

                "$bytes B"
            }
        }
    }
}