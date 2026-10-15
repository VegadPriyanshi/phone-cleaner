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

class ScannedDocumentsAdapter(
    private val onSelectionChanged: (Int) -> Unit
) : RecyclerView.Adapter<ScannedDocumentsAdapter.DocumentViewHolder>() {

    private var documentFiles =
        emptyList<CleanerFile>()

    private val selectedUris =
        mutableSetOf<String>()


    /*
     * =========================================================
     * Submit List
     * =========================================================
     *
     * IMPORTANT:
     * Do NOT call onSelectionChanged() here.
     *
     * submitList() is also used when the screen is
     * being restored. Loading the list must not
     * modify the global selection.
     */
    fun submitList(
        files: List<CleanerFile>
    ) {

        documentFiles =
            files.toList()

        val currentUris =
            documentFiles
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
     * =========================================================
     * Restore Selected URIs
     * =========================================================
     *
     * Used when opening Documents again.
     *
     * IMPORTANT:
     * Do NOT call onSelectionChanged() here.
     */
    fun setSelectedUris(
        uris: Set<String>
    ) {

        selectedUris.clear()

        val availableUris =
            documentFiles
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


    /*
     * =========================================================
     * Get Selected Documents
     * =========================================================
     */
    fun getSelectedDocuments(): List<CleanerFile> {

        return documentFiles.filter {
            it.uri in selectedUris
        }
    }


    /*
     * =========================================================
     * Select All
     * =========================================================
     */
    fun selectAll() {

        selectedUris.clear()

        selectedUris.addAll(
            documentFiles.map {
                it.uri
            }
        )

        notifyDataSetChanged()

        onSelectionChanged(
            selectedUris.size
        )
    }


    /*
     * =========================================================
     * Clear Selection
     * =========================================================
     */
    fun clearSelection() {

        selectedUris.clear()

        notifyDataSetChanged()

        onSelectionChanged(0)
    }


    /*
     * =========================================================
     * Clear Selection Silently
     * =========================================================
     *
     * Used after deletion or while restoring the screen.
     */
    fun clearSelectionSilently() {

        selectedUris.clear()

        notifyDataSetChanged()
    }


    /*
     * =========================================================
     * Are All Selected
     * =========================================================
     */
    fun areAllSelected(): Boolean {

        return documentFiles.isNotEmpty() &&
                selectedUris.size ==
                documentFiles.size
    }


    /*
     * =========================================================
     * RecyclerView
     * =========================================================
     */
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): DocumentViewHolder {

        val view =
            LayoutInflater.from(
                parent.context
            ).inflate(
                R.layout.item_scanned_document,
                parent,
                false
            )

        return DocumentViewHolder(
            view
        )
    }


    override fun onBindViewHolder(
        holder: DocumentViewHolder,
        position: Int
    ) {

        holder.bind(
            documentFiles[position]
        )
    }


    override fun getItemCount(): Int =
        documentFiles.size


    /*
     * =========================================================
     * ViewHolder
     * =========================================================
     */
    inner class DocumentViewHolder(
        itemView: View
    ) : RecyclerView.ViewHolder(
        itemView
    ) {

        private val nameText =
            itemView.findViewById<TextView>(
                R.id.tvDocumentItemName
            )

        private val detailsText =
            itemView.findViewById<TextView>(
                R.id.tvDocumentItemDetails
            )

        private val typeText =
            itemView.findViewById<TextView>(
                R.id.tvDocumentItemType
            )

        private val checkBox =
            itemView.findViewById<MaterialCheckBox>(
                R.id.checkDocument
            )


        fun bind(
            document: CleanerFile
        ) {

            nameText.text =
                document.name

            detailsText.text =
                formatDataSize(
                    document.sizeBytes
                )

            typeText.text =
                document.name
                    .substringAfterLast(
                        '.',
                        "FILE"
                    )
                    .uppercase(
                        Locale.US
                    )


            /*
             * IMPORTANT:
             *
             * Remove the old listener before changing
             * the checkbox state.
             */
            checkBox.setOnCheckedChangeListener(
                null
            )

            checkBox.isChecked =
                document.uri in selectedUris


            /*
             * User actually changed the checkbox.
             */
            checkBox.setOnCheckedChangeListener {
                    _, checked ->

                if (checked) {

                    selectedUris.add(
                        document.uri
                    )

                } else {

                    selectedUris.remove(
                        document.uri
                    )
                }

                onSelectionChanged(
                    selectedUris.size
                )
            }


            /*
             * Tapping the entire row toggles the checkbox.
             */
            itemView.setOnClickListener {

                checkBox.isChecked =
                    !checkBox.isChecked
            }
        }
    }


    /*
     * =========================================================
     * Format Data Size
     * =========================================================
     */
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

                "0 KB"
            }
        }
    }
}