package com.example.phonecleaner.ui.results

import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

class ScannedVideoGridAdapter(
    private val onSelectionChanged: (
        selectedCount: Int
    ) -> Unit
) : RecyclerView.Adapter<ScannedVideoGridAdapter.VideoViewHolder>() {

    private var videos: List<CleanerFile> =
        emptyList()

    private val selectedUris =
        mutableSetOf<String>()

    private val adapterScope =
        CoroutineScope(
            SupervisorJob() +
                    Dispatchers.Main.immediate
        )

    class VideoViewHolder(
        parent: ViewGroup
    ) : RecyclerView.ViewHolder(
        LayoutInflater.from(parent.context)
            .inflate(
                R.layout.item_scanned_video,
                parent,
                false
            )
    ) {

        val thumbnail: ImageView =
            itemView.findViewById(
                R.id.ivVideoThumbnail
            )

        val sizeText: TextView =
            itemView.findViewById(
                R.id.tvVideoSize
            )

        val checkBox: MaterialCheckBox =
            itemView.findViewById(
                R.id.checkVideo
            )

        var boundUri: String? = null

        var thumbnailJob: Job? = null
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): VideoViewHolder {

        return VideoViewHolder(
            parent
        )
    }

    override fun onBindViewHolder(
        holder: VideoViewHolder,
        position: Int
    ) {

        val video =
            videos[position]

        holder.thumbnailJob?.cancel()

        holder.boundUri =
            video.uri

        holder.thumbnail.setImageDrawable(
            null
        )

        holder.thumbnail.setBackgroundColor(
            0xFFEDE9F5.toInt()
        )

        holder.sizeText.text =
            formatDataSize(
                video.sizeBytes
            )

        holder.checkBox.setOnCheckedChangeListener(
            null
        )

        holder.checkBox.isChecked =
            selectedUris.contains(
                video.uri
            )

        holder.checkBox.setOnCheckedChangeListener {
                _, isChecked ->

            if (isChecked) {

                selectedUris.add(
                    video.uri
                )

            } else {

                selectedUris.remove(
                    video.uri
                )
            }

            onSelectionChanged(
                selectedUris.size
            )
        }

        holder.itemView.setOnClickListener {

            val currentlySelected =
                selectedUris.contains(
                    video.uri
                )

            if (currentlySelected) {

                selectedUris.remove(
                    video.uri
                )

            } else {

                selectedUris.add(
                    video.uri
                )
            }

            holder.checkBox.setOnCheckedChangeListener(
                null
            )

            holder.checkBox.isChecked =
                selectedUris.contains(
                    video.uri
                )

            holder.checkBox.setOnCheckedChangeListener {
                    _, isChecked ->

                if (isChecked) {

                    selectedUris.add(
                        video.uri
                    )

                } else {

                    selectedUris.remove(
                        video.uri
                    )
                }

                onSelectionChanged(
                    selectedUris.size
                )
            }

            onSelectionChanged(
                selectedUris.size
            )
        }

        val videoUri =
            Uri.parse(
                video.uri
            )

        val expectedUri =
            video.uri

        holder.thumbnailJob =
            adapterScope.launch {

                val bitmap =
                    withContext(
                        Dispatchers.IO
                    ) {
                        loadVideoThumbnail(
                            holder.itemView.context,
                            videoUri
                        )
                    }

                if (
                    holder.boundUri ==
                    expectedUri
                ) {

                    if (bitmap != null) {

                        holder.thumbnail
                            .setImageBitmap(
                                bitmap
                            )

                    } else {

                        holder.thumbnail
                            .setImageDrawable(
                                null
                            )
                    }
                }
            }
    }

    override fun getItemCount(): Int =
        videos.size

    fun submitList(
        newVideos: List<CleanerFile>
    ) {

        videos =
            newVideos.toList()

        /*
         * Keep only selections that still exist
         * in the current video list.
         *
         * IMPORTANT:
         * Do NOT call onSelectionChanged() here.
         *
         * submitList() is also used when the
         * screen is reopened/restored.
         */
        val currentUris =
            videos
                .map {
                    it.uri
                }
                .toSet()

        selectedUris.retainAll(
            currentUris
        )

        notifyDataSetChanged()
    }


    fun setSelectedUris(
        uris: Set<String>
    ) {

        selectedUris.clear()

        val availableUris =
            videos
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

    fun getSelectedVideos(): List<CleanerFile> {

        return videos.filter {
            selectedUris.contains(
                it.uri
            )
        }
    }

    fun selectAll() {

        selectedUris.clear()

        selectedUris.addAll(
            videos.map {
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

        onSelectionChanged(
            0
        )
    }

    fun areAllSelected(): Boolean {

        return videos.isNotEmpty() &&
                selectedUris.size ==
                videos.size
    }

    private fun loadVideoThumbnail(
        context: android.content.Context,
        uri: Uri
    ): Bitmap? {

        val retriever =
            MediaMetadataRetriever()

        return try {

            retriever.setDataSource(
                context,
                uri
            )

            retriever.getFrameAtTime(
                0L,
                MediaMetadataRetriever.OPTION_CLOSEST_SYNC
            )

        } catch (
            exception: Exception
        ) {

            null

        } finally {

            try {

                retriever.release()

            } catch (
                exception: Exception
            ) {
                // Ignore release errors.
            }
        }
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

    override fun onViewRecycled(
        holder: VideoViewHolder
    ) {

        holder.thumbnailJob?.cancel()

        holder.thumbnailJob =
            null

        holder.boundUri =
            null

        super.onViewRecycled(
            holder
        )
    }

    override fun onDetachedFromRecyclerView(
        recyclerView: RecyclerView
    ) {

        adapterScope.coroutineContext[
            Job
        ]?.cancel()

        super.onDetachedFromRecyclerView(
            recyclerView
        )
    }
}