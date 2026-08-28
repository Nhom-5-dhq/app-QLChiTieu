package com.example.appquanlychitieu

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TransactionAdapter(
    private val list: List<Transaction>,
    private val onItemClick: (Transaction) -> Unit
) : RecyclerView.Adapter<TransactionAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvCategory: TextView = view.findViewById(R.id.tvCategory)
        val tvNote: TextView = view.findViewById(R.id.tvNote)
        val tvDate: TextView = view.findViewById(R.id.tvDate)
        val tvAmount: TextView = view.findViewById(R.id.tvAmount)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_transaction, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]

        // Gán tên danh mục & ghi chú
        holder.tvCategory.text = item.category
        holder.tvNote.text = if (item.note.isNotEmpty()) item.note else "Không có ghi chú"

        // Địn dạng hiển thị thời gian: Giờ:Phút - Ngày/Tháng/Năm
        val sdf = SimpleDateFormat("HH:mm - dd/MM/yyyy", Locale.getDefault())
        holder.tvDate.text = sdf.format(Date(item.date))

        // Tô màu đỏ cho Chi tiêu, màu xanh cho Thu nhập
        if (item.type == "EXPENSE") {
            holder.tvAmount.text = String.format("-%,.0f đ", item.amount)
            holder.tvAmount.setTextColor(Color.parseColor("#F44336")) // Đỏ
        } else {
            holder.tvAmount.text = String.format("+%,.0f đ", item.amount)
            holder.tvAmount.setTextColor(Color.parseColor("#4CAF50")) // Xanh lá
        }

        // Bắt sự kiện bấm vào từng item
        holder.itemView.setOnClickListener { onItemClick(item) }
    }

    override fun getItemCount() = list.size
}