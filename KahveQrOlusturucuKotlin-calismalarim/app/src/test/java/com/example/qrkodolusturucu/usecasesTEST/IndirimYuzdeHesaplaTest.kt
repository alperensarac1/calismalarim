package com.example.qrkodolusturucu.usecasesTEST

import com.example.qrkodolusturucu.usecases.indirimYuzdeHesapla
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class IndirimYuzdeHesaplaTest {

    @Test
    fun indirimYuzdeHesaplaTest(){
        var indirimYuzdesi:String = indirimYuzdeHesapla(100.0,90.0)
        Assert.assertEquals(indirimYuzdesi,"10,0")
    }

}