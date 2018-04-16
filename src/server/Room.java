package server;

import com.sun.istack.internal.NotNull;

import java.util.ArrayList;

/**
 * This class represents the game room. All players are allocated in some room.
 */
final class Room {
    /**
     * The room capacity.
     */
    private static final byte CAPACITY;
    /**
     * The maximum number of permitted rooms.
     */
    private static final byte MAX_ROOMS;
    /**
     * .
     * A list of all current instantiated rooms.
     */
    private static final ArrayList<Room> rooms;

    static {
        CAPACITY = 2;
        MAX_ROOMS = 100;
        rooms = new ArrayList<>();
    }

    /**
     * A list of all clients in this room
     */
    private ArrayList<ClientListener> listeners;

    private Room() throws RuntimeException {
        if (rooms.size() == MAX_ROOMS) throw new RuntimeException("The server is full!");
        listeners = new ArrayList<>();
        rooms.add(this);
    }

    /**
     * Returns a new non empty room.
     */
    private static Room getRoom() {
        Room lastRoom = rooms.isEmpty() ? null : rooms.get(rooms.size() - 1);
        // There is no allocated room. Need to allocate a new one.
        if (lastRoom == null) return new Room();
        else {
            // Room is full. Need to allocate a new one.
            if (lastRoom.listeners.size() == CAPACITY) return new Room();
            else return lastRoom;
        }
    }

    static void allocateListener(@NotNull ClientListener listener) {
        Room lastRoom = getRoom();
        lastRoom.listeners.add(listener);
        listener.setClientRoom(lastRoom);
        if (lastRoom.listeners.size() == CAPACITY) {
            // TODO start the game
        }
    }

    /**
     * Send a message to all clients except the given one.
     *
     * @param message        the message to be sent.
     * @param excludedClient the client that won't receive the message.
     */
    void broadcast(@NotNull Object message, @NotNull ClientListener excludedClient) {
        for (ClientListener clientListener : listeners) {
            if (excludedClient == clientListener)
                continue;
            sendMessage(message, clientListener);
        }
    }

    /**
     * Send a message to a single client.
     *
     * @param message        the message to be sent.
     * @param clientListener the client listener that will receive the message
     */
    private void sendMessage(@NotNull Object message, @NotNull ClientListener clientListener) {
        clientListener.getWriter().println(message);
    }
}