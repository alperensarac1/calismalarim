import {PlayerInfo} from "./playerInfo";

export interface BoardSetData {
    roomCode: string;
    players: PlayerInfo[];
    message: string;
}
