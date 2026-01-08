// src/gameStore.ts
import { create } from "zustand";
import { devtools } from "zustand/middleware";
import { loadGame, loadPerks, saveGame, savePerks, defaultGameState, defaultPerks } from "./prefs";
import {Upgrade} from "../model/upgrade";
import {GameState} from "../model/game_state";
import {PerkStore} from "../model/perk_store";
import {FloatingText} from "../model/floating_text";

const clamp = (v: number, min: number, max: number) => Math.max(min, Math.min(max, v));

function currentPrice(u: Upgrade): number {
    const mult = u.priceMultiplier ?? 1.15;
    return u.basePrice * Math.pow(mult, u.level);
}

const initialUpgrades: Upgrade[] = [
    { id: 1, title: "Otomatik Tıklayıcı", desc: "Saniyede +1", icon: "Bolt", basePrice: 50, cpsGain: 1, level: 0 },
    { id: 2, title: "Hızlı Karıştırıcı", desc: "Tıklama +1", icon: "FastForward", basePrice: 75, tapGain: 1, level: 0 },
    { id: 3, title: "Minik Fırın", desc: "Saniyede +5", icon: "LocalFireDepartment", basePrice: 250, cpsGain: 5, level: 0 },
    { id: 4, title: "Çikolata Parçaları", desc: "Tıklama +3", icon: "GridView", basePrice: 300, tapGain: 3, level: 0 },
    { id: 5, title: "Pastane", desc: "Saniyede +25", icon: "Store", basePrice: 1200, cpsGain: 25, level: 0 },
    { id: 6, title: "Fabrika", desc: "Saniyede +120", icon: "Factory", basePrice: 6000, cpsGain: 120, level: 0 },
    { id: 7, title: "Araştırma Lab.", desc: "Tıklama +10", icon: "Science", basePrice: 8000, tapGain: 10, level: 0 },
    { id: 8, title: "Roket Fırın", desc: "Saniyede +750", icon: "Rocket", basePrice: 42000, cpsGain: 750, level: 0 },
];

type GameStore = {
    game: GameState;
    perks: PerkStore;
    upgrades: Upgrade[];
    floaters: FloatingText[];

    critReady: boolean;
    critCooldownLeft: number;

    // internal
    _loopId?: any;
    _critId?: any;
    _hydrated: boolean;

    hydrate: () => Promise<void>;
    startLoop: () => void;
    stopLoop: () => void;

    onTapCookie: (x: number, y: number) => void;
    doCrit: (x: number, y: number) => void;

    buyUpgrade: (id: number) => void;
    reset: () => void;
    prestige: () => void;

    buyPerk: (key: "gprod" | "crit" | "discount" | "tapTop", cost: number, maxLevel?: number) => void;
};

