package client;

import java.io.IOException;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

public class GameClient {
    public static void main(String[] args) {
        final String serverAddress = args.length == 2 ? args[0] : "localhost";
        final int port = args.length == 2 ? Integer.parseInt(args[1]) : 5000;
        final Logger LOGGER = Logger.getLogger(GameClient.class.getName());

        try {
            Socket serverSocket = new Socket(serverAddress, port);
            LOGGER.log(Level.INFO, "Connection successful! {0}\n", serverSocket);
            new Thread(new ClientWriterThread(serverSocket)).start();
            new Thread(new ClientReaderThread(serverSocket)).start();
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE,"Failed to connect with the server\n", e);
            e.printStackTrace();
        }
    }
}
