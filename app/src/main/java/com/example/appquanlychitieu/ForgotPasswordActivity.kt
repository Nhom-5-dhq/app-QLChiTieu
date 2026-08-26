package com.example.appquanlychitieu

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class ForgotPasswordActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_password)

        auth = FirebaseAuth.getInstance()

        val tvBack = findViewById<TextView>(R.id.tvBack)
        val edtResetEmail = findViewById<EditText>(R.id.edtResetEmail)
        val btnSendReset = findViewById<Button>(R.id.btnSendReset)

        // Nút quay lại
        tvBack.setOnClickListener {
            finish()
        }

        // Nút gửi yêu cầu đặt lại mật khẩu
        btnSendReset.setOnClickListener {
            val email = edtResetEmail.text.toString().trim()

            if (email.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập Email", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!email.endsWith("@gmail.com")) {
                Toast.makeText(this, "Email phải có đuôi @gmail.com", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Gọi Firebase gửi Email khôi phục mật khẩu
            auth.sendPasswordResetEmail(email)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this, "Đã gửi liên kết khôi phục tới Email của bạn!", Toast.LENGTH_LONG).show()
                        finish() // Đóng trang sau khi gửi thành công
                    } else {
                        Toast.makeText(this, "Lỗi: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                    }
                }
        }
    }
}