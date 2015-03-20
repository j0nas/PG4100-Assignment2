package assignment2;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

public class Client {
    private Socket socket;
    private DataOutputStream output;
    private DataInputStream input;

    public Client(InetAddress serverAdress, int port) throws IOException {
        socket = new Socket(serverAdress, port);
        output = new DataOutputStream(socket.getOutputStream());
        input = new DataInputStream(socket.getInputStream());

        // Create an output stream to send data to the server

        new Thread(() -> {
            int counter = 0;
            while (true) {
                try {
                    Log.v("Sending message to server.. ");
                    sendMessageToServer("Message: " + counter);
                } catch (IOException e) {
                    Log.e("Could not send message: " + e.getMessage());
                }

                counter++;
                try {
                    Thread.sleep((long) (1000 + (Math.random() * 5000)));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).run();

        // Create an input stream to recieve data from the server
//        final String data = input.readUTF();
//        Log.s("Recieved message from server: " + data);
    }

    public static void main(String[] args) {
        Client client = null;
        try {
            client = new Client(InetAddress.getLocalHost(), Server.COMMUNCATION_PORT);
        } catch (IOException e) {
            Log.e("Error when connecting to server: " + e.getMessage());
        } finally {
            try {
                client.destroy();
            } catch (Exception ignored) {
            }
        }
    }

    private void sendMessageToServer(String message) throws IOException {
        output.writeUTF(message);
        output.flush();
    }

    public void destroy() throws IOException {
        socket.close();
        input.close();
        output.close();
    }
}
