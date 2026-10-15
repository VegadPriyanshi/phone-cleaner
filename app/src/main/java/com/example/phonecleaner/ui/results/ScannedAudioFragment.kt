package com.example.phonecleaner.ui.results

import android.app.Activity
import android.net.Uri
import android.os.Bundle
import android.os.Build
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
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder

import java.util.Locale


class ScannedAudioFragment : Fragment() {

    private lateinit var resultsViewModel: ScanResultsViewModel

    private lateinit var audioAdapter: ScannedAudioAdapter
    private lateinit var recyclerAudio: RecyclerView

    private lateinit var tvAudioSelectionCount: TextView
    private lateinit var tvAudioSelectAll: TextView
    private lateinit var btnCleanSelectedAudio: MaterialButton

    private var pendingDeletionAudio =
        emptyList<com.example.phonecleaner.data.model.CleanerFile>()


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        return inflater.inflate(
            R.layout.fragment_scanned_audio,
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

        initializeSelectionViews(view)

        setupBackButton(view)
        setupSelectionControls()
        displayAudioSummary(view)
        setupAudioList(view)
        setupSystemInsets(view)
    }


    override fun onResume() {

        super.onResume()

        (requireActivity() as MainActivity)
            .hideBottomNavigation()
    }


    override fun onPause() {

        super.onPause()

        (requireActivity() as MainActivity)
            .showBottomNavigation()
    }


    private fun initializeSelectionViews(
        view: View
    ) {

        tvAudioSelectionCount =
            view.findViewById(
                R.id.tvAudioSelectionCount
            )

        tvAudioSelectAll =
            view.findViewById(
                R.id.tvAudioSelectAll
            )

        btnCleanSelectedAudio =
            view.findViewById(
                R.id.btnCleanSelectedAudio
            )
    }


    private fun setupBackButton(
        view: View
    ) {

        val backButton =
            view.findViewById<ImageButton>(
                R.id.btnScannedAudioBack
            )

        backButton.setOnClickListener {

            parentFragmentManager
                .popBackStack()
        }
    }


    private fun setupSelectionControls() {

        tvAudioSelectionCount.text =
            "0 selected"

        tvAudioSelectAll.text =
            "Select all"

        btnCleanSelectedAudio.visibility =
            View.GONE

        tvAudioSelectAll.setOnClickListener {

            if (audioAdapter.areAllSelected()) {

                audioAdapter.clearSelection()

            } else {

                audioAdapter.selectAll()
            }
        }

        btnCleanSelectedAudio.setOnClickListener {

            showDeleteConfirmation()
        }
    }


    private fun displayAudioSummary(
        view: View
    ) {

        val audioFiles =
            resultsViewModel
                .getAudioFiles()

        val audioCount =
            audioFiles.size

        val audioSize =
            audioFiles.sumOf {
                it.sizeBytes
            }

        view.findViewById<TextView>(
            R.id.tvAudioTotalSummary
        ).text =
            String.format(
                Locale.US,
                "%,d audio files • %s",
                audioCount,
                formatDataSize(
                    audioSize
                )
            )
    }


    private fun showDeleteConfirmation() {

        val selectedAudio =
            audioAdapter.getSelectedAudio()

        if (selectedAudio.isEmpty()) {
            return
        }

        val selectedCount =
            selectedAudio.size

        val selectedSize =
            selectedAudio.sumOf {
                it.sizeBytes
            }

        val sizeText =
            formatDataSize(
                selectedSize
            )

        val message =
            if (selectedCount == 1) {

                "You selected 1 audio file ($sizeText).\n\n" +
                        "Are you sure you want to delete it?"

            } else {

                "You selected $selectedCount audio files ($sizeText).\n\n" +
                        "Are you sure you want to delete them?"
            }

        MaterialAlertDialogBuilder(
            requireContext()
        )
            .setTitle(
                "Delete selected audio?"
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

                requestAudioDeletion(
                    selectedAudio
                )
            }
            .show()
    }


