package com.example.appquanlychitieu

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*

class HomeFragment : Fragment() {

    private lateinit var tvBalance: TextView
    private lateinit var tvTotalIncome: TextView
    private lateinit var tvTotalExpense: TextView
    private lateinit var boxTotalIncome: LinearLayout
    private lateinit var boxTotalExpense: LinearLayout
    private lateinit var rvTransactions: RecyclerView

    private lateinit var adapter: TransactionAdapter
    private val transactionList = mutableListOf<Transaction>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        // Khớp ID chính xác với fragment_home.xml
        tvBalance = view.findViewById(R.id.tvBalance)
        tvTotalIncome = view.findViewById(R.id.tvTotalIncome)
        tvTotalExpense = view.findViewById(R.id.tvTotalExpense)
        boxTotalIncome = view.findViewById(R.id.boxTotalIncome)
        boxTotalExpense = view.findViewById(R.id.boxTotalExpense)

        setupRecyclerView()

        // Bấm Tổng Thu -> Mở Trang Lịch sử Tổng thu
        boxTotalIncome.setOnClickListener {
            val intent = Intent(requireContext(), TransactionHistoryActivity::class.java)
            intent.putExtra("TYPE", "INCOME")
            startActivity(intent)
        }

        // Bấm Tổng Chi -> Mở Trang Lịch sử Tổng chi
        boxTotalExpense.setOnClickListener {
            val intent = Intent(requireContext(), TransactionHistoryActivity::class.java)
            intent.putExtra("TYPE", "EXPENSE")
            startActivity(intent)
        }

        loadTransactions()
        return view
    }

    private fun setupRecyclerView() {
        adapter = TransactionAdapter(transactionList) { transaction ->
            showTransactionDetailDialog(transaction)
        }
        rvTransactions.layoutManager = LinearLayoutManager(requireContext())
        rvTransactions.adapter = adapter
    }

    private fun loadTransactions() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val db = FirebaseFirestore.getInstance()

        db.collection("users").document(userId).collection("transactions")
            .get()
            .addOnSuccessListener { querySnapshot ->
                transactionList.clear()
                var totalInc = 0.0
                var totalExp = 0.0

                for (doc in querySnapshot) {
                    val id = doc.id
                    val amount = doc.getDouble("amount") ?: 0.0
                    val category = doc.getString("category") ?: "Khác"
                    val note = doc.getString("note") ?: ""
                    val type = doc.getString("type") ?: ""
                    val date = doc.getLong("date") ?: System.currentTimeMillis()

                    val transaction = Transaction(id, amount, category, note, type, date)
                    transactionList.add(transaction)

                    if (type == "INCOME" || type == "THU") {
                        totalInc += amount
                    } else if (type == "EXPENSE" || type == "CHI") {
                        totalExp += amount
                    }
                }

                transactionList.sortByDescending { it.date }

                val balance = totalInc - totalExp
                tvBalance.text = String.format("%,.0f đ", balance)
                tvTotalIncome.text = String.format("%,.0f đ", totalInc)
                tvTotalExpense.text = String.format("%,.0f đ", totalExp)

                adapter.updateData(transactionList)
            }
    }

    private fun showTransactionDetailDialog(transaction: Transaction) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_transaction_detail, null)
        val builder = AlertDialog.Builder(requireContext()).setView(dialogView)
        val dialog = builder.create()

        val etAmount = dialogView.findViewById<EditText>(R.id.etAmount)
        val spinnerCategory = dialogView.findViewById<Spinner>(R.id.spinnerCategory)
        val etNote = dialogView.findViewById<EditText>(R.id.etNote)
        val btnDelete = dialogView.findViewById<Button>(R.id.btnDelete)
        val btnUpdate = dialogView.findViewById<Button>(R.id.btnUpdate)

        etAmount.setText(transaction.amount.toString())
        etNote.setText(transaction.note)

        val categories = if (transaction.type == "EXPENSE" || transaction.type == "CHI") {
            arrayOf("Ăn uống", "Mua sắm", "Chi tiêu gia đình", "Chi tiêu cá nhân", "Khác")
        } else {
            arrayOf("Lương", "Khác")
        }

        val adapterCategory = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, categories)
        adapterCategory.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerCategory.adapter = adapterCategory

        val catIndex = categories.indexOf(transaction.category)
        if (catIndex >= 0) spinnerCategory.setSelection(catIndex)

        // Bấm SỬA trong Dialog
        btnUpdate.setOnClickListener {
            val newAmount = etAmount.text.toString().toDoubleOrNull() ?: transaction.amount
            val newCategory = spinnerCategory.selectedItem.toString()
            val newNote = etNote.text.toString().trim()

            updateTransactionInFirestore(transaction.id, newAmount, newCategory, newNote)
            dialog.dismiss()
        }

        // Bấm XÓA trong Dialog
        btnDelete.setOnClickListener {
            dialog.dismiss()
            deleteTransactionFromFirestore(transaction)
        }

        dialog.show()
    }

    private fun updateTransactionInFirestore(id: String, amount: Double, category: String, note: String) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val db = FirebaseFirestore.getInstance()

        val updates = hashMapOf<String, Any>(
            "amount" to amount,
            "category" to category,
            "note" to note
        )

        db.collection("users").document(userId).collection("transactions")
            .document(id)
            .update(updates)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Cập nhật thành công!", Toast.LENGTH_SHORT).show()
                loadTransactions()
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Lỗi khi cập nhật!", Toast.LENGTH_SHORT).show()
            }
    }

    private fun deleteTransactionFromFirestore(transaction: Transaction) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val db = FirebaseFirestore.getInstance()

        db.collection("users").document(userId).collection("transactions")
            .document(transaction.id)
            .delete()
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Đã xóa giao dịch", Toast.LENGTH_SHORT).show()
                loadTransactions()
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Lỗi khi xóa giao dịch", Toast.LENGTH_SHORT).show()
            }
    }
}