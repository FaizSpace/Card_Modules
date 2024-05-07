package com.business.visiting.card.creator.editor.ui.savedwork

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.business.visiting.card.creator.editor.databinding.ItemPdfFileBinding
import java.io.File

class MyWorkPdfAdapter(
    var listOfPdf: MutableList<File> = mutableListOf(),
    private var onSingleClick: (File) -> Unit,
    private var onDeleteClick: (Int,File) -> Unit,
    private var onShareClick:(File)-> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    inner class MyWorkPdfViewHolder(var binding: ItemPdfFileBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(position: Int) {
            binding.tvPdfName.text=(listOfPdf[position]).name
            binding.tvPdfName.setOnClickListener {
                onSingleClick.invoke(listOfPdf[position])
            }
            binding.imgPdfShare.setOnClickListener {
                onShareClick.invoke(listOfPdf[position])
            }
            binding.imgDelete.setOnClickListener {
                onDeleteClick.invoke(position,listOfPdf[position])
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return MyWorkPdfViewHolder(
            ItemPdfFileBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )

    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val myWorkPdfViewHolder:MyWorkPdfViewHolder = holder as MyWorkPdfViewHolder
        myWorkPdfViewHolder.bind(position)
    }

    override fun getItemId(position: Int): Long {
        return 0
    }

    override fun getItemCount(): Int {
        return listOfPdf.size
    }

    fun removeItem(position: Int){
        listOfPdf.removeAt(position)
        notifyDataSetChanged()
    }
}