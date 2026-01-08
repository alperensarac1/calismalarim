export const Routes = {
    LOGIN: "login",
    REGISTER: "register",

    TABS: "tabs",

    GUNDEM: "gundem",
    BUGUN: "bugun",
    PROFIL: "profil",

    ENTRY_ADD: "entry_add",
    ENTRY_DETAIL: "entry_detail",
} as const;

export type RootStackParamList = {
    [Routes.LOGIN]: undefined;
    [Routes.REGISTER]: undefined;
    [Routes.TABS]: undefined;

    [Routes.ENTRY_ADD]: undefined;
    [Routes.ENTRY_DETAIL]: { id: number };
};

export type TabsParamList = {
    [Routes.GUNDEM]: undefined;
    [Routes.BUGUN]: undefined;
    [Routes.PROFIL]: undefined;
};
