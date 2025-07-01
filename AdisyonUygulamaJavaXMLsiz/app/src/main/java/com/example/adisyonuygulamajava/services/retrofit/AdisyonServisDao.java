package com.example.adisyonuygulamajava.services.retrofit;

import com.example.adisyonuygulamajava.model.Kategori;
import com.example.adisyonuygulamajava.model.Masa;
import com.example.adisyonuygulamajava.model.MasaUrun;
import com.example.adisyonuygulamajava.model.Urun;
import com.example.adisyonuygulamajava.services.Services;
import com.example.adisyonuygulamajava.services.response.KategoriSilResponse;
import com.example.adisyonuygulamajava.services.response.UrunSilResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;

public class AdisyonServisDao implements Services {

    private final AdisyonServiceInterface servis = ApiUtils.getAdisyonServisDaoInterface();

    @Override
    public List<Urun> urunleriGetir() {
        try {
            Response<List<Urun>> response = servis.getUrunler().execute();
            return response.body();
        } catch (IOException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    @Override
    public ResponseBody masaSil(int masaId) {
        try {
            Response<ResponseBody> response = servis.masaSil(masaId).execute();
            return response.body();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void urunEkle(String urunAd, float fiyat, int kategoriId, int adet, String base64) {
        new Thread(() -> {
            try {
                servis.urunEkle(urunAd, fiyat, kategoriId, adet, base64).execute();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    @Override
    public ResponseBody masaBirlestir(int anaMasaId, int birlestirilecekMasaId) {
        try {
            Response<ResponseBody> response = servis.masaBirlestir(anaMasaId, birlestirilecekMasaId).execute();
            return response.body();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public ResponseBody masaEkle() {
        try {
            Response<ResponseBody> response = servis.masaEkle().execute();
            return response.body();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public List<Masa> masalariGetir() {
        try {
            Response<List<Masa>> response = servis.getMasalar().execute();
            return response.body();
        } catch (IOException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    @Override
    public List<MasaUrun> masaUrunleriniGetir(int masaId) {
        try {
            Response<List<MasaUrun>> response = servis.getMasaUrunleri(masaId).execute();
            return response.body();
        } catch (IOException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    @Override
    public ResponseBody urunEkle(int masaId, int urunId, int adet) {
        try {
            Response<ResponseBody> response = servis.urunEkle(masaId, urunId, adet).execute();
            return response.body();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public ResponseBody masaOdemeYap(int masaId) {
        try {
            Response<ResponseBody> response = servis.masaOde(masaId).execute();
            return response.body();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public float masaToplamFiyat(int masaId) {
        try {
            Response<Map<String, Float>> response = servis.getToplamFiyat(masaId).execute();
            Map<String, Float> map = response.body();
            if (map != null && map.containsKey("toplam_fiyat")) {
                return map.get("toplam_fiyat");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return 0f;
    }

    @Override
    public ResponseBody urunCikar(int masaId, int urunId) {
        try {
            Response<ResponseBody> response = servis.urunCikar(masaId, urunId).execute();
            return response.body();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public List<Kategori> kategorileriGetir() {
        try {
            Response<List<Kategori>> response = servis.getKategoriler().execute();
            return new ArrayList<>(response.body());
        } catch (IOException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    @Override
    public Masa masaGetir(int masaId) {
        try {
            Response<Masa> response = servis.masaGetir(masaId).execute();
            return response.body();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public ResponseBody kategoriEkle(String ad) {
        try {
            Response<ResponseBody> response = servis.kategoriEkle(ad).execute();
            return response.body();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public KategoriSilResponse kategoriSil(int id) {
        try {
            Response<KategoriSilResponse> response = servis.kategoriSil(id).execute();
            return response.body();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public UrunSilResponse urunSil(String urunAd) {
        try {
            Response<UrunSilResponse> response = servis.urunSil(urunAd).execute();
            return response.body();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}

