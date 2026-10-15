package com.example.phonecleaner.ui.results

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.phonecleaner.R
import com.example.phonecleaner.data.model.CleanerFile
import java.util.Locale

class ScannedLargeFilesAdapter(
    private val onSelectionChanged: (Int) -> Unit
) : RecyclerView.Adapter<ScannedLargeFilesAdapter.LargeFileViewHolder>() {

    private var largeFiles: List<CleanerFile> =
        emptyList()

    private val selectedUris =
        mutableSetOf<String>()

    /*
     * Load Large Files.
     *
     * IMPORTANT:
     * Do NOT call onSelectionChanged() here.
     *
     * submitList() can also be called while
     * restoring the screen. It must not change
     * the global Clean selection.
     */
    fun submitList(
        files: List<CleanerFile>
    ) {

        largeFiles =
            files.toList()

        val currentUris =
            largeFiles
                .map {
                    it.uri
                }
                .toSet()

        selectedUris.retainAll(
            currentUris
        )

        notifyDataSetChanged()
    }

    /*
     * Restore selections from the global
     * Clean selection.
     *
     * IMPORTANT:
     * Do NOT call onSelectionChanged() here.
     */
    fun setSelectedUris(
        uris: Set<String>
    ) {

        selectedUris.clear()

        val availableUris =
            largeFiles
                .map {
                    it.uri
                }
                .toSet()

        selectedUris.addAll(
            uris.intersect(
                availableUris
            )
        )

        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): LargeFileViewHolder {

        val view =
            LayoutInflater.from(
                parent.context
            ).inflate(
                R.layout.item_scanned_large_file,
                parent,
                false
            )

        return LargeFileViewHolder(
            view
        )
    }

    override fun onBindViewHolder(
        holder: LargeFileViewHolder,
        position: Int
    ) {

        holder.bind(
            largeFiles[position],
            selectedUris.contains(
                largeFiles[position].uri
            )
        )
    }

    override fun getItemCount(): Int {
        return largeFiles.size
    }

    fun selectAll() {

        selectedUris.clear()

        selectedUris.addAll(
            largeFiles.map {
                it.uri
            }
        )

        notifyDataSetChanged()

        onSelectionChanged(
            selectedUris.size
        )
    }

    fun clearSelection() {

        selectedUris.clear()

        notifyDataSetChanged()

        onSelectionChanged(0)
    }

    fun areAllSelected(): Boolean {

        return largeFiles.isNotEmpty() &&
                selectedUris.size == largeFiles.size
    }

    fun getSelectedLargeFiles(): List<CleanerFile> {

        return largeFiles.filter {
            selectedUris.contains(
                it.uri
            )
        }
    }

    private fun toggleSelection(
        file: CleanerFile
    ) {

        if (selectedUris.contains(file.uri)) {

            selectedUris.remove(
                file.uri
            )

        } else {

            selectedUris.add(
                file.uri
            )
        }

        notifyDataSetChanged()

        /*
         * This callback happens only because
         * the user actually changed selection.
         */
        onSelectionChanged(
            selectedUris.size
        )
    }

    inner class LargeFileViewHolder(
        itemView: View
    ) : RecyclerView.ViewHolder(itemView) {

        private val nameText =
            itemView.findViewById<TextView>(
                R.id.tvLargeFileItemName
            )

        private val detailsText =
            itemView.findViewById<TextView>(
                R.id.tvLargeFileItemDetails
            )

        private val checkBox =
            itemView.findViewById<CheckBox>(
                R.id.cbLargeFileSelected
            )

        fun bind(
            file: CleanerFile,
            isSelected: Boolean
        ) {

            nameText.text =
                file.name

            val typeText =
                file.mimeType
                    ?.takeIf {
                        it.isNotBlank()
                    }
                    ?: "File"

            detailsText.text =
                String.format(
                    Locale.US,
                    "%s • %s",
                    formatDataSize(
                        file.sizeBytes
                    ),
                    typeText
                )

            /*
             * Remove the old listener before
             * changing isChecked.
             *
             * Otherwise RecyclerView binding can
             * trigger a selection callback.
             */
            checkBox.setOnCheckedChangeListener(
                null
            )

            checkBox.isChecked =
                isSelected

            checkBox.setOnCheckedChangeListener {
                    _, checked ->

                val currentlySelected =
                    selectedUris.contains(
                        file.uri
                    )

                if (
                    checked != currentlySelected
                ) {

                    toggleSelection(
                        file
                    )
                }
            }

            itemView.setOnClickListener {

                toggleSelection(
                    file
                )
            }
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
}