import * as Location from "expo-location";

export type SimpleLocation = { lat: number; lng: number };

export const LocationService = {
    async getBestLocation(): Promise<SimpleLocation | null> {
        const perm = await Location.requestForegroundPermissionsAsync();
        if (perm.status !== "granted") return null;

        const enabled = await Location.hasServicesEnabledAsync();
        if (!enabled) return null;

        try {
            const cur = await Location.getCurrentPositionAsync({
                accuracy: Location.Accuracy.High,
            });
            if (this.isUsable(cur)) {
                return { lat: cur.coords.latitude, lng: cur.coords.longitude };
            }
        } catch (_) {}

        try {
            const last = await Location.getLastKnownPositionAsync();
            if (last && this.isUsable(last)) {
                return { lat: last.coords.latitude, lng: last.coords.longitude };
            }
        } catch (_) {}

        return null;
    },

    isUsable(pos: Location.LocationObject): boolean {
        const { latitude, longitude, accuracy } = pos.coords;
        if (latitude === 0 && longitude === 0) return false;

        if (pos.timestamp) {
            const ageMs = Date.now() - pos.timestamp;
            if (ageMs > 120_000) return false;
        }

        if (typeof accuracy === "number" && accuracy > 100) return false;

        return true;
    },
};
