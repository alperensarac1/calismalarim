export type KonusulanKisi = {
    id: number;
    ad: string;
    numara: string;
    son_mesaj: string;
    tarih: string;
};
export type KonusulanKisiListResponse = {
    success: boolean;
    kisiler: KonusulanKisi[];
};
