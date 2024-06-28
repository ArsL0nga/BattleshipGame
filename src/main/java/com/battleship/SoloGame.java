package com.battleship;

import com.battleship.DataTypes.*;

import java.io.IOException;
import java.util.Scanner;

import static com.battleship.DataTypes.BotState.*;
import static com.battleship.DataTypes.FieldState.HIT;
import static com.battleship.DataTypes.FieldState.MISS;


public class SoloGame extends Game {

    private BotState botState = SEARCH;
    private int[] firstHitCoordinates;
    private int[] lastHitCoordinates;
    private int searchRange = 6;
    private int[] currentSearchField = new int[] {5, 0};

    public SoloGame(Player firstPlayer) {
        super(firstPlayer);
        setPlayer2(new Player("Bot", PlayerType.BOT));
        getPlayer2().placeShips(getPlayer2Gameboard());
        startGame();
    }

    private void makeATurn() {
        int[] shotCoordinates = new int[2];
        if ( getState() == GameState.PLAYER2TURN) {
            getShotPositionFromBot(shotCoordinates);
            Application.clearScreen();
            ShootResult shootResult = shoot(shotCoordinates);
            switch (shootResult) {
                case HIT -> {
                    if (botState != ATTACK) {
                        botState = ATTACK;
                        firstHitCoordinates = new int[2];
                        firstHitCoordinates[0] = shotCoordinates[0];
                        firstHitCoordinates[1] = shotCoordinates[1];
                    } else {
                        if (lastHitCoordinates == null) lastHitCoordinates = new int[2];
                        lastHitCoordinates[0] = shotCoordinates[0];
                        lastHitCoordinates[1] = shotCoordinates[1];
                    }
                }
                case DESTROYED -> {
                    botState = SEARCH;
                    firstHitCoordinates = null;
                    lastHitCoordinates = null;
                }
                default -> {}
            }
            getPlayer1().showGameboards(getPlayer1Gameboard(), getPlayer2Gameboard());
        } else {
            if (!getPlayer1().getShotPositionFromUser(shotCoordinates)) {
                System.out.println("Incorrect values!");
            } else {
                Application.clearScreen();
                shoot(shotCoordinates);
                getPlayer1().showGameboards(getPlayer1Gameboard(), getPlayer2Gameboard());
            }
        }
    }

