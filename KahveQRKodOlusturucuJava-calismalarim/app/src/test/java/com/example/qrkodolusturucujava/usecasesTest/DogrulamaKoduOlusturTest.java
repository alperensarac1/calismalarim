package com.example.qrkodolusturucujava.usecasesTest;

import com.example.qrkodolusturucujava.usecases.DogrulamaKoduOlustur;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class DogrulamaKoduOlusturTest {

    private DogrulamaKoduOlustur dogrulamaKoduOlustur;

    @Before
    public void setup() {
        dogrulamaKoduOlustur = new DogrulamaKoduOlustur();
    }

    @Test
    public void dogrulamaKoduOlusturTest() {
        String kod = dogrulamaKoduOlustur.randomIdOlustur();
        Assert.assertNotNull(kod);
    }
}

