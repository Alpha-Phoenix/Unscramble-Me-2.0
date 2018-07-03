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

    private Room clientRoom;

    private int remainingGuesses;

    /**
     * Instantiate a new {@link ClientListener} with a given client socket.
     *
     * @param clientSocket  the socket of the delegated client.
     * @param server        the server reference used to call the broadcast method.
     * */
    ClientListener(@NotNull Socket clientSocket, @NotNull GameServer server) {
        this.clientSocket = clientSocket;
        this.server = server;
        this.remainingGuesses = 5;
        try {
            this.writer = new PrintWriter(clientSocket.getOutputStream(), true);
            this.reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Failed on retrieve data from clientSocket: {0}", e);
            e.printStackTrace();
        }
    }

    Room getClientRoom() {
        return clientRoom;
    }

    void setClientRoom(Room clientRoom) {
        this.clientRoom = clientRoom;
    }

    PrintWriter getWriter() {
        return writer;
    }

    Socket getClientSocket() {
        return clientSocket;
    }

    GameServer getServer() {
        return server;
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
                LOGGER.log(Level.INFO, "Message received!\n\t   From: {0}\n\tMessage: {1}\n", new Object[]{clientSocket, message});

                String word = clientRoom.getWord();
                if (word != null && word.equals(message)) {
                    clientRoom.broadcast(String.format("Player %s wins: %s", clientSocket, message), null);
                    clientRoom.destroy();
                } else {
                    clientRoom.broadcast(String.format("Client %s missed the guess: %s", clientSocket, message), this);
                    clientRoom.sendMessage("Wrong! Try again...", this);
                    this.remainingGuesses--;
                    if (this.remainingGuesses == 0) {
                        clientRoom.sendMessage("Your attempts have ended!", this);
                        clientRoom.removeListener(this);
                        server.removeClient(this);
                    }
                    clientRoom.sendMessage("Remaining guesses: " + remainingGuesses, this);
                }
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
