package com.example.appquanlychitieu

data class Transaction(
    val id: String = "",         // Mã ID duy nhất của từng giao dịch
    val amount: Double = 0.0,    // Số tiền (Ví dụ: 50000.0)
    val type: String = "",       // Loại giao dịch: "EXPENSE" (Chi tiêu) hoặc "INCOME" (Thu nhập)
    val category: String = "",   // Danh mục (Ví dụ: "Ăn uống", "Mua sắm", "Lương"...)
    val note: String = "",       // Ghi chú đính kèm
    val date: Long = 0L          // Thời gian lưu dưới dạng số Miliseconds
)