package com.battleship;

import com.battleship.DataTypes.*;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

import static com.battleship.Game.GAMEBOARD_SIZE;

public class ClientHandler implements Runnable{
    private GameServer gameServer;

    public static ArrayList<ClientHandler> clientHandlers = new ArrayList<>();
    private Socket socket;
    private BufferedReader bufferedReader;
    private BufferedWriter bufferedWriter;
    private String clientUsername;

    public ClientHandler(GameServer gameServer, Socket socket) {
        try {
            this.gameServer = gameServer;
            this.socket = socket;
            bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.clientUsername = bufferedReader.readLine();
            clientHandlers.add(this);

            if (gameServer.getGame().getPlayer1() == null) {
                gameServer.getGame().setPlayer1(new Player(clientUsername, PlayerType.PLAYER));
                sendMessage("PLAYER:1");
            } else if (gameServer.getGame().getPlayer2() == null) {
                gameServer.getGame().setPlayer2(new Player(clientUsername, PlayerType.PLAYER));
                sendMessage("PLAYER:2");
            } else {
                gameServer.getSpectators().add(new Player(clientUsername, PlayerType.SPECTATOR));
                sendMessage("SPECTATOR");
                sendMessage("GAMEBOARDS:" + gameboardsToString());
            }
        } catch (IOException e) {
            closeClientHandler();
        }
    }

    @Override
    public void run() {
        String messageFromClient;
        while (!socket.isClosed()) {
            try {
                messageFromClient = bufferedReader.readLine();
                if (messageFromClient == null) {
                    closeClientHandler();
                    return;
                }
                if (messageFromClient.startsWith("SHIPS_PLACED")) {
                    stringToGameboard(clientUsername, messageFromClient);
                } else if (messageFromClient.startsWith("SHIPS_INFO")) {
                    stringToShips(clientUsername, messageFromClient);
                    if (isGameReady()) {
                        gameServer.getGame().setState(GameState.PLAYER1TURN);
                        broadcastMessage("TURN:" + gameServer.getGame().getPlayer1().getName());
                    }
                } else if (messageFromClient.startsWith("SHOT")) {
                    handlePlayerShot(messageFromClient);
                }
            } catch (IOException e) {
                closeClientHandler();
                break;
            }
        }
    }

    private void sendMessage(String msg) {
        try {
            bufferedWriter.write(msg);
            bufferedWriter.newLine();
            bufferedWriter.flush();
        } catch (IOException e) {
            closeClientHandler();
        }
    }

    private void broadcastMessage(String msg) {
        for (ClientHandler clientHandler : clientHandlers) {
            clientHandler.sendMessage(msg);
        }
    }

    private boolean isGameReady() {
        if (gameServer.getGame().getPlayer1() != null && gameServer.getGame().getPlayer2() != null) {
            return !gameServer.getGame().getPlayer1().getShips().isEmpty() && !gameServer.getGame().getPlayer2().getShips().isEmpty();
        }
        return false;
    }

    private void handlePlayerShot(String coordinatesStr) {
        coordinatesStr = coordinatesStr.substring(coordinatesStr.indexOf(":") + 1);
        String[] shotCoordinates = coordinatesStr.split(" ");
        int[] coordinates = new int[] {Integer.parseInt(shotCoordinates[0]), Integer.parseInt(shotCoordinates[1])};

        ShootResult shootResult = gameServer.getGame().shoot(coordinates);
        switch (shootResult) {
            case MISSED, HIT, DESTROYED -> {
                broadcastMessage("SHOT_RESULT: " + shootResult + "!");
                broadcastMessage("GAMEBOARDS:" + gameboardsToString());
                if (gameServer.getGame().getState() == GameState.FINISHED) {
                    broadcastMessage("GAME_OVER! Winner is:" + gameServer.getGame().getWinner());
                    closeClientHandler();
                    return;
                }
                broadcastMessage("TURN:" + (gameServer.getGame().getState() == GameState.PLAYER1TURN ?
                        gameServer.getGame().getPlayer1().getName() : gameServer.getGame().getPlayer2().getName()));
            }
            case ERROR -> {
                sendMessage("SHOT_RESULT:ERROR! Try again!");
                sendMessage("TURN:" + (gameServer.getGame().getState() == GameState.PLAYER1TURN ?
                        gameServer.getGame().getPlayer1().getName() : gameServer.getGame().getPlayer2().getName()));
            }
        }
    }

    private void stringToGameboard(String username, String stringGameboard) {
        stringGameboard = stringGameboard.substring(stringGameboard.indexOf(":") + 1);
        String[] fields = stringGameboard.split(",");
        ArrayList<ArrayList<Field>> gameboard = username.equals(gameServer.getGame().getPlayer1().getName()) ?
                gameServer.getGame().getPlayer1Gameboard() : gameServer.getGame().getPlayer2Gameboard();

        for (int i = 0; i < GAMEBOARD_SIZE; i++) {
            for (int j = 0; j < GAMEBOARD_SIZE; j++) {
                gameboard.get(i).set(j, new Field(Field.valueOf(fields[i*GAMEBOARD_SIZE + j])));
            }
        }
    }

    private void stringToShips(String username, String stringShips) {
        stringShips = stringShips.substring(stringShips.indexOf(":") + 1);
        String[] shipsInfo = stringShips.split(";");
        ArrayList<Ship> ships = username.equals(gameServer.getGame().getPlayer1().getName()) ?
                gameServer.getGame().getPlayer1().getShips() : gameServer.getGame().getPlayer2().getShips();

        for (String shipInfo : shipsInfo) {
            String[] shipValues = shipInfo.split(" ");
            ShipDirection direction = ShipDirection.values()[Integer.parseInt(shipValues[0])];
            int size = Integer.parseInt(shipValues[1]);
            int startX = Integer.parseInt(shipValues[2]);
            int startY = Integer.parseInt(shipValues[3]);
            ships.add(new Ship(direction, size, startX, startY));
        }
    }

    private String gameboardsToString() {
        StringBuilder gameboardsInfo = new StringBuilder();
        ArrayList<ArrayList<Field>> player1Gameboard = gameServer.getGame().getPlayer1Gameboard();
        ArrayList<ArrayList<Field>> player2Gameboard = gameServer.getGame().getPlayer2Gameboard();

        for (int i = 0; i < GAMEBOARD_SIZE; i++) {
            for (int j = 0; j < GAMEBOARD_SIZE; j++) {
                gameboardsInfo.append(player1Gameboard.get(i).get(j));
            }
            for (int j = 0; j < GAMEBOARD_SIZE; j++) {
                gameboardsInfo.append(player2Gameboard.get(i).get(j));
            }
        }
        return new String(gameboardsInfo);
    }

    private void closeClientHandler() {
        clientHandlers.remove(this);
        try {
            if (bufferedReader != null) bufferedReader.close();
            if (bufferedWriter != null) bufferedWriter.close();
            if (socket != null) socket.close();
            if (clientHandlers.isEmpty()) gameServer.closeServerSocket();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
