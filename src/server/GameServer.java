/*
* This file contains the application core server responsible to wait and accept clients connections requests.
* */

package server;

import com.sun.istack.internal.NotNull;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author      Michael Pacheco <b>pacheco@decom.ufop.br</b>
 * @version     1.0
 *
 * This class is responsible to receive clients connections and send messages to them.
 * */
public class GameServer implements Runnable {
    /**
     * The port number used to start the server.
     * */
    private int port;

    /**
     * The socket used to accept clients connections.
     * */
    private ServerSocket serverSocket;

    /**
     * A {@link Logger} used to print messages for debug purpose.
     * */
    private final static Logger LOGGER = Logger.getLogger(GameServer.class.getName());

    /**
     * A hash set to store the clients sockets
     * */
    private HashSet<Socket> clientsSockets;

    private GameServer() {
        this.clientsSockets = new HashSet<>();
    }

    /**
     * Instantiates a new {@link GameServer} with a given port number.
     * @param port the port number used to start the server.
     * */
    public GameServer(int port) {
        this();
        this.port = port;
    }

    /**
     * Override method from Runnable. This method is called when an attempt to close the application occur.
     * */
    @Override
    public void run() {
        Scanner s = new Scanner(System.in);
        while (s.hasNext())
            s.nextLine();

        shutdown();
    }

    /**
     * Start the server and listen for clients connections requests.
     * */
    public void start () {
        try {
            LOGGER.log(Level.INFO, "Trying to start the server...\n");
            this.serverSocket = new ServerSocket(this.port);
            final String ip = InetAddress.getLocalHost().getHostAddress();
            LOGGER.log(Level.INFO, "Server started!\n\tPort: {0}\n\t  IP: {1}\n", new Object[] {this.port, ip});
            LOGGER.log(Level.INFO, "Press Ctrl-D to shutdown the server!\n");
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to initialize the server!\n", e.getMessage());
            e.printStackTrace();
        }

        waitForConnections();
    }

    /**
     * Wait for clients connections requests
     * */
    private void waitForConnections() {
        new Thread(this).start();

        try {
            //noinspection InfiniteLoopStatement
            while (true) {
                Socket userSocket = this.serverSocket.accept();
                LOGGER.log(Level.INFO, "New user connected!\n");
                allocateClient(userSocket);
            }
        } catch (IOException e) {
            // The user socket was closed by the shutdown method, so, no need for printing the stack trace.
            //e.printStackTrace();
        }
    }

    /**
     * This method is responsible to delegate the communication with the user to the {@link ClientListener}.
     * @param userSocket the user socket to delegate.
     * */
    private void allocateClient(Socket userSocket) {
        clientsSockets.add(userSocket);
        new Thread(new ClientListener(userSocket, this)).start();
    }

    /**
     * Shutdown the server
     * */
    private void shutdown () {
        try {
            LOGGER.log(Level.INFO, "Trying to shutdown the server...\n");

            // TODO Clear resources
            for (Socket soc : this.clientsSockets)
                removeClient(soc);

            serverSocket.close();

            LOGGER.log(Level.INFO, "Server successfully shut down!\n");
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to shutdown the server!\n", e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Send a message to a single client.
     * @param message the message to be sent.
     * @param clientSocket the socket of the client that will receive the message
     * */
    private void sendMessage (Object message, Socket clientSocket) {
        try (PrintWriter writer = new PrintWriter(clientSocket.getOutputStream())) {
            writer.println(message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Send a message to all clients except the given one.
     * @param message           the message to be sent.
     * @param excludedClient    the client that won't receive the message.
     * */
    void broadcast (Object message, Socket excludedClient) {
        for (Socket client : this.clientsSockets) {
            if (excludedClient == client)
                continue;
            this.sendMessage(message, client);
        }
    }

    /**
     * Remove the given client from server.
     * @param clientSocket the client to be removed.
     * */
    void removeClient (@NotNull Socket clientSocket) {
        try {
            clientSocket.close();
            this.clientsSockets.remove(clientSocket);
            // TODO broadcast the user disconnection
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
