import * as SecureStore from "expo-secure-store";

const KEY = "cargo_token";

export const tokenStore = {
    async get(): Promise<string | null> {
        return SecureStore.getItemAsync(KEY);
    },
    async set(token: string): Promise<void> {
        await SecureStore.setItemAsync(KEY, token);
    },
    async clear(): Promise<void> {
        await SecureStore.deleteItemAsync(KEY);
    },
};
