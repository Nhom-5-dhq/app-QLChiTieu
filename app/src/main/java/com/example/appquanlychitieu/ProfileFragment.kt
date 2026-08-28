package com.example.appquanlychitieu

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth

class ProfileFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        val tvUserName = view.findViewById<TextView>(R.id.tvUserName)
        val tvUserEmail = view.findViewById<TextView>(R.id.tvUserEmail)
        val btnManageCategory = view.findViewById<LinearLayout>(R.id.btnManageCategory)
        val btnExportExcel = view.findViewById<LinearLayout>(R.id.btnExportExcel)
        val btnGeneralSettings = view.findViewById<LinearLayout>(R.id.btnGeneralSettings)
        val btnSecurityPin = view.findViewById<LinearLayout>(R.id.btnSecurityPin)
        val btnLogout = view.findViewById<Button>(R.id.btnLogout)

        // Hiển thị thông tin tài khoản Firebase
        val currentUser = FirebaseAuth.getInstance().currentUser
        val email = currentUser?.email ?: "dhq@gmail.com"
        val name = currentUser?.displayName ?: email.substringBefore("@")

        tvUserName.text = name
        tvUserEmail.text = email

        // Bắt sự kiện click các nút chức năng
        btnManageCategory?.setOnClickListener {
            Toast.makeText(requireContext(), "Chức năng Quản lý danh mục", Toast.LENGTH_SHORT).show()
        }

        btnExportExcel?.setOnClickListener {
            Toast.makeText(requireContext(), "Chức năng Xuất báo cáo Excel", Toast.LENGTH_SHORT).show()
        }

        btnGeneralSettings?.setOnClickListener {
            Toast.makeText(requireContext(), "Chức năng Cài đặt chung", Toast.LENGTH_SHORT).show()
        }

        btnSecurityPin?.setOnClickListener {
            Toast.makeText(requireContext(), "Chức năng Bảo mật & Mã PIN", Toast.LENGTH_SHORT).show()
        }

        // Đăng xuất và quay về màn hình Login
        btnLogout?.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            val intent = Intent(requireContext(), LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }

        return view
    }
}