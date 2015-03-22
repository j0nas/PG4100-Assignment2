package assignment;

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

    public Client() throws IOException {
        socket = new Socket(InetAddress.getLocalHost(), Server.COMMUNCATION_PORT);
        Log.s("Connected to server: " + socket.getLocalAddress());

        output = new DataOutputStream(socket.getOutputStream());
        input = new DataInputStream(socket.getInputStream());
    }

    public static void main(String[] args) {
        Log.debugLevel = Log.LOG_VERBOSE;

        Client client = null;
        try {
            client = new Client();
            client.startClient();
        } catch (IOException e) {
            Log.e("Error when connecting to server: " + e.getMessage());
        } finally {
            if (client != null) {
                client.destroy();
            }
        }
    }

    private void startClient() {
        try (Scanner scanner = new Scanner(System.in)) {
            while (true) {
                try {
                    // Create an input stream to receive data from the server
                    handleIncomingServerMessages(scanner);
                } catch (IOException e) {
                    Log.e("Could not send message: " + e.getMessage());
                }
            }
        }
    }

    private void handleIncomingServerMessages(Scanner scanner) throws IOException {
        final String message = input.readUTF();
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

    private void askUserAndSubmitResponse(Scanner scanner, String message) throws IOException {
        System.out.println(message.substring(Server.PREFIX_QUESTION.length()));
        if (scanner.hasNextLine()) {
            String answer = scanner.nextLine();
            sendMessageToServer(answer);
            Log.v("Sending message to server: " + answer);
        } else {
            Log.e("Could not access scanner!");
        }
    }

    private void sendMessageToServer(String message) throws IOException {
        output.writeUTF(message);
        output.flush();
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
