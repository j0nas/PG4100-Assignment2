package assignment;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.BindException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    public static final int COMMUNCATION_PORT = 3000;
    public static final String STATUS_END_OF_QUIZ = "STATUS_END_OF_QUIZ";
    public static final String QUESTION_PREFIX = "QUESTION:";

    private ServerSocket socket;
    private List<ClientModel> clients = new ArrayList<>();


    private String[] questions = new String[]{"Question 1", "Question 2", "Question 3", "Question 4", "Question 5"};
    private String[] answers = new String[]{"ans1", "ans2", "ans3", "ans4", "ans5"};

    public Server() {
        bindSocketToPort(COMMUNCATION_PORT);
        ExecutorService threadPool = Executors.newCachedThreadPool();

        Log.s("Awaiting client connections..");
        while (true) {
            try {
                clients.add(new ClientModel(clients.size() + 1, socket.accept()));
                threadPool.execute(newClientThread());
            } catch (IOException e) {
                Log.e("An error occurred whilst waiting for connections:\n" + e.getMessage());
            }

            Log.s("Accepted client #" + clients.size());
        }
    }

    public static void main(String[] args) {
        Log.debugLevel = Log.LOG_VERBOSE;
        new Server();
    }

    private Runnable newClientThread() {
        return () -> {
            ClientModel client = clients.get(clients.size() - 1);
            try (DataInputStream input = new DataInputStream(client.getSocket().getInputStream());
                 DataOutputStream output = new DataOutputStream(client.getSocket().getOutputStream())
            ) {
                while (true) {
                    try {
                        String message = QUESTION_PREFIX + (client.getCurrentQuestion() == -1 ?
                                "Want to start a quiz? (y/n): " : questions[client.getCurrentQuestion()]);
                        output.writeUTF(message);
                        output.flush();
                        Log.v(String.format("Sent message to client #%d: %s", client.getId(), message));

                        final String data = input.readUTF();
                        Log.s(String.format("Got message from client #%d: %s", client.getId(), data));
                        if (client.getCurrentQuestion() == -1) {
                            if (data.toLowerCase().contains("y")) { // TODO: externalize to GUI (controller)
                                client.setCurrentQuestion(0);
                                Log.s("Client #" + client.getId() + " opted to start quiz.");
                            }
                        } else if (client.getCurrentQuestion() < questions.length - 1) {

                            boolean scored = data.toLowerCase().contains(answers[client.getCurrentQuestion()].toLowerCase());
                            Log.s(String.format("Client #%d answered question #%d %scorrectly.",
                                    client.getId(), client.getCurrentQuestion(), scored ? "" : "in"));

                            output.writeUTF(scored ? "Correct!" : "Incorrect - correct answer is " +
                                    answers[client.getCurrentQuestion()].toUpperCase());
                            output.flush();

                            client.incCurrentQuestion(scored);
                        } else {
                            Log.v(String.format("Client #%d finished quiz with score %d.",
                                    client.getId(), client.getScore()));

                            output.writeUTF(String.format("Quiz completed. Your final score is %d/%d.\n" +
                                    "Thank you for participating.", client.getScore(), questions.length));
                            output.writeUTF(STATUS_END_OF_QUIZ);
                            output.flush();
                        }

                    } catch (Exception e) {
                        Log.e("An error occurred: " + e.getMessage());
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        };
    }

    private void bindSocketToPort(int port) {
        while (true) {
            Log.s("Attempting to bind to port " + port + ".. ");
            try {
                socket = new ServerSocket(port);
                Log.s("Success.");
                break;
            } catch (IOException e) {
                if (e instanceof BindException) {
                    Log.s("Port is taken.");
                    port++;
                } else {
                    e.printStackTrace();
                    return;
                }
            }
        }
    }
}