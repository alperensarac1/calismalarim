import {api} from "./api_client";
import {Masa} from "../model/masa";
import {MasaUrun} from "../model/masa_urun";
import {Urun} from "../model/urun";
import {Kategori} from "../model/kategori";
import {KategoriSilResponse, UrunSilResponse} from "../model/response";


export const AdisyonAPI = {
    getMasalar: async () => (await api.get<Masa[]>("masa_listesi.php")).data,

    getMasaUrunleri: async (masaId: number) =>
        (await api.get<MasaUrun[]>("masa_urunleri.php", { params: { masa_id: masaId } })).data,

    masaSil: async (masaId: number) =>
        (await api.post("masa_sil.php", new URLSearchParams({ masa_id: String(masaId) }))).data,

    masaEkle: async () => (await api.post("masa_ekle.php")).data,

    masaBirlestir: async (anaId: number, bId: number) =>
        (await api.post("masa_birlestir.php", new URLSearchParams({
            ana_masa_id: String(anaId),
            birlestirilecek_masa_id: String(bId),
        }))).data,

    masaOde: async (masaId: number) =>
        (await api.post("masa_odeme.php", new URLSearchParams({ masa_id: String(masaId) }))).data,

    getToplamFiyat: async (masaId: number) =>
        (await api.get<Record<string, number>>("masa_toplam_fiyat.php", { params: { masa_id: masaId } })).data,

    urunCikar: async (masaId: number, urunId: number) =>
        (await api.post("urun_cikar.php", new URLSearchParams({
            masa_id: String(masaId),
            urun_id: String(urunId),
        }))).data,

    urunEkleMasaya: async (masaId: number, urunId: number, adet: number) =>
        (await api.get("masa_urun_ekle.php", { params: { masa_id: masaId, urun_id: urunId, adet } })).data,

    getUrunler: async () => (await api.get<Urun[]>("urunleri_getir.php")).data,

    getKategoriler: async () => (await api.get<Kategori[]>("kategorileri_getir.php")).data,

    masaGetir: async (masaId: number) =>
        (await api.get<Masa>("masa_getir.php", { params: { masa_id: masaId } })).data,

    kategoriEkle: async (ad: string) =>
        (await api.post("kategori_ekle.php", new URLSearchParams({ kategori_ad: ad }))).data,

    kategoriSil: async (id: number) =>
        (await api.post<KategoriSilResponse>("kategori_sil.php", new URLSearchParams({ kategori_id: String(id) }))).data,

    urunSil: async (urunAd: string) =>
        (await api.post<UrunSilResponse>("urun_sil.php", new URLSearchParams({ urun_ad: urunAd }))).data,

    urunEkleYeni: async (p: { urun_ad: string; urun_fiyat: number; urun_kategori: number; urun_adet: number; urun_resim: string }) =>
        (await api.post("urun_ekle.php", new URLSearchParams({
            urun_ad: p.urun_ad,
            urun_fiyat: String(p.urun_fiyat),
            urun_kategori: String(p.urun_kategori),
            urun_adet: String(p.urun_adet),
            urun_resim: p.urun_resim,
        }))).data,
};
