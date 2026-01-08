// src/prefs.ts
import AsyncStorage from "@react-native-async-storage/async-storage";
import {PerkStore} from "../model/perk_store";
import {GameState} from "../model/game_state";

const KEY_GAME = "cookie_prefs_compose_game";
const KEY_PERKS = "cookie_prefs_compose_perks";

export const defaultGameState: GameState = {
    score: 0,
    cps: 0,
    baseTap: 1,
    extraTap: 0,
    prestigeLevel: 0,
};

export const defaultPerks: PerkStore = {
    points: 0,
    gprod: 0,
    crit: 0,
    discount: 0,
    tapTop: 0,
};

export async function loadGame(): Promise<GameState> {
    const s = await AsyncStorage.getItem(KEY_GAME);
    if (!s) return defaultGameState;
    try {
        const j = JSON.parse(s);
        return {
            score: Number(j.score ?? 0),
            cps: Number(j.cps ?? 0),
            baseTap: Number(j.baseTap ?? 1),
            extraTap: Number(j.extraTap ?? 0),
            prestigeLevel: Number(j.prestigeLevel ?? 0),
        };
    } catch {
        return defaultGameState;
    }
}

export async function saveGame(gs: GameState) {
    await AsyncStorage.setItem(KEY_GAME, JSON.stringify(gs));
}

export async function loadPerks(): Promise<PerkStore> {
    const s = await AsyncStorage.getItem(KEY_PERKS);
    if (!s) return defaultPerks;
    try {
        const j = JSON.parse(s);
        return {
            points: Number(j.points ?? 0),
            gprod: Number(j.gprod ?? 0),
            crit: Number(j.crit ?? 0),
            discount: Number(j.discount ?? 0),
            tapTop: Number(j.tapTop ?? 0),
        };
    } catch {
        return defaultPerks;
    }
}

export async function savePerks(ps: PerkStore) {
    await AsyncStorage.setItem(KEY_PERKS, JSON.stringify(ps));
}
