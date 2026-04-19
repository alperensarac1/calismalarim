export type RootStackParamList = {
    Lobby: undefined;
    Placement: {
        roomCode: string;
        playerId: string;
        playerName: string;
    };
    Game: {
        roomCode: string;
        playerId: string;
        playerName: string;
        firstTurnPlayerId: string;
        ownBoardMatrix: number[][];
    };
};