    private fun requestAudioDeletion(
        selectedAudio:
        List<com.example.phonecleaner.data.model.CleanerFile>
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

        pendingDeletionAudio =
            selectedAudio

        val uris =
            selectedAudio.map {
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

        val deletedAudio =
            pendingDeletionAudio

        if (deletedAudio.isEmpty()) {
            return
        }

        val deletedUris =
            deletedAudio
                .map {
                    it.uri
                }
                .toSet()

        resultsViewModel.removeSelectedCleanFiles(
            deletedUris
        )

        val remainingAudio =
            resultsViewModel
                .getAudioFiles()
                .filterNot {
                    it.uri in deletedUris
                }

        resultsViewModel.updateAudioFiles(
            remainingAudio
        )

        audioAdapter.submitList(
            remainingAudio
        )

        pendingDeletionAudio =
            emptyList()

        displayAudioSummary(
            requireView()
        )

        updateAudioSelectionUI(
            0
        )

        val deletionMessage =
            if (deletedAudio.size == 1) {

                "1 audio file deleted"

            } else {

                "${deletedAudio.size} audio files deleted"
            }

        Toast.makeText(
            requireContext(),
            deletionMessage,
            Toast.LENGTH_SHORT
        ).show()
    }


    private fun updateAudioSelectionUI(
        selectedCount: Int
    ) {

        tvAudioSelectionCount.text =
            if (selectedCount == 1) {
                "1 selected"
            } else {
                "$selectedCount selected"
            }

        tvAudioSelectAll.text =
            if (audioAdapter.areAllSelected()) {
                "Clear all"
            } else {
                "Select all"
            }

        /*
         * IMPORTANT:
         * Keep the existing visibility behavior,
         * but also show the selected count in the
         * Clean button.
         */
        btnCleanSelectedAudio.text =
            if (selectedCount == 1) {
                "Clean 1 Selected"
            } else {
                "Clean $selectedCount Selected"
            }

        btnCleanSelectedAudio.visibility =
            if (selectedCount > 0) {
                View.VISIBLE
            } else {
                View.GONE
            }
    }


    private fun setupAudioList(
        view: View
    ) {

        recyclerAudio =
            view.findViewById(
                R.id.scannedAudioGrid
            )

        audioAdapter =
            ScannedAudioAdapter { selectedCount ->

                updateAudioSelectionUI(
                    selectedCount
                )

                /*
                 * This callback happens only when
                 * the user actually changes selection.
                 *
                 * Sync that selection with the
                 * global Clean selection.
                 */
                syncAudioSelectionWithGlobalList()
            }

        recyclerAudio.layoutManager =
            LinearLayoutManager(
                requireContext()
            )

        recyclerAudio.adapter =
            audioAdapter


        // =========================================================
        // Load Audio files
        // =========================================================

        val audioFiles =
            resultsViewModel
                .getAudioFiles()

        audioAdapter.submitList(
            audioFiles
        )


        // =========================================================
        // Restore global Audio selections
        // =========================================================

        val globallySelectedUris =
            resultsViewModel
                .getSelectedCleanFiles()
                .map {
                    it.uri
                }
                .toSet()

        audioAdapter.setSelectedUris(
            globallySelectedUris
        )


        // =========================================================
        // Update UI using restored selection
        // =========================================================

        val restoredSelectedCount =
            audioAdapter
                .getSelectedAudio()
                .size

        updateAudioSelectionUI(
            restoredSelectedCount
        )
    }


    private fun syncAudioSelectionWithGlobalList() {

        val audioFiles =
            resultsViewModel
                .getAudioFiles()

        val selectedAudio =
            audioAdapter
                .getSelectedAudio()

        val selectedUris =
            selectedAudio
                .map {
                    it.uri
                }
                .toSet()

        /*
         * Synchronize Audio selection
         * with the global selectedCleanFiles list.
         *
         * Existing Audio deletion flow
         * remains unchanged.
         */
        audioFiles.forEach { audio ->

            resultsViewModel.setCleanFileSelected(
                file = audio,
                selected =
                    audio.uri in selectedUris
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

            bytes > 0 ->
                String.format(
                    Locale.US,
                    "%.0f KB",
                    bytes / 1000.0
                )

            else ->
                "0 MB"
        }
    }


    override fun onDestroyView() {

        /*
         * Save the current Audio selection before
         * this Fragment view is destroyed.
         */
        if (
            ::audioAdapter.isInitialized
        ) {

            syncAudioSelectionWithGlobalList()
        }

        (activity as? MainActivity)
            ?.showBottomNavigation()

        super.onDestroyView()
    }


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
}