package assignment;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Scanner;

public class Client extends Application {
    private Socket socket;
    private DataOutputStream output;
    private DataInputStream input;

    public Client() throws IOException {
        socket = new Socket(InetAddress.getLocalHost(), Server.COMMUNCATION_PORT);
        Log.s("Connected to server: " + socket.getLocalAddress());

        output = new DataOutputStream(socket.getOutputStream());
        input = new DataInputStream(socket.getInputStream());

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

    public static void main(String[] args) {
        //launch(args);
        Log.debugLevel = Log.LOG_VERBOSE;

        Client client = null;
        try {
            client = new Client();
        } catch (IOException e) {
            Log.e("Error when connecting to server: " + e.getMessage());
        } finally {
            if (client != null) {
                client.destroy();
            }
        }
    }

    private void handleIncomingServerMessages(Scanner scanner) throws IOException {
        final String message = input.readUTF();
        Log.v("Received message from server: " + message);

        if (message.startsWith(Server.STATUS_END_OF_QUIZ)) {
            Log.w("Received shutdown instruction. Bye!");
            destroy();
            return;
        }

        if (message.startsWith(Server.QUESTION_PREFIX)) {
            askClientAndSendResponse(scanner, message);
        }
    }

    private void askClientAndSendResponse(Scanner scanner, String message) throws IOException {
        if (scanner.hasNextLine()) {
            System.out.println(message.substring(Server.QUESTION_PREFIX.length()));
            String answer = scanner.nextLine();
            sendMessageToServer(answer);
            Log.v("Sending message to server: " + answer);
        } else {
            Log.e("Could not access scanner?");
        }
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("sample.fxml"));
        primaryStage.setTitle("Hello World");
        primaryStage.setScene(new Scene(root, 300, 275));
        primaryStage.show();
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
