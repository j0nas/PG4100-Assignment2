package assignment2;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.BindException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    public static final int COMMUNCATION_PORT = 3000;
    private final ExecutorService threadPool;
    private ServerSocket socket;
    private List<ClientModel> clients = new ArrayList<>();

    public Server() {
        bindSocketToPort(COMMUNCATION_PORT);

        threadPool = Executors.newCachedThreadPool();


        final Thread clientManagerThread = new Thread(() -> {
            Log.s("Awaiting client connections..");
            while (true) {
                try {
                    final ClientModel clientModel = new ClientModel(clients.size() + 1, socket.accept());
                    clients.add(clientModel);
                    threadPool.execute(() -> {
                        ClientModel clientModelForThisThread = clientModel;
                        try (DataInputStream input = new DataInputStream(clients.get(clients.size() - 1)
                                .getSocket()
                                .getInputStream())) {
                            final String data = input.readUTF();
                            Log.s("Got message from client #" + clientModelForThisThread.getId() + ": " + data);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });
                } catch (IOException e) {
                    Log.e("An error occurred whilst waiting for connections:\n" + e.getMessage());
                }

                Log.s("Accepted client #" + clients.size() + 1);
            }
        });
        clientManagerThread.start();


    }

    public static void main(String[] args) {
        new Server();
    }

    private void bindSocketToPort(int port) {
        Log.s("Attempting to bind to port " + port + ".. ");

        try {
            socket = new ServerSocket(port);
        } catch (IOException e) {
            if (e instanceof BindException) {
                Log.s("port is taken.");
                bindSocketToPort(port + 1);
                return;
            }

            e.printStackTrace();
        }

        Log.s("Success.");
    }


//    private static void ServerStuff(final int port) {
//        System.out.println("Starting server, listening for connection..");
//        try (ServerSocket server = new ServerSocket(COMMUNCATION_PORT);
//             Socket client = server.accept()
//        ) {
//            System.out.println("Accepted connection. Sending and recieving message..");
//            try (DataOutputStream output = new DataOutputStream(client.getOutputStream())) {
//                final String message = "Message to client!";
//                output.writeUTF(message);
//                output.flush();
//                System.out.println("Sent message to client.");
//            }
//
//            try (DataInputStream input = new DataInputStream(client.getInputStream())) {
//                System.out.println("Attempting to get message from server");
//                final String data = input.readUTF();
//                System.out.println(data);
//            } catch (SocketException e) {
//                System.out.println("Socket is already closed.");
//            }
//        } catch (IOException e) {
//            if (e instanceof BindException) {
//                System.out.println("Address already in use! Trying port: " + port + 1);
//                ServerStuff(port + 1);
//            } else {
//                e.printStackTrace();
//            }
//        }
//    }
}
