package com.example.phonecleaner.ui.apps

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.phonecleaner.data.model.InstalledApp
import com.example.phonecleaner.databinding.ItemInstalledAppBinding
import com.example.phonecleaner.utils.StorageUtils
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class InstalledAppAdapter(
    private val onAppClick: (InstalledApp) -> Unit
) : ListAdapter<InstalledApp, InstalledAppAdapter.AppViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): AppViewHolder {

        val binding = ItemInstalledAppBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )

        return AppViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: AppViewHolder,
        position: Int
    ) {
        val app = getItem(position)
        holder.bind(app)

        holder.itemView.setOnClickListener {
            onAppClick(app)
        }
    }

    class AppViewHolder(
        private val binding: ItemInstalledAppBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(app: InstalledApp) {

            // App icon
            try {
                val packageManager = binding.root.context.packageManager

                val applicationInfo =
                    packageManager.getApplicationInfo(app.packageName, 0)

                binding.ivAppIcon.setImageDrawable(
                    packageManager.getApplicationIcon(applicationInfo)
                )

            } catch (_: Exception) {
                binding.ivAppIcon.setImageResource(
                    android.R.drawable.sym_def_app_icon
                )
            }

            // App name
            binding.tvAppName.text = app.appName

            // App size
            val sizeText =
                StorageUtils.formatStorageSize(app.sizeBytes)

            // Last used
            val lastUsedText = formatLastUsed(app.lastUsedTime)

            binding.tvAppDetails.text =
                "$sizeText • $lastUsedText"
        }

        private fun formatLastUsed(timestamp: Long): String {

            if (timestamp <= 0L) {
                return "Never used"
            }

            val now = System.currentTimeMillis()
            val difference = now - timestamp

            val oneDay = 24L * 60L * 60L * 1000L

            return when {
                difference < oneDay -> {
                    "Used today"
                }

                difference < 2 * oneDay -> {
                    "Used yesterday"
                }

                difference < 30 * oneDay -> {
                    val days = difference / oneDay
                    "Used $days days ago"
                }

                difference < 365 * oneDay -> {
                    val months = difference / (30 * oneDay)

                    if (months == 1L) {
                        "Used 1 month ago"
                    } else {
                        "Used $months months ago"
                    }
                }

                else -> {
                    val dateFormat =
                        SimpleDateFormat(
                            "dd MMM yyyy",
                            Locale.getDefault()
                        )

                    "Used ${dateFormat.format(Date(timestamp))}"
                }
            }
        }
    }

    private class DiffCallback : DiffUtil.ItemCallback<InstalledApp>() {

        override fun areItemsTheSame(
            oldItem: InstalledApp,
            newItem: InstalledApp
        ): Boolean {
            return oldItem.packageName == newItem.packageName
        }

        override fun areContentsTheSame(
            oldItem: InstalledApp,
            newItem: InstalledApp
        ): Boolean {
            return oldItem == newItem
        }
    }
}