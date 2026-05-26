export class SocketMessageFactory {
    static createRoom(username: string, questionTime: number): string {
        return JSON.stringify({
            type: "create_room",
            username,
            question_time: questionTime,
        });
    }

    static joinRoom(roomCode: string, username: string): string {
        return JSON.stringify({
            type: "join_room",
            room_code: roomCode,
            username,
        });
    }

    static addQuestion(
        roomCode: string,
        questionText: string,
        options: string[],
        correctIndex: number
    ): string {
        return JSON.stringify({
            type: "add_question",
            room_code: roomCode,
            question_text: questionText,
            options,
            correct_index: correctIndex,
        });
    }

    static startQuiz(roomCode: string): string {
        return JSON.stringify({
            type: "start_quiz",
            room_code: roomCode,
        });
    }

    static submitAnswer(
        roomCode: string,
        username: string,
        answerIndex: number
    ): string {
        return JSON.stringify({
            type: "submit_answer",
            room_code: roomCode,
            username,
            answer_index: answerIndex,
        });
    }
}