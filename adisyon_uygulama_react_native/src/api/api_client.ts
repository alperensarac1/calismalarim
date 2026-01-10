import axios from "axios";


export const api = axios.create({
    baseURL: "https://alperensaracdeneme.com/adisyon/",
    timeout: 20000,
});
