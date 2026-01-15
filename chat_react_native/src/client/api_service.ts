import {SimpleResponse} from "../model/simple_response";
import {MesajListResponse} from "../model/mesaj";
import {KonusulanKisiListResponse} from "../model/konusulan_kisi";
import {KullaniciListResponse} from "../model/kullanici";
import {client, toFormUrlEncoded} from "./api_client";


export const apiService = {
    async kullaniciKayit(ad: string, numara: string) {
        const res = await client.post<SimpleResponse>(
            "kullanici-kayit.php",
            toFormUrlEncoded({ ad, numara })
        );
        return res.data;
    },

    async mesajGonder(args: {
        gonderenId: number;
        aliciId: number;
        mesajText: string;
        resimVar: number;
        base64Img?: string | null;
    }) {
        const res = await client.post<SimpleResponse>(
            "mesaj-gonder.php",
            toFormUrlEncoded({
                gonderen_id: args.gonderenId,
                alici_id: args.aliciId,
                mesaj_text: args.mesajText,
                resim_var: args.resimVar,
                base64_img: args.base64Img ?? null,
            })
        );
        return res.data;
    },

    async mesajlariGetir(gonderenId: number, aliciId: number) {
        const res = await client.get<MesajListResponse>("mesajlari-getir.php", {
            params: { gonderen_id: gonderenId, alici_id: aliciId },
        });
        return res.data;
    },

    async konusulanKisiler(kullaniciId: number) {
        const res = await client.get<KonusulanKisiListResponse>(
            "konusulan-kullanicilar.php",
            { params: { kullanici_id: kullaniciId } }
        );
        return res.data;
    },

    async kullanicilariGetir() {
        const res = await client.get<KullaniciListResponse>("kullanicilari-getir.php");
        return res.data;
    },

    async testConnection() {
        const res = await client.get<SimpleResponse>("test-connection.php");
        return res.data;
    },
};
