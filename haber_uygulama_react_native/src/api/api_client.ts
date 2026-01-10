import axios from "axios";

export const api = axios.create({
    baseURL: "https://alperensaracdeneme.com/haberservis/",
    timeout: 20000,
    headers: {
        Accept: "application/json",
        "Content-Type": "application/json",
    },
});

// Debug için: istek/cevap logla
api.interceptors.response.use(
    (res) => {
        console.log("[API OK]", res.config.url, res.status);
        return res;
    },
    (err) => {
        const url = err?.config?.url;
        const status = err?.response?.status;
        console.log("[API ERR]", url, status, err?.message);
        console.log("[API ERR DATA]", err?.response?.data);
        return Promise.reject(err);
    }
);
