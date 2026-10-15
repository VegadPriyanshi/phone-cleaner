package com.example.phonecleaner.ui.results

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.phonecleaner.R
import com.example.phonecleaner.data.model.JunkCategory
import java.util.Locale
import android.widget.ImageView
class JunkCategoryAdapter(
    private val onCategoryClick: (JunkCategory) -> Unit
) : RecyclerView.Adapter<JunkCategoryAdapter.JunkCategoryViewHolder>() {

    private val categories = mutableListOf<JunkCategory>()

    fun submitList(newCategories: List<JunkCategory>) {
        categories.clear()
        categories.addAll(newCategories)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): JunkCategoryViewHolder {

        val view = LayoutInflater.from(parent.context)
            .inflate(
                R.layout.item_junk_category,
                parent,
                false
            )

        return JunkCategoryViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: JunkCategoryViewHolder,
        position: Int
    ) {
        holder.bind(categories[position])
    }

    override fun getItemCount(): Int {
        return categories.size
    }

    inner class JunkCategoryViewHolder(
        itemView: View
    ) : RecyclerView.ViewHolder(itemView) {


        private val iconView: ImageView =
            itemView.findViewById(R.id.ivJunkCategoryIcon)

        private val titleText: TextView =
            itemView.findViewById(R.id.tvJunkCategoryTitle)

        private val countText: TextView =
            itemView.findViewById(R.id.tvJunkCategoryCount)

        private val sizeText: TextView =
            itemView.findViewById(R.id.tvJunkCategorySize)

        fun bind(category: JunkCategory) {


            when (category.id) {

                "temporary_files" -> {
                    iconView.setImageResource(R.drawable.ic_description)
                }

                "empty_files" -> {
                    iconView.setImageResource(R.drawable.ic_description)
                }

                "old_apk_files" -> {
                    iconView.setImageResource(R.drawable.ic_old_apk)
                }

                "thumbnail_caches" -> {
                    iconView.setImageResource(R.drawable.ic_thumbnail_cache)
                }

                "leftover_app_data" -> {
                    iconView.setImageResource(R.drawable.ic_apps)
                }

                else -> {
                    iconView.setImageResource(R.drawable.ic_description)
                }
            }
            titleText.text = category.title

            countText.text =
                "${category.itemCount} files"

            sizeText.text =
                formatStorageSize(category.sizeBytes)

            itemView.setOnClickListener {
                onCategoryClick(category)
            }
        }
    }

    private fun formatStorageSize(bytes: Long): String {

        val kb = bytes / 1_000.0
        val mb = bytes / 1_000_000.0
        val gb = bytes / 1_000_000_000.0

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
                    "%.2f MB",
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
}