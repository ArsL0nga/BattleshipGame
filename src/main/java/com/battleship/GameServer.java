package com.battleship;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class GameServer implements Runnable {
    private ServerSocket serverSocket;
    private MultiplayerGame game;
    private ArrayList<Player> spectators = new ArrayList<>();

    public GameServer(ServerSocket serverSocket) {
        this.serverSocket = serverSocket;
        game = new MultiplayerGame();
    }

    public MultiplayerGame getGame() {
        return game;
    }
    public ArrayList<Player> getSpectators() {
        return spectators;
    }

    public void closeServerSocket() {
        try {
            if (serverSocket != null) {
                serverSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        try {
            while (!serverSocket.isClosed()) {
                Socket socket = serverSocket.accept();
                ClientHandler clientHandler = new ClientHandler(this, socket);
                Thread clientThread = new Thread(clientHandler);
                clientThread.start();
            }
        } catch (IOException e) {
            closeServerSocket();
        }
    }
}
