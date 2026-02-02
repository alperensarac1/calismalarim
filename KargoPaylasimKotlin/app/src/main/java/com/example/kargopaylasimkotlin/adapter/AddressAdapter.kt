package com.example.kargopaylasimkotlin.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.kargopaylasimkotlin.R
import com.example.kargopaylasimkotlin.dto.AddressDto

class AddressAdapter(
    private val onEdit: (AddressDto) -> Unit,
    private val onSetDefault: (AddressDto) -> Unit,
    private val onDelete: (AddressDto) -> Unit
) : RecyclerView.Adapter<AddressAdapter.VH>() {

    private val items = ArrayList<AddressDto>()

    fun submit(list: List<AddressDto>) {
        items.clear()
        items.addAll(list)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_address, parent, false)
        return VH(v)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        val a = items[position]
        holder.bind(a)
    }

    inner class VH(v: View) : RecyclerView.ViewHolder(v) {
        private val tvTitle = v.findViewById<TextView>(R.id.tvAddrTitle)
        private val tvDefault = v.findViewById<TextView>(R.id.tvDefault)
        private val tvCityDist = v.findViewById<TextView>(R.id.tvCityDist)
        private val tvLine = v.findViewById<TextView>(R.id.tvLine)
        private val btnEdit = v.findViewById<Button>(R.id.btnEdit)
        private val btnDefault = v.findViewById<Button>(R.id.btnDefault)
        private val btnDelete = v.findViewById<Button>(R.id.btnDelete)

        fun bind(a: AddressDto) {
            tvTitle.text = a.title
            tvCityDist.text = "${a.city} / ${a.district}"
            tvLine.text = a.address_line

            val isDef = a.is_default == 1
            tvDefault.visibility = if (isDef) View.VISIBLE else View.GONE
            btnDefault.isEnabled = !isDef

            btnEdit.setOnClickListener { onEdit(a) }
            btnDefault.setOnClickListener { onSetDefault(a) }
            btnDelete.setOnClickListener { onDelete(a) }
        }
    }
}
