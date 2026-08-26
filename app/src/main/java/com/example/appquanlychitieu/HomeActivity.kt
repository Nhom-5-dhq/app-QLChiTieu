package com.example.appquanlychitieu

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class HomeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        val fabAdd = findViewById<FloatingActionButton>(R.id.fabAddTransaction)

        if (savedInstanceState == null) {
            loadFragment(HomeFragment())
        }

        bottomNav?.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> { loadFragment(HomeFragment()); true }
                R.id.nav_statistic -> { loadFragment(StatisticFragment()); true }
                R.id.nav_calendar -> { loadFragment(CalendarFragment()); true }
                R.id.nav_profile -> { loadFragment(ProfileFragment()); true }
                else -> false
            }
        }

        fabAdd?.setOnClickListener {
            showAddTransactionDialog()
        }
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commitAllowingStateLoss()
    }

    private fun showAddTransactionDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_transaction, null)
        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        val rgType = dialogView.findViewById<RadioGroup>(R.id.rgType)
        val edtAmount = dialogView.findViewById<EditText>(R.id.edtAmount)
        val edtCategory = dialogView.findViewById<EditText>(R.id.edtCategory)
        val edtNote = dialogView.findViewById<EditText>(R.id.edtNote)
        val btnSave = dialogView.findViewById<Button>(R.id.btnSaveTransaction)

        btnSave.setOnClickListener {
            val amountStr = edtAmount.text.toString().trim()
            val category = edtCategory.text.toString().trim()
            val note = edtNote.text.toString().trim()

            if (amountStr.isEmpty() || category.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập số tiền và danh mục", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val amount = amountStr.toDoubleOrNull() ?: 0.0
            val isExpense = rgType.checkedRadioButtonId == R.id.rbExpense
            val type = if (isExpense) "EXPENSE" else "INCOME"

            val userId = FirebaseAuth.getInstance().currentUser?.uid ?: "guest_user"
            val db = FirebaseFirestore.getInstance()
            val docRef = db.collection("users").document(userId).collection("transactions").document()

            val transactionMap = hashMapOf(
                "id" to docRef.id,
                "amount" to amount,
                "type" to type,
                "category" to category,
                "date" to System.currentTimeMillis(),
                "note" to note
            )

            docRef.set(transactionMap)
                .addOnSuccessListener {
                    Toast.makeText(this, "Đã lưu giao dịch thành công!", Toast.LENGTH_SHORT).show()
                    dialog.dismiss()
                    loadFragment(HomeFragment())
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Lỗi lưu: ${e.message}", Toast.LENGTH_LONG).show()
                }
        }

        dialog.show()
    }
}