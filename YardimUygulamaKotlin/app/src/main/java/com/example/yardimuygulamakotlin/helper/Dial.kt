package com.example.yardimuygulamakotlin.helper

import android.content.Intent
import android.net.Uri

fun dial(phone: String): Intent {
    return Intent(Intent.ACTION_DIAL).apply {
        data = Uri.parse("tel:$phone")
    }
}
