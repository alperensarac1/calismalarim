package com.example.qrkodolusturucujava.usecasesTest;

import com.example.qrkodolusturucujava.usecases.IndirimHesapla;

import org.junit.Assert;
import org.junit.Test;

public class IndirimYuzdeHesaplaTest {

    @Test
    public void indirimYuzdeHesaplaTest() {
        IndirimHesapla indirimHesapla = new IndirimHesapla();
        String indirimYuzdesi = indirimHesapla.indirimYuzdeHesapla(100.0, 90.0);
        Assert.assertEquals("10,0", indirimYuzdesi);
    }
}

