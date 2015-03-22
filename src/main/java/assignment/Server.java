package assignment;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.BindException;
import java.net.ServerSocket;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    public static final int COMMUNCATION_PORT = 3000;
    public static final String PREFIX_QUESTION = "QUESTION:";
    public static final String PREFIX_END_OF_QUIZ = "CLIENTENDQUIZ";
    private static final String PREFIX_CLIENT_WANTS_TO_END_QUIZ = "-q";

    private ServerSocket socket;
    private List<ClientModel> clients = new ArrayList<>();
    private List<Book> quizBooks = new ArrayList<>();

    public Server() {
        Log.s("Fetching books from database, using Config.java's settings..");
        fetchAndParseQuizContent(quizBooks);

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

    public static void fetchAndParseQuizContent(List<Book> listToFill) {
        try (ConnectToDB dbConnection = new ConnectToDB(Config.DB_HOST, Config.DB_NAME, Config.DB_USER, Config.DB_PASS)) {
            ResultSet result = dbConnection
                    .getConnection()
                    .prepareStatement("SELECT * FROM " + Config.DB_TABLE_NAME)
                    .executeQuery();

            while (result.next()) {
                listToFill.add(new Book(
                        result.getInt(result.findColumn("id")),
                        result.getString(result.findColumn("author")),
                        result.getString(result.findColumn("title")),
                        result.getString(result.findColumn("ISBN")),
                        result.getInt(result.findColumn("pages")),
                        result.getInt(result.findColumn("released"))
                ));
            }
        } catch (Exception e) {
            Log.e("Error upon connecting to database: " + e.getMessage());
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
                        String message = PREFIX_QUESTION + (client.getCurrentQuestion() == -1 ?
                                "Want to start a quiz? (y/n): " : "Who wrote " + currentBook.getTitle() + "?");
                        output.writeUTF(message);
                        output.flush();
                        Log.v(String.format("Sent message to client #%d: %s", client.getId(), message));

                        final String data = input.readUTF();
                        Log.s(String.format("Got message from client #%d: %s", client.getId(), data));
                        if (client.getCurrentQuestion() == -1) {
                            if (data.toLowerCase().contains("y")) { // TODO: externalize to GUI (controller)
                                client.setCurrentQuestion(0);
                                Log.s("Client #" + client.getId() + " opted to start quiz.");
                                output.writeUTF("Type \"" + PREFIX_CLIENT_WANTS_TO_END_QUIZ + "\" at any time to end the quiz.");
                            }
                        } else if (!data.startsWith(PREFIX_CLIENT_WANTS_TO_END_QUIZ)) {
                            boolean scored = data.toLowerCase().contains(currentBook.getAuthor().toLowerCase());
                            Log.s(String.format("Client #%d answered question #%d %scorrectly.",
                                    client.getId(), client.getCurrentQuestion(), scored ? "" : "in"));

                            output.writeUTF(scored ? "Correct!" : "Incorrect - correct answer is " +
                                    currentBook.getAuthor().toUpperCase());
                            output.flush();

                            client.incCurrentQuestion(scored);
                        } else {
                            Log.v(String.format("Client #%d finished quiz with score %d.",
                                    client.getId(), client.getScore()));

                            output.writeUTF(String.format("Quiz completed. Your final score is %d/%d.\n" +
                                    "Thank you for participating.", client.getScore(), client.getCurrentQuestion()));
                            output.writeUTF(PREFIX_END_OF_QUIZ);
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