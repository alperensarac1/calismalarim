package com.example.qrkodolusturucu.servicesTest

import com.example.qrkodolusturucu.model.Urun
import com.example.qrkodolusturucu.model.UrunCevap
import com.example.qrkodolusturucu.services.retrofitservice.KahveHTTPServisDao
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import kotlin.coroutines.coroutineContext

class KahveHTTPServiceTest {

    lateinit var kahveHTTPServisDao: KahveHTTPServisDao
    lateinit var urunCevap: UrunCevap
    @Before
    fun setup(){
        kahveHTTPServisDao = KahveHTTPServisDao()

    }

    @Test
    fun tumKahveleriGetirTest(){

            runBlocking {
              urunCevap = kahveHTTPServisDao.tumKahveler()
            }
        Assert.assertNotNull(urunCevap.kahve_urun)

    }

}