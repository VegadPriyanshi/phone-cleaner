package com.example.phonecleaner.ui.results

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

import androidx.recyclerview.widget.RecyclerView

import com.example.phonecleaner.R
import com.example.phonecleaner.data.model.CleanerFile
import com.google.android.material.checkbox.MaterialCheckBox

import java.util.Locale


class ScannedAudioAdapter(
    private val onSelectionChanged: (Int) -> Unit
) : RecyclerView.Adapter<ScannedAudioAdapter.AudioViewHolder>() {

    private var audioFiles: List<CleanerFile> =
        emptyList()

    private val selectedUris =
        mutableSetOf<String>()


    // =========================================================
    // Load audio files
    // =========================================================

    fun submitList(
        files: List<CleanerFile>
    ) {

        audioFiles =
            files.toList()

        /*
         * Keep only selections that still exist
         * in the current audio list.
         *
         * IMPORTANT:
         * Do NOT call onSelectionChanged() here.
         *
         * submitList() can happen while restoring
         * the screen. It must not modify the
         * global selection state.
         */
        val currentUris =
            audioFiles
                .map { it.uri }
                .toSet()

        selectedUris.retainAll(
            currentUris
        )

        notifyDataSetChanged()
    }


    // =========================================================
    // Restore global selections
    // =========================================================

    fun setSelectedUris(
        uris: Set<String>
    ) {

        selectedUris.clear()

        val availableUris =
            audioFiles
                .map { it.uri }
                .toSet()

        selectedUris.addAll(
            uris.intersect(
                availableUris
            )
        )

        /*
         * Only update the RecyclerView.
         *
         * Do NOT call onSelectionChanged().
         *
         * These selections already exist in the
         * global Clean selection.
         */
        notifyDataSetChanged()
    }


    // =========================================================
    // Get selected audio
    // =========================================================

    fun getSelectedAudio(): List<CleanerFile> {

        return audioFiles.filter { audio ->
            audio.uri in selectedUris
        }
    }


    // =========================================================
    // Select all
    // =========================================================

    fun selectAll() {

        selectedUris.clear()

        selectedUris.addAll(
            audioFiles.map {
                it.uri
            }
        )

        notifyDataSetChanged()

        /*
         * This is a real user action.
         * Therefore sync with the global selection.
         */
        onSelectionChanged(
            selectedUris.size
        )
    }


    // =========================================================
    // Clear selection
    // =========================================================

    fun clearSelection() {

        selectedUris.clear()

        notifyDataSetChanged()

        /*
         * This is a real user action.
         */
        onSelectionChanged(
            0
        )
    }


    // =========================================================
    // Check whether all audio is selected
    // =========================================================

    fun areAllSelected(): Boolean {

        return audioFiles.isNotEmpty() &&
                selectedUris.size ==
                audioFiles.size
    }


    // =========================================================
    // RecyclerView
    // =========================================================

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): AudioViewHolder {

        val view =
            LayoutInflater.from(
                parent.context
            ).inflate(
                R.layout.item_scanned_audio,
                parent,
                false
            )

        return AudioViewHolder(view)
    }


    override fun onBindViewHolder(
        holder: AudioViewHolder,
        position: Int
    ) {

        holder.bind(
            audioFiles[position]
        )
    }


    override fun getItemCount(): Int =
        audioFiles.size


    // =========================================================
    // ViewHolder
    // =========================================================

    inner class AudioViewHolder(
        itemView: View
    ) : RecyclerView.ViewHolder(itemView) {

        private val nameText =
            itemView.findViewById<TextView>(
                R.id.tvAudioItemName
            )

        private val detailsText =
            itemView.findViewById<TextView>(
                R.id.tvAudioItemDetails
            )

        private val checkBox =
            itemView.findViewById<MaterialCheckBox>(
                R.id.checkAudio
            )


        fun bind(
            audio: CleanerFile
        ) {

            nameText.text =
                audio.name

            detailsText.text =
                formatDataSize(
                    audio.sizeBytes
                )


            /*
             * Remove the listener before changing
             * isChecked.
             *
             * This prevents RecyclerView binding
             * from triggering selection changes.
             */
            checkBox.setOnCheckedChangeListener(
                null
            )

            checkBox.isChecked =
                audio.uri in selectedUris


            /*
             * Listen only for actual user changes.
             */
            checkBox.setOnCheckedChangeListener { _,
                                                  isChecked ->

                if (isChecked) {

                    selectedUris.add(
                        audio.uri
                    )

                } else {

                    selectedUris.remove(
                        audio.uri
                    )
                }

                onSelectionChanged(
                    selectedUris.size
                )
            }


            /*
             * Tapping the whole audio row toggles
             * the checkbox.
             */
            itemView.setOnClickListener {

                checkBox.isChecked =
                    !checkBox.isChecked
            }
        }
    }


    // =========================================================
    // File size
    // =========================================================

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
}