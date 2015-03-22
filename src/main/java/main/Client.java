package main;

import util.Log;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Scanner;

public class Client {
    private Socket socket;
    private DataOutputStream output;
    private DataInputStream input;

    public Client(InetAddress serverAddress, int port) throws IOException {
        socket = new Socket(serverAddress, port);
        Log.s("Connected to server at " + socket.getLocalAddress());

        output = new DataOutputStream(socket.getOutputStream());
        input = new DataInputStream(socket.getInputStream());
    }

    private void executeLifecycle() {
        try (Scanner scanner = new Scanner(System.in)) {
            while (true) {
                handleIncomingServerMessages(scanner);
            }
        }
    }

    public static void main(String[] args) {
        Log.debugLevel = Log.LOG_VERBOSE;

        Client client = null;
        try {
            client = new Client(InetAddress.getLocalHost(), Config.SERVER_PORT);
            client.executeLifecycle();
        } catch (IOException e) {
            Log.e("Error when connecting to server: " + e.getMessage());
        } finally {
            if (client != null) {
                client.destroy();
            }
        }
    }

    private void handleIncomingServerMessages(Scanner scanner) {
        String message;

        try {
            message = input.readUTF();
        } catch (IOException e) {
            Log.e("Could not receive incoming data: " + e.getMessage());
            return;
        }

        Log.v("Received message from server: " + message);

        if (message.startsWith(Server.PREFIX_END_OF_QUIZ)) {
            Log.w("Received shutdown instruction. Bye!");
            destroy();
            return;
        }

        if (message.startsWith(Server.PREFIX_QUESTION)) {
            askUserAndSubmitResponse(scanner, message);
        }
    }

    private void askUserAndSubmitResponse(Scanner scanner, String message) {
        System.out.println(message.substring(Server.PREFIX_QUESTION.length()));
        if (scanner.hasNextLine()) {
            String answer = scanner.nextLine();
            sendMessageToServer(answer);
            Log.v("Sending message to server: " + answer);
        } else {
            Log.e("Could not access scanner!");
        }
    }

    private void sendMessageToServer(String message) {
        try {
            output.writeUTF(message);
            output.flush();
        } catch (IOException e) {
            Log.e("Could not send message to server: " + e.getMessage());
        }
    }

    public void destroy() {
        try {
            socket.close();
            input.close();
            output.close();
        } catch (IOException e) {
            Log.e("Could not execute destroy(): " + e.getMessage());
        }
    }
}
