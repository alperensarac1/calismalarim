import axios from "axios";
import { Endpoints } from "./endpoints";

import type { ApiResp } from "./types";
import {tokenStore} from "../storage/token_store";

export const api = axios.create({
    baseURL: Endpoints.base,
    timeout: 20000,
});

api.interceptors.request.use(async (config) => {
    const token = await tokenStore.get();
    if (token) {
        config.headers = config.headers ?? {};
        config.headers["X-Auth-Token"] = token;
    }
    config.headers = config.headers ?? {};
    config.headers["Content-Type"] = "application/json";
    return config;
});

export class APIError extends Error {
    constructor(message: string) {
        super(message);
        this.name = "APIError";
    }
}

// Helper: post json
export async function postJSON<T>(path: string, body: any): Promise<ApiResp<T>> {
    const res = await api.post(path, body);
    return res.data as ApiResp<T>;
}

// Helper: get json with query
export async function getJSON<T>(path: string, params?: Record<string, any>): Promise<ApiResp<T>> {
    const res = await api.get(path, { params });
    return res.data as ApiResp<T>;
}
