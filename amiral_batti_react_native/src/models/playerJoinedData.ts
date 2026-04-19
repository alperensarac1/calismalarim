import {PlayerInfo} from "./playerInfo";

export interface PlayerJoinedData {
    roomCode: string;
    players: PlayerInfo[];
    message: string;
}