    public void startGame() {
        setState(GameState.PLAYER1TURN);
        while (!isGameOver()) {
            makeATurn();
        }
        setWinner(getPlayer1().areAllShipsSank() ? getPlayer2().getName() : getPlayer1().getName());
        System.out.println("GAME_OVER! Winner:" + getWinner());
        try {
            System.out.println("Enter anything to return to the main menu!");
            new Scanner(System.in).next();
            Application.mainMenu();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean getShotPositionFromBot(int[] shotCoordinates) {
        switch (botState) {
            case SEARCH -> {
                while (!search(shotCoordinates));
            }
            case ATTACK -> {
                if (lastHitCoordinates == null) { // hit only 1 square and need to check 4 squares around
                    if (checkFieldAbove(firstHitCoordinates, shotCoordinates)) return true;
                    if (checkFieldBelow(firstHitCoordinates, shotCoordinates)) return true;
                    if (checkFieldLeftward(firstHitCoordinates, shotCoordinates)) return true;
                    if (checkFieldRightward(firstHitCoordinates, shotCoordinates)) return true;
                } else { // bot knows direction
                    if (firstHitCoordinates[0] == lastHitCoordinates[0]) { // vertical
                        // 3 cases - empty below last, empty above last and empty at the other side of the ship
                        if (checkFieldAbove(lastHitCoordinates, shotCoordinates)) return true;
                        if (checkFieldBelow(lastHitCoordinates, shotCoordinates)) return true;
                        return firstHitCoordinates[1] > lastHitCoordinates[1] ?
                                checkFieldBelow(firstHitCoordinates, shotCoordinates) :
                                checkFieldAbove(firstHitCoordinates, shotCoordinates);
                    } else { // horizontal
                        if (checkFieldLeftward(lastHitCoordinates, shotCoordinates)) return true;
                        if (checkFieldRightward(lastHitCoordinates, shotCoordinates)) return true;
                        return firstHitCoordinates[0] > lastHitCoordinates[0] ?
                                checkFieldRightward(firstHitCoordinates, shotCoordinates) :
                                checkFieldLeftward(firstHitCoordinates, shotCoordinates);
                    }
                }
            }
        }
        return true;
    }

    private boolean checkFieldAbove(int[] fieldCoordinates, int[] shotCoordinates) {
        if (fieldCoordinates[1] <= 0) return false;
        FieldState fieldState = getPlayer1Gameboard().get(fieldCoordinates[1] - 1).get(fieldCoordinates[0]).getFieldState();
        if (fieldState != MISS && fieldState != FieldState.HIT) {
            shotCoordinates[0] = fieldCoordinates[0];
            shotCoordinates[1] = fieldCoordinates[1] - 1;
            return true;
        }
        return false;
    }

    private boolean checkFieldBelow(int[] fieldCoordinates, int[] shotCoordinates) {
        if (fieldCoordinates[1] >= 15) return false;
        FieldState fieldState = getPlayer1Gameboard().get(fieldCoordinates[1] + 1).get(fieldCoordinates[0]).getFieldState();
        if (fieldState != MISS && fieldState != FieldState.HIT) {
            shotCoordinates[0] = fieldCoordinates[0];
            shotCoordinates[1] = fieldCoordinates[1] + 1;
            return true;
        }
        return false;
    }

    private boolean checkFieldLeftward(int[] fieldCoordinates, int[] shotCoordinates) {
        if (fieldCoordinates[0] <= 0) return false;
        FieldState fieldState = getPlayer1Gameboard().get(fieldCoordinates[1]).get(fieldCoordinates[0] - 1).getFieldState();
        if (fieldState != MISS && fieldState != FieldState.HIT) {
            shotCoordinates[0] = fieldCoordinates[0] - 1;
            shotCoordinates[1] = fieldCoordinates[1];
            return true;
        }
        return false;
    }

    private boolean checkFieldRightward(int[] fieldCoordinates, int[] shotCoordinates) {
        if (fieldCoordinates[0] >= 15) return false;
        FieldState fieldState = getPlayer1Gameboard().get(fieldCoordinates[1]).get(fieldCoordinates[0] + 1).getFieldState();
        if (fieldState != MISS && fieldState != FieldState.HIT) {
            shotCoordinates[0] = fieldCoordinates[0] + 1;
            shotCoordinates[1] = fieldCoordinates[1];
            return true;
        }
        return false;
    }

    private boolean search(int[] shotCoordinates) {
        shotCoordinates[0] = currentSearchField[0];
        shotCoordinates[1] = currentSearchField[1];
        if (currentSearchField[0] > 0 && currentSearchField[1] < 15) { // can go 1 square down and left
            currentSearchField[0]--;
            currentSearchField[1]++;
        } else { // can't go - should change the diagonal or range
            if (currentSearchField[0] + searchRange < 16) { // change diagonal
                int tmp = currentSearchField[0];
                currentSearchField[0] = Math.min(currentSearchField[1] + searchRange, 15);
                currentSearchField[1] = (tmp > 0 ? tmp + searchRange : Math.max(currentSearchField[1] + searchRange - 15, 0));
            } else { // change range and try again
                switch(searchRange) {
                    case 6 -> {
                        if (getPlayer1Gameboard().get(0).get(2).getFieldState() != MISS && getPlayer1Gameboard().get(0).get(2).getFieldState() != FieldState.HIT) {
                            currentSearchField[0] = 2;
                            currentSearchField[1] = 0;
                        } else {
                            currentSearchField[0] = 0;
                            currentSearchField[1] = 0;
                            searchRange = 3;
                        }
                    }
                    case 3 -> {
                        currentSearchField[0] = 1;
                        currentSearchField[1] = 0;
                    }
                }
            }
        }
        FieldState fieldState = getPlayer1Gameboard().get(shotCoordinates[1]).get(shotCoordinates[0]).getFieldState();
        return fieldState != MISS && fieldState != HIT;
    }
}
