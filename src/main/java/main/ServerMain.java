package main;

import models.Server;

public class ServerMain {
    /**
     * The main method of the program, which controls its execution flow.
     *
     * @param args Not used.
     */
    public static void main(String[] args) {
        try (Server server = new Server()) {
            server.start();
        }
    }
}
