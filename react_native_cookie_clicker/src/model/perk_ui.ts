import {PerkStore} from "./perk_store";

export type PerkUi = {
    key: keyof Omit<PerkStore, "points">;
    title: string;
    desc: string;
    baseCost: number;
    scaling: number;
    level: number;
    maxLevel?: number;
};