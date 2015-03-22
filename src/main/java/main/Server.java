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

public class Server implements AutoCloseable {
    public static final String PREFIX_QUESTION = "QUESTION:";
    public static final String PREFIX_END_OF_QUIZ = "CLIENTENDQUIZ";
    private static final String PREFIX_CLIENT_WANTS_TO_END_QUIZ = "-q";
    ServerSocket socket;
    private List<ClientModel> clients = new ArrayList<>();
    private ArrayList<Book> quizBooks;

    /**
     * The constructor for the class.
     */
    public Server() {
        Log.s("Fetching books from database, using Config.java's settings..");
        quizBooks = Db.fetchAndParseBooks();

        socket = getSocketBoundToAvailablePort();
        if (socket == null) {
            Log.e("Socket initialization failed, cannot continue.");
        }
    }

    public static void main(String[] args) {
        // Log.debugLevel = Log.LOG_VERBOSE;
        try (Server server = new Server()) {
            server.start();
        }
    }

    private void start() {
        ExecutorService clientThreads = Executors.newCachedThreadPool();
        Log.s("Awaiting client connections..");
        while (true) {
            try {
                clients.add(new ClientModel(clients.size() + 1, socket.accept()));
                clientThreads.execute(newClientThread());
            } catch (IOException e) {
                Log.e("An error occurred whilst waiting for connections:\n" + e.getMessage());
            }

            Log.s("Accepted client #" + clients.size());
        }
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
                        messageClient(output, PREFIX_QUESTION + (client.getCurrentQuestionNumber() == -1 ?
                                "Want to start a quiz? (y/n): " : "Who wrote " + currentBook.getTitle() + "?"));

                        final String data = input.readUTF();
                        Log.s(String.format("Got message from client #%d: %s", client.getId(), data));

                        if (client.getCurrentQuestionNumber() == -1) {
                            if (data.toLowerCase().contains("y")) {
                                client.setCurrentQuestion(0);
                                Log.s("Client #" + client.getId() + " opted to start quiz.");
                                messageClient(output, "Type \"" + PREFIX_CLIENT_WANTS_TO_END_QUIZ + "\" at any time to end the quiz.");
                            } else {
                                messageClient(output, "Some other time, then. Good bye!");
                                messageClient(output, PREFIX_END_OF_QUIZ);
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

    private void messageClient(DataOutputStream output, String message) throws IOException {
        output.writeUTF(message);
        output.flush();
    }

    private void checkClientAnswerAndProvideFeedback(ClientModel client, DataOutputStream output, Book currentBook, String answer) throws IOException {
        boolean scored = answer.toLowerCase().contains(currentBook.getAuthorFirstName().toLowerCase()) &&
                answer.toLowerCase().contains(currentBook.getAuthorLastName().toLowerCase());

        Log.s(String.format("Client #%d answered question #%d %scorrectly.",
                client.getId(), client.getCurrentQuestionNumber(), scored ? "" : "in"));
        messageClient(output, scored ? "Correct!" :
                "Incorrect - correct answer is " + currentBook.getAuthorFullNameCaps());
        client.incCurrentQuestion(scored);
    }

    private void clientFinishedQuiz(ClientModel client, DataOutputStream output) throws IOException {
        Log.v(String.format("Client #%d finished quiz with score %d.",
                client.getId(), client.getScore()));

        messageClient(output, String.format("Quiz completed. Your final score is %d/%d.\n" +
                "Thank you for participating.", client.getScore(), client.getCurrentQuestionNumber()));
        messageClient(output, PREFIX_END_OF_QUIZ);
    }

    private ServerSocket getSocketBoundToAvailablePort() {
        ServerSocket serverSocket;
        while (true) {
            Log.s("Attempting to bind to port " + Config.SERVER_PORT + ".. ");
            try {
                serverSocket = new ServerSocket(Config.SERVER_PORT);
                Log.s("Success.");
                return serverSocket;
            } catch (IOException e) {
                if (e instanceof BindException) {
                    Log.s("Port is taken.");
                    Config.SERVER_PORT++;
                } else {
                    Log.e("Encountered unknown error: " + e.getMessage());
                    return null;
                }
            }
        }
    }

    @Override
    public void close() {
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException e) {
            Log.e("Couldn't close socket: " + e.getMessage());
        }
    }
}