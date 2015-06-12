package main;

import config.Config;
import models.Client;
import org.apache.logging.log4j.LogManager;

import java.io.IOException;
import java.net.InetAddress;

public class ClientMain {
    /**
     * Main method, controlling the flow of execution for the class.
     *
     * @param args not used.
     */
    public static void main(String[] args) {
        try (Client client = new Client(InetAddress.getLocalHost(), Config.SERVER_PORT)) {
            client.start();
        } catch (IOException e) {
            LogManager.getLogger(Config.LOG_CLIENT).error("Error when connecting to server: " + e.getMessage());
        }
    }
}
