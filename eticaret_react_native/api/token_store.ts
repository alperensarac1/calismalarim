import AsyncStorage from "@react-native-async-storage/async-storage";


const KEY = "auth_token";

export const tokenStore = {
    async set(token: string) {
        await AsyncStorage.setItem(KEY, token);
    },
    async get() {
        return await AsyncStorage.getItem(KEY);
    },
    async clear() {
        await AsyncStorage.removeItem(KEY);
    },
};
