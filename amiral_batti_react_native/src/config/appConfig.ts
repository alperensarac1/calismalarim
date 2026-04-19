export const AppConfig = {
    serverIp: "10.19.82.112",
    serverPort: 8080,
    get webSocketUrl() {
        return `ws://${this.serverIp}:${this.serverPort}`;
    },
    get httpBaseUrl() {
        return `http://${this.serverIp}:${this.serverPort}/`;
    },
};
