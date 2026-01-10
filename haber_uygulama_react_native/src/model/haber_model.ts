export type HaberModel = {
    id: number;
    baslik: string;
    icerik: string;
    media_type: string;   // "video" | "image" vs
    media_url: string;
    yayinlanma_tarihi: string;
    sondakika: number;
    yazar_id: number;
    tur_id: number;
    ad: string;
    soyad: string;
    unvan: string;
    tur_adi: string;
};
