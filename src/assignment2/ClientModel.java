package assignment2;

import java.net.Socket;

public class ClientModel {
    private int score = 0;
    private int currentQuestionNumber = 0;
    private int id;
    private Socket socket;

    public ClientModel(int id, Socket socket) {
        this.id = id;
        this.socket = socket;
    }

    public int getId() {
        return id;
    }

    public Socket getSocket() {
        return socket;
    }
}
