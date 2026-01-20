import axios from "axios";
import { Endpoints } from "./endpoints";
import {tokenStore} from "./token_store";

export const http = axios.create({
    baseURL: Endpoints.baseUrl,
    timeout: 20000,
});

http.interceptors.request.use(async (config) => {
    const token = await tokenStore.get();
    if (token) {
        config.headers = config.headers ?? {};
        config.headers["Authorization"] = `Bearer ${token}`;
    }
    return config;
});
