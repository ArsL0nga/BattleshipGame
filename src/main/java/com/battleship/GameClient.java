package com.battleship;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;

import static com.battleship.DataTypes.FieldState.EMPTY;
import static com.battleship.Game.GAMEBOARD_SIZE;

public class GameClient {
    private Socket socket;
    private Player player;
    int playerNumber = 0;
    private BufferedReader bufferedReader;
    private BufferedWriter bufferedWriter;

    public GameClient(Socket socket, String username) {
        try {
            this.socket = socket;
            this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.bufferedWriter= new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            this.player = new Player(username);
            sendMessageToServer(username);
        } catch (IOException e) {
            closeGameClient();
        }
    }

    private void sendMessageToServer(String msg) {
        try {
            bufferedWriter.write(msg);
            bufferedWriter.newLine();
            bufferedWriter.flush();
        } catch (IOException e) {
            closeGameClient();
        }
    }

    public void listenForMessage() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String msg;
                while (!socket.isClosed()) {
                    try {
                        msg = bufferedReader.readLine();
                        handleMessage(msg);
                    } catch (IOException e) {
                        closeGameClient();
                    }
                }
            }
        }).start();
    }

    private void handleMessage(String msg) {
        if (msg.startsWith("PLAYER")) {
            System.out.println(msg);
            playerNumber = Integer.parseInt(msg.substring(msg.indexOf(":") + 1));
            placeShips();
        } else if (msg.startsWith("SHOT_RESULT")) {
            if (!msg.contains("ERROR")) Application.clearScreen();
            System.out.println(msg);
        } else if (msg.startsWith("GAMEBOARDS")) {
            if (playerNumber == 2) {
                player.showGameboards(msg.substring(msg.indexOf(":") + 1), true, true);
            } else if (playerNumber == 1){
                player.showGameboards(msg.substring(msg.indexOf(":") + 1), false, true);
            } else {
                player.showGameboards(msg.substring(msg.indexOf(":") + 1), false, false);
            }
        } else if (msg.startsWith("TURN")) {
            if (msg.substring(msg.indexOf(":") + 1).equals(player.getName())) {
                shoot();
            }
        } else if (msg.startsWith("GAME_OVER")) {
            System.out.println(msg);
            System.out.println("Enter anything to return to the main menu!");
            new Scanner(System.in).next();
            closeGameClient();
        }
    }

    private void placeShips() {
        ArrayList<ArrayList<Field>> gameboard = create_empty_gameboard();
        player.placeShips(gameboard);
        sendMessageToServer("SHIPS_PLACED:" + gameboardToString(gameboard));
        sendMessageToServer("SHIPS_INFO:" + shipsToString(player.getShips()));
    }

    private ArrayList<ArrayList<Field>> create_empty_gameboard() {
        ArrayList<ArrayList<Field>> gameboard = new ArrayList<>();
        for (int i = 0; i < GAMEBOARD_SIZE; i++) {
            gameboard.add(new ArrayList<Field>(GAMEBOARD_SIZE));
            for (int j = 0; j < GAMEBOARD_SIZE; j++) {
                gameboard.get(i).add(new Field(EMPTY));
            }
        }
        return gameboard;
    }

    private String gameboardToString(ArrayList<ArrayList<Field>> gameboard) {
        StringBuilder gameboardString = new StringBuilder();
        for (ArrayList<Field> row : gameboard) {
            for (Field field : row) {
                gameboardString.append(field.toString());
                gameboardString.append(",");
            }
        }
        return new String(gameboardString);
    }

    private String shipsToString(ArrayList<Ship> ships) {
        StringBuilder shipsString = new StringBuilder();
        for (Ship ship : ships) {
            shipsString.append(ship.getDirection().ordinal() + " ");
            shipsString.append(ship.getSize() + " ");
            shipsString.append(ship.getStartX() + " ");
            shipsString.append(ship.getStartY() + ";");
        }
        return new String(shipsString);
    }

    private void shoot() {
        int[] coordinates = new int[2];
        while (!player.getShotPositionFromUser(coordinates)) {};
        sendMessageToServer("SHOT:" + coordinates[0] + " " + coordinates[1]);
    }

    private void closeGameClient() {
        try {
            if (bufferedReader != null) bufferedReader.close();
            if (bufferedWriter != null) bufferedWriter.close();
            if (socket != null) socket.close();
            Application.mainMenu();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
