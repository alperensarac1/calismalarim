package com.example.qrkodolusturucujava.services.dummyservice;

import com.example.qrkodolusturucujava.model.Urun;
import com.example.qrkodolusturucujava.model.UrunKategori;

import java.util.Arrays;
import java.util.List;

public class Urunler {

    public static final List<Urun> dummyUrunler = Arrays.asList(
            new Urun(1, "Mocha",
                    "https://elizinn.com.tr/wp-content/uploads/2023/08/12-sq-mocha-.jpg",
                    "Çikolata, espresso, süt",
                    UrunKategori.ICECEKLER.kategoriKodu(),
                    0, 120.0, 120.0),

            new Urun(2, "Latte",
                    "https://www.tchibo.com.tr/dw/image/v2/BDTD_PRD/on/demandware.static/-/Sites-tchibo-master-catalog/default/dw75853654/130226.webp",
                    "Espresso ve süt karışımı",
                    UrunKategori.ICECEKLER.kategoriKodu(),
                    1, 100.0, 90.0),

            new Urun(3, "Türk Kahvesi",
                    "https://cdn.yemek.com/mnresize/1250/833/uploads/2020/10/turk-kahvesi-yemekcom.jpg",
                    "Geleneksel Türk kahvesi",
                    UrunKategori.ICECEKLER.kategoriKodu(),
                    0, 80.0, 76.0),

            new Urun(4, "Americano",
                    "https://www.peetshop.com/image/cache/catalog/Americano-1000x1000.jpg",
                    "Espresso ve sıcak su karışımı",
                    UrunKategori.ICECEKLER.kategoriKodu(),
                    0, 90.0, 90.0),

            new Urun(5, "Brownie",
                    "https://cdn.yemek.com/mnresize/1250/833/uploads/2014/10/brownie-yemekcom.jpg",
                    "Yoğun çikolatalı kek",
                    UrunKategori.ATISTIRMALIKLAR.kategoriKodu(),
                    1, 70.0, 59.5),

            new Urun(6, "Cheesecake",
                    "https://www.nefisyemektarifleri.com/wp-content/uploads/2020/09/orijinal-san-sebastian-cheesecake.jpg",
                    "Krem peynirli tatlı",
                    UrunKategori.ATISTIRMALIKLAR.kategoriKodu(),
                    0, 85.0, 68.0),

            new Urun(7, "Sufle",
                    "https://cdn.yemek.com/mnresize/1250/833/uploads/2021/10/akiskan-cikolatali-sufle.jpg",
                    "Akışkan çikolatalı tatlı",
                    UrunKategori.ATISTIRMALIKLAR.kategoriKodu(),
                    1, 80.0, 72.0),

            new Urun(8, "Filtre Kahve",
                    "https://www.coffeemag.com.tr/wp-content/uploads/2021/07/filtre-kahve.jpg",
                    "Klasik filtre kahve",
                    UrunKategori.ICECEKLER.kategoriKodu(),
                    0, 75.0, 75.0),

            new Urun(9, "Muffin",
                    "https://cdn.yemek.com/mnresize/1250/833/uploads/2016/09/cikolatali-muffin-yeni-onecikan.jpg",
                    "Yumuşak kek",
                    UrunKategori.ATISTIRMALIKLAR.kategoriKodu(),
                    1, 60.0, 57.0),

            new Urun(10, "Chai Tea Latte",
                    "https://img.kidspot.com.au/Nvvxe3WJ/w1000-h667-q90-KP/2e79/717c2f6fbead2bb874421d8219ebc4a7d882/image.jpg",
                    "Baharatlı sütlü çay",
                    UrunKategori.ICECEKLER.kategoriKodu(),
                    1, 95.0, 85.5)
    );
}

