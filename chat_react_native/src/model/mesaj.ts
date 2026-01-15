export type Mesaj = {
    id: number;
    gonderen_id: number;
    alici_id: number;
    mesaj_text?: string | null;
    resim_var: number;
    resim_url?: string | null;
    tarih: string;
};
export type MesajListResponse = {
    success: boolean;
    mesajlar: Mesaj[];
};