export type Kullanici = {
    id: number;
    ad: string;
    numara: string;
};
export type KullaniciListResponse = {
    success: boolean;
    kullanicilar: Kullanici[];
};
