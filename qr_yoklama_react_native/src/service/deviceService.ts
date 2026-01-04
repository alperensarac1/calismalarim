import * as Device from "expo-device";
import * as Application from "expo-application";
import { v4 as uuidv4 } from "uuid";
import { Prefs } from "../storage/prefs";

export const DeviceService = {
    async getOrCreateDeviceId(): Promise<string> {
        const existing = await Prefs.getDeviceId();
        if (existing) return existing;

        const id = uuidv4();
        await Prefs.setDeviceId(id);
        return id;
    },

    async getDeviceInfo(): Promise<string> {
        const brand = Device.brand ?? "";
        const model = Device.modelName ?? Device.modelId ?? "";
        const os = Device.osName ?? "";
        const osVer = Device.osVersion ?? "";
        const appId = Application.applicationId ?? "";

        const parts = [
            `${brand} ${model}`.trim(),
            `${os} ${osVer}`.trim(),
            appId ? `appId:${appId}` : "",
        ].filter(Boolean);

        return parts.join(" / ");
    },
};
