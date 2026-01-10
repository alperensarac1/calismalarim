import {Kategori} from "./kategori";

export type Urun = {
    id: number;
    urun_ad: string;
    urun_fiyat: number;
    urun_resim: string;
    urun_adet: number;
    urunKategori: Kategori;
};