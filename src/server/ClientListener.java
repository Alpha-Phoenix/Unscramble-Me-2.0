package server;

import com.sun.istack.internal.NotNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author      Michael Pacheco <b>pacheco@decom.ufop.br</b>
 * @version     1.0
 *
 * This class is responsible to listen messages of a single user and send them to the server and then to the other clients.
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
    }

    /**
     * Listen for client messages and send it to the server.
     * */
    @Override
    public void run() {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(this.clientSocket.getInputStream()))) {
            String message;
            while ((message = reader.readLine()) != null) {
                // TODO send received message to the server
                LOGGER.log(Level.INFO, "Message received!");
                server.broadcast(message, clientSocket);
            }
            // TODO send the client to server to be disconnected
            server.removeClient(this.clientSocket);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
