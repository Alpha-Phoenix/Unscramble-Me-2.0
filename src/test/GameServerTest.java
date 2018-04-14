package test;

import server.GameServer;

public class GameServerTest {
    public static void main (String[] args) {
        new GameServer(args.length == 1 ? Integer.parseInt(args[0]) : 5000).start();
    }
}
