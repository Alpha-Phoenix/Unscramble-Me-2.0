package client;

import com.sun.istack.internal.NotNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

/**
 * @author Michael Pacheco
 * @version 1.0
 * This class is responsible to read messages sent by the server and show them to the client.
 */
public class ClientReaderThread implements Runnable {
    /**
     * The socket used to read messages sent by the server.
     */
    private Socket serverSocket;

    ClientReaderThread(@NotNull Socket serverSocket) {
        this.serverSocket = serverSocket;
    }

    /**
     * Read messages sent by the server.
     * */
    @Override
    public void run() {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(serverSocket.getInputStream()))) {
            String message;
            while ((message = reader.readLine()) != null) System.out.println(message);
        } catch (IOException e) {
            if (!serverSocket.isClosed())
                e.printStackTrace();
        }
    }
}
