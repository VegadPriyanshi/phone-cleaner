package com.example.phonecleaner.features.language.ui

import android.view.LayoutInflater
import android.view.ViewGroup

import androidx.recyclerview.widget.RecyclerView

import com.example.phonecleaner.R
import com.example.phonecleaner.databinding.ItemLanguageBinding
import com.example.phonecleaner.features.language.data.Language


class LanguageAdapter(
    private val languages: List<Language>,
    private val initiallySelectedCode: String? = null,
    private val onSelected: (Language) -> Unit
) : RecyclerView.Adapter<LanguageAdapter.LanguageViewHolder>() {

    private var selectedPosition =
        languages.indexOfFirst {
            it.translateCode == initiallySelectedCode
        }


    inner class LanguageViewHolder(
        val binding: ItemLanguageBinding
    ) : RecyclerView.ViewHolder(
        binding.root
    )


    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): LanguageViewHolder {

        val binding =
            ItemLanguageBinding.inflate(
                LayoutInflater.from(
                    parent.context
                ),
                parent,
                false
            )

        return LanguageViewHolder(
            binding
        )
    }


    override fun onBindViewHolder(
        holder: LanguageViewHolder,
        position: Int
    ) {

        val language =
            languages[position]


        val isSelected =
            position == selectedPosition


        holder.binding.tvLanguageName.text =
            language.displayName


        holder.binding.tvLanguageNative.text =
            language.nativeName


        holder.binding.root.setBackgroundResource(

            if (isSelected) {

                R.drawable.bg_language_item_selected

            } else {

                R.drawable.bg_language_item
            }
        )


        holder.binding.ivRadio.setImageResource(

            if (isSelected) {

                R.drawable.ic_radio_checked

            } else {

                R.drawable.ic_radio_unchecked
            }
        )


        holder.binding.root.setOnClickListener {

            val clickedPosition =
                holder.bindingAdapterPosition


            if (
                clickedPosition ==
                RecyclerView.NO_POSITION
            ) {
                return@setOnClickListener
            }


            val previousPosition =
                selectedPosition


            selectedPosition =
                clickedPosition


            if (previousPosition >= 0) {

                notifyItemChanged(
                    previousPosition
                )
            }


            notifyItemChanged(
                selectedPosition
            )


            onSelected(
                languages[selectedPosition]
            )
        }
    }


    override fun getItemCount(): Int =
        languages.size
}