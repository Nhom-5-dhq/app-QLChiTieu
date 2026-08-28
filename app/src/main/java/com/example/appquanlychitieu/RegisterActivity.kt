package com.example.appquanlychitieu

import android.os.Bundle
import android.util.Patterns
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
class RegisterActivity : AppCompatActivity() {

    // Khai báo biến FirebaseAuth
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        // Khởi tạo Firebase Auth
        auth = FirebaseAuth.getInstance()

        setContentView(R.layout.activity_register)

        val tvBack = findViewById<TextView>(R.id.tvBack)

        val edtName = findViewById<EditText>(R.id.edtName)
        val edtEmail = findViewById<EditText>(R.id.edtEmail)
        val edtPhone = findViewById<EditText>(R.id.edtPhone)
        val edtPassword = findViewById<EditText>(R.id.edtPassword)
        val edtConfirmPassword =
            findViewById<EditText>(R.id.edtConfirmPassword)

        val cbTerms = findViewById<CheckBox>(R.id.cbTerms)

        val btnRegister =
            findViewById<Button>(R.id.btnRegister)

        val tvLogin =
            findViewById<TextView>(R.id.tvLogin)

        // =========================
        // NÚT QUAY LẠI
        // =========================

        tvBack.setOnClickListener {
            finish()
        }

        // =========================
        // NÚT ĐĂNG KÝ
        // =========================

        btnRegister.setOnClickListener {

            val name = edtName.text.toString().trim()
            val email = edtEmail.text.toString().trim()
            val phone = edtPhone.text.toString().trim()
            val password = edtPassword.text.toString()
            val confirmPassword =
                edtConfirmPassword.text.toString()

            // Kiểm tra họ tên
            if (name.isEmpty()) {

                edtName.error = "Vui lòng nhập họ và tên"
                edtName.requestFocus()

                return@setOnClickListener
            }

            // Kiểm tra email
            if (email.isEmpty()) {

                edtEmail.error = "Vui lòng nhập Email"
                edtEmail.requestFocus()

                return@setOnClickListener
            }

            // Kiểm tra Email Gmail
            if (!email.endsWith("@gmail.com", ignoreCase = true)) {

                edtEmail.error =
                    "Email phải có đuôi @gmail.com"

                edtEmail.requestFocus()

                return@setOnClickListener
            }

            // Kiểm tra định dạng Email
            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {

                edtEmail.error =
                    "Email không đúng định dạng"

                edtEmail.requestFocus()

                return@setOnClickListener
            }

            // Kiểm tra số điện thoại
            if (phone.isEmpty()) {

                edtPhone.error =
                    "Vui lòng nhập số điện thoại"

                edtPhone.requestFocus()

                return@setOnClickListener
            }

            // Kiểm tra mật khẩu
            if (password.isEmpty()) {

                edtPassword.error =
                    "Vui lòng nhập mật khẩu"

                edtPassword.requestFocus()

                return@setOnClickListener
            }

            // Kiểm tra xác nhận mật khẩu
            if (confirmPassword.isEmpty()) {

                edtConfirmPassword.error =
                    "Vui lòng xác nhận mật khẩu"

                edtConfirmPassword.requestFocus()

                return@setOnClickListener
            }

            // Hai mật khẩu phải giống nhau
            if (password != confirmPassword) {

                edtConfirmPassword.error =
                    "Mật khẩu xác nhận không khớp"

                edtConfirmPassword.requestFocus()

                return@setOnClickListener
            }

            // Kiểm tra điều khoản
            if (!cbTerms.isChecked) {

                Toast.makeText(
                    this,
                    "Vui lòng đồng ý với Điều khoản & Chính sách",
                    Toast.LENGTH_SHORT
                ).show()

                return@setOnClickListener
            }

            // Tạo tài khoản trên Firebase Authentication
            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Đăng ký thành công trên Firebase
                        Toast.makeText(this, "Đăng ký tài khoản thành công!", Toast.LENGTH_SHORT).show()

                        // Đóng RegisterActivity để quay về màn hình Đăng nhập
                        finish()
                    } else {
                        // Thất bại (ví dụ: Email đã tồn tại, mật khẩu yếu...)
                        Toast.makeText(this, "Đăng ký thất bại: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                    }
                }

        }

        // =========================
        // ĐĂNG NHẬP
        // =========================

        tvLogin.setOnClickListener {

            finish()
        }
    }
}