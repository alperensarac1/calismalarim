package com.example.kargopaylasimkotlin.util

object PhoneUtil {
    fun normalizeTrToE164(input: String): String {
        val raw = input.trim().replace(" ", "").replace("-", "")
        if (raw.isBlank()) return raw

        return when {
            raw.startsWith("+90") -> "+90" + raw.removePrefix("+90").filter { it.isDigit() }
            raw.startsWith("90") && raw.length >= 12 -> "+90" + raw.removePrefix("90").filter { it.isDigit() }
            raw.startsWith("0") -> "+90" + raw.drop(1).filter { it.isDigit() }
            raw.startsWith("5") -> "+90" + raw.filter { it.isDigit() }
            else -> raw // fallback
        }
    }

    fun isLikelyTrPhoneE164(phone: String): Boolean {
        // +90 + 10 hane => 13 char (örn +905xxxxxxxxx)
        if (!phone.startsWith("+90")) return false
        val digits = phone.drop(1).filter { it.isDigit() } // "90xxxxxxxxxx"
        return digits.length == 12
    }
}