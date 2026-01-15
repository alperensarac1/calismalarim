export const Routes = {
    LOGIN: 'login',
    REGISTER: 'register',
    HOME: 'home',
    ODA: 'oda',
} as const;


export type RootStackParamList = {
    [Routes.LOGIN]: undefined;
    [Routes.REGISTER]: undefined;
    [Routes.HOME]: { userId: number };
    [Routes.ODA]: { roomId: number; userId: number };
};
