package main;

import db.Db;
import util.Log;

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
    public static final String PREFIX_QUESTION = "QUESTION:";
    public static final String PREFIX_END_OF_QUIZ = "CLIENTENDQUIZ";
    private static final String PREFIX_CLIENT_WANTS_TO_END_QUIZ = "-q";

    private ServerSocket socket;
    private List<ClientModel> clients = new ArrayList<>();
    private ArrayList<Book> quizBooks;

    public Server() {
        Log.s("Fetching books from database, using Config.java's settings..");
        quizBooks = Db.fetchAndParseBooks();

        bindSocketToPort();
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
        // Log.debugLevel = Log.LOG_VERBOSE;
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
                        Book currentBook = quizBooks.get((int) (Math.random() * quizBooks.size()));

                        String message = PREFIX_QUESTION + (client.getCurrentQuestionNumber() == -1 ?
                                "Want to start a quiz? (y/n): " : "Who wrote " + currentBook.getTitle() + "?");
                        output.writeUTF(message);
                        output.flush();
                        Log.v(String.format("Sent message to client #%d: %s", client.getId(), message));

                        final String data = input.readUTF();
                        Log.s(String.format("Got message from client #%d: %s", client.getId(), data));

                        if (client.getCurrentQuestionNumber() == -1) {
                            if (data.toLowerCase().contains("y")){
                                client.setCurrentQuestion(0);
                                Log.s("Client #" + client.getId() + " opted to start quiz.");
                                output.writeUTF("Type \"" + PREFIX_CLIENT_WANTS_TO_END_QUIZ + "\" at any time to end the quiz.");
                                output.flush();
                            } else {
                                output.writeUTF(PREFIX_END_OF_QUIZ);
                                output.flush();
                            }
                        } else if (!data.startsWith(PREFIX_CLIENT_WANTS_TO_END_QUIZ)) {
                            checkClientAnswerAndProvideFeedback(client, output, currentBook, data);
                        } else {
                            clientFinishedQuiz(client, output);
                        }

                    } catch (Exception e) {
                        Log.e("An error occurred: " + e.getMessage());
                    }
                }
            } catch (IOException e) {
                Log.e("An error occurred whilst accessing I/O streams: " + e.getMessage());
            }
        };
    }

    private void checkClientAnswerAndProvideFeedback(ClientModel client, DataOutputStream output, Book currentBook, String answer) throws IOException {
        boolean scored = answer.toLowerCase().contains(currentBook.getAuthorFirstName().toLowerCase()) &&
                answer.toLowerCase().contains(currentBook.getAuthorLastName().toLowerCase());
        Log.s(String.format("Client #%d answered question #%d %scorrectly.",
                client.getId(), client.getCurrentQuestionNumber(), scored ? "" : "in"));

        output.writeUTF(scored ? "Correct!" : "Incorrect - correct answer is " + currentBook.getAuthorFullNameCaps());
        output.flush();

        client.incCurrentQuestion(scored);
    }

    private void clientFinishedQuiz(ClientModel client, DataOutputStream output) throws IOException {
        Log.v(String.format("Client #%d finished quiz with score %d.",
                client.getId(), client.getScore()));

        output.writeUTF(String.format("Quiz completed. Your final score is %d/%d.\n" +
                "Thank you for participating.", client.getScore(), client.getCurrentQuestionNumber()));
        output.writeUTF(PREFIX_END_OF_QUIZ);
        output.flush();
    }

    private void bindSocketToPort() {
        while (true) {
            Log.s("Attempting to bind to port " + Config.SERVER_PORT + ".. ");
            try {
                socket = new ServerSocket(Config.SERVER_PORT);
                Log.s("Success.");
                break;
            } catch (IOException e) {
                if (e instanceof BindException) {
                    Log.s("Port is taken.");
                    Config.SERVER_PORT++;
                } else {
                    Log.e("Encountered unknown error: " + e.getMessage());
                    return;
                }
            }
        }
    }
}