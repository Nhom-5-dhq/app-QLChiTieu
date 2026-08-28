package com.example.appquanlychitieu

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    // Launcher nhận kết quả từ màn hình chọn tài khoản Google
    private val googleLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)!!
            firebaseAuthWithGoogle(account.idToken!!)
        } catch (e: Exception) {
            Toast.makeText(this, "Đăng nhập Google thất bại: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()

        // Khai báo đúng các view theo XML
        val edtEmail = findViewById<EditText>(R.id.EdtEmail)
        val edtPassword = findViewById<EditText>(R.id.EdtPassword)
        val btnLogin = findViewById<Button>(R.id.BtnLogin)
        val btnGoogle = findViewById<Button>(R.id.btnGoogle) // Thẻ <Button> trong XML
        val tvForgotPassword = findViewById<TextView>(R.id.tvForgotPassword)
        val tvRegister = findViewById<TextView>(R.id.tvRegister)

        // 1. Đăng nhập Email / Mật khẩu
        btnLogin?.setOnClickListener {
            val email = edtEmail?.text.toString().trim()
            val password = edtPassword?.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập đầy đủ email và mật khẩu", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show()
                        navigateToHome()
                    } else {
                        Toast.makeText(this, "Đăng nhập thất bại: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                    }
                }
        }

        // 2. Đăng nhập Google
        btnGoogle?.setOnClickListener {
            try {
                // Lấy Web Client ID từ file google-services.json
                val defaultWebClientIdResId = resources.getIdentifier("default_web_client_id", "string", packageName)

                if (defaultWebClientIdResId == 0) {
                    Toast.makeText(this, "Lỗi: Chưa tìm thấy cấu hình Google trong google-services.json!", Toast.LENGTH_LONG).show()
                    return@setOnClickListener
                }

                val webClientId = getString(defaultWebClientIdResId)

                val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(webClientId)
                    .requestEmail()
                    .build()

                val googleSignInClient = GoogleSignIn.getClient(this, gso)

                // Đăng xuất phiên cũ trước để ép hiện bảng chọn tài khoản Google
                googleSignInClient.signOut().addOnCompleteListener {
                    val signInIntent = googleSignInClient.signInIntent
                    googleLauncher.launch(signInIntent)
                }
            } catch (e: Exception) {
                Toast.makeText(this, "Lỗi khởi tạo Google Sign-In: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }

        // 3. Quên mật khẩu
        tvForgotPassword?.setOnClickListener {
            startActivity(Intent(this, ForgotPasswordActivity::class.java))
        }

        // 4. Chuyển sang trang Đăng ký
        tvRegister?.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Đăng nhập Google thành công!", Toast.LENGTH_SHORT).show()
                    navigateToHome()
                } else {
                    Toast.makeText(this, "Lỗi xác thực Firebase: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun navigateToHome() {
        val intent = Intent(this, HomeActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}