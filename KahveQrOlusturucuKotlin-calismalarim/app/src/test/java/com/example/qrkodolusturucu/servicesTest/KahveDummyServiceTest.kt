package com.example.qrkodolusturucu.servicesTest

import com.example.qrkodolusturucu.model.UrunCevap
import com.example.qrkodolusturucu.services.dummyservice.KahveDummyServisDao
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class KahveDummyServiceTest {

    lateinit var kahveDummyServisDao: KahveDummyServisDao
    lateinit var urunCevap: UrunCevap
    @Before
    fun setup(){
        kahveDummyServisDao = KahveDummyServisDao()
    }
    @Test
    fun tumKahvelerGetirTest(){
        runBlocking {
            urunCevap = kahveDummyServisDao.tumKahveler()
        }
        Assert.assertNotNull(urunCevap.kahve_urun)
    }


}