export type Upgrade = {
    id: number;
    title: string;
    desc: string;
    icon: string; // name
    basePrice: number;
    cpsGain?: number;
    tapGain?: number;
    priceMultiplier?: number;
    level: number;
};