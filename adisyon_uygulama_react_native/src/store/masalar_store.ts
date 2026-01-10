import {Masa} from "../model/masa";
import {StyleSheet} from "react-native";
import create = StyleSheet.create;
import {AdisyonAPI} from "../api/adisyon_api";

type MasalarState = {
    masalar: Masa[];
    loading: boolean;
    error?: string;

    masalariYukle: () => Promise<void>;
    masaEkle: () => Promise<void>;
    masaSil: (id: number) => Promise<void>;
    masaBirlestir: (ana: number, b: number) => Promise<void>;
};

// @ts-ignore
export const useMasalarStore = create<MasalarState>((set, get) => ({
    masalar: [],
    loading: false,

    masalariYukle: async () => {
        set({ loading: true, error: undefined });
        try {
            const data = await AdisyonAPI.getMasalar();
            set({ masalar: data, loading: false });
        } catch (e: any) {
            set({ error: e?.message ?? "Masalar yüklenemedi", loading: false });
        }
    },

    masaEkle: async () => {
        await AdisyonAPI.masaEkle();
        await get().masalariYukle();
    },

    masaSil: async (id: number) => {
        await AdisyonAPI.masaSil(id);
        await get().masalariYukle();
    },

    masaBirlestir: async (ana: number, b: number) => {
        await AdisyonAPI.masaBirlestir(ana, b);
        await get().masalariYukle();
    },
}));
