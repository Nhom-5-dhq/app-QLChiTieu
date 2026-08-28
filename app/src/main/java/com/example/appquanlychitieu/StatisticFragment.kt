package com.example.appquanlychitieu

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.PercentFormatter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Calendar
import java.util.Locale

class StatisticFragment : Fragment() {

    private lateinit var tvTimePeriod: TextView
    private lateinit var btnPrevMonth: ImageView
    private lateinit var btnNextMonth: ImageView

    private lateinit var boxExpense: LinearLayout
    private lateinit var boxIncome: LinearLayout
    private lateinit var lblExpense: TextView
    private lateinit var lblIncome: TextView
    private lateinit var tvExpenseAmount: TextView
    private lateinit var tvIncomeAmount: TextView

    private lateinit var btnTabPhanBo: Button
    private lateinit var btnTabXuHuong: Button
    private lateinit var layoutPhanBo: LinearLayout
    private lateinit var layoutXuHuong: LinearLayout

    private lateinit var pieChart: PieChart
    private lateinit var barChart: BarChart
    private lateinit var containerCategoryList: LinearLayout

    private var calendar = Calendar.getInstance()
    private var isExpenseSelected = true
    private var isPhanBoTabSelected = true

    private var totalExpense = 0.0
    private var totalIncome = 0.0

    private val expenseCategoryMap = mutableMapOf<String, Double>()
    private val incomeCategoryMap = mutableMapOf<String, Double>()

    private val expenseMonthlyMap = mutableMapOf<String, Double>()
    private val incomeMonthlyMap = mutableMapOf<String, Double>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_statistic, container, false)

        tvTimePeriod = view.findViewById(R.id.tvTimePeriod)
        btnPrevMonth = view.findViewById(R.id.btnPrevMonth)
        btnNextMonth = view.findViewById(R.id.btnNextMonth)

        boxExpense = view.findViewById(R.id.boxExpense)
        boxIncome = view.findViewById(R.id.boxIncome)
        lblExpense = view.findViewById(R.id.lblExpense)
        lblIncome = view.findViewById(R.id.lblIncome)
        tvExpenseAmount = view.findViewById(R.id.tvExpenseAmount)
        tvIncomeAmount = view.findViewById(R.id.tvIncomeAmount)

        btnTabPhanBo = view.findViewById(R.id.btnTabPhanBo)
        btnTabXuHuong = view.findViewById(R.id.btnTabXuHuong)
        layoutPhanBo = view.findViewById(R.id.layoutPhanBo)
        layoutXuHuong = view.findViewById(R.id.layoutXuHuong)

        pieChart = view.findViewById(R.id.pieChart)
        barChart = view.findViewById(R.id.barChart)
        containerCategoryList = view.findViewById(R.id.containerCategoryList)

        // 1. Chuyển đổi tháng
        updateMonthText()
        btnPrevMonth.setOnClickListener {
            calendar.add(Calendar.MONTH, -1)
            updateMonthText()
            loadStatisticsData()
        }

        btnNextMonth.setOnClickListener {
            calendar.add(Calendar.MONTH, 1)
            updateMonthText()
            loadStatisticsData()
        }

        // 2. Chuyển đổi Chi tiêu ↔ Thu nhập
        boxExpense.setOnClickListener {
            isExpenseSelected = true
            updateTypeUI()
            setupPieChart()
            setupBarChart()
            renderCategoryList()
        }

        boxIncome.setOnClickListener {
            isExpenseSelected = false
            updateTypeUI()
            setupPieChart()
            setupBarChart()
            renderCategoryList()
        }

        // 3. Chuyển đổi Tab Phân bổ ↔ Xu hướng
        btnTabPhanBo.setOnClickListener {
            isPhanBoTabSelected = true
            updateTabUI()
        }

        btnTabXuHuong.setOnClickListener {
            isPhanBoTabSelected = false
            updateTabUI()
        }

        loadStatisticsData()
        return view
    }

    private fun updateMonthText() {
        val month = calendar.get(Calendar.MONTH) + 1
        val year = calendar.get(Calendar.YEAR)
        tvTimePeriod.text = String.format(Locale.getDefault(), "Tháng %02d/%d", month, year)
    }

    private fun updateTypeUI() {
        if (isExpenseSelected) {
            boxExpense.setBackgroundResource(R.drawable.bg_card_outline_pink)
            lblExpense.setTextColor(Color.parseColor("#F43F5E"))
            boxIncome.setBackgroundResource(R.drawable.bg_card_light_gray)
            lblIncome.setTextColor(Color.parseColor("#64748B"))
        } else {
            boxIncome.setBackgroundResource(R.drawable.bg_card_outline_blue)
            lblIncome.setTextColor(Color.parseColor("#2563EB"))
            boxExpense.setBackgroundResource(R.drawable.bg_card_light_gray)
            lblExpense.setTextColor(Color.parseColor("#64748B"))
        }
    }

    private fun updateTabUI() {
        if (isPhanBoTabSelected) {
            btnTabPhanBo.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#F43F5E")))
            btnTabPhanBo.setTextColor(Color.WHITE)
            btnTabXuHuong.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.TRANSPARENT))
            btnTabXuHuong.setTextColor(Color.parseColor("#64748B"))

            layoutPhanBo.visibility = View.VISIBLE
            layoutXuHuong.visibility = View.GONE
        } else {
            btnTabXuHuong.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#F43F5E")))
            btnTabXuHuong.setTextColor(Color.WHITE)
            btnTabPhanBo.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.TRANSPARENT))
            btnTabPhanBo.setTextColor(Color.parseColor("#64748B"))

            layoutPhanBo.visibility = View.GONE
            layoutXuHuong.visibility = View.VISIBLE
            setupBarChart()
        }
    }

    private fun loadStatisticsData() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val db = FirebaseFirestore.getInstance()

        db.collection("users").document(userId).collection("transactions")
            .get()
            .addOnSuccessListener { querySnapshot ->
                totalExpense = 0.0
                totalIncome = 0.0
                expenseCategoryMap.clear()
                incomeCategoryMap.clear()
                expenseMonthlyMap.clear()
                incomeMonthlyMap.clear()

                val targetMonth = calendar.get(Calendar.MONTH)
                val targetYear = calendar.get(Calendar.YEAR)

                for (doc in querySnapshot) {
                    val dateMillis = doc.getLong("date") ?: continue
                    val docCal = Calendar.getInstance().apply { timeInMillis = dateMillis }
                    val docMonth = docCal.get(Calendar.MONTH)
                    val docYear = docCal.get(Calendar.YEAR)

                    val amount = doc.getDouble("amount") ?: 0.0
                    val type = doc.getString("type") ?: ""
                    val category = doc.getString("category") ?: "Khác"

                    val monthKey = String.format(Locale.getDefault(), "%02d/%d", docMonth + 1, docYear)

                    if (type == "EXPENSE" || type == "CHI") {
                        expenseMonthlyMap[monthKey] = (expenseMonthlyMap[monthKey] ?: 0.0) + amount
                    } else if (type == "INCOME" || type == "THU") {
                        incomeMonthlyMap[monthKey] = (incomeMonthlyMap[monthKey] ?: 0.0) + amount
                    }

                    if (docMonth == targetMonth && docYear == targetYear) {
                        if (type == "EXPENSE" || type == "CHI") {
                            totalExpense += amount
                            expenseCategoryMap[category] = (expenseCategoryMap[category] ?: 0.0) + amount
                        } else if (type == "INCOME" || type == "THU") {
                            totalIncome += amount
                            incomeCategoryMap[category] = (incomeCategoryMap[category] ?: 0.0) + amount
                        }
                    }
                }

                tvExpenseAmount.text = String.format("%,.0f đ", totalExpense)
                tvIncomeAmount.text = String.format("%,.0f đ", totalIncome)

                updateTypeUI()
                setupPieChart()
                setupBarChart()
                renderCategoryList()
            }
    }

    // 1. Dựng Biểu đồ Tròn (Donut Chart với chỉ dẫn tỉ lệ % bên ngoài)
    private fun setupPieChart() {
        val currentMap = if (isExpenseSelected) expenseCategoryMap else incomeCategoryMap
        val totalAmount = if (isExpenseSelected) totalExpense else totalIncome

        if (currentMap.isEmpty() || totalAmount == 0.0) {
            pieChart.clear()
            return
        }

        val entries = ArrayList<PieEntry>()
        for ((category, amount) in currentMap) {
            entries.add(PieEntry(amount.toFloat(), category))
        }

        val dataSet = PieDataSet(entries, "")
        dataSet.colors = listOf(
            Color.parseColor("#F43F5E"),
            Color.parseColor("#F59E0B"),
            Color.parseColor("#10B981"),
            Color.parseColor("#3B82F6"),
            Color.parseColor("#8B5CF6")
        )

        // Cấu hình nhãn nằm bên ngoài biểu đồ tròn
        dataSet.xValuePosition = PieDataSet.ValuePosition.OUTSIDE_SLICE
        dataSet.yValuePosition = PieDataSet.ValuePosition.OUTSIDE_SLICE
        dataSet.valueLinePart1OffsetPercentage = 80f
        dataSet.valueLinePart1Length = 0.5f
        dataSet.valueLinePart2Length = 0.4f
        dataSet.valueLineColor = Color.parseColor("#CBD5E1")

        val data = PieData(dataSet)
        data.setValueFormatter(PercentFormatter(pieChart))
        data.setValueTextSize(12f)
        data.setValueTextColor(Color.parseColor("#0F172A"))

        pieChart.apply {
            this.data = data
            setUsePercentValues(true)
            description.isEnabled = false
            isDrawHoleEnabled = true
            setHoleColor(Color.TRANSPARENT)
            holeRadius = 60f
            transparentCircleRadius = 65f
            legend.isEnabled = false
            animateY(800)
            invalidate()
        }
    }

    // 2. Dựng Biểu đồ Cột (Bar Chart 3 tháng gần nhất với Trục Y)
    private fun setupBarChart() {
        val monthlyMap = if (isExpenseSelected) expenseMonthlyMap else incomeMonthlyMap
        val color = if (isExpenseSelected) Color.parseColor("#3B82F6") else Color.parseColor("#10B981")

        // Lấy 3 tháng gần nhất
        val months = mutableListOf<String>()
        val tempCal = calendar.clone() as Calendar
        tempCal.add(Calendar.MONTH, -2)

        for (i in 0..2) {
            val m = tempCal.get(Calendar.MONTH) + 1
            val y = tempCal.get(Calendar.YEAR)
            months.add(String.format(Locale.getDefault(), "%02d/%d", m, y))
            tempCal.add(Calendar.MONTH, 1)
        }

        val entries = ArrayList<BarEntry>()
        val labels = ArrayList<String>()

        for ((index, monthKey) in months.withIndex()) {
            val amount = (monthlyMap[monthKey] ?: 0.0) / 1000000.0 // Đơn vị Triệu
            entries.add(BarEntry(index.toFloat(), amount.toFloat()))

            if (index == 2) {
                labels.add("Tháng này")
            } else {
                labels.add("T${monthKey.substring(0, 2)}")
            }
        }

        val dataSet = BarDataSet(entries, "")
        dataSet.color = color
        dataSet.valueTextSize = 10f

        val data = BarData(dataSet)
        data.barWidth = 0.45f

        barChart.apply {
            this.data = data
            description.isEnabled = false
            legend.isEnabled = false
            setDrawGridBackground(false)

            // Cấu hình Trục X
            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                valueFormatter = IndexAxisValueFormatter(labels)
                setDrawGridLines(false)
                textColor = Color.parseColor("#64748B")
            }

            // Cấu hình Trục Y bên trái
            axisLeft.apply {
                setDrawGridLines(true)
                gridColor = Color.parseColor("#E2E8F0")
                textColor = Color.parseColor("#64748B")
                axisMinimum = 0f
            }

            axisRight.isEnabled = false
            animateY(800)
            invalidate()
        }
    }

    // 3. Hiển thị danh sách chi tiết bên dưới
    private fun renderCategoryList() {
        containerCategoryList.removeAllViews()
        val currentMap = if (isExpenseSelected) expenseCategoryMap else incomeCategoryMap
        val totalAmount = if (isExpenseSelected) totalExpense else totalIncome

        if (currentMap.isEmpty() || totalAmount == 0.0) {
            val emptyTv = TextView(requireContext()).apply {
                text = "Không có dữ liệu giao dịch"
                setTextColor(Color.GRAY)
                setPadding(16, 16, 16, 16)
            }
            containerCategoryList.addView(emptyTv)
            return
        }

        for ((category, amount) in currentMap) {
            val percent = (amount / totalAmount) * 100
            val itemLayout = LinearLayout(requireContext()).apply {
                orientation = LinearLayout.HORIZONTAL
                setPadding(20, 16, 20, 16)
                setBackgroundColor(Color.WHITE)
                val params = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                params.setMargins(0, 0, 0, 12)
                layoutParams = params
            }

            val tvCat = TextView(requireContext()).apply {
                text = category
                textSize = 15f
                setTextColor(Color.BLACK)
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            }

            val tvVal = TextView(requireContext()).apply {
                text = String.format("%,.0f đ (%.1f%%)", amount, percent)
                textSize = 14f
                setTextColor(if (isExpenseSelected) Color.parseColor("#F43F5E") else Color.parseColor("#2563EB"))
            }

            itemLayout.addView(tvCat)
            itemLayout.addView(tvVal)
            containerCategoryList.addView(itemLayout)
        }
    }
}