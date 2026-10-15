package com.example.phonecleaner.ui.results

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView

import androidx.recyclerview.widget.RecyclerView

import com.example.phonecleaner.R
import com.example.phonecleaner.data.model.CleanerFile
import com.example.phonecleaner.data.model.DuplicatePhotoGroup

import java.util.Locale


class DuplicatePhotoGroupAdapter(
    private val onSelectionChanged:
    ((selectedCount: Int) -> Unit)? = null
) : RecyclerView.Adapter<DuplicatePhotoGroupAdapter.DuplicateGroupViewHolder>() {

    private var duplicateGroups:
            List<DuplicatePhotoGroup> = emptyList()

    private val selectedPhotoUris =
        mutableSetOf<String>()


    fun submitList(
        groups: List<DuplicatePhotoGroup>
    ) {

        duplicateGroups =
            groups.toList()

        val currentUris =
            duplicateGroups
                .flatMap { it.files }
                .map { it.uri }
                .toSet()

        /*
         * Keep selections that still exist in
         * the current duplicate-photo list.
         *
         * IMPORTANT:
         * Do NOT notify onSelectionChanged() here.
         *
         * submitList() is also used when restoring
         * the screen. It must not modify the global
         * selection state.
         */
        selectedPhotoUris.retainAll(
            currentUris
        )

        notifyDataSetChanged()
    }


    fun setSelectedUris(
        uris: Set<String>
    ) {

        selectedPhotoUris.clear()

        val availableUris =
            duplicateGroups
                .flatMap { it.files }
                .map { it.uri }
                .toSet()

        selectedPhotoUris.addAll(
            uris.intersect(
                availableUris
            )
        )

        notifyDataSetChanged()
    }


    fun areAllSelected(): Boolean {

        val allPhotos =
            duplicateGroups
                .flatMap { it.files }

        if (allPhotos.isEmpty()) {
            return false
        }

        return allPhotos.all { file ->
            selectedPhotoUris.contains(
                file.uri
            )
        }
    }


    fun selectAll() {

        duplicateGroups
            .flatMap { it.files }
            .forEach { file ->

                selectedPhotoUris.add(
                    file.uri
                )
            }

        notifyDataSetChanged()

        onSelectionChanged?.invoke(
            selectedPhotoUris.size
        )
    }


    fun clearSelection() {

        selectedPhotoUris.clear()

        notifyDataSetChanged()

        onSelectionChanged?.invoke(
            0
        )
    }


    fun getSelectedPhotos(): List<CleanerFile> {

        return duplicateGroups
            .flatMap { it.files }
            .filter { file ->
                selectedPhotoUris.contains(
                    file.uri
                )
            }
    }


    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): DuplicateGroupViewHolder {

        val view =
            LayoutInflater.from(parent.context)
                .inflate(
                    R.layout.item_duplicate_photo_group,
                    parent,
                    false
                )

        return DuplicateGroupViewHolder(
            view
        )
    }


    override fun onBindViewHolder(
        holder: DuplicateGroupViewHolder,
        position: Int
    ) {

        holder.bind(
            duplicateGroups[position],
            position
        )
    }


    override fun getItemCount(): Int {
        return duplicateGroups.size
    }


    inner class DuplicateGroupViewHolder(
        itemView: View
    ) : RecyclerView.ViewHolder(itemView) {

        private val tvGroupTitle =
            itemView.findViewById<TextView>(
                R.id.tvDuplicateGroupTitle
            )

        private val tvGroupCount =
            itemView.findViewById<TextView>(
                R.id.tvDuplicateGroupCount
            )

        private val photoContainer =
            itemView.findViewById<LinearLayout>(
                R.id.duplicatePhotoContainer
            )

        private val tvGroupInfo =
            itemView.findViewById<TextView>(
                R.id.tvDuplicateGroupInfo
            )


        fun bind(
            group: DuplicatePhotoGroup,
            position: Int
        ) {

            val files =
                group.files

            tvGroupTitle.text =
                "Duplicate Group ${position + 1}"

            tvGroupCount.text =
                if (files.size == 1) {
                    "1 photo"
                } else {
                    "${files.size} photos"
                }

            photoContainer.removeAllViews()

            files.forEach { file ->

                val imageView =
                    ImageView(
                        itemView.context
                    )

                val size =
                    dpToPx(88)

                val margin =
                    dpToPx(6)

                val layoutParams =
                    LinearLayout.LayoutParams(
                        size,
                        size
                    ).apply {
                        marginEnd = margin
                    }

                imageView.layoutParams =
                    layoutParams

                imageView.scaleType =
                    ImageView.ScaleType.CENTER_CROP

                imageView.setPadding(
                    2,
                    2,
                    2,
                    2
                )

                imageView.contentDescription =
                    file.name

                loadThumbnail(
                    imageView,
                    file
                )

                updateSelectionAppearance(
                    imageView,
                    file
                )

                imageView.setOnClickListener {

                    togglePhotoSelection(
                        file
                    )

                    updateSelectionAppearance(
                        imageView,
                        file
                    )
                }

                photoContainer.addView(
                    imageView
                )
            }

            val firstFile =
                files.firstOrNull()

            val fileSize =
                firstFile?.sizeBytes ?: 0L

            tvGroupInfo.text =
                String.format(
                    Locale.US,
                    "Same content • %s each",
                    formatDataSize(
                        fileSize
                    )
                )
        }


        private fun togglePhotoSelection(
            file: CleanerFile
        ) {

            if (
                selectedPhotoUris.contains(
                    file.uri
                )
            ) {

                selectedPhotoUris.remove(
                    file.uri
                )

            } else {

                selectedPhotoUris.add(
                    file.uri
                )
            }

            onSelectionChanged?.invoke(
                selectedPhotoUris.size
            )
        }


        private fun updateSelectionAppearance(
            imageView: ImageView,
            file: CleanerFile
        ) {

            val isSelected =
                selectedPhotoUris.contains(
                    file.uri
                )

            val background =
                GradientDrawable().apply {

                    shape =
                        GradientDrawable.RECTANGLE

                    cornerRadius =
                        dpToPx(8).toFloat()

                    setColor(
                        Color.TRANSPARENT
                    )

                    if (isSelected) {

                        setStroke(
                            dpToPx(3),
                            Color.rgb(
                                118,
                                85,
                                246
                            )
                        )

                    } else {

                        setStroke(
                            dpToPx(1),
                            Color.rgb(
                                231,
                                222,
                                255
                            )
                        )
                    }
                }

            imageView.background =
                background

            imageView.alpha =
                if (isSelected) {
                    0.75f
                } else {
                    1.0f
                }
        }


        private fun loadThumbnail(
            imageView: ImageView,
            file: CleanerFile
        ) {

            try {

                val uri =
                    Uri.parse(
                        file.uri
                    )

                val resolver =
                    itemView.context
                        .contentResolver

                val boundsOptions =
                    BitmapFactory.Options().apply {
                        inJustDecodeBounds = true
                    }

                resolver
                    .openInputStream(uri)
                    ?.use { inputStream ->

                        BitmapFactory.decodeStream(
                            inputStream,
                            null,
                            boundsOptions
                        )
                    }

                val imageWidth =
                    boundsOptions.outWidth

                val imageHeight =
                    boundsOptions.outHeight

                if (
                    imageWidth <= 0 ||
                    imageHeight <= 0
                ) {

                    imageView.setImageResource(
                        R.drawable.ic_photos
                    )

                    return
                }

                val targetSize =
                    dpToPx(88)

                var sampleSize =
                    1

                while (
                    imageWidth / sampleSize >
                    targetSize * 2 ||
                    imageHeight / sampleSize >
                    targetSize * 2
                ) {

                    sampleSize *= 2
                }

                val bitmapOptions =
                    BitmapFactory.Options().apply {

                        inSampleSize =
                            sampleSize

                        inPreferredConfig =
                            Bitmap.Config.RGB_565
                    }

                resolver
                    .openInputStream(uri)
                    ?.use { inputStream ->

                        val bitmap =
                            BitmapFactory.decodeStream(
                                inputStream,
                                null,
                                bitmapOptions
                            )

                        if (bitmap != null) {

                            imageView.setImageBitmap(
                                bitmap
                            )

                        } else {

                            imageView.setImageResource(
                                R.drawable.ic_photos
                            )
                        }
                    }

            } catch (_: Exception) {

                imageView.setImageResource(
                    R.drawable.ic_photos
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

                else ->
                    String.format(
                        Locale.US,
                        "%.0f KB",
                        bytes / 1000.0
                    )
            }
        }


        private fun dpToPx(
            dp: Int
        ): Int {

            return (
                    dp *
                            itemView.resources
                                .displayMetrics
                                .density
                    ).toInt()
        }
    }
}