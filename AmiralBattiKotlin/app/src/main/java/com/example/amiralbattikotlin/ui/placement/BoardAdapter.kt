package com.example.amiralbattikotlin.ui.placement


import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.amiralbattikotlin.R
import com.example.amiralbattikotlin.model.BoardCell
import com.example.amiralbattikotlin.model.CellState

class BoardAdapter(
    private val cells: MutableList<BoardCell>,
    private val onCellClick: (BoardCell) -> Unit
) : RecyclerView.Adapter<BoardAdapter.BoardViewHolder>() {

    inner class BoardViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val viewCell: View = itemView.findViewById(R.id.viewCell)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BoardViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_board_cell, parent, false)
        return BoardViewHolder(view)
    }

    override fun onBindViewHolder(holder: BoardViewHolder, position: Int) {
        val cell = cells[position]

        when (cell.state) {
            CellState.EMPTY -> holder.viewCell.setBackgroundColor(Color.parseColor("#D9EAF7"))
            CellState.SHIP -> holder.viewCell.setBackgroundColor(Color.parseColor("#5B7C99"))
            CellState.HIT -> holder.viewCell.setBackgroundColor(Color.RED)
            CellState.MISS -> holder.viewCell.setBackgroundColor(Color.WHITE)
        }

        holder.itemView.setOnClickListener {
            onCellClick(cell)
        }
    }

    override fun getItemCount(): Int = cells.size

    fun refreshBoard() {
        notifyDataSetChanged()
    }
}