package com.example.appquanlychitieu

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import java.util.concurrent.TimeUnit

class PhoneLoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private var verificationId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_phone_login)

        auth = FirebaseAuth.getInstance()

        val tvBack = findViewById<TextView>(R.id.tvBack)
        val edtPhone = findViewById<EditText>(R.id.edtPhone)
        val btnSendOTP = findViewById<Button>(R.id.btnSendOTP)
        val edtOTP = findViewById<EditText>(R.id.edtOTP)
        val btnVerifyOTP = findViewById<Button>(R.id.btnVerifyOTP)
        val layoutPhoneInput = findViewById<LinearLayout>(R.id.layoutPhoneInput)
        val layoutOTPInput = findViewById<LinearLayout>(R.id.layoutOTPInput)

        tvBack.setOnClickListener { finish() }

        // Bấm nút Gửi OTP
        btnSendOTP.setOnClickListener {
            val phone = edtPhone.text.toString().trim()
            if (phone.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập số điện thoại", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val options = PhoneAuthOptions.newBuilder(auth)
                .setPhoneNumber(phone)
                .setTimeout(60L, TimeUnit.SECONDS)
                .setActivity(this)
                .setCallbacks(object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                    override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                        signInWithPhoneAuthCredential(credential)
                    }

                    override fun onVerificationFailed(e: FirebaseException) {
                        Toast.makeText(this@PhoneLoginActivity, "Lỗi: ${e.message}", Toast.LENGTH_LONG).show()
                    }

                    override fun onCodeSent(
                        vId: String,
                        token: PhoneAuthProvider.ForceResendingToken
                    ) {
                        verificationId = vId
                        Toast.makeText(this@PhoneLoginActivity, "Đã gửi mã OTP!", Toast.LENGTH_SHORT).show()
                        layoutPhoneInput.visibility = View.GONE
                        layoutOTPInput.visibility = View.VISIBLE
                    }
                })
                .build()

            PhoneAuthProvider.verifyPhoneNumber(options)
        }

        // Bấm nút Xác thực OTP
        btnVerifyOTP.setOnClickListener {
            val code = edtOTP.text.toString().trim()
            if (code.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập mã OTP", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (verificationId != null) {
                val credential = PhoneAuthProvider.getCredential(verificationId!!, code)
                signInWithPhoneAuthCredential(credential)
            }
        }
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Đăng nhập SĐT thành công!", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                } else {
                    Toast.makeText(this, "Mã OTP không chính xác", Toast.LENGTH_SHORT).show()
                }
            }
    }
}