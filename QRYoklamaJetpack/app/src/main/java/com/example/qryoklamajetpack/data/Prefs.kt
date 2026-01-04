package com.example.qryoklamajetpack.data

import android.content.Context

class Prefs(context: Context) {

    private val sp = context.getSharedPreferences("app", Context.MODE_PRIVATE)

    fun setStudentNo(no: String) =
        sp.edit().putString("student_no", no).apply()

    fun getStudentNo(): String? =
        sp.getString("student_no", null)
}
