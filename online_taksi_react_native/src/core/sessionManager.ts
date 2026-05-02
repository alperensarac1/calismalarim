import AsyncStorage from "@react-native-async-storage/async-storage";

const TOKEN_KEY = "token";
const USER_ID_KEY = "user_id";
const FULL_NAME_KEY = "full_name";
const ROLE_KEY = "role";

export const SessionManager = {
    async saveAuth(params: {
        token: string;
        userId: number;
        fullName: string;
        role: string;
    }) {
        await AsyncStorage.setItem(TOKEN_KEY, params.token);
        await AsyncStorage.setItem(USER_ID_KEY, String(params.userId));
        await AsyncStorage.setItem(FULL_NAME_KEY, params.fullName);
        await AsyncStorage.setItem(ROLE_KEY, params.role);
    },

    async getToken(): Promise<string | null> {
        return AsyncStorage.getItem(TOKEN_KEY);
    },

    async getRole(): Promise<string | null> {
        return AsyncStorage.getItem(ROLE_KEY);
    },

    async getFullName(): Promise<string | null> {
        return AsyncStorage.getItem(FULL_NAME_KEY);
    },

    async isLoggedIn(): Promise<boolean> {
        const token = await this.getToken();
        return !!token && token.trim().length > 0;
    },

    async clear() {
        await AsyncStorage.removeItem(TOKEN_KEY);
        await AsyncStorage.removeItem(USER_ID_KEY);
        await AsyncStorage.removeItem(FULL_NAME_KEY);
        await AsyncStorage.removeItem(ROLE_KEY);
    },
};