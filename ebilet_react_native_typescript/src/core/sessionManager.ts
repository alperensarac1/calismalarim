import AsyncStorage from '@react-native-async-storage/async-storage';

import {User} from '../models/User';

const KEYS = {
    userId: 'user_id',
    fullName: 'full_name',
    email: 'email',
    phone: 'phone',
    role: 'role',
    apiToken: 'api_token',
    isLoggedIn: 'is_logged_in',
};

export class SessionManager {
    static async saveUser(user: User): Promise<void> {
        await AsyncStorage.multiSet([
            [KEYS.userId, String(user.id)],
            [KEYS.fullName, user.fullName ?? user.full_name ?? ''],
            [KEYS.email, user.email ?? ''],
            [KEYS.phone, user.phone ?? ''],
            [KEYS.role, user.role ?? 'user'],
            [KEYS.apiToken, user.apiToken ?? user.api_token ?? ''],
            [KEYS.isLoggedIn, 'true'],
        ]);
    }

    static async isLoggedIn(): Promise<boolean> {
        const value = await AsyncStorage.getItem(KEYS.isLoggedIn);
        return value === 'true';
    }

    static async getApiToken(): Promise<string> {
        return (await AsyncStorage.getItem(KEYS.apiToken)) ?? '';
    }

    static async getFullName(): Promise<string> {
        return (await AsyncStorage.getItem(KEYS.fullName)) ?? '';
    }

    static async getEmail(): Promise<string> {
        return (await AsyncStorage.getItem(KEYS.email)) ?? '';
    }

    static async getRole(): Promise<string> {
        return (await AsyncStorage.getItem(KEYS.role)) ?? 'user';
    }

    static async isStaffOrAdmin(): Promise<boolean> {
        const role = await SessionManager.getRole();
        return role === 'staff' || role === 'admin';
    }

    static async logout(): Promise<void> {
        await AsyncStorage.multiRemove([
            KEYS.userId,
            KEYS.fullName,
            KEYS.email,
            KEYS.phone,
            KEYS.role,
            KEYS.apiToken,
            KEYS.isLoggedIn,
        ]);
    }
}