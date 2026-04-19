import {RematchPlayerInfo} from "./rematchPlayerInfo";

export interface RematchStatusData {
    roomCode: string;
    players: RematchPlayerInfo[];
    message: string;
}
