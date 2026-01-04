package com.example.qryoklamajava.data;

// Prefs.java
import android.content.Context;
import android.content.SharedPreferences;

public class Prefs {
    private final SharedPreferences sp;
    public Prefs(Context ctx){ sp = ctx.getSharedPreferences("app", Context.MODE_PRIVATE); }
    public void setStudentNo(String no){ sp.edit().putString("student_no", no).apply(); }
    public String getStudentNo(){ return sp.getString("student_no", null); }
}

