package com.example.phonecleaner.ui.scanning

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView

import androidx.activity.OnBackPressedCallback
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope

import com.example.phonecleaner.R
import com.example.phonecleaner.data.scanner.DocumentScanner
import com.example.phonecleaner.data.scanner.DuplicatePhotoScanner
import com.example.phonecleaner.data.scanner.InstalledAppScanner
import com.example.phonecleaner.data.scanner.JunkCategoryScanner
import com.example.phonecleaner.data.scanner.JunkFileScanner
import com.example.phonecleaner.data.scanner.LeftoverAppDataScanner
import com.example.phonecleaner.data.scanner.OldApkScanner
import com.example.phonecleaner.data.scanner.ThumbnailCacheScanner
import com.example.phonecleaner.domain.usecase.ScanPhoneUseCase
import com.example.phonecleaner.ui.results.ScanResultsFragment
import com.example.phonecleaner.ui.results.ScanResultsViewModel
import com.example.phonecleaner.utils.PermissionHelper
import com.example.phonecleaner.utils.ScanSummaryPreferences
import com.google.android.material.button.MaterialButton
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.google.android.material.progressindicator.LinearProgressIndicator

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

import java.util.Locale
import kotlin.math.roundToInt


class ScanningFragment : Fragment() {

    private var scanJob: Job? = null

    private var scanCompleted = false


    private lateinit var scanUseCase: ScanPhoneUseCase


    private lateinit var scanProgress: LinearProgressIndicator

    private lateinit var circularProgress: CircularProgressIndicator

    private lateinit var categoryProgress: LinearProgressIndicator


    private lateinit var percentageText: TextView

    private lateinit var itemsText: TextView

    private lateinit var sizeText: TextView

    private lateinit var statusText: TextView


    private lateinit var currentCategoryText: TextView

    private lateinit var currentCategoryPercentText: TextView


    private lateinit var currentCategoryLabel: TextView

    private lateinit var currentCategoryPercentDisplay: TextView


    private lateinit var stagePhotosIcon: TextView

    private lateinit var stagePhotosStatus: TextView


    private lateinit var stageScreenshotsIcon: TextView

    private lateinit var stageScreenshotsStatus: TextView


    private lateinit var stageVideosIcon: TextView

    private lateinit var stageVideosStatus: TextView


    private lateinit var stageAudioIcon: TextView

    private lateinit var stageAudioStatus: TextView


    private lateinit var scanIcon: ImageView

    private lateinit var cancelButton: MaterialButton

    private lateinit var backButton: ImageButton


    private lateinit var resultsViewModel: ScanResultsViewModel


    // =========================================================
    // CREATE VIEW
    // =========================================================

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        return inflater.inflate(
            R.layout.fragment_scanning,
            container,
            false
        )
    }


    // =========================================================
    // VIEW CREATED
    // =========================================================

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {

        super.onViewCreated(
            view,
            savedInstanceState
        )


        initializeViews(
            view
        )


        scanUseCase =
            ScanPhoneUseCase(
                requireContext()
            )


        resultsViewModel =
            ViewModelProvider(
                requireActivity()
            ).get(
                ScanResultsViewModel::class.java
            )


        setupBackButton()

        setupCancelButton()

        setupSystemBackNavigation()

        initializeStageUI()


        if (scanCompleted) {

            finishImageScan()

        } else {

            setCancelButtonAppearance()

            startScanningAnimation()
        }
    }


    // =========================================================
    // INITIALIZE VIEWS
    // =========================================================

    private fun initializeViews(
        view: View
    ) {

        backButton =
            view.findViewById(
                R.id.btnScanningBack
            )


        backButton.imageTintList =
            ColorStateList.valueOf(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.text_primary
                )
            )


        scanProgress =
            view.findViewById(
                R.id.scanningProgress
            )


        circularProgress =
            view.findViewById(
                R.id.scanCircularProgress
            )


        categoryProgress =
            view.findViewById(
                R.id.categoryProgress
            )


        percentageText =
            view.findViewById(
                R.id.tvScanningPercentage
            )


        itemsText =
            view.findViewById(
                R.id.tvScanningItems
            )


        sizeText =
            view.findViewById(
                R.id.tvScanningSize
            )


        statusText =
            view.findViewById(
                R.id.tvScanningStatus
            )


        currentCategoryText =
            view.findViewById(
                R.id.tvCurrentCategory
            )


        currentCategoryPercentText =
            view.findViewById(
                R.id.tvCurrentCategoryPercent
            )


        currentCategoryLabel =
            view.findViewById(
                R.id.tvCurrentCategoryLabel
            )


        currentCategoryPercentDisplay =
            view.findViewById(
                R.id.tvCurrentCategoryPercentDisplay
            )


        stagePhotosIcon =
            view.findViewById(
                R.id.tvStagePhotosIcon
            )


        stagePhotosStatus =
            view.findViewById(
                R.id.tvStagePhotosStatus
            )


        stageScreenshotsIcon =
            view.findViewById(
                R.id.tvStageScreenshotsIcon
            )


        stageScreenshotsStatus =
            view.findViewById(
                R.id.tvStageScreenshotsStatus
            )


        stageVideosIcon =
            view.findViewById(
                R.id.tvStageVideosIcon
            )


        stageVideosStatus =
            view.findViewById(
                R.id.tvStageVideosStatus
            )


        stageAudioIcon =
            view.findViewById(
                R.id.tvStageAudioIcon
            )


        stageAudioStatus =
            view.findViewById(
                R.id.tvStageAudioStatus
            )


        scanIcon =
            view.findViewById(
                R.id.ivScanIcon
            )


        cancelButton =
            view.findViewById(
                R.id.btnCancelScan
            )
    }


    // =========================================================
    // JUNK SUMMARY
    // =========================================================

    private fun saveJunkFilesSummary() {

        val context =
            requireContext()


        val categoryFiles =
            resultsViewModel
                .getJunkCategories()
                .flatMap { category ->
                    category.files
                }
                .distinctBy { file ->
                    file.uri
                }


        val junkFiles =
            if (
                categoryFiles.isNotEmpty()
            ) {

                categoryFiles

            } else {

                resultsViewModel
                    .getJunkFiles()
                    .distinctBy { file ->
                        file.uri
                    }
            }


        val count =
            junkFiles.size


        val sizeBytes =
            junkFiles.sumOf {
                it.sizeBytes
            }


        ScanSummaryPreferences
            .saveJunkFileSummary(
                context = context,
                count = count,
                sizeBytes = sizeBytes
            )


        android.util.Log.d(
            "PhoneCleaner",
            "JUNK_SUMMARY_SAVED: " +
                    "$count files | " +
                    "$sizeBytes bytes"
        )
    }


    // =========================================================
    // SAVE SCAN SUMMARY
    // =========================================================

    private fun saveScanSummary() {

        val context =
            requireContext()


        // -----------------------------------------------------
        // PHOTOS
        // -----------------------------------------------------

        ScanSummaryPreferences
            .savePhotoSummary(
                context = context,
                count =
                    resultsViewModel.getPhotoCount(),
                sizeBytes =
                    resultsViewModel.getTotalSize()
            )


        // -----------------------------------------------------
        // SCREENSHOTS
        // -----------------------------------------------------

        val screenshotFiles =
            resultsViewModel
                .getScreenshotFiles()


        ScanSummaryPreferences
            .saveScreenshotSummary(
                context = context,
                count =
                    screenshotFiles.size,
                sizeBytes =
                    screenshotFiles.sumOf {
                        it.sizeBytes
                    }
            )


        // -----------------------------------------------------
        // VIDEOS
        // -----------------------------------------------------

        ScanSummaryPreferences
            .saveVideoSummary(
                context = context,
                count =
                    resultsViewModel.getVideoCount(),
                sizeBytes =
                    resultsViewModel.getVideoTotalSize()
            )


        // -----------------------------------------------------
        // AUDIO
        // -----------------------------------------------------

        val audioFiles =
            resultsViewModel
                .getAudioFiles()


        ScanSummaryPreferences
            .saveAudioSummary(
                context = context,
                count =
                    audioFiles.size,
                sizeBytes =
                    audioFiles.sumOf {
                        it.sizeBytes
                    }
            )


        // -----------------------------------------------------
        // DUPLICATE PHOTOS
        // -----------------------------------------------------

        ScanSummaryPreferences
            .saveDuplicateSummary(
                context = context,
                groupCount =
                    resultsViewModel
                        .getDuplicateGroupCount(),
                photoCount =
                    resultsViewModel
                        .getDuplicatePhotoCount()
            )


        // -----------------------------------------------------
        // LARGE FILES
        // -----------------------------------------------------

        ScanSummaryPreferences
            .saveLargeFileSummary(
                context = context,
                count =
                    resultsViewModel
                        .getLargeFileCount(),
                sizeBytes =
                    resultsViewModel
                        .getLargeFileTotalSize()
            )
    }


    // =========================================================
    // BACK BUTTON
    // =========================================================

    private fun setupBackButton() {

        backButton.setOnClickListener {

            goBack()
        }
    }


    private fun setupSystemBackNavigation() {

        requireActivity()
            .onBackPressedDispatcher
            .addCallback(
                viewLifecycleOwner,
                object : OnBackPressedCallback(true) {

                    override fun handleOnBackPressed() {

                        goBack()
                    }
                }
            )
    }


    private fun goBack() {

        scanJob?.cancel()

        scanJob = null

        scanIcon.clearAnimation()


        if (
            parentFragmentManager
                .backStackEntryCount > 0
        ) {

            parentFragmentManager
                .popBackStack()

        } else {

            requireActivity()
                .onBackPressedDispatcher
                .onBackPressed()
        }
    }


    // =========================================================
    // CANCEL BUTTON
    // =========================================================

    private fun setupCancelButton() {

        cancelButton.setOnClickListener {

            cancelScan()
        }
    }


    private fun setCancelButtonAppearance() {

        cancelButton.text =
            "Cancel Scan"


        cancelButton.setBackgroundTintList(
            ColorStateList.valueOf(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.clean_secondary_button
                )
            )
        )


        cancelButton.setTextColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.primary
            )
        )
    }


    private fun setViewResultsButtonAppearance() {

        cancelButton.text =
            "View Scan Results"


        cancelButton.setBackgroundTintList(
            ColorStateList.valueOf(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.primary
                )
            )
        )


        cancelButton.setTextColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.text_on_primary
            )
        )
    }


    // =========================================================
    // START SCANNING
    // =========================================================

    @RequiresApi(Build.VERSION_CODES.R)
    private fun startScanningAnimation() {

        val pulseAnimation =
            AnimationUtils.loadAnimation(
                requireContext(),
                R.anim.scan_pulse
            )


        scanIcon.startAnimation(
            pulseAnimation
        )


        /*
         * IMPORTANT:
         *
         * HomeFragment has already completed
         * the complete permission flow.
         *
         * Therefore ScanningFragment does NOT request:
         *
         * - Photos
         * - Videos
         * - Audio
         * - All Files Access
         *
         * It immediately starts the existing scanner.
         */

        startRealImageScan()
    }


    // =========================================================
    // STAGE UI
    // =========================================================

    private fun initializeStageUI() {

        setStageWaiting(
            stagePhotosIcon,
            stagePhotosStatus
        )


        setStageWaiting(
            stageScreenshotsIcon,
            stageScreenshotsStatus
        )


        setStageWaiting(
            stageVideosIcon,
            stageVideosStatus
        )


        setStageWaiting(
            stageAudioIcon,
            stageAudioStatus
        )


        currentCategoryLabel.text =
            "Preparing scan..."


        currentCategoryPercentDisplay.text =
            "0%"
    }


    private fun setStageWaiting(
        icon: TextView,
        status: TextView
    ) {

        icon.animate().cancel()

        icon.scaleX = 1f

        icon.scaleY = 1f

        icon.text = "✓"

        icon.alpha = 0.35f

        status.text = "Waiting"

        status.alpha = 0.65f

        status.setTextColor(
            Color.parseColor(
                "#8A8496"
            )
        )
    }


    // =========================================================
    // REAL SCAN
    // =========================================================

    @RequiresApi(Build.VERSION_CODES.R)
    private fun startRealImageScan() {

        scanJob?.cancel()


        scanIcon.startAnimation(
            AnimationUtils.loadAnimation(
                requireContext(),
                R.anim.scan_pulse
            )
        )


        setCancelButtonAppearance()


        cancelButton.setOnClickListener {

            cancelScan()
        }


        initializeStageUI()


        statusText.text =
            "Scanning your phone..."


        currentCategoryText.text =
            "Scanning photos..."


        currentCategoryLabel.text =
            "Scanning photos..."


        currentCategoryPercentText.text =
            "0%"


        currentCategoryPercentDisplay.text =
            "0%"


        setStageScanning(
            stagePhotosIcon,
            stagePhotosStatus
        )


        scanProgress.setProgress(
            0,
            false
        )


        circularProgress.setProgress(
            0,
            false
        )


        categoryProgress.setProgress(
            0,
            false
        )


        scanJob =
            viewLifecycleOwner.lifecycleScope.launch {

                // =================================================
                // PHOTO SCANNER
                // =================================================

                val scannedFiles =
                    scanUseCase.scanImages {
                            scanned,
                            total,
                            sizeBytes ->

                        if (!isAdded) {
                            return@scanImages
                        }


                        val progress =
                            if (total > 0) {

                                (
                                        (
                                                scanned.toFloat() /
                                                        total
                                                ) * 100
                                        )
                                    .roundToInt()
                                    .coerceIn(
                                        0,
                                        100
                                    )

                            } else {

                                0
                            }


                        requireActivity()
                            .runOnUiThread {

                                if (!isAdded) {
                                    return@runOnUiThread
                                }


                                updateRealProgress(
                                    progress =
                                        progress,
                                    scanned =
                                        scanned,
                                    total =
                                        total,
                                    sizeBytes =
                                        sizeBytes
                                )
                            }
                    }


                resultsViewModel.setScannedFiles(
                    scannedFiles
                )


                // =================================================
                // DUPLICATE PHOTO SCANNER
                // =================================================

                val duplicateGroups =
                    DuplicatePhotoScanner
                        .findDuplicateGroups(
                            requireContext(),
                            photos =
                                scannedFiles
                        )


                resultsViewModel
                    .setDuplicatePhotoGroups(
                        duplicateGroups
                    )


                // =================================================
                // PHOTOS COMPLETE
                // =================================================

                setStageComplete(
                    stagePhotosIcon,
                    stagePhotosStatus
                )


                // =================================================
                // DOCUMENT SCANNER
                // =================================================

                currentCategoryText.text =
                    "Scanning documents..."

                currentCategoryLabel.text =
                    "Scanning documents..."

                currentCategoryPercentText.text =
                    "100%"

                currentCategoryPercentDisplay.text =
                    "100%"

                statusText.text =
                    "Scanning your documents..."


                val documentFiles =
                    withContext(
                        Dispatchers.IO
                    ) {

                        DocumentScanner
                            .scanDocuments(
                                requireContext()
                            )
                    }


                resultsViewModel
                    .setDocumentFiles(
                        documentFiles
                    )


                ScanSummaryPreferences
                    .saveDocumentSummary(
                        context =
                            requireContext(),
                        count =
                            documentFiles.size,
                        sizeBytes =
                            documentFiles.sumOf {
                                it.sizeBytes
                            }
                    )


                android.util.Log.d(
                    "PhoneCleaner",
                    "Detected documents: " +
                            documentFiles.size
                )


                // =================================================
                // SCREENSHOT SCANNER
                // =================================================

                currentCategoryText.text =
                    "Scanning screenshots..."

                currentCategoryLabel.text =
                    "Scanning screenshots..."

                currentCategoryPercentText.text =
                    "100%"

                currentCategoryPercentDisplay.text =
                    "100%"


                setStageScanning(
                    stageScreenshotsIcon,
                    stageScreenshotsStatus
                )


                statusText.text =
                    "Scanning your phone..."


                val screenshotFiles =
                    scanUseCase.scanScreenshots()


                resultsViewModel
                    .setScreenshotFiles(
                        screenshotFiles
                    )


                android.util.Log.d(
                    "PhoneCleaner",
                    "Detected screenshots: " +
                            screenshotFiles.size
                )


                setStageComplete(
                    stageScreenshotsIcon,
                    stageScreenshotsStatus
                )


                // =================================================
                // VIDEO SCANNER
                // =================================================

                currentCategoryText.text =
                    "Scanning videos..."

                currentCategoryLabel.text =
                    "Scanning videos..."

                currentCategoryPercentText.text =
                    "100%"

                currentCategoryPercentDisplay.text =
                    "100%"


                setStageScanning(
                    stageVideosIcon,
                    stageVideosStatus
                )


                statusText.text =
                    "Scanning your phone..."


                if (
                    PermissionHelper
                        .hasVideosPermission(
                            requireContext()
                        )
                ) {

                    val videoFiles =
                        withContext(
                            Dispatchers.IO
                        ) {

                            scanUseCase
                                .scanVideos()
                        }


                    resultsViewModel
                        .setVideoFiles(
                            videoFiles
                        )


                    ScanSummaryPreferences
                        .saveVideoSummary(
                            context =
                                requireContext(),
                            count =
                                videoFiles.size,
                            sizeBytes =
                                videoFiles.sumOf {
                                    it.sizeBytes
                                }
                        )


                    android.util.Log.d(
                        "PhoneCleaner",
                        "Detected videos: " +
                                videoFiles.size
                    )


                    setStageComplete(
                        stageVideosIcon,
                        stageVideosStatus
                    )

                } else {

                    resultsViewModel
                        .setVideoFiles(
                            emptyList()
                        )


                    ScanSummaryPreferences
                        .saveVideoSummary(
                            context =
                                requireContext(),
                            count = 0,
                            sizeBytes = 0L
                        )


                    setStageSkipped(
                        stageVideosIcon,
                        stageVideosStatus
                    )
                }


                // =================================================
                // AUDIO SCANNER
                // =================================================

                currentCategoryText.text =
                    "Scanning audio..."

                currentCategoryLabel.text =
                    "Scanning audio..."

                currentCategoryPercentText.text =
                    "100%"

                currentCategoryPercentDisplay.text =
                    "100%"


                setStageScanning(
                    stageAudioIcon,
                    stageAudioStatus
                )


                statusText.text =
                    "Scanning your phone..."


                if (
                    PermissionHelper
                        .hasAudioPermission(
                            requireContext()
                        )
                ) {

                    val audioFiles =
                        withContext(
                            Dispatchers.IO
                        ) {

                            scanUseCase
                                .scanAudio()
                        }


                    resultsViewModel
                        .setAudioFiles(
                            audioFiles
                        )


                    android.util.Log.d(
                        "PhoneCleaner",
                        "Detected audio files: " +
                                audioFiles.size
                    )


                    setStageComplete(
                        stageAudioIcon,
                        stageAudioStatus
                    )

                } else {

                    resultsViewModel
                        .setAudioFiles(
                            emptyList()
                        )


                    ScanSummaryPreferences
                        .saveAudioSummary(
                            context =
                                requireContext(),
                            count = 0,
                            sizeBytes = 0L
                        )


                    setStageSkipped(
                        stageAudioIcon,
                        stageAudioStatus
                    )
                }


                // =================================================
                // LARGE FILE SCANNER
                // =================================================

                val largeFiles =
                    withContext(
                        Dispatchers.IO
                    ) {

                        scanUseCase
                            .scanLargeFiles()
                    }


                resultsViewModel
                    .setLargeFiles(
                        largeFiles
                    )


                android.util.Log.d(
                    "PhoneCleaner",
                    "Large files detected: " +
                            largeFiles.size
                )


                // =================================================
                // JUNK FILE SCANNER
                // =================================================

                currentCategoryText.text =
                    "Scanning junk files..."

                currentCategoryLabel.text =
                    "Scanning junk files..."

                currentCategoryPercentText.text =
                    "100%"

                currentCategoryPercentDisplay.text =
                    "100%"


                statusText.text =
                    "Scanning your junk files..."


                val junkFiles =
                    if (
                        Build.VERSION.SDK_INT >=
                        Build.VERSION_CODES.R
                    ) {

                        withContext(
                            Dispatchers.IO
                        ) {

                            JunkFileScanner
                                .scanJunkFiles(
                                    requireContext()
                                )
                        }

                    } else {

                        emptyList()
                    }


                // =================================================
                // OLD APK SCANNER
                // =================================================

                val oldApkFiles =
                    if (
                        Build.VERSION.SDK_INT >=
                        Build.VERSION_CODES.R
                    ) {

                        withContext(
                            Dispatchers.IO
                        ) {

                            OldApkScanner
                                .scanOldApkFiles(
                                    requireContext()
                                )
                        }

                    } else {

                        emptyList()
                    }


                android.util.Log.d(
                    "PhoneCleaner",
                    "Old APK files found: " +
                            oldApkFiles.size
                )


                // =================================================
                // THUMBNAIL CACHE SCANNER
                // =================================================

                val thumbnailCacheFiles =
                    withContext(
                        Dispatchers.IO
                    ) {

                        ThumbnailCacheScanner
                            .scanThumbnailCaches(
                                requireContext()
                            )
                    }


                // =================================================
                // LEFTOVER APP DATA SCANNER
                // =================================================

                val leftoverAppDataFiles =
                    withContext(
                        Dispatchers.IO
                    ) {

                        LeftoverAppDataScanner
                            .scanLeftoverAppData(
                                requireContext()
                            )
                    }


                // =================================================
                // SAVE JUNK RESULTS
                // =================================================

                resultsViewModel
                    .setJunkFiles(
                        junkFiles
                    )


                val junkCategories =
                    JunkCategoryScanner
                        .createCategories(
                            junkFiles =
                                junkFiles,
                            oldApkFiles =
                                oldApkFiles,
                            thumbnailCacheFiles =
                                thumbnailCacheFiles,
                            leftoverAppDataFiles =
                                leftoverAppDataFiles
                        )


                resultsViewModel
                    .setJunkCategories(
                        junkCategories
                    )


                saveJunkFilesSummary()


                // =================================================
                // FINISH
                // =================================================

                finishImageScan()
            }
    }


    // =========================================================
    // PROGRESS
    // =========================================================

    private fun updateRealProgress(
        progress: Int,
        scanned: Int,
        total: Int,
        sizeBytes: Long
    ) {

        scanProgress.setProgress(
            progress,
            true
        )


        circularProgress.setProgress(
            progress,
            false
        )


        categoryProgress.setProgress(
            progress,
            true
        )


        percentageText.text =
            "$progress%"


        itemsText.text =
            String.format(
                Locale.US,
                "%,d",
                scanned
            )


        sizeText.text =
            formatDataSize(
                sizeBytes
            )


        currentCategoryPercentText.text =
            "$progress%"


        currentCategoryPercentDisplay.text =
            "$progress%"


        currentCategoryLabel.text =
            "Scanning photos..."


        statusText.text =
            "Scanning your phone..."
    }


    // =========================================================
    // FINISH SCAN
    // =========================================================

    private fun finishImageScan() {

        scanCompleted = true


        /*
         * Save all normal scan summaries.
         */

        saveScanSummary()


        /*
         * Calculate Apps size ONLY after
         * the real scan has completed.
         *
         * HomeFragment never scans installed apps.
         */

        viewLifecycleOwner.lifecycleScope.launch {

            val appSize =
                withContext(
                    Dispatchers.IO
                ) {

                    InstalledAppScanner
                        .scan(
                            requireContext()
                        )
                        .sumOf { app ->

                            app.sizeBytes
                        }
                }


            ScanSummaryPreferences
                .saveAppSize(
                    context =
                        requireContext(),
                    sizeBytes =
                        appSize
                )


            /*
             * Mark scan completed only after
             * all summary data has been saved.
             */

            ScanSummaryPreferences
                .markScanCompleted(
                    requireContext()
                )


            android.util.Log.d(
                "PhoneCleaner",
                "APP_SIZE_SAVED: " +
                        "$appSize bytes"
            )


            android.util.Log.d(
                "PhoneCleaner",
                "SCAN_SUMMARY_COMPLETED"
            )
        }


        if (!isAdded) {
            return
        }


        val overallCount =
            resultsViewModel
                .getOverallScanCount()


        val overallSize =
            resultsViewModel
                .getOverallScanSize()


        android.util.Log.d(
            "PhoneCleaner",
            "Overall scan results: " +
                    "$overallCount items"
        )


        android.util.Log.d(
            "PhoneCleaner",
            "Overall scan size: " +
                    "$overallSize bytes"
        )


        scanIcon.clearAnimation()


        scanProgress.setProgress(
            100,
            true
        )


        circularProgress.setProgress(
            100,
            true
        )


        categoryProgress.setProgress(
            100,
            true
        )


        percentageText.text =
            "100%"


        itemsText.text =
            String.format(
                Locale.US,
                "%,d",
                overallCount
            )


        sizeText.text =
            formatDataSize(
                overallSize
            )


        statusText.text =
            "Scan completed"


        currentCategoryText.text =
            "Your phone has been scanned"


        currentCategoryLabel.text =
            "Scan complete"


        currentCategoryPercentText.text =
            "100%"


        currentCategoryPercentDisplay.text =
            "100%"


        // -----------------------------------------------------
        // PHOTOS
        // -----------------------------------------------------

        setStageComplete(
            stagePhotosIcon,
            stagePhotosStatus
        )


        // -----------------------------------------------------
        // SCREENSHOTS
        // -----------------------------------------------------

        setStageComplete(
            stageScreenshotsIcon,
            stageScreenshotsStatus
        )


        // -----------------------------------------------------
        // VIDEOS
        // -----------------------------------------------------

        if (
            PermissionHelper
                .hasVideosPermission(
                    requireContext()
                )
        ) {

            setStageComplete(
                stageVideosIcon,
                stageVideosStatus
            )

        } else {

            setStageSkipped(
                stageVideosIcon,
                stageVideosStatus
            )
        }


        // -----------------------------------------------------
        // AUDIO
        // -----------------------------------------------------

        if (
            PermissionHelper
                .hasAudioPermission(
                    requireContext()
                )
        ) {

            setStageComplete(
                stageAudioIcon,
                stageAudioStatus
            )

        } else {

            setStageSkipped(
                stageAudioIcon,
                stageAudioStatus
            )
        }


        setViewResultsButtonAppearance()


        cancelButton.setOnClickListener {

            openScanResults()
        }
    }


    // =========================================================
    // STAGE SCANNING
    // =========================================================

    private fun setStageScanning(
        icon: TextView,
        status: TextView
    ) {

        icon.animate().cancel()

        icon.text =
            "•"

        icon.alpha =
            1f

        icon.scaleX =
            1f

        icon.scaleY =
            1f


        status.text =
            "Scanning"

        status.alpha =
            1f


        status.setTextColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.primary
            )
        )


        icon.animate()
            .scaleX(1.10f)
            .scaleY(1.10f)
            .setDuration(650L)
            .withEndAction {

                if (
                    isAdded &&
                    icon.visibility ==
                    View.VISIBLE
                ) {

                    icon.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(650L)
                        .withEndAction {

                            if (
                                isAdded &&
                                icon.visibility ==
                                View.VISIBLE
                            ) {

                                setStageScanning(
                                    icon,
                                    status
                                )
                            }
                        }
                        .start()
                }
            }
            .start()
    }


    // =========================================================
    // STAGE COMPLETE
    // =========================================================

    private fun setStageComplete(
        icon: TextView,
        status: TextView
    ) {

        icon.animate().cancel()

        icon.scaleX =
            1f

        icon.scaleY =
            1f

        icon.text =
            "✓"

        icon.alpha =
            1f


        status.text =
            "Complete"

        status.alpha =
            1f


        status.setTextColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.primary
            )
        )


        icon.animate()
            .scaleX(1.08f)
            .scaleY(1.08f)
            .setDuration(180L)
            .withEndAction {

                if (
                    isAdded &&
                    icon.visibility ==
                    View.VISIBLE
                ) {

                    icon.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(180L)
                        .start()
                }
            }
            .start()
    }


    // =========================================================
    // STAGE SKIPPED
    // =========================================================

    private fun setStageSkipped(
        icon: TextView,
        status: TextView
    ) {

        icon.animate().cancel()

        icon.scaleX =
            1f

        icon.scaleY =
            1f

        icon.text =
            "—"

        icon.alpha =
            0.55f


        status.text =
            "Skipped"

        status.alpha =
            0.75f


        status.setTextColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.text_secondary
            )
        )
    }


    // =========================================================
    // OPEN RESULTS
    // =========================================================

    private fun openScanResults() {

        parentFragmentManager
            .beginTransaction()
            .replace(
                R.id.mainFragmentContainer,
                ScanResultsFragment()
            )
            .addToBackStack(null)
            .commit()
    }


    // =========================================================
    // FORMAT DATA SIZE
    // =========================================================

    private fun formatDataSize(
        bytes: Long
    ): String {

        val kb =
            bytes / 1000.0

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


            kb >= 1.0 -> {

                String.format(
                    Locale.US,
                    "%.0f KB",
                    kb
                )
            }


            bytes > 0 -> {

                "$bytes B"
            }


            else -> {

                "0 MB"
            }
        }
    }


    // =========================================================
    // CANCEL SCAN
    // =========================================================

    private fun cancelScan() {

        scanJob?.cancel()

        scanJob = null

        scanIcon.clearAnimation()


        statusText.text =
            "Scan cancelled"


        currentCategoryText.text =
            "Scanning stopped"


        currentCategoryLabel.text =
            "Scanning stopped"


        currentCategoryPercentText.text =
            "—"


        currentCategoryPercentDisplay.text =
            "—"


        setCancelButtonAppearance()


        cancelButton.text =
            "Back to Home"


        cancelButton.setOnClickListener {

            goBack()
        }
    }


    // =========================================================
    // RESUME
    // =========================================================

    override fun onResume() {

        super.onResume()

        if (!isAdded) {
            return
        }

        /*
         * No permission handling here.
         *
         * HomeFragment handles the complete permission flow
         * before this Fragment is opened.
         */
    }


    // =========================================================
    // DESTROY VIEW
    // =========================================================

    override fun onDestroyView() {

        scanJob?.cancel()

        scanJob = null

        super.onDestroyView()
    }
}