export interface FireResultData {
    roomCode: string;
    shooterPlayerId: string;
    targetPlayerId: string;
    row: number;
    col: number;
    hit: boolean;
    nextTurnPlayerId: string | null;
    gameOver: boolean;
    winnerPlayerId: string | null;
    message: string;
}
