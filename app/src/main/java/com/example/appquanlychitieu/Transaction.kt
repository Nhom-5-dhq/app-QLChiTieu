package com.example.appquanlychitieu

data class Transaction(
    val id: String = "",
    val amount: Double = 0.0,
    val type: String = "EXPENSE", // "EXPENSE" (Chi tiêu) hoặc "INCOME" (Thu nhập)
    val category: String = "Khác",
    val date: Long = System.currentTimeMillis(),
    val note: String = ""
)