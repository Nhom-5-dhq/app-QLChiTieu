package com.example.appquanlychitieu

import android.app.DatePickerDialog
import android.graphics.Color
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class AddTransactionActivity : AppCompatActivity() {

    private lateinit var btnBack: ImageView
    private lateinit var btnTypeExpense: Button
    private lateinit var btnTypeIncome: Button
    private lateinit var etAmount: EditText
    private lateinit var spinnerCategory: Spinner
    private lateinit var etNote: EditText
    private lateinit var tvSelectedDate: TextView
    private lateinit var btnSelectDate: LinearLayout
    private lateinit var btnSave: Button

    private var isExpense = true
    private val selectedCalendar = Calendar.getInstance()

    private val expenseCategories = arrayOf("Ăn uống", "Mua sắm", "Chi tiêu gia đình", "Chi tiêu cá nhân", "Khác")
    private val incomeCategories = arrayOf("Lương", "Khác")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_transaction)

        // Ánh xạ ID
        btnBack = findViewById(R.id.btnBack)
        btnTypeExpense = findViewById(R.id.btnTypeExpense)
        btnTypeIncome = findViewById(R.id.btnTypeIncome)
        etAmount = findViewById(R.id.etAmount)
        spinnerCategory = findViewById(R.id.spinnerCategory)
        etNote = findViewById(R.id.etNote)
        tvSelectedDate = findViewById(R.id.tvSelectedDate)
        btnSelectDate = findViewById(R.id.btnSelectDate)
        btnSave = findViewById(R.id.btnSave)

        // Sự kiện bấm Nút Quay Lại
        btnBack.setOnClickListener {
            finish()
        }

        updateDateDisplay()

        btnSelectDate.setOnClickListener {
            showDatePicker()
        }

        btnTypeExpense.setOnClickListener {
            isExpense = true
            updateTypeUI()
            setupCategorySpinner()
        }

        btnTypeIncome.setOnClickListener {
            isExpense = false
            updateTypeUI()
            setupCategorySpinner()
        }

        setupCategorySpinner()

        btnSave.setOnClickListener {
            saveTransaction()
        }
    }

    private fun updateDateDisplay() {
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        tvSelectedDate.text = sdf.format(selectedCalendar.time)
    }

    private fun showDatePicker() {
        val dateSetListener = DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
            selectedCalendar.set(Calendar.YEAR, year)
            selectedCalendar.set(Calendar.MONTH, month)
            selectedCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            updateDateDisplay()
        }

        DatePickerDialog(
            this,
            dateSetListener,
            selectedCalendar.get(Calendar.YEAR),
            selectedCalendar.get(Calendar.MONTH),
            selectedCalendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun setupCategorySpinner() {
        val categories = if (isExpense) expenseCategories else incomeCategories
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categories)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerCategory.adapter = adapter
    }

    private fun updateTypeUI() {
        if (isExpense) {
            btnTypeExpense.setBackgroundColor(Color.parseColor("#2196F3"))
            btnTypeExpense.setTextColor(Color.WHITE)
            btnTypeIncome.setBackgroundColor(Color.parseColor("#E0E0E0"))
            btnTypeIncome.setTextColor(Color.BLACK)
        } else {
            btnTypeIncome.setBackgroundColor(Color.parseColor("#4CAF50"))
            btnTypeIncome.setTextColor(Color.WHITE)
            btnTypeExpense.setBackgroundColor(Color.parseColor("#E0E0E0"))
            btnTypeExpense.setTextColor(Color.BLACK)
        }
    }

    private fun saveTransaction() {
        val amountStr = etAmount.text.toString().trim()
        if (amountStr.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập số tiền", Toast.LENGTH_SHORT).show()
            return
        }

        val amount = amountStr.toDoubleOrNull() ?: 0.0
        val category = spinnerCategory.selectedItem.toString()
        val note = etNote.text.toString().trim()
        val type = if (isExpense) "EXPENSE" else "INCOME"
        val dateMillis = selectedCalendar.timeInMillis

        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val db = FirebaseFirestore.getInstance()

        val transactionData = hashMapOf(
            "amount" to amount,
            "category" to category,
            "note" to note,
            "type" to type,
            "date" to dateMillis
        )

        db.collection("users").document(userId).collection("transactions")
            .add(transactionData)
            .addOnSuccessListener {
                Toast.makeText(this, "Thêm giao dịch thành công!", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Lỗi khi lưu giao dịch", Toast.LENGTH_SHORT).show()
            }
    }
}