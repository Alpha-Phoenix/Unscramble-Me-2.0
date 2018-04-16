/*
* This file contains the application core server responsible to wait and accept clients connections requests.
* */

package server;

import com.sun.istack.internal.NotNull;

import java.io.IOException;
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
     * A hash set to store the connected clients
     * */
    private HashSet<ClientListener> clientListeners;

    private GameServer() {
        clientListeners = new HashSet<>();
    }

    /**
     * Instantiates a new {@link GameServer} with a given port number.
     * @param port the port number used to start the server.
     * */
    private GameServer(int port) {
        this();
        this.port = port;
    }

    /**
     * Override method from Runnable. This method is called when an attempt to close the application occur.
     * */
    @Override
    public void run() {
        Scanner s = new Scanner(System.in);
        while (s.hasNext()) s.nextLine();
        shutdown();
    }

    public static void main(String[] args) {
        new GameServer(args.length == 1 ? Integer.parseInt(args[0]) : 5000).start();
    }

    /**
     * Wait for clients connections requests
     * */
    private void waitForConnections() {
        new Thread(this).start();

        try {
            //noinspection InfiniteLoopStatement
            while (true) {
                Socket clientSocket = serverSocket.accept();
                LOGGER.log(Level.INFO, "New client connected! {0}\n", clientSocket);
                clientSocket.getOutputStream().write("You're now connected to the server\n".getBytes());
                clientSocket.getOutputStream().flush();
                allocateClient(clientSocket);
            }
        } catch (IOException e) {
            // No need for printing stacktrace if the serverSocket was closed by the shutdown method
            if (!serverSocket.isClosed())
                e.printStackTrace();
        }
    }

    /**
     * This method is responsible to delegate the communication with the client to the {@link ClientListener}.
     * @param clientSocket the client socket to delegate.
     * */
    private void allocateClient(@NotNull Socket clientSocket) {
        ClientListener listener = new ClientListener(clientSocket, this);
        clientListeners.add(listener);
        broadcast(String.format("New user connected! %s\n", clientSocket), listener);
        new Thread(listener).start();
    }

    /**
     * Shutdown the server
     * */
    private void shutdown () {
        try {
            LOGGER.log(Level.INFO, "Trying to shutdown the server...\n");
            while (clientListeners.iterator().hasNext()) removeClient(clientListeners.iterator().next());
            serverSocket.close();
            LOGGER.log(Level.INFO, "Server successfully shut down!\n");
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to shutdown the server! {0}\n", e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Send a message to a single client.
     * @param message the message to be sent.
     * @param clientListener the client listener that will receive the message
     * */
    private void sendMessage (@NotNull Object message, @NotNull ClientListener clientListener) {
        clientListener.getWriter().println(message);
    }

    /**
     * Send a message to all clients except the given one.
     * @param message           the message to be sent.
     * @param excludedClient    the client that won't receive the message.
     * */
    void broadcast (@NotNull Object message, @NotNull ClientListener excludedClient) {
        for (ClientListener clientListener : clientListeners) {
            if (excludedClient == clientListener)
                continue;
            sendMessage(message, clientListener);
        }
    }

    /**
     * Remove the given client from server.
     * @param clientListener the client to be removed.
     * */
    void removeClient (@NotNull ClientListener clientListener) {
        try {
            clientListener.getWriter().println("You're now disconnected to the server! Press Ctrl-D to exit...");
            clientListener.getClientSocket().close();
            if(clientListeners.remove(clientListener)) {
                LOGGER.log(Level.INFO, "Client disconnected! {0}\n", clientListener.getClientSocket());
                broadcast(String.format("Client disconnected! %s\n", clientListener.getClientSocket()), null);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Start the server and listen for clients connections requests.
     */
    private void start() {
        try {
            LOGGER.log(Level.INFO, "Trying to start the server...\n");
            serverSocket = new ServerSocket(this.port);
            final String ip = InetAddress.getLocalHost().getHostAddress();
            LOGGER.log(Level.INFO, "Server started!\n\tPort: {0}\n\t  IP: {1}\n", new Object[]{port, ip});
            LOGGER.log(Level.INFO, "Press Ctrl-D to shutdown the server!\n");
            waitForConnections();
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to initialize the server! {0}\n", e.getMessage());
            e.printStackTrace();
        }
    }
}
