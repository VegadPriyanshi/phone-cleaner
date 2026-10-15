package com.example.phonecleaner.ui.results

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.example.phonecleaner.R
import com.example.phonecleaner.data.model.CleanerFile
import com.google.android.material.checkbox.MaterialCheckBox
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ScannedPhotoGridAdapter(
    private val onSelectionChanged: (Int) -> Unit
) : RecyclerView.Adapter<ScannedPhotoGridAdapter.PhotoGridViewHolder>() {

    private val photos =
        mutableListOf<CleanerFile>()

    /*
     * Only the selection state of the currently
     * displayed Photos screen.
     *
     * Global selection is stored in ScanResultsViewModel.
     */
    private val selectedUris =
        mutableSetOf<String>()

    private val adapterScope =
        CoroutineScope(Dispatchers.Main)


    // =========================================================
    // Submit Photos
    // =========================================================

    fun submitList(
        newPhotos: List<CleanerFile>
    ) {

        photos.clear()
        photos.addAll(newPhotos)

        /*
         * IMPORTANT:
         *
         * Do NOT clear selectedUris here.
         *
         * When the Photos screen is recreated,
         * we must allow the global selection to
         * be restored.
         *
         * Keep only selections that still exist
         * in the current photo list.
         */
        val availableUris =
            photos
                .map {
                    it.uri
                }
                .toSet()

        selectedUris.retainAll(
            availableUris
        )

        /*
         * Loading the list must NOT call
         * onSelectionChanged().
         *
         * Otherwise the adapter could report
         * "0 selected" and accidentally remove
         * the global selection.
         */
        notifyDataSetChanged()
    }


    // =========================================================
    // Restore Global Selection
    // =========================================================

    fun setSelectedUris(
        uris: Set<String>
    ) {

        selectedUris.clear()

        val availableUris =
            photos
                .map {
                    it.uri
                }
                .toSet()

        selectedUris.addAll(
            uris.intersect(
                availableUris
            )
        )

        /*
         * This is only restoring the UI.
         *
         * Do NOT call onSelectionChanged().
         *
         * The global ViewModel already contains
         * these selected files.
         */
        notifyDataSetChanged()
    }


    // =========================================================
    // Get Selected Photos
    // =========================================================

    fun getSelectedPhotos(): List<CleanerFile> {

        return photos.filter {
            selectedUris.contains(
                it.uri
            )
        }
    }


    // =========================================================
    // Select All
    // =========================================================

    fun selectAll() {

        selectedUris.clear()

        photos.forEach {

            selectedUris.add(
                it.uri
            )
        }

        notifyDataSetChanged()

        onSelectionChanged(
            selectedUris.size
        )
    }


    // =========================================================
    // Clear Selection
    // =========================================================

    fun clearSelection() {

        selectedUris.clear()

        notifyDataSetChanged()

        onSelectionChanged(0)
    }


    // =========================================================
    // Are All Selected
    // =========================================================

    fun areAllSelected(): Boolean {

        return photos.isNotEmpty() &&
                selectedUris.size ==
                photos.size
    }


    // =========================================================
    // RecyclerView
    // =========================================================

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): PhotoGridViewHolder {

        val view =
            LayoutInflater.from(
                parent.context
            ).inflate(
                R.layout.item_scanned_photo_grid,
                parent,
                false
            )

        return PhotoGridViewHolder(
            view
        )
    }


    override fun onBindViewHolder(
        holder: PhotoGridViewHolder,
        position: Int
    ) {

        holder.bind(
            photos[position]
        )
    }


    override fun getItemCount(): Int {

        return photos.size
    }


    override fun onViewRecycled(
        holder: PhotoGridViewHolder
    ) {

        holder.clearThumbnailJob()

        super.onViewRecycled(
            holder
        )
    }


    // =========================================================
    // ViewHolder
    // =========================================================

    inner class PhotoGridViewHolder(
        itemView: View
    ) : RecyclerView.ViewHolder(
        itemView
    ) {

        private val imageView =
            itemView.findViewById<ImageView>(
                R.id.ivGridPhoto
            )

        private val overlay =
            itemView.findViewById<View>(
                R.id.viewSelectionOverlay
            )

        private val checkBox =
            itemView.findViewById<MaterialCheckBox>(
                R.id.checkGridPhoto
            )

        private var thumbnailJob: Job? =
            null

        /*
         * URI of the photo currently bound
         * to this ViewHolder.
         */
        private var boundUri: String? =
            null


        // =====================================================
        // Bind
        // =====================================================

        fun bind(
            photo: CleanerFile
        ) {

            thumbnailJob?.cancel()

            boundUri =
                photo.uri

            /*
             * Read current selection from the
             * adapter's selected URI set.
             */
            val isSelected =
                selectedUris.contains(
                    photo.uri
                )

            /*
             * Prevent the listener from firing
             * while restoring checkbox state.
             */
            checkBox.setOnCheckedChangeListener(
                null
            )

            checkBox.isChecked =
                isSelected

            updateSelectionAppearance(
                isSelected
            )

            /*
             * Now attach the listener again.
             */
            checkBox.setOnCheckedChangeListener {
                    _, checked ->

                updateSelection(
                    photo,
                    checked
                )
            }

            /*
             * Tapping the photo also toggles
             * its selection.
             */
            itemView.setOnClickListener {

                checkBox.isChecked =
                    !checkBox.isChecked
            }

            loadThumbnail(
                photo.uri
            )
        }


        // =====================================================
        // Update Selection
        // =====================================================

        private fun updateSelection(
            photo: CleanerFile,
            selected: Boolean
        ) {

            if (selected) {

                selectedUris.add(
                    photo.uri
                )

            } else {

                selectedUris.remove(
                    photo.uri
                )
            }

            updateSelectionAppearance(
                selected
            )

            /*
             * This is a REAL user selection change.
             *
             * The Fragment will synchronize
             * this change with the global ViewModel.
             */
            onSelectionChanged(
                selectedUris.size
            )
        }


        // =====================================================
        // Selection Appearance
        // =====================================================

        private fun updateSelectionAppearance(
            selected: Boolean
        ) {

            overlay.visibility =
                if (selected) {

                    View.VISIBLE

                } else {

                    View.GONE
                }
        }


        // =====================================================
        // Thumbnail
        // =====================================================

        private fun loadThumbnail(
            uriString: String
        ) {

            imageView.setImageDrawable(
                null
            )

            thumbnailJob =
                adapterScope.launch {

                    val bitmap =
                        withContext(
                            Dispatchers.IO
                        ) {

                            try {

                                val uri =
                                    Uri.parse(
                                        uriString
                                    )

                                val resolver =
                                    itemView.context
                                        .contentResolver

                                /*
                                 * Read image dimensions first.
                                 */
                                val bounds =
                                    BitmapFactory.Options()
                                        .apply {

                                            inJustDecodeBounds =
                                                true
                                        }

                                resolver
                                    .openInputStream(
                                        uri
                                    )
                                    ?.use { input ->

                                        BitmapFactory
                                            .decodeStream(
                                                input,
                                                null,
                                                bounds
                                            )
                                    }

                                val targetSize =
                                    400

                                val sampleSize =
                                    calculateInSampleSize(
                                        bounds.outWidth,
                                        bounds.outHeight,
                                        targetSize,
                                        targetSize
                                    )

                                val options =
                                    BitmapFactory.Options()
                                        .apply {

                                            inSampleSize =
                                                sampleSize

                                            inPreferredConfig =
                                                Bitmap.Config.RGB_565
                                        }

                                resolver
                                    .openInputStream(
                                        uri
                                    )
                                    ?.use { input ->

                                        BitmapFactory
                                            .decodeStream(
                                                input,
                                                null,
                                                options
                                            )
                                    }

                            } catch (
                                exception: Exception
                            ) {

                                null
                            }
                        }

                    /*
                     * Check that this ViewHolder
                     * is still displaying the same photo.
                     */
                    if (
                        boundUri ==
                        uriString
                    ) {

                        imageView.setImageBitmap(
                            bitmap
                        )
                    }
                }
        }


        // =====================================================
        // Thumbnail Job
        // =====================================================

        fun clearThumbnailJob() {

            thumbnailJob?.cancel()

            thumbnailJob =
                null
        }
    }


    // =========================================================
    // Calculate Sample Size
    // =========================================================

    private fun calculateInSampleSize(
        width: Int,
        height: Int,
        requestedWidth: Int,
        requestedHeight: Int
    ): Int {

        if (
            width <= 0 ||
            height <= 0
        ) {

            return 1
        }

        var sampleSize = 1

        if (
            height > requestedHeight ||
            width > requestedWidth
        ) {

            val halfHeight =
                height / 2

            val halfWidth =
                width / 2

            while (
                halfHeight / sampleSize >=
                requestedHeight &&
                halfWidth / sampleSize >=
                requestedWidth
            ) {

                sampleSize *= 2
            }
        }

        return sampleSize
    }
}