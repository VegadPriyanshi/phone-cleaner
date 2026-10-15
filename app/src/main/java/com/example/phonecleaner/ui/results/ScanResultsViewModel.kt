package com.example.phonecleaner.ui.results

import androidx.lifecycle.ViewModel
import com.example.phonecleaner.data.model.CleanerFile
import com.example.phonecleaner.data.model.DuplicatePhotoGroup
import com.example.phonecleaner.data.model.JunkCategory

class ScanResultsViewModel : ViewModel() {

    private var scannedFiles: List<CleanerFile> = emptyList()

    private var screenshotFiles: List<CleanerFile> = emptyList()

    private var videoFiles: List<CleanerFile> = emptyList()

    private var audioFiles: List<CleanerFile> = emptyList()

    private var largeFiles: List<CleanerFile> = emptyList()

    private var junkFiles: List<CleanerFile> = emptyList()

    private var junkCategories: List<JunkCategory> = emptyList()


    private var duplicatePhotoGroups: List<DuplicatePhotoGroup> = emptyList()

    fun setDuplicatePhotoGroups(
        groups: List<DuplicatePhotoGroup>
    ) {
        duplicatePhotoGroups = groups.toList()
    }

    fun getDuplicatePhotoGroups(): List<DuplicatePhotoGroup> {
        return duplicatePhotoGroups
    }

    fun getDuplicatePhotoCount(): Int {
        return duplicatePhotoGroups.sumOf {
            it.files.size
        }
    }

    fun getDuplicateGroupCount(): Int {
        return duplicatePhotoGroups.size
    }

    fun getDuplicateTotalSize(): Long {
        return duplicatePhotoGroups
            .sumOf { group ->
                group.files
                    .sumOf { it.sizeBytes }
            }
    }

    fun setScannedFiles(
        files: List<CleanerFile>
    ) {
        scannedFiles = files.toList()
    }

    fun getScannedFiles(): List<CleanerFile> {
        return scannedFiles
    }

    fun hasResults(): Boolean {
        return scannedFiles.isNotEmpty()
    }

    fun getPhotoCount(): Int {
        return scannedFiles.size
    }

    fun getTotalSize(): Long {
        return scannedFiles.sumOf {
            it.sizeBytes
        }
    }

    fun setScreenshotFiles(
        files: List<CleanerFile>
    ) {
        screenshotFiles = files.toList()
    }

    fun getScreenshotFiles(): List<CleanerFile> {
        return screenshotFiles
    }

    fun getScreenshotCount(): Int {
        return screenshotFiles.size
    }

    fun getScreenshotTotalSize(): Long {
        return screenshotFiles.sumOf {
            it.sizeBytes
        }
    }

    fun updateScannedFiles(
        updatedFiles: List<CleanerFile>
    ) {
        scannedFiles = updatedFiles
    }

    fun updateScreenshotFiles(
        updatedFiles: List<CleanerFile>
    ) {
        screenshotFiles = updatedFiles
    }


    fun setVideoFiles(
        files: List<CleanerFile>
    ) {
        videoFiles = files.toList()
    }

    fun getVideoFiles(): List<CleanerFile> {
        return videoFiles
    }

    fun getVideoCount(): Int {
        return videoFiles.size
    }

    fun getVideoTotalSize(): Long {
        return videoFiles.sumOf {
            it.sizeBytes
        }
    }

    fun updateVideoFiles(
        updatedFiles: List<CleanerFile>
    ) {
        videoFiles = updatedFiles
    }

    fun setAudioFiles(files: List<CleanerFile>) {
        audioFiles = files.toList()
    }

    fun getAudioFiles(): List<CleanerFile> = audioFiles

    fun getAudioCount(): Int = audioFiles.size

    fun getAudioTotalSize(): Long = audioFiles.sumOf { it.sizeBytes }

    fun updateAudioFiles(updatedFiles: List<CleanerFile>) {
        audioFiles = updatedFiles
    }


    fun setLargeFiles(files: List<CleanerFile>) {
        largeFiles = files.toList()
    }

    fun getLargeFiles(): List<CleanerFile> = largeFiles

    fun getLargeFileCount(): Int = largeFiles.size

    fun getLargeFileTotalSize(): Long =
        largeFiles.sumOf { it.sizeBytes }

    fun updateLargeFiles(updatedFiles: List<CleanerFile>) {
        largeFiles = updatedFiles
    }


    fun setJunkFiles(files: List<CleanerFile>) {
        junkFiles = files.toList()
    }

    fun setJunkCategories(categories: List<JunkCategory>) {
        junkCategories = categories.toList()
    }

    fun getJunkCategories(): List<JunkCategory> {
        return junkCategories
    }

    fun getJunkFiles(): List<CleanerFile> =
        junkFiles

    fun getJunkFileCount(): Int =
        junkFiles.size

    fun getJunkFileTotalSize(): Long =
        junkFiles.sumOf {
            it.sizeBytes
        }

    fun updateJunkFiles(
        updatedFiles: List<CleanerFile>
    ) {
        junkFiles = updatedFiles
    }


    fun getOverallScanCount(): Int {
        return scannedFiles.size +
                videoFiles.size +
                audioFiles.size
    }

    fun getOverallScanSize(): Long {
        return scannedFiles.sumOf { it.sizeBytes } +
                videoFiles.sumOf { it.sizeBytes } +
                audioFiles.sumOf { it.sizeBytes }
    }


    // Documents
    private var documentFiles =
        emptyList<CleanerFile>()

    fun setDocumentFiles(
        files: List<CleanerFile>
    ) {
        documentFiles =
            files.toList()
    }

    fun getDocumentFiles(): List<CleanerFile> =
        documentFiles

    fun getDocumentCount(): Int =
        documentFiles.size

    fun getDocumentTotalSize(): Long =
        documentFiles.sumOf {
            it.sizeBytes
        }

    fun updateDocumentFiles(
        files: List<CleanerFile>
    ) {
        documentFiles =
            files.toList()
    }



    // =========================================================
    // Global Clean Selected Files
    // =========================================================
    //
    // Keeps the files selected by the user across all
    // cleaner result screens.
    //
    // Example:
    //
    // Photos       -> 3 selected
    // Videos       -> 2 selected
    // Documents    -> 1 selected
    //
    // Clean tab can then show:
    //
    // 3.25 GB
    // can be freed across 3 categories
    //
    // =========================================================

    private val selectedCleanFiles =
        linkedMapOf<String, CleanerFile>()


    /**
     * Add or remove a file from the global
     * Clean Selected list.
     */
    fun setCleanFileSelected(
        file: CleanerFile,
        selected: Boolean
    ) {

        if (selected) {

            selectedCleanFiles[
                file.uri
            ] = file

        } else {

            selectedCleanFiles.remove(
                file.uri
            )
        }
    }


    /**
     * Returns all files currently selected
     * for the global Clean action.
     */
    fun getSelectedCleanFiles():
            List<CleanerFile> {

        return selectedCleanFiles.values.toList()
    }


    /**
     * Number of files currently selected.
     */
    fun getSelectedCleanFileCount(): Int {

        return selectedCleanFiles.size
    }


    /**
     * Total size of all globally selected files.
     */
    fun getSelectedCleanFileTotalSize(): Long {

        return selectedCleanFiles.values.sumOf {
            it.sizeBytes
        }
    }


    /**
     * Check whether a particular file is
     * currently selected.
     */
    fun isCleanFileSelected(
        uri: String
    ): Boolean {

        return selectedCleanFiles.containsKey(
            uri
        )
    }


    /**
     * Remove files from the global selection
     * after they have been successfully deleted.
     */
    fun removeSelectedCleanFiles(
        deletedUris: Set<String>
    ) {

        deletedUris.forEach { uri ->

            selectedCleanFiles.remove(
                uri
            )
        }
    }


    /**
     * Clear every globally selected file.
     */
    fun clearSelectedCleanFiles() {

        selectedCleanFiles.clear()
    }


}