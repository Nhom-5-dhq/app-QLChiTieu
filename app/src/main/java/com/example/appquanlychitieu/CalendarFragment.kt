package com.example.appquanlychitieu

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CalendarView
import android.widget.TextView
import androidx.fragment.app.Fragment

class CalendarFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_calendar, container, false)

        val calendarView = view.findViewById<CalendarView>(R.id.calendarView)
        val tvSelectedDate = view.findViewById<TextView>(R.id.tvSelectedDate)

        calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            val dateStr = "Giao dịch ngày: $dayOfMonth/${month + 1}/$year"
            tvSelectedDate.text = dateStr
        }

        return view
    }
}