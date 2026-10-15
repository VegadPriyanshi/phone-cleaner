package com.example.phonecleaner.ui.results

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.phonecleaner.MainActivity
import com.example.phonecleaner.R
import java.util.Locale

class ScanResultsFragment : Fragment() {

    private lateinit var resultsViewModel: ScanResultsViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        return inflater.inflate(
            R.layout.fragment_scan_results,
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

        resultsViewModel =
            ViewModelProvider(requireActivity())
                .get(
                    ScanResultsViewModel::class.java
                )

        setupBackButton(view)

        setupScannedPhotosCard(view)

        setupScreenshotsCard(view)

        setupVideosCard(view)

        setupAudioCard(view)

        setupLargeFilesCard(view)

        setupDuplicatePhotosCard(view)

        displayScanSummary(view)

        displayScannedPhotosSummary(view)

        displayScreenshotsSummary(view)

        displayVideosSummary(view)

        displayAudioSummary(view)

        // Large Files summary
        displayLargeFilesSummary(view)

        displayDuplicatePhotosSummary(view)

        android.util.Log.d(
            "PhoneCleaner",
            "Scan Results video count: ${resultsViewModel.getVideoCount()}"
        )

        android.util.Log.d(
            "PhoneCleaner",
            "Scan Results video size: ${
                resultsViewModel.getVideoTotalSize()
            } bytes"
        )

        android.util.Log.d(
            "PhoneCleaner",
            "Scan Results large file count: ${
                resultsViewModel.getLargeFileCount()
            }"
        )

        android.util.Log.d(
            "PhoneCleaner",
            "Scan Results large file size: ${
                resultsViewModel.getLargeFileTotalSize()
            } bytes"
        )
    }

    override fun onResume() {
        super.onResume()

        (requireActivity() as MainActivity)
            .showBottomNavigation()

        if (::resultsViewModel.isInitialized && view != null) {

            displayVideosSummary(
                requireView()
            )

            displayScreenshotsSummary(
                requireView()
            )

            displayScannedPhotosSummary(
                requireView()
            )

            displayAudioSummary(
                requireView()
            )

            // Refresh Large Files summary
            displayLargeFilesSummary(
                requireView()
            )

            displayDuplicatePhotosSummary(
                requireView()
            )
        }
    }

    private fun displayDuplicatePhotosSummary(
        view: View
    ) {

        val duplicateGroups =
            resultsViewModel.getDuplicatePhotoGroups()

        val groupCount =
            duplicateGroups.size

        val duplicatePhotoCount =
            duplicateGroups.sumOf {
                it.files.size
            }

        val duplicatePhotosSummary =
            view.findViewById<TextView>(
                R.id.tvDuplicatePhotosSummary
            )

        duplicatePhotosSummary.text =
            String.format(
                Locale.US,
                "%,d groups • %,d photos",
                groupCount,
                duplicatePhotoCount
            )
    }

    private fun setupBackButton(
        view: View
    ) {

        val backButton =
            view.findViewById<ImageButton>(
                R.id.btnResultsBack
            )

        backButton.setOnClickListener {

            parentFragmentManager
                .popBackStack()
        }
    }

    private fun displayAudioSummary(
        view: View
    ) {

        val audioFiles =
            resultsViewModel.getAudioFiles()

        val audioCount =
            audioFiles.size

        val audioSize =
            audioFiles.sumOf {
                it.sizeBytes
            }

        view.findViewById<TextView>(
            R.id.tvAudioSummary
        ).text =
            String.format(
                Locale.US,
                "%,d audio files • %s",
                audioCount,
                formatDataSize(audioSize)
            )
    }



    private fun setupLargeFilesCard(
        view: View
    ) {

        val largeFilesCard =
            view.findViewById<View>(
                R.id.cardLargeFiles
            )

        largeFilesCard.setOnClickListener {

            parentFragmentManager
                .beginTransaction()
                .replace(
                    R.id.mainFragmentContainer,
                    ScannedLargeFilesFragment()
                )
                .addToBackStack(null)
                .commit()
        }
    }

    private fun setupVideosCard(
        view: View
    ) {

        val videosCard =
            view.findViewById<View>(
                R.id.cardVideos
            )

        videosCard.setOnClickListener {

            parentFragmentManager
                .beginTransaction()
                .replace(
                    R.id.mainFragmentContainer,
                    ScannedVideosFragment()
                )
                .addToBackStack(null)
                .commit()
        }
    }

    private fun setupAudioCard(
        view: View
    ) {

        val audioCard =
            view.findViewById<View>(
                R.id.cardAudio
            )

        audioCard.setOnClickListener {

            parentFragmentManager
                .beginTransaction()
                .replace(
                    R.id.mainFragmentContainer,
                    ScannedAudioFragment()
                )
                .addToBackStack(null)
                .commit()
        }
    }


    private fun setupDuplicatePhotosCard(view: View) {

        val duplicatePhotosCard =
            view.findViewById<View>(
                R.id.cardDuplicatePhotos
            )

        duplicatePhotosCard.setOnClickListener {

            parentFragmentManager
                .beginTransaction()
                .replace(
                    R.id.mainFragmentContainer,
                    DuplicatePhotosFragment()
                )
                .addToBackStack(null)
                .commit()
        }
    }

    private fun displayVideosSummary(
        view: View
    ) {

        val videos =
            resultsViewModel.getVideoFiles()

        val videoCount =
            videos.size

        val videoSize =
            videos.sumOf {
                it.sizeBytes
            }

        view.findViewById<TextView>(
            R.id.tvVideosSummary
        ).text =
            String.format(
                Locale.US,
                "%,d videos • %s",
                videoCount,
                formatDataSize(videoSize)
            )
    }

    private fun setupScannedPhotosCard(
        view: View
    ) {

        val scannedPhotosCard =
            view.findViewById<View>(
                R.id.cardScannedPhotos
            )

        scannedPhotosCard.setOnClickListener {

            parentFragmentManager
                .beginTransaction()
                .replace(
                    R.id.mainFragmentContainer,
                    ScannedPhotosFragment()
                )
                .addToBackStack(null)
                .commit()
        }
    }

    private fun setupScreenshotsCard(
        view: View
    ) {

        val screenshotsCard =
            view.findViewById<View>(
                R.id.cardScreenshots
            )

        screenshotsCard.setOnClickListener {

            parentFragmentManager
                .beginTransaction()
                .replace(
                    R.id.mainFragmentContainer,
                    ScannedScreenshotsFragment()
                )
                .addToBackStack(null)
                .commit()
        }
    }

    private fun displayScanSummary(
        view: View
    ) {

        val totalCount =
            resultsViewModel.getOverallScanCount()

        val totalSize =
            resultsViewModel.getOverallScanSize()

        view.findViewById<TextView>(
            R.id.tvResultsTitle
        ).text =
            "Items scanned"

        view.findViewById<TextView>(
            R.id.tvResultsCount
        ).text =
            String.format(
                Locale.US,
                "%,d",
                totalCount
            )

        view.findViewById<TextView>(
            R.id.tvResultsSize
        ).text =
            formatDataSize(
                totalSize
            )

        view.findViewById<TextView>(
            R.id.tvResultsDescription
        ).text =
            "Review the scanned items before cleaning."
    }

    private fun displayScannedPhotosSummary(
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

        val scannedPhotosSummary =
            view.findViewById<TextView>(
                R.id.tvScannedPhotosSummary
            )

        scannedPhotosSummary.text =
            String.format(
                Locale.US,
                "%,d photos • %s",
                totalCount,
                formatDataSize(totalSize)
            )
    }

    private fun displayScreenshotsSummary(
        view: View
    ) {

        val screenshots =
            resultsViewModel.getScreenshotFiles()

        val screenshotCount =
            screenshots.size

        val screenshotSize =
            screenshots.sumOf {
                it.sizeBytes
            }

        val screenshotsSummary =
            view.findViewById<TextView>(
                R.id.tvScreenshotsSummary
            )

        screenshotsSummary.text =
            String.format(
                Locale.US,
                "%,d screenshots • %s",
                screenshotCount,
                formatDataSize(
                    screenshotSize
                )
            )
    }

    /**
     * Displays the actual Large Files scan result.
     *
     * Large Files are kept separate from the overall scan total
     * because they can overlap with photos, videos, audio, etc.
     */
    private fun displayLargeFilesSummary(
        view: View
    ) {

        val largeFileCount =
            resultsViewModel.getLargeFileCount()

        val largeFileSize =
            resultsViewModel.getLargeFileTotalSize()

        view.findViewById<TextView>(
            R.id.tvLargeFilesSummary
        ).text =
            String.format(
                Locale.US,
                "%,d files • %s",
                largeFileCount,
                formatDataSize(
                    largeFileSize
                )
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
}