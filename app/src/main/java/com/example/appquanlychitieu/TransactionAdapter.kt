package com.example.appquanlychitieu

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.*

class TransactionAdapter(
    private var transactionList: MutableList<Transaction>,
    private val onItemClick: (Transaction) -> Unit
) : RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder>() {

    class TransactionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvCategory: TextView = itemView.findViewById(R.id.tvCategory)
        val tvNote: TextView = itemView.findViewById(R.id.tvNote)
        val tvDate: TextView = itemView.findViewById(R.id.tvDate)
        val tvAmount: TextView = itemView.findViewById(R.id.tvAmount)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_transaction, parent, false)
        return TransactionViewHolder(view)
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        val transaction = transactionList[position]

        // 1. Danh mục
        holder.tvCategory.text = transaction.category

        // 2. Ghi chú (Nếu trống hoặc bị trùng với type thì hiện "Không có ghi chú")
        val noteText = transaction.note
        if (noteText.isNullOrEmpty() || noteText == "EXPENSE" || noteText == "INCOME") {
            holder.tvNote.text = "Không có ghi chú"
        } else {
            holder.tvNote.text = noteText
        }

        // 3. Ngày tháng
        val sdf = SimpleDateFormat("HH:mm - dd/MM/yyyy", Locale.getDefault())
        holder.tvDate.text = sdf.format(Date(transaction.date))

        // 4. Số tiền & Dấu +/-
        if (transaction.type == "EXPENSE" || transaction.type == "CHI") {
            holder.tvAmount.text = String.format("-%,.0f đ", transaction.amount)
            holder.tvAmount.setTextColor(Color.parseColor("#F43F5E"))
        } else {
            holder.tvAmount.text = String.format("+%,.0f đ", transaction.amount)
            holder.tvAmount.setTextColor(Color.parseColor("#10B981"))
        }

        holder.itemView.setOnClickListener {
            onItemClick(transaction)
        }
    }

    override fun getItemCount(): Int = transactionList.size

    fun updateData(newList: List<Transaction>) {
        transactionList.clear()
        transactionList.addAll(newList)
        notifyDataSetChanged()
    }

    fun getItemAt(position: Int): Transaction = transactionList[position]
}