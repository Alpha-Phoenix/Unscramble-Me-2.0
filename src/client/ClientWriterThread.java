package client;

import com.sun.istack.internal.NotNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * @author Michael Pacheco
 * @version 1.0
 * This class is responsible to read client messages and send it to the server.
 * */
public class ClientWriterThread implements Runnable {
    /**
     * The socket used to send messages to the server.
     * */
    private Socket serverSocket;

    /**
     * Instantiate a new {@link ClientReaderThread} with a given server socket.
     * @param serverSocket the socket used to send messages to the server.
     * */
    ClientWriterThread(@NotNull Socket serverSocket) {
        this.serverSocket = serverSocket;
    }

    /**
     * Read messages typed by the client and send it to the server.
     * */
    @Override
    public void run() {
        try {Thread.sleep(1000);}
        catch (InterruptedException e) {e.printStackTrace();}

        BufferedReader keyboardReader = null;
        try (PrintWriter socketWriter = new PrintWriter(serverSocket.getOutputStream(), true)) {
            keyboardReader = new BufferedReader(new InputStreamReader(System.in));
            String line;
            while ((line = keyboardReader.readLine()) != null) socketWriter.write(line);
        } catch (IOException e) {
            if (!serverSocket.isClosed())
                e.printStackTrace();
        } finally {
            try {
                if (keyboardReader != null) keyboardReader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
