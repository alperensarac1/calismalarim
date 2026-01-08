import AsyncStorage from "@react-native-async-storage/async-storage";

const KEY_USER_ID = "sozluk_user_id";
const KEY_USERNAME = "sozluk_username";
const KEY_IS_LOGGED_IN = "sozluk_is_logged_in";

export const SessionManager = {
    saveUserSession: async (userId: number, username: string) => {
        await AsyncStorage.multiSet([
            [KEY_USER_ID, String(userId)],
            [KEY_USERNAME, username],
            [KEY_IS_LOGGED_IN, "true"],
        ]);
    },

    isLoggedIn: async () => {
        const v = await AsyncStorage.getItem(KEY_IS_LOGGED_IN);
        return v === "true";
    },

    getUserId: async () => {
        const v = await AsyncStorage.getItem(KEY_USER_ID);
        return v ? parseInt(v, 10) : -1;
    },

    getUsername: async () => {
        return (await AsyncStorage.getItem(KEY_USERNAME)) ?? null;
    },

    clearSession: async () => {
        await AsyncStorage.multiRemove([KEY_USER_ID, KEY_USERNAME, KEY_IS_LOGGED_IN]);
    },
};
