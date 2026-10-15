package com.example.phonecleaner.ui.clean

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.phonecleaner.R
import com.example.phonecleaner.data.model.CleanCategory
import com.example.phonecleaner.databinding.ItemCleanCategoryBinding
import java.util.Locale

class CleanCategoryAdapter(
    private val onSelectionChanged: (CleanCategory, Boolean) -> Unit,
    private val onCategoryClick: (CleanCategory) -> Unit
) : ListAdapter<CleanCategory, CleanCategoryAdapter.CategoryViewHolder>(
    DiffCallback()
) {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): CategoryViewHolder {

        val binding =
            ItemCleanCategoryBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )

        return CategoryViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: CategoryViewHolder,
        position: Int
    ) {

        val category = getItem(position)

        holder.bind(category)

        holder.itemView.setOnClickListener {
            onCategoryClick(category)
        }
    }

    class CategoryViewHolder(
        private val binding: ItemCleanCategoryBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(category: CleanCategory) {

            binding.tvCategoryTitle.text =
                category.title

            binding.tvCategoryDetails.text =
                "${category.itemCount} items · ${
                    formatStorageSize(
                        category.sizeBytes
                    )
                }"

            when (category.id) {

                "photos" -> {

                    binding.categoryCardContent.setBackgroundResource(
                        R.drawable.bg_category_photos
                    )

                    binding.ivCategoryIcon.setImageResource(
                        R.drawable.ic_photos
                    )

                    binding.ivCategoryIcon.imageTintList =
                        ColorStateList.valueOf(
                            Color.parseColor("#FF5C8A")
                        )
                }

                "screenshots" -> {

                    binding.categoryCardContent.setBackgroundResource(
                        R.drawable.bg_quick_junk
                    )

                    binding.ivCategoryIcon.setImageResource(
                        R.drawable.ic_screenshot
                    )

                    binding.ivCategoryIcon.imageTintList =
                        ColorStateList.valueOf(
                            Color.parseColor("#FF7A45")
                        )
                }

                "duplicates" -> {

                    binding.categoryCardContent.setBackgroundResource(
                        R.drawable.bg_quick_duplicate
                    )

                    binding.ivCategoryIcon.setImageResource(
                        R.drawable.ic_duplicate_photos
                    )

                    binding.ivCategoryIcon.imageTintList =
                        ColorStateList.valueOf(
                            Color.parseColor("#7655F6")
                        )
                }

                "videos" -> {

                    binding.categoryCardContent.setBackgroundResource(
                        R.drawable.bg_category_videos
                    )

                    binding.ivCategoryIcon.setImageResource(
                        R.drawable.ic_videos
                    )

                    binding.ivCategoryIcon.imageTintList =
                        ColorStateList.valueOf(
                            Color.parseColor("#4285F4")
                        )
                }

                "audio" -> {

                    binding.categoryCardContent.setBackgroundResource(
                        R.drawable.bg_category_photos
                    )

                    binding.ivCategoryIcon.setImageResource(
                        R.drawable.ic_audio
                    )

                    binding.ivCategoryIcon.imageTintList =
                        ColorStateList.valueOf(
                            Color.parseColor("#7655F6")
                        )
                }

                "large_files" -> {

                    binding.categoryCardContent.setBackgroundResource(
                        R.drawable.bg_quick_large
                    )

                    binding.ivCategoryIcon.setImageResource(
                        R.drawable.ic_large_files
                    )

                    binding.ivCategoryIcon.imageTintList =
                        ColorStateList.valueOf(
                            Color.parseColor("#4285F4")
                        )
                }

                "junk" -> {

                    binding.categoryCardContent.setBackgroundResource(
                        R.drawable.bg_quick_junk
                    )

                    binding.ivCategoryIcon.setImageResource(
                        R.drawable.ic_junk_files
                    )

                    binding.ivCategoryIcon.imageTintList =
                        ColorStateList.valueOf(
                            Color.parseColor("#FF7A45")
                        )
                }

                "documents" -> {

                    binding.categoryCardContent.setBackgroundResource(
                        R.drawable.bg_category_photos
                    )

                    binding.ivCategoryIcon.setImageResource(
                        R.drawable.ic_large_files
                    )

                    binding.ivCategoryIcon.imageTintList =
                        ColorStateList.valueOf(
                            Color.parseColor("#7655F6")
                        )
                }

                else -> {

                    binding.categoryCardContent.setBackgroundResource(
                        R.drawable.bg_quick_duplicate
                    )

                    binding.ivCategoryIcon.setImageResource(
                        R.drawable.ic_duplicate_photos
                    )

                    binding.ivCategoryIcon.imageTintList =
                        ColorStateList.valueOf(
                            Color.parseColor("#7655F6")
                        )
                }
            }
        }

        /**
         * Formats storage sizes using the same decimal
         * convention used by the scan results screens.
         *
         * Examples:
         * 168,900,000 bytes -> 168.9 MB
         * 1,610,000,000 bytes -> 1.61 GB
         */
        private fun formatStorageSize(
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

                else -> {

                    String.format(
                        Locale.US,
                        "%.0f KB",
                        bytes / 1000.0
                    )
                }
            }
        }
    }

    private class DiffCallback :
        DiffUtil.ItemCallback<CleanCategory>() {

        override fun areItemsTheSame(
            oldItem: CleanCategory,
            newItem: CleanCategory
        ): Boolean {

            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(
            oldItem: CleanCategory,
            newItem: CleanCategory
        ): Boolean {

            return oldItem == newItem
        }
    }
}