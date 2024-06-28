package com.battleship;

import com.battleship.DataTypes.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

import static com.battleship.DataTypes.FieldState.EMPTY;
import static com.battleship.DataTypes.FieldState.MISS;
import static com.battleship.DataTypes.ShootResult.*;
import static com.battleship.DataTypes.ShootResult.ERROR;

public class Game {
    public static final int GAMEBOARD_SIZE = 16;

    private int gameId;
    private Player player1;
    private Player player2;
    private ArrayList<ArrayList<Field>> player1Gameboard = new ArrayList<>();
    private ArrayList<ArrayList<Field>> player2Gameboard = new ArrayList<>();
    private GameState state;
    private String winner;

    public Game() {
        initGameboards();
    }

    public Game (Player firstPlayer) {
        player1 = firstPlayer;
        initGameboards();
        if (!player1.placeShips(player1Gameboard)) {
            try {
                Application.mainMenu();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public Player getPlayer1() {
        return player1;
    }
    public Player getPlayer2() {
        return player2;
    }
    public ArrayList<ArrayList<Field>> getPlayer1Gameboard() {
        return player1Gameboard;
    }
    public ArrayList<ArrayList<Field>> getPlayer2Gameboard() {
        return player2Gameboard;
    }
    public GameState getState() {
        return state;
    }
    public String getWinner() {
        return winner;
    }

    public void setPlayer1(Player player) {
        player1 = player;
    }
    public void setPlayer2(Player player) {
        player2 = player;
    }
    public void setState(GameState state) {
        this.state = state;
    }
    public void setWinner(String winner) {
        this.winner = winner;
    }

    public boolean isGameOver() {
        return player1.areAllShipsSank() || player2.areAllShipsSank();
    }

    private void initGameboards() {
        for (int i = 0; i < GAMEBOARD_SIZE; i++) {
            player1Gameboard.add(new ArrayList<Field>(GAMEBOARD_SIZE));
            player2Gameboard.add(new ArrayList<Field>(GAMEBOARD_SIZE));
            for (int j = 0; j < GAMEBOARD_SIZE; j++) {
                player1Gameboard.get(i).add(new Field(EMPTY));
                player2Gameboard.get(i).add(new Field(EMPTY));
            }
        }
    }

    public ShootResult updateField(ArrayList<ArrayList<Field>> gameboard, int x, int y) {
        switch (gameboard.get(y).get(x).getFieldState()) {
            case EMPTY -> {
                gameboard.get(y).get(x).setFieldState(MISS);
                return MISSED;
            }
            case SHIP -> {
                gameboard.get(y).get(x).setFieldState(FieldState.HIT);
                // find the ship that were hit. change its field to true. check isSank()
                for (Ship ship : (state == GameState.PLAYER1TURN ? getPlayer2().getShips() : getPlayer1().getShips())) {
                    if (ship.isHit(x, y) && ship.isSank()) {
                        // if ship is destroyed then change fields around to MISS
                        if (ship.getDirection() == ShipDirection.HORIZONTAL) {
                            for (int i = Math.max(ship.getStartX() - 1, 0); i < Math.min(ship.getStartX() + ship.getSize() + 1, 16) ; i++) {
                                for (int j = Math.max(ship.getStartY() - 1, 0); j <= Math.min(ship.getStartY() + 1, 15); j++) {
                                    if (gameboard.get(j).get(i).getFieldState() == EMPTY) gameboard.get(j).get(i).setFieldState(FieldState.MISS);
                                }
                            }
                        } else {
                            for (int i = Math.max(ship.getStartX() - 1, 0); i <= Math.min(ship.getStartX() + 1, 15) ; i++) {
                                for (int j = Math.max(ship.getStartY() - 1, 0); j < Math.min(ship.getStartY() + ship.getSize() + 1, 16); j++) {
                                    if (gameboard.get(j).get(i).getFieldState() == EMPTY) gameboard.get(j).get(i).setFieldState(FieldState.MISS);
                                }
                            }
                        }
                        return DESTROYED;
                    }
                }
                return HIT;
            }
            case MISS, HIT -> {
                return ERROR;
            }
        }
        return ERROR;
    }

    public ShootResult shoot(int[] shotCoordinates) {
        ShootResult shootResult = updateField(state == GameState.PLAYER1TURN ? player2Gameboard : player1Gameboard, shotCoordinates[0], shotCoordinates[1]);
        System.out.println("Result: " + shootResult.name() + "!");
        switch (shootResult) {
            case MISSED -> state = (state == GameState.PLAYER1TURN ? GameState.PLAYER2TURN : GameState.PLAYER1TURN);
            case HIT -> {} // state stays
            case DESTROYED -> {
                if (isGameOver()) {
                    state = GameState.FINISHED;
                    winner = player1.areAllShipsSank() ? player2.getName() : player1.getName();
                }
            }
            case ERROR -> System.out.println("You can't shoot there! Try another field!");
        }
        return shootResult;
    }
}
