import {HaberModel} from "../model/haber_model";
import {api} from "./api_client";
import {HaberTuruModel} from "../model/haber_turu_model";
import {YorumInsertRequest, YorumModel} from "../model/yorum_model";
import {ApiResponse} from "../model/api_response";


function unwrapList<T>(data: any): T[] {
    // API bazen direkt array, bazen {data: []} döndürebilir
    if (Array.isArray(data)) return data as T[];
    if (data && Array.isArray(data.data)) return data.data as T[];
    return [];
}

// ✅ Son Dakika
export async function getSonDakika(): Promise<HaberModel[]> {
    const { data } = await api.get("haber_haberler-sondakika-get.php");
    return unwrapList<HaberModel>(data);
}

// ✅ Gündem
export async function getGundem(): Promise<HaberModel[]> {
    const { data } = await api.get("haber_haberler-gundem-get.php");
    return unwrapList<HaberModel>(data);
}

// ✅ Son 3
export async function getSon3Haber(): Promise<HaberModel[]> {
    const { data } = await api.get("haber_haberler-son3-get.php");
    return unwrapList<HaberModel>(data);
}

// ✅ Tüm Haberler (Kategori ekranında filtrelemek için)
export async function getHaberler(): Promise<HaberModel[]> {
    const { data } = await api.get("haber_haberler-get.php");
    return unwrapList<HaberModel>(data);
}

// ✅ Kategoriler
export async function getKategoriler(): Promise<HaberTuruModel[]> {
    const { data } = await api.get("haber_haberturleri-get.php");
    return unwrapList<HaberTuruModel>(data);
}

// ✅ Yorumlar
export async function getYorumlar(haberId: number): Promise<YorumModel[]> {
    // burada param adı backend'e göre haber_id veya haberId olabilir
    const { data } = await api.get("haber_yorumlar-get.php", {
        params: { haber_id: haberId },
    });
    return unwrapList<YorumModel>(data);
}

/**
 * ⚠️ Yorum ekleme endpoint'ini listede göremedim.
 * Eğer sende "haber_yorumlar-insert.php" gibi bir dosya varsa buraya yaz.
 * Yoksa yorum ekleme çalışmaz.
 */
export async function insertYorum(req: YorumInsertRequest): Promise<ApiResponse> {
    const { data } = await api.post("haber_yorumlar-insert.php", req);
    return data as ApiResponse;
}
