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
import android.widget.LinearLayout
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
import com.example.phonecleaner.utils.ScanSummaryPreferences
import java.util.Locale


class ScannedVideosFragment : Fragment() {

    private lateinit var resultsViewModel: ScanResultsViewModel
    private lateinit var videoAdapter: ScannedVideoGridAdapter

    private lateinit var recyclerVideos: RecyclerView
    private lateinit var videoEmptyState: LinearLayout
    private lateinit var tvVideoSummary: TextView
    private lateinit var tvVideoSelectionCount: TextView
    private lateinit var btnCleanSelectedVideos: MaterialButton

    private var pendingDeletionVideos =
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
            R.layout.fragment_scanned_videos,
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
                .get(ScanResultsViewModel::class.java)

        initializeViews(view)

        setupBackButton(view)

        setupRecyclerView()

        setupSelectionControls(view)

        setupSystemInsets(view)

        displayVideos()
    }


    private fun initializeViews(view: View) {

        recyclerVideos =
            view.findViewById(
                R.id.recyclerVideos
            )

        videoEmptyState =
            view.findViewById(
                R.id.videoEmptyState
            )

        tvVideoSummary =
            view.findViewById(
                R.id.tvVideoSummary
            )

        tvVideoSelectionCount =
            view.findViewById(
                R.id.tvVideoSelectionCount
            )

        btnCleanSelectedVideos =
            view.findViewById(
                R.id.btnCleanSelectedVideos
            )
    }


    private fun setupSelectionControls(
        view: View
    ) {

        val selectAllButton =
            view.findViewById<TextView>(
                R.id.tvVideoSelectAll
            )

        selectAllButton.setOnClickListener {

            if (videoAdapter.areAllSelected()) {

                videoAdapter.clearSelection()

            } else {

                videoAdapter.selectAll()
            }
        }

        btnCleanSelectedVideos.setOnClickListener {

            showDeleteConfirmation()
        }
    }


    private fun updateSelectionUI(
        selectedCount: Int
    ) {

        tvVideoSelectionCount.text =
            if (selectedCount == 1) {
                "1 selected"
            } else {
                "$selectedCount selected"
            }

        val selectAllButton =
            view?.findViewById<TextView>(
                R.id.tvVideoSelectAll
            )

        selectAllButton?.text =
            if (videoAdapter.areAllSelected()) {
                "Deselect all"
            } else {
                "Select all"
            }

        updateCleanSelectedButton(
            selectedCount
        )
    }


    private fun updateCleanSelectedButton(
        selectedCount: Int
    ) {

        if (selectedCount > 0) {

            btnCleanSelectedVideos.visibility =
                View.VISIBLE

            btnCleanSelectedVideos.isEnabled =
                true

            btnCleanSelectedVideos.text =
                "Clean $selectedCount Selected"

            btnCleanSelectedVideos.setTextColor(
                Color.WHITE
            )

            btnCleanSelectedVideos.backgroundTintList =
                ColorStateList.valueOf(
                    Color.rgb(
                        118,
                        85,
                        246
                    )
                )

        } else {

            btnCleanSelectedVideos.visibility =
                View.GONE
        }
    }


    private fun showDeleteConfirmation() {

        val selectedVideos =
            videoAdapter.getSelectedVideos()

        if (selectedVideos.isEmpty()) {
            return
        }

        val selectedCount =
            selectedVideos.size

        val selectedSize =
            selectedVideos.sumOf {
                it.sizeBytes
            }

        val sizeText =
            formatDataSize(
                selectedSize
            )

        val message =
            if (selectedCount == 1) {

                "You selected 1 video ($sizeText).\n\nAre you sure you want to delete it?"

            } else {

                "You selected $selectedCount videos ($sizeText).\n\nAre you sure you want to delete them?"
            }

        com.google.android.material.dialog.MaterialAlertDialogBuilder(
            requireContext()
        )
            .setTitle(
                "Delete selected videos?"
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

                requestVideoDeletion(
                    selectedVideos
                )
            }
            .show()
    }


    private fun requestVideoDeletion(
        selectedVideos:
        List<com.example.phonecleaner.data.model.CleanerFile>
    ) {

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {

            Toast.makeText(
                requireContext(),
                "Deletion confirmation requires Android 11 or newer.",
                Toast.LENGTH_LONG
            ).show()

            return
        }

        pendingDeletionVideos =
            selectedVideos

        val uris =
            selectedVideos.map {
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

        val deletedVideos =
            pendingDeletionVideos

        if (deletedVideos.isEmpty()) {
            return
        }

        val deletedUris =
            deletedVideos
                .map {
                    it.uri
                }
                .toSet()


        /*
 * Remove deleted videos from the global
 * Clean selection.
 */
        resultsViewModel.removeSelectedCleanFiles(
            deletedUris
        )

        val remainingVideos =
            resultsViewModel
                .getVideoFiles()
                .filterNot {
                    it.uri in deletedUris
                }

        /*
         * Update the ViewModel first so the Scan Results
         * screen immediately reflects the deleted videos.
         */
        resultsViewModel.updateVideoFiles(
            remainingVideos
        )

        /*
         * Save the latest Video count and size so the
         * Clean screen can also stay synchronized.
         */
        ScanSummaryPreferences.saveVideoSummary(
            context = requireContext(),
            count = remainingVideos.size,
            sizeBytes = remainingVideos.sumOf {
                it.sizeBytes
            }
        )

        /*
         * Refresh the grid with the remaining videos.
         */
        videoAdapter.submitList(
            remainingVideos
        )

        pendingDeletionVideos =
            emptyList()

        /*
         * Refresh summary, empty state and selection UI.
         */
        displayVideos()

        updateSelectionUI(0)

        val deletionMessage =
            if (deletedVideos.size == 1) {
                "1 video deleted"
            } else {
                "${deletedVideos.size} videos deleted"
            }

        Toast.makeText(
            requireContext(),
            deletionMessage,
            Toast.LENGTH_SHORT
        ).show()
    }


    private fun setupBackButton(
        view: View
    ) {

        val backButton =
            view.findViewById<ImageButton>(
                R.id.btnBackVideos
            )

        backButton.setOnClickListener {

            parentFragmentManager.popBackStack()
        }
    }


    private fun setupRecyclerView() {

        videoAdapter =
            ScannedVideoGridAdapter(
                onSelectionChanged = { selectedCount ->

                    updateSelectionUI(
                        selectedCount
                    )

                    // Sync user selection with global Clean selection
                    syncVideoSelectionWithGlobalList()
                }
            )

        recyclerVideos.layoutManager =
            GridLayoutManager(
                requireContext(),
                3
            )

        recyclerVideos.adapter =
            videoAdapter

        recyclerVideos.setHasFixedSize(false)
    }


    private fun displayVideos() {

        val videos =
            resultsViewModel
                .getVideoFiles()

        val videoCount =
            videos.size

        val totalSize =
            videos.sumOf {
                it.sizeBytes
            }

        tvVideoSummary.text =
            String.format(
                Locale.US,
                "%,d videos • %s",
                videoCount,
                formatDataSize(totalSize)
            )

        /*
         * Load the current video list.
         *
         * IMPORTANT:
         * submitList() does NOT change the
         * global selection.
         */
        videoAdapter.submitList(
            videos
        )

        /*
         * Get selections that already exist
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
         * Restore those selections into
         * the Video adapter.
         */
        videoAdapter.setSelectedUris(
            globallySelectedUris
        )

        /*
         * Get the actual restored selection count.
         */
        val restoredSelectedCount =
            videoAdapter
                .getSelectedVideos()
                .size

        if (videos.isEmpty()) {

            recyclerVideos.visibility =
                View.GONE

            videoEmptyState.visibility =
                View.VISIBLE

            tvVideoSelectionCount.text =
                "0 selected"

            view?.findViewById<TextView>(
                R.id.tvVideoSelectAll
            )?.text =
                "Select all"

            btnCleanSelectedVideos.visibility =
                View.GONE

        } else {

            recyclerVideos.visibility =
                View.VISIBLE

            videoEmptyState.visibility =
                View.GONE

            /*
             * Show the restored global selection.
             */
            updateSelectionUI(
                restoredSelectedCount
            )
        }
    }





    private fun setupSystemInsets(
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

    private fun syncVideoSelectionWithGlobalList() {

        val videos =
            resultsViewModel
                .getVideoFiles()

        val selectedVideos =
            videoAdapter
                .getSelectedVideos()

        val selectedUris =
            selectedVideos
                .map {
                    it.uri
                }
                .toSet()

        videos.forEach { video ->

            resultsViewModel.setCleanFileSelected(
                file = video,
                selected =
                    video.uri in selectedUris
            )
        }
    }



    override fun onResume() {

        super.onResume()

        (activity as? MainActivity)
            ?.hideBottomNavigation()
    }


    override fun onDestroyView() {

        /*
         * Save the current Video selection before
         * this Fragment view is destroyed.
         */
        if (
            ::videoAdapter.isInitialized
        ) {

            syncVideoSelectionWithGlobalList()
        }

        (activity as? MainActivity)
            ?.showBottomNavigation()

        super.onDestroyView()
    }
}