import { MARK_URL } from "../util/constants";
import { ApiClient } from "./apiClient";
import { DeviceService } from "./deviceService";
import { LocationService } from "./locationService";
import { QRParser } from "./qrParser";

export class AttendanceService {
    private api = new ApiClient();

    async sendByQR(studentNo: string, qrRaw: string): Promise<void> {
        const loc = await LocationService.getBestLocation();
        if (!loc) throw new Error("Konum alınamadı veya izin yok.");

        const qrPayload = QRParser.parsePayload(qrRaw);

        const deviceId = await DeviceService.getOrCreateDeviceId();
        const deviceInfo = await DeviceService.getDeviceInfo();

        const body = {
            student_no: studentNo,
            method: "QR",
            qr_payload: qrPayload,
            lat: loc.lat,
            lng: loc.lng,
            device_id: deviceId,
            device_info: deviceInfo,
        };

        const resp = await this.api.postJson(MARK_URL, body);
        const text = await resp.text();

        if (!resp.ok) {
            throw new Error(prettyServerError(text, resp.status));
        }
    }

    async sendByCode(studentNo: string, joinCode: string): Promise<void> {
        const code = joinCode.trim();
        if (!code) throw new Error("Kod boş olamaz.");

        const loc = await LocationService.getBestLocation();
        if (!loc) throw new Error("Konum alınamadı veya izin yok.");

        const deviceId = await DeviceService.getOrCreateDeviceId();
        const deviceInfo = await DeviceService.getDeviceInfo();

        const body = {
            student_no: studentNo,
            method: "CODE",
            join_code: code,
            lat: loc.lat,
            lng: loc.lng,
            device_id: deviceId,
            device_info: deviceInfo,
        };

        const resp = await this.api.postJson(MARK_URL, body);
        const text = await resp.text();

        if (!resp.ok) {
            throw new Error(prettyServerError(text, resp.status));
        }
    }
}

function prettyServerError(resp: string, code: number): string {
    const s = (resp ?? "").trim();
    const clean =
        s.startsWith("{") || s.startsWith("[")
            ? s
            : s
                .replace(/<[^>]*>/g, " ")
                .replace(/&quot;/g, '"')
                .replace(/&lt;/g, "<")
                .replace(/&gt;/g, ">")
                .replace(/&amp;/g, "&")
                .replace(/\s+/g, " ")
                .trim();

    return `Sunucu Hatası (${code})\n${clean}`;
}
