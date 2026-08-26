package com.example.appquanlychitieu

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val btnStart = findViewById<Button>(R.id.BtnStart)
        val btnLogin = findViewById<Button>(R.id.BtnLogin)

        // Bấm "Bắt đầu ngay" -> Chuyển sang màn hình Đăng ký
        btnStart?.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        // Bấm "Tôi đã có tài khoản" -> Chuyển sang màn hình Đăng nhập
        btnLogin?.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }
    }
}