import {PlayerInfo} from "./playerInfo";

export interface GameStartedData {
    roomCode: string;
    firstTurnPlayerId: string;
    players: PlayerInfo[];
    message: string;
}
