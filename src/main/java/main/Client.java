package main;

import org.apache.logging.log4j.LogManager;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Scanner;

public class Client implements AutoCloseable {
    private Socket socket;
    private DataOutputStream output;
    private DataInputStream input;
    private boolean isAlive = true;

    /**
     * Initializes an instance of the class.
     *
     * @param serverAddress The server address to which the instance will attempt to connect to..
     * @param port          .. and the respective port.
     * @throws IOException
     */
    public Client(InetAddress serverAddress, int port) throws IOException {
        socket = new Socket(serverAddress, port);
        LogManager.getLogger(Config.LOG_CLIENT).debug("Connected to server at " + socket.getLocalAddress());

        output = new DataOutputStream(socket.getOutputStream());
        input = new DataInputStream(socket.getInputStream());
    }

    /**
     * Main method, controlling the flow of execution for the class.
     *
     * @param args not used.
     */
    public static void main(String[] args) {
        try (Client client = new Client(InetAddress.getLocalHost(), Config.SERVER_PORT)) {
            client.start();
        } catch (IOException e) {
            final String error = "Error when connecting to server: " + e.getMessage();
            LogManager.getLogger(Config.LOG_CLIENT).error(error);
        }
    }

    /**
     * Lifecycle method.
     */
    private void start() {
        try (Scanner scanner = new Scanner(System.in)) {
            while (isAlive) {
                handleIncomingServerMessages(scanner);
            }
        }
    }

    /**
     * Receives and handles messages from the server, filtering on predefined prefixes.
     *
     * @param scanner Scanner to utilize for responses to the server if needed.
     */
    private void handleIncomingServerMessages(Scanner scanner) {
        String message;

        try {
            message = input.readUTF();
        } catch (IOException e) {
            LogManager.getLogger(Config.LOG_CLIENT).error("Could not receive incoming data: " + e.getMessage());
            return;
        }

        if (message.startsWith(Server.PREFIX_END_OF_QUIZ)) {
            close();
            return;
        }

        if (message.startsWith(Server.PREFIX_QUESTION)) {
            askUserAndSubmitResponse(scanner, message);
            return;
        }

        LogManager.getLogger(Config.LOG_CLIENT).debug(message);
    }

    /**
     * Query for the user to respond to a message from the server.
     *
     * @param scanner The scanner used to receive a response to the server.
     * @param message The message from the server
     */
    private void askUserAndSubmitResponse(Scanner scanner, String message) {
        LogManager.getLogger(Config.LOG_CLIENT).debug(message.substring(Server.PREFIX_QUESTION.length()));
        if (scanner.hasNextLine()) {
            String answer = scanner.nextLine();
            sendMessageToServer(answer);
            LogManager.getLogger(Config.LOG_CLIENT).debug("Sending message to server: " + answer);
        } else {
            LogManager.getLogger(Config.LOG_CLIENT).error("Could not access scanner!");
        }
    }

    /**
     * Convenience method for sending a string message to the server.
     *
     * @param message The string to be sent.
     */
    private void sendMessageToServer(String message) {
        try {
            output.writeUTF(message);
            output.flush();
        } catch (IOException e) {
            LogManager.getLogger(Config.LOG_CLIENT).error("Could not send message to server: " + e.getMessage());
        }
    }

    /**
     * Shutdown method to be called when server requests shutdown or when object instance no longer is in use.
     */
    @Override
    public void close() {
        isAlive = false;
        try {
            socket.close();
            input.close();
            output.close();
        } catch (IOException e) {
            LogManager.getLogger(Config.LOG_CLIENT).error("Could not close(): " + e.getMessage());
        }
    }
}
