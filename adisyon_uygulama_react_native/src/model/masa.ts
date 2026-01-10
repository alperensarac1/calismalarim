import {Urun} from "./urun";

export type Masa = {
    id: number;
    masa_adi: string;
    acik_mi: number;
    sure: string;
    toplam_fiyat: number;

    // transient (local)
    urunler?: Urun[];
};