export type ScreenName =
    | "home"
    | "create_room"
    | "join_room"
    | "owner_room"
    | "waiting_room"
    | "quiz"
    | "winner";

export type AppScreen =
    | { name: "home" }
    | { name: "create_room" }
    | { name: "join_room" }
    | {
    name: "owner_room";
    roomCode: string;
    username: string;
    questionTime: number;
}
    | {
    name: "waiting_room";
    roomCode: string;
    username: string;
    questionTime: number;
}
    | {
    name: "quiz";
    roomCode: string;
    username: string;
    questionTime: number;
    isOwner: boolean;
}
    | {
    name: "winner";
    winners: ScoreItem[];
    scoreboard: ScoreItem[];
};

export type ScoreItem = {
    username: string;
    score: number;
};

export type QuestionData = {
    questionNumber: number;
    totalQuestions: number;
    questionText: string;
    options: string[];
    questionTime: number;
};

export type SocketListener = {
    onOpen?: () => void;
    onMessage?: (message: string) => void;
    onClose?: () => void;
    onError?: (error: string) => void;
};