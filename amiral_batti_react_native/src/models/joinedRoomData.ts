import {PlayerInfo} from "./playerInfo";

export interface JoinedRoomData {
    roomCode: string;
    playerId: string;
    players: PlayerInfo[];
    message: string;
}
