package com.example.eticaretkotlin.adapters


import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.eticaretkotlin.databinding.ItemCategoryBinding
import com.example.eticaretkotlin.model.CategoryDto

class CategoryAdapter(
    private val onSelected: (CategoryDto?) -> Unit
) : ListAdapter<CategoryDto, CategoryAdapter.VH>(DIFF) {

    private var selectedId: Int? = null

    fun setSelected(id: Int?) {
        selectedId = id
        notifyDataSetChanged()
    }

    inner class VH(val b: ItemCategoryBinding) : RecyclerView.ViewHolder(b.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val b = ItemCategoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(b)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = getItem(position)
        holder.b.chip.text = item.name
        holder.b.chip.isChecked = (item.id == selectedId)
        holder.b.chip.setOnClickListener {
            selectedId = if (selectedId == item.id) null else item.id
            onSelected(if (selectedId == null) null else item)
            notifyDataSetChanged()
        }
    }

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<CategoryDto>() {
            override fun areItemsTheSame(o: CategoryDto, n: CategoryDto) = o.id == n.id
            override fun areContentsTheSame(o: CategoryDto, n: CategoryDto) = o == n
        }
    }
}
