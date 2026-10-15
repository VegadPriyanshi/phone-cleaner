package com.example.phonecleaner.ui.results

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.phonecleaner.data.model.CleanerFile
import com.example.phonecleaner.databinding.ItemScannedJunkFileBinding
import java.util.Locale

class ScannedJunkFilesAdapter(
    private val onSelectionChanged: (Int) -> Unit
) : RecyclerView.Adapter<ScannedJunkFilesAdapter.JunkFileViewHolder>() {

    private val junkFiles =
        mutableListOf<CleanerFile>()

    private val selectedFiles =
        mutableSetOf<String>()


    // =========================================================
    // Submit List
    // =========================================================

    fun submitList(
        files: List<CleanerFile>
    ) {

        submitListInternal(
            files = files,
            notifySelectionChanged = true
        )
    }


    // =========================================================
    // Submit List Silently
    // =========================================================
    /*
     * Used when changing Junk File categories.
     *
     * IMPORTANT:
     * This does NOT call onSelectionChanged().
     *
     * Therefore the global selectedCleanFiles list
     * will not be modified while we are restoring
     * an existing selection.
     */

    fun submitListSilently(
        files: List<CleanerFile>
    ) {

        submitListInternal(
            files = files,
            notifySelectionChanged = false
        )
    }


    private fun submitListInternal(
        files: List<CleanerFile>,
        notifySelectionChanged: Boolean
    ) {

        junkFiles.clear()

        junkFiles.addAll(
            files
        )

        /*
         * Keep only selections that belong to
         * the currently displayed files.
         */
        val currentUris =
            junkFiles
                .map {
                    it.uri
                }
                .toSet()

        selectedFiles.retainAll(
            currentUris
        )

        notifyDataSetChanged()

        if (notifySelectionChanged) {

            onSelectionChanged(
                selectedFiles.size
            )
        }
    }


    // =========================================================
    // Get Selected Junk Files
    // =========================================================

    fun getSelectedJunkFiles(): List<CleanerFile> {

        return junkFiles.filter {
            selectedFiles.contains(
                it.uri
            )
        }
    }


    // =========================================================
    // Select All
    // =========================================================

    fun selectAll() {

        selectedFiles.clear()

        selectedFiles.addAll(
            junkFiles.map {
                it.uri
            }
        )

        notifyDataSetChanged()

        onSelectionChanged(
            selectedFiles.size
        )
    }


    // =========================================================
    // Clear Selection
    // =========================================================

    fun clearSelection() {

        selectedFiles.clear()

        notifyDataSetChanged()

        onSelectionChanged(
            selectedFiles.size
        )
    }


    // =========================================================
    // Clear Selection Silently
    // =========================================================

    fun clearSelectionSilently() {

        selectedFiles.clear()

        notifyDataSetChanged()
    }


    // =========================================================
    // Restore Selected URIs
    // =========================================================

    fun setSelectedUris(
        selectedUris: Set<String>
    ) {

        selectedFiles.clear()

        val availableUris =
            junkFiles
                .map {
                    it.uri
                }
                .toSet()

        selectedFiles.addAll(
            selectedUris.intersect(
                availableUris
            )
        )

        /*
         * IMPORTANT:
         *
         * Do NOT call onSelectionChanged() here.
         *
         * This function is only restoring the
         * existing global selection.
         */
        notifyDataSetChanged()
    }

    // =========================================================
    // Are All Selected
    // =========================================================

    fun areAllSelected(): Boolean {

        return junkFiles.isNotEmpty() &&
                selectedFiles.size == junkFiles.size
    }


    // =========================================================
    // RecyclerView
    // =========================================================

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): JunkFileViewHolder {

        val binding =
            ItemScannedJunkFileBinding.inflate(
                LayoutInflater.from(
                    parent.context
                ),
                parent,
                false
            )

        return JunkFileViewHolder(
            binding
        )
    }


    override fun onBindViewHolder(
        holder: JunkFileViewHolder,
        position: Int
    ) {

        holder.bind(
            junkFiles[position]
        )
    }


    override fun getItemCount(): Int =
        junkFiles.size


    // =========================================================
    // ViewHolder
    // =========================================================

    inner class JunkFileViewHolder(
        private val binding: ItemScannedJunkFileBinding
    ) : RecyclerView.ViewHolder(
        binding.root
    ) {

        fun bind(
            file: CleanerFile
        ) {

            binding.tvJunkFileName.text =
                file.name

            binding.tvJunkFileDetails.text =
                formatStorageSize(
                    file.sizeBytes
                )


            binding.checkJunkFile.setOnCheckedChangeListener(
                null
            )

            binding.checkJunkFile.isChecked =
                selectedFiles.contains(
                    file.uri
                )


            binding.checkJunkFile.setOnCheckedChangeListener {
                    _, isChecked ->

                if (isChecked) {

                    selectedFiles.add(
                        file.uri
                    )

                } else {

                    selectedFiles.remove(
                        file.uri
                    )
                }

                onSelectionChanged(
                    selectedFiles.size
                )
            }
        }
    }


    // =========================================================
    // Storage Size
    // =========================================================

    private fun formatStorageSize(
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
}