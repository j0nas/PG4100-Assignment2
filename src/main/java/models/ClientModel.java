package models;

import java.net.Socket;

public class ClientModel {
    private final int id;
    private final Socket socket;
    private int score = 0;
    private int currentQuestion = -1;

    public ClientModel(int id, Socket socket) {
        this.id = id;
        this.socket = socket;
    }

    public int getScore() {
        return score;
    }

    public int getCurrentQuestionNumber() {
        return currentQuestion;
    }

    public void setCurrentQuestion(int currentQuestion) {
        this.currentQuestion = currentQuestion;
    }

    public void incCurrentQuestion(boolean incScore) {
        this.currentQuestion++;
        if (incScore) {
            this.score++;
        }
    }

    public int getId() {
        return id;
    }

    public Socket getSocket() {
        return socket;
    }
}
