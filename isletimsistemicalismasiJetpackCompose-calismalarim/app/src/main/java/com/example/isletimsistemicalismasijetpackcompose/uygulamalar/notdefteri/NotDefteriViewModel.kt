package com.example.isletimsistemicalismasijetpackcompose.uygulamalar.notdefteri

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

class NotDefteriViewModel(context: Context) {

    private val db = NoteDatabase(context)
    private val _notlar = MutableLiveData<List<Not>>()
    val notlar: LiveData<List<Not>> get() = _notlar

    init {
        getNotlarim()
    }

    private fun getNotlarim() {
        _notlar.value = db.getNotlarim()
    }

    fun notAra(query: String) {
        _notlar.value = if (query.isEmpty()) {
            db.getNotlarim()
        } else {
            db.notAra(query)
        }
    }

    fun getNotById(id: Int): Not? {
        return db.getNotById(id)
    }

    fun yeniNot(not: Not) {
        if (not.id == 0) {
            db.yeniNot(not)
        } else {
            db.notGuncelle(not.id, not)
        }
        getNotlarim()
    }

    fun notSil(notId: Int) {
        db.notSil(Not(id = notId, "", "", "", "", "", ""))
        getNotlarim()
    }
}

