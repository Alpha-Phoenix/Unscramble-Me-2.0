package server;

import com.sun.istack.internal.NotNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author      Michael Pacheco <b>pacheco@decom.ufop.br</b>
 * @version     1.0
 *
 * This class is responsible to listen messages of a single client and send them to the server and then to the other clients.
 * */
public class ClientListener implements Runnable {
    /**
     * The socket used to communicate with the delegated client.
     * */
    private Socket clientSocket;

    /**
     * A reference to the {@link GameServer} used to call the {@link GameServer} broadcast method.
     * @see GameServer
     * */
    private GameServer server;

    /**
     * Reader used to read messages sent by the client
     * */
    private BufferedReader reader;

    /**
     * Writer used to send messages to the client
     * */
    private PrintWriter writer;

    /**
     * A {@link Logger} used to print messages for debug purpose.
     * */
    private final static Logger LOGGER = Logger.getLogger(ClientListener.class.getName());

    /**
     * Instantiate a new {@link ClientListener} with a given client socket.
     *
     * @param clientSocket  the socket of the delegated client.
     * @param server        the server reference used to call the broadcast method.
     * */
    ClientListener(@NotNull Socket clientSocket, @NotNull GameServer server) {
        this.clientSocket = clientSocket;
        this.server = server;
        try {
            this.writer = new PrintWriter(clientSocket.getOutputStream(), true);
            this.reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Failed on retrieve data from clientSocket: {0}", e);
            e.printStackTrace();
        }
    }

    PrintWriter getWriter() {
        return writer;
    }

    Socket getClientSocket() {
        return clientSocket;
    }

    /**
     * Listen for client messages and send it to the server.
     * */
    @Override
    public void run() {
        try {
            String message;
            while ((message = reader.readLine()) != null) {
                // send received message to the server
                LOGGER.log(Level.INFO, "Message received!\n\t   From: {0}\n\tMessage: {1}\n",
                        new Object[]{clientSocket, message});
                server.broadcast(message, this);
            }
        } catch (IOException e) {
            if (!clientSocket.isClosed()) {
                LOGGER.log(Level.SEVERE, "Failed on read client message: {0}", e);
                e.printStackTrace();
            }
        } finally {
            // send the client to server to be disconnected
            server.removeClient(this);
        }
    }
}