export const useGameStore = create<GameStore>()(
    devtools((set, get) => ({
        game: defaultGameState,
        perks: defaultPerks,
        upgrades: initialUpgrades,
        floaters: [],
        critReady: true,
        critCooldownLeft: 0,
        _hydrated: false,

        hydrate: async () => {
            const [g, p] = await Promise.all([loadGame(), loadPerks()]);
            set({ game: g, perks: p, _hydrated: true }, false, "hydrate");
            get().startLoop();
        },

        startLoop: () => {
            const { _loopId } = get();
            if (_loopId) return;

            const id = setInterval(async () => {
                const st = get();
                if (!st._hydrated) return;

                const prestigeMult = 1 + st.game.prestigeLevel * 0.1;
                const gprodMult = 1 + st.perks.gprod * 0.05;
                const totalMult = prestigeMult * gprodMult;

                const eff = st.game.cps * totalMult;
                const newGame: GameState = { ...st.game, score: st.game.score + eff * 0.1 };

                set({ game: newGame }, false, "loopTick");
                // persist (çok sık yazmasın diye istersen throttle yaparız)
                await Promise.all([saveGame(newGame), savePerks(get().perks)]);
            }, 100);

            set({ _loopId: id }, false, "startLoop");
        },

        stopLoop: () => {
            const st = get();
            if (st._loopId) clearInterval(st._loopId);
            if (st._critId) clearInterval(st._critId);
            set({ _loopId: undefined, _critId: undefined }, false, "stopLoop");
        },

        onTapCookie: (x, y) => {
            const st = get();
            const prestigeMult = 1 + st.game.prestigeLevel * 0.1;
            const gprodMult = 1 + st.perks.gprod * 0.05;
            const totalMult = prestigeMult * gprodMult;

            const tapPower = st.game.baseTap + st.game.extraTap + st.perks.tapTop;
            let gain = Math.floor(tapPower * totalMult);

            const critChance = st.perks.crit; // %1/level
            const roll = Math.floor(Math.random() * 100);

            let isCrit = false;
            if (critChance > 0 && roll < critChance) {
                gain *= 3;
                isCrit = true;
            }

            const id = Date.now() * 1000 + Math.floor(Math.random() * 999);
            const floater: FloatingText = {
                id,
                text: isCrit ? `CRIT +${gain}` : `+${gain}`,
                x,
                y,
                isCrit,
            };

            const newGame: GameState = { ...st.game, score: st.game.score + gain };
            set({ game: newGame, floaters: [...st.floaters, floater] }, false, "onTapCookie");

            // remove floater after 700ms
            setTimeout(() => {
                const now = get();
                set({ floaters: now.floaters.filter(f => f.id !== id) }, false, "removeFloater");
            }, 700);

            void Promise.all([saveGame(newGame), savePerks(get().perks)]);
        },

        doCrit: (x, y) => {
            const st = get();
            if (!st.critReady) return;

            const tapPower = st.game.baseTap + st.game.extraTap + st.perks.tapTop;
            const gain = tapPower * 10;

            const id = Date.now() * 1000 + Math.floor(Math.random() * 999);
            const floater: FloatingText = { id, text: `CRIT +${gain}`, x, y, isCrit: true };

            const newGame: GameState = { ...st.game, score: st.game.score + gain };

            set(
                {
                    game: newGame,
                    floaters: [...st.floaters, floater],
                    critReady: false,
                    critCooldownLeft: 30,
                },
                false,
                "doCrit",
            );

            setTimeout(() => {
                const now = get();
                set({ floaters: now.floaters.filter(f => f.id !== id) }, false, "removeCritFloater");
            }, 700);

            if (st._critId) clearInterval(st._critId);

            const critId = setInterval(async () => {
                const cur = get();
                const left = cur.critCooldownLeft - 1;
                if (left <= 0) {
                    clearInterval(critId);
                    set({ critReady: true, critCooldownLeft: 0, _critId: undefined }, false, "critDone");
                } else {
                    set({ critCooldownLeft: left }, false, "critTick");
                }
                await Promise.all([saveGame(get().game), savePerks(get().perks)]);
            }, 1000);

            set({ _critId: critId }, false, "critStart");
            void Promise.all([saveGame(newGame), savePerks(get().perks)]);
        },

        buyUpgrade: (id) => {
            const st = get();
            const idx = st.upgrades.findIndex(u => u.id === id);
            if (idx < 0) return;

            const discount = clamp(st.perks.discount * 0.02, 0, 0.5);
            const u = st.upgrades[idx];
            const price = currentPrice(u) * (1 - discount);
            if (st.game.score < price) return;

            const newU: Upgrade = { ...u, level: u.level + 1 };
            const newUpgrades = [...st.upgrades];
            newUpgrades[idx] = newU;

            const newGame: GameState = {
                ...st.game,
                score: st.game.score - price,
                cps: st.game.cps + (u.cpsGain ?? 0),
                extraTap: st.game.extraTap + (u.tapGain ?? 0),
            };

            set({ upgrades: newUpgrades, game: newGame }, false, "buyUpgrade");
            void Promise.all([saveGame(newGame), savePerks(get().perks)]);
        },

        reset: () => {
            const st = get();
            const newGame: GameState = { ...st.game, score: 0, cps: 0, extraTap: 0 };
            const newUpgrades = st.upgrades.map(u => ({ ...u, level: 0 }));
            set({ game: newGame, upgrades: newUpgrades }, false, "reset");
            void Promise.all([saveGame(newGame), savePerks(get().perks)]);
        },

        prestige: () => {
            const st = get();
            const gain = Math.floor(Math.sqrt(st.game.score / 1000));
            if (gain <= 0) return;

            const newPerks: PerkStore = { ...st.perks, points: st.perks.points + gain };
            const newGame: GameState = {
                ...st.game,
                prestigeLevel: st.game.prestigeLevel + gain,
                score: 0,
                cps: 0,
                extraTap: 0,
            };
            const newUpgrades = st.upgrades.map(u => ({ ...u, level: 0 }));

            set({ perks: newPerks, game: newGame, upgrades: newUpgrades }, false, "prestige");
            void Promise.all([saveGame(newGame), savePerks(newPerks)]);
        },

        buyPerk: (key, cost, maxLevel) => {
            const st = get();
            if (st.perks.points < cost) return;

            const cur = { ...st.perks, points: st.perks.points - cost };
            let next = cur;

            if (key === "gprod") next = { ...cur, gprod: cur.gprod + 1 };
            if (key === "crit") next = { ...cur, crit: cur.crit + 1 };
            if (key === "tapTop") next = { ...cur, tapTop: cur.tapTop + 1 };
            if (key === "discount") {
                const v = cur.discount + 1;
                next = { ...cur, discount: maxLevel != null ? Math.min(v, maxLevel) : v };
            }

            set({ perks: next }, false, "buyPerk");
            void savePerks(next);
        },
    })),
);
