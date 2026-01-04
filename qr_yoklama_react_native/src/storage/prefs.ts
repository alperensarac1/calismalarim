import AsyncStorage from "@react-native-async-storage/async-storage";

const K_STUDENT_NO = "student_no";
const K_DEVICE_ID = "device_id";

export const Prefs = {
    async getStudentNo(): Promise<string | null> {
        const v = await AsyncStorage.getItem(K_STUDENT_NO);
        const s = (v ?? "").trim();
        return s.length ? s : null;
    },
    async setStudentNo(no: string): Promise<void> {
        await AsyncStorage.setItem(K_STUDENT_NO, no.trim());
    },

    async getDeviceId(): Promise<string | null> {
        const v = await AsyncStorage.getItem(K_DEVICE_ID);
        return v && v.length ? v : null;
    },
    async setDeviceId(id: string): Promise<void> {
        await AsyncStorage.setItem(K_DEVICE_ID, id);
    },
};
