package com.example.adisyonuygulamakotlin.utils

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer

enum class SizeType {
    MATCH_PARENT,
    WRAP_CONTENT;

    fun toLayoutParam(): Int {
        return when (this) {
            MATCH_PARENT -> ViewGroup.LayoutParams.MATCH_PARENT
            WRAP_CONTENT -> ViewGroup.LayoutParams.WRAP_CONTENT
        }
    }
}

fun View.sizeBelirle(width: SizeType, height: SizeType) {
    val params = layoutParams ?: LinearLayout.LayoutParams(0, 0)
    params.width = width.toLayoutParam()
    params.height = height.toLayoutParam()
    layoutParams = params
}
fun View.sizeBelirle(width: Int, height: Int) {
    val params = layoutParams ?: LinearLayout.LayoutParams(0, 0)
    params.width = width
    params.height = height
    layoutParams = params
}

// Margin eklemek için
fun View.marginEkle(top: Int = 0, bottom: Int = 0, left: Int = 0, right: Int = 0) {
    val params = (layoutParams as? ViewGroup.MarginLayoutParams)
        ?: ViewGroup.MarginLayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

    params.setMargins(left, top, right, bottom)
    layoutParams = params
}
fun View.paddingEkle(top: Int = 0, bottom: Int = 0, left: Int = 0, right: Int = 0) {
    setPadding(left, top, right, bottom)
}
fun View.paddingEkleAll(all:Int = 0) {
    setPadding(all, all, all, all)
}
fun View.paddingEkleHorizontal(horizontal:Int = 0) {
    setPadding(horizontal, 0, horizontal, 0)
}
fun View.paddingEkleVertical(vertical:Int = 0) {
    setPadding(0, vertical, 0, vertical)
}
fun dpToPx(dp: Int,context:Context): Int {
    val scale = context.resources.displayMetrics.density
    return (dp * scale).toInt()
}

