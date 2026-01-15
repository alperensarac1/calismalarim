import axios from 'axios';

export const api = axios.create({
    baseURL: 'https://alperensaracdeneme.com/meme/',
    timeout: 30000,
    headers: {
        Accept: 'application/json',
    },
});

// PHP bazen text/plain döndürebilir; axios çoğu zaman yine parse eder.
// Gerekirse burada interceptor eklenebilir.
