import AsyncStorage from "@react-native-async-storage/async-storage";

const KEY_KULLANICI_ID = "kullanici_id";

export const PrefManager = {
    async kaydetKullaniciId(id: number) {
        await AsyncStorage.setItem(KEY_KULLANICI_ID, String(id));
    },
    async getirKullaniciId(): Promise<number> {
        const v = await AsyncStorage.getItem(KEY_KULLANICI_ID);
        return v ? parseInt(v, 10) : -1;
    },
    async temizleKullanici() {
        await AsyncStorage.removeItem(KEY_KULLANICI_ID);
    },
    async kullaniciVarMi(): Promise<boolean> {
        return (await this.getirKullaniciId()) !== -1;
    },
};
