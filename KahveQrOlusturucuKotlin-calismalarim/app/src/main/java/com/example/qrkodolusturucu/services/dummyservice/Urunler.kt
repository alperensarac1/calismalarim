package com.example.qrkodolusturucu.services.dummyservice

import com.example.qrkodolusturucu.model.Urun
import com.example.qrkodolusturucu.model.UrunKategori

object Urunler {
    var dummyUrunler = listOf(
        Urun(
            id = 1,
            urunAd = "Mocha",
            urunResim = "https://elizinn.com.tr/wp-content/uploads/2023/08/12-sq-mocha-.jpg",
            urunAciklama = "Çikolata, espresso, süt",
            urunKategoriId = UrunKategori.ICECEKLER.kategoriKodu(),
            urunIndirim = 0,
            urunFiyat = 120.0,
            urunIndirimliFiyat = 120.0
        ),
        Urun(
            id = 2,
            urunAd = "Latte",
            urunResim = "https://www.tchibo.com.tr/dw/image/v2/BDTD_PRD/on/demandware.static/-/Sites-tchibo-master-catalog/default/dw75853654/130226.webp",
            urunAciklama = "Espresso ve süt karışımı",
            urunKategoriId = UrunKategori.ICECEKLER.kategoriKodu(),
            urunIndirim = 1,
            urunFiyat = 100.0,
            urunIndirimliFiyat = 90.0
        ),
        Urun(
            id = 3,
            urunAd = "Türk Kahvesi",
            urunResim = "https://cdn.yemek.com/mnresize/1250/833/uploads/2020/10/turk-kahvesi-yemekcom.jpg",
            urunAciklama = "Geleneksel Türk kahvesi",
            urunKategoriId = UrunKategori.ICECEKLER.kategoriKodu(),
            urunIndirim = 0,
            urunFiyat = 80.0,
            urunIndirimliFiyat = 76.0
        ),
        Urun(
            id = 4,
            urunAd = "Americano",
            urunResim = "https://www.peetshop.com/image/cache/catalog/Americano-1000x1000.jpg",
            urunAciklama = "Espresso ve sıcak su karışımı",
            urunKategoriId = UrunKategori.ICECEKLER.kategoriKodu(),
            urunIndirim = 0,
            urunFiyat = 90.0,
            urunIndirimliFiyat = 90.0
        ),
        Urun(
            id = 5,
            urunAd = "Brownie",
            urunResim = "https://cdn.yemek.com/mnresize/1250/833/uploads/2014/10/brownie-yemekcom.jpg",
            urunAciklama = "Yoğun çikolatalı kek",
            urunKategoriId = UrunKategori.ATISTIRMALIKLAR.kategoriKodu(),
            urunIndirim = 1,
            urunFiyat = 70.0,
            urunIndirimliFiyat = 59.5
        ),
        Urun(
            id = 6,
            urunAd = "Cheesecake",
            urunResim = "https://www.nefisyemektarifleri.com/wp-content/uploads/2020/09/orijinal-san-sebastian-cheesecake.jpg",
            urunAciklama = "Krem peynirli tatlı",
            urunKategoriId = UrunKategori.ATISTIRMALIKLAR.kategoriKodu(),
            urunIndirim = 0,
            urunFiyat = 85.0,
            urunIndirimliFiyat = 68.0
        ),
        Urun(
            id = 7,
            urunAd = "Sufle",
            urunResim = "https://cdn.yemek.com/mnresize/1250/833/uploads/2021/10/akiskan-cikolatali-sufle.jpg",
            urunAciklama = "Akışkan çikolatalı tatlı",
            urunKategoriId = UrunKategori.ATISTIRMALIKLAR.kategoriKodu(),
            urunIndirim = 1,
            urunFiyat = 80.0,
            urunIndirimliFiyat = 72.0
        ),
        Urun(
            id = 8,
            urunAd = "Filtre Kahve",
            urunResim = "https://www.coffeemag.com.tr/wp-content/uploads/2021/07/filtre-kahve.jpg",
            urunAciklama = "Klasik filtre kahve",
            urunKategoriId = UrunKategori.ICECEKLER.kategoriKodu(),
            urunIndirim = 0,
            urunFiyat = 75.0,
            urunIndirimliFiyat = 75.0
        ),
        Urun(
            id = 9,
            urunAd = "Muffin",
            urunResim = "https://cdn.yemek.com/mnresize/1250/833/uploads/2016/09/cikolatali-muffin-yeni-onecikan.jpg",
            urunAciklama = "Yumuşak kek",
            urunKategoriId = UrunKategori.ATISTIRMALIKLAR.kategoriKodu(),
            urunIndirim = 1,
            urunFiyat = 60.0,
            urunIndirimliFiyat = 57.0
        ),
        Urun(
            id = 10,
            urunAd = "Chai Tea Latte",
            urunResim = "https://img.kidspot.com.au/Nvvxe3WJ/w1000-h667-q90-KP/2e79/717c2f6fbead2bb874421d8219ebc4a7d882/image.jpg",
            urunAciklama = "Baharatlı sütlü çay",
            urunKategoriId = UrunKategori.ICECEKLER.kategoriKodu(),
            urunIndirim = 1,
            urunFiyat = 95.0,
            urunIndirimliFiyat = 85.5
        )
    )
}
