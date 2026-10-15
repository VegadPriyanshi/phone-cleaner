package com.example.phonecleaner.ui.results

import android.graphics.BitmapFactory
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.phonecleaner.R
import com.example.phonecleaner.data.model.CleanerFile
import com.google.android.material.checkbox.MaterialCheckBox
import java.util.Locale
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ScannedPhotoAdapter(
    private val onSelectionChanged: (Int) -> Unit
) : RecyclerView.Adapter<ScannedPhotoAdapter.PhotoViewHolder>() {

    private val photos = mutableListOf<CleanerFile>()

    private val selectedUris =
        mutableSetOf<String>()

    private val adapterScope =
        CoroutineScope(Dispatchers.Main)

    fun submitList(
        newPhotos: List<CleanerFile>
    ) {

        photos.clear()
        photos.addAll(newPhotos)

        selectedUris.clear()

        notifyDataSetChanged()

        onSelectionChanged(0)
    }

    fun getSelectedPhotos(): List<CleanerFile> {

        return photos.filter {
            selectedUris.contains(it.uri)
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): PhotoViewHolder {

        val view =
            LayoutInflater.from(parent.context)
                .inflate(
                    R.layout.item_scanned_photo,
                    parent,
                    false
                )

        return PhotoViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: PhotoViewHolder,
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
        holder: PhotoViewHolder
    ) {

        holder.clearThumbnailJob()

        super.onViewRecycled(holder)
    }

    inner class PhotoViewHolder(
        itemView: View
    ) : RecyclerView.ViewHolder(itemView) {

        private val thumbnail =
            itemView.findViewById<ImageView>(
                R.id.ivPhotoThumbnail
            )

        private val name =
            itemView.findViewById<TextView>(
                R.id.tvPhotoName
            )

        private val size =
            itemView.findViewById<TextView>(
                R.id.tvPhotoSize
            )

        private val checkBox =
            itemView.findViewById<MaterialCheckBox>(
                R.id.checkPhoto
            )

        private var thumbnailJob: Job? = null

        fun bind(
            photo: CleanerFile
        ) {

            thumbnailJob?.cancel()

            name.text =
                photo.name

            size.text =
                formatSize(
                    photo.sizeBytes
                )

            checkBox.setOnCheckedChangeListener(
                null
            )

            checkBox.isChecked =
                selectedUris.contains(
                    photo.uri
                )

            checkBox.setOnCheckedChangeListener { _, checked ->

                if (checked) {

                    selectedUris.add(
                        photo.uri
                    )

                } else {

                    selectedUris.remove(
                        photo.uri
                    )
                }

                onSelectionChanged(
                    selectedUris.size
                )
            }

            itemView.setOnClickListener {

                checkBox.isChecked =
                    !checkBox.isChecked
            }

            loadThumbnail(
                photo.uri
            )
        }

        private fun loadThumbnail(
            uriString: String
        ) {

            thumbnail.setImageDrawable(null)

            thumbnailJob =
                adapterScope.launch {

                    val bitmap =
                        withContext(
                            Dispatchers.IO
                        ) {

                            try {

                                itemView.context
                                    .contentResolver
                                    .openInputStream(
                                        Uri.parse(uriString)
                                    )
                                    ?.use { input ->

                                        BitmapFactory
                                            .decodeStream(
                                                input
                                            )
                                    }

                            } catch (
                                exception: Exception
                            ) {

                                null
                            }
                        }

                    if (
                        bitmap != null &&
                        bindingAdapterPosition !=
                        RecyclerView.NO_POSITION
                    ) {

                        thumbnail.setImageBitmap(
                            bitmap
                        )
                    }
                }
        }

        fun clearThumbnailJob() {

            thumbnailJob?.cancel()
            thumbnailJob = null
        }

        private fun formatSize(
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