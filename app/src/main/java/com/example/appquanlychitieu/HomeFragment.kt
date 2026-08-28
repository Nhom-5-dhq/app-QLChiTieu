package com.example.appquanlychitieu

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HomeFragment : Fragment() {

    private lateinit var tvTotalBalance: TextView
    private lateinit var tvTotalIncome: TextView
    private lateinit var tvTotalExpense: TextView
    private lateinit var btnViewHistory: LinearLayout
    private lateinit var rvTransactions: RecyclerView

    private val transactionList = mutableListOf<Transaction>()
    private lateinit var adapter: TransactionAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        tvTotalBalance = view.findViewById(R.id.tvTotalBalance)
        tvTotalIncome = view.findViewById(R.id.tvTotalIncome)
        tvTotalExpense = view.findViewById(R.id.tvTotalExpense)
        btnViewHistory = view.findViewById(R.id.btnViewHistory)
        rvTransactions = view.findViewById(R.id.rvTransactions)

        rvTransactions.layoutManager = LinearLayoutManager(requireContext())
        adapter = TransactionAdapter(transactionList) { item ->
            showTransactionDetailDialog(item)
        }
        rvTransactions.adapter = adapter

        btnViewHistory.setOnClickListener {
            Toast.makeText(requireContext(), "Bạn đang ở màn hình Lịch sử giao dịch", Toast.LENGTH_SHORT).show()
        }

        loadTransactions()

        return view
    }

    private fun loadTransactions() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val db = FirebaseFirestore.getInstance()

        db.collection("users").document(userId).collection("transactions")
            .get()
            .addOnSuccessListener { querySnapshot ->
                transactionList.clear()
                var totalIncome = 0.0
                var totalExpense = 0.0

                for (doc in querySnapshot) {
                    val id = doc.getString("id") ?: ""
                    val amount = doc.getDouble("amount") ?: 0.0
                    val type = doc.getString("type") ?: ""
                    val category = doc.getString("category") ?: ""
                    val note = doc.getString("note") ?: ""
                    val date = doc.getLong("date") ?: 0L

                    val transaction = Transaction(id, amount, type, category, note, date)
                    transactionList.add(transaction)

                    if (type == "EXPENSE") {
                        totalExpense += amount
                    } else if (type == "INCOME") {
                        totalIncome += amount
                    }
                }

                // Sắp xếp ngày mới nhất lên đầu
                transactionList.sortByDescending { it.date }
                adapter.notifyDataSetChanged()

                // Cập nhật các ô số tiền
                val balance = totalIncome - totalExpense
                tvTotalBalance.text = String.format("%,.0f đ", balance)
                tvTotalIncome.text = String.format("%,.0f đ", totalIncome)
                tvTotalExpense.text = String.format("%,.0f đ", totalExpense)
            }
    }

    // Hiển thị Dialog chi tiết giao dịch khi bấm vào từng mục
    private fun showTransactionDetailDialog(item: Transaction) {
        val sdf = SimpleDateFormat("HH:mm:ss - dd/MM/yyyy", Locale.getDefault())
        val dateStr = sdf.format(Date(item.date))
        val typeStr = if (item.type == "EXPENSE") "Chi tiêu" else "Thu nhập"

        val message = """
            📌 Danh mục: ${item.category}
            💵 Số tiền: ${String.format("%,.0f đ", item.amount)}
            🏷 Loại: $typeStr
            📅 Thời gian: $dateStr
            📝 Ghi chú: ${if (item.note.isNotEmpty()) item.note else "Không có"}
        """.trimIndent()

        AlertDialog.Builder(requireContext())
            .setTitle("Chi tiết giao dịch")
            .setMessage(message)
            .setPositiveButton("Đóng") { dialog, _ -> dialog.dismiss() }
            .show()
    }
}