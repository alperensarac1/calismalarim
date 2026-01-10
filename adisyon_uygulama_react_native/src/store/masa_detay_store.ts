import {StyleSheet} from "react-native";
import create = StyleSheet.create;
import {Kategori} from "../model/kategori";
import {Urun} from "../model/urun";
import {MasaUrun} from "../model/masa_urun";
import {Masa} from "../model/masa";
import {AdisyonAPI} from "../api/adisyon_api";

type MasaDetayState = {
    masaId?: number;

    loading: boolean;
    masa: Masa | null;

    urunler: MasaUrun[];
    tumUrunler: Urun[];
    kategoriler: Kategori[];
    toplamFiyat: number;

    init: (masaId: number) => Promise<void>;
    yukleTumVeriler: () => Promise<void>;

    urunEkle: (urunId: number, adet?: number) => Promise<void>;
    urunCikar: (urunId: number) => Promise<void>;
    odemeAl: () => Promise<void>;
};

// @ts-ignore
export const useMasaDetayStore = create<MasaDetayState>((set, get) => ({
    masaId: undefined,

    loading: false,
    masa: null,

    urunler: [],
    tumUrunler: [],
    kategoriler: [],
    toplamFiyat: 0,

    init: async (masaId: any) => {
        set({ masaId });
        await get().yukleTumVeriler();
    },

    yukleTumVeriler: async () => {
        const masaId = get().masaId;
        if (!masaId) return;

        set({ loading: true });
        try {
            const [masa, masaUrunleri, urunler, kategoriler] = await Promise.all([
                AdisyonAPI.masaGetir(masaId),
                AdisyonAPI.getMasaUrunleri(masaId),
                AdisyonAPI.getUrunler(),
                AdisyonAPI.getKategoriler(),
            ]);

            // ürün adetlerini masadaki adetlere göre eşitle
            const updatedUrunler = urunler.map((u) => {
                const mu = masaUrunleri.find((x) => x.urun_id === u.id);
                return { ...u, urun_adet: mu ? mu.adet : 0 };
            });

            const toplam = masaUrunleri.reduce((acc, x) => acc + (x.toplam_fiyat ?? 0), 0);

            set({
                masa,
                urunler: masaUrunleri,
                tumUrunler: updatedUrunler,
                kategoriler,
                toplamFiyat: toplam,
                loading: false,
            });
        } catch (e) {
            set({ loading: false });
            console.log("MasaDetayStore yukleme hata:", e);
        }
    },

    urunEkle: async (urunId: number, adet: number = 1) => {
        const masaId = get().masaId;
        if (!masaId) return;

        await AdisyonAPI.urunEkleMasaya(masaId, urunId, adet);
        await get().yukleTumVeriler();
    },

    urunCikar: async (urunId: number) => {
        const masaId = get().masaId;
        if (!masaId) return;

        await AdisyonAPI.urunCikar(masaId, urunId);
        await get().yukleTumVeriler();
    },

    odemeAl: async () => {
        const masaId = get().masaId;
        if (!masaId) return;
        await AdisyonAPI.masaOde(masaId);
        await get().yukleTumVeriler();
    },
}));
