package com.example.qrkodolusturucu.usecasesTEST

import com.example.qrkodolusturucu.usecases.DogrulamaKoduOlustur
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class DogrulamaKoduOlusturTest {

    lateinit var dogrulamaKoduOlustur:DogrulamaKoduOlustur

    @Before
    fun setup(){
        dogrulamaKoduOlustur = DogrulamaKoduOlustur()
    }

    @Test
    fun dogrulamaKoduOlusturTest(){
        Assert.assertNotEquals(dogrulamaKoduOlustur.randomIdOlustur(),null)
    }

}