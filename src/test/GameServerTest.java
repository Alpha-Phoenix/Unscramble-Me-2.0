package test;

import server.GameServer;

public class GameServerTest {
    public static void main (String[] args) {
        new GameServer(5000).start();
    }
}
