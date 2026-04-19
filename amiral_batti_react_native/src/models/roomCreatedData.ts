import {PlayerInfo} from "./playerInfo";

export interface RoomCreatedData {
    roomCode: string;
    playerId: string;
    players: PlayerInfo[];
    message: string;
}
