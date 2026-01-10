export type YorumModel = {
    id: number;
    haber_id: number;
    takma_ad: string;
    yorum_metni: string;
    onayli: number;
    yorum_tarihi: string;
};

export type YorumInsertRequest = {
    haber_id: number;
    takma_ad: string;
    yorum_metni: string;
};
