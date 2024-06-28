package com.battleship;

import com.battleship.DataTypes.GameState;
import com.battleship.DataTypes.PlayerType;
import com.battleship.DataTypes.ShootResult;

public class MultiplayerGame extends Game{
    public MultiplayerGame() {
        super();
    }

    @Override
    public ShootResult shoot(int[] shotCoordinates) {
        ShootResult shootResult = updateField(getState() == GameState.PLAYER1TURN ? getPlayer2Gameboard(): getPlayer1Gameboard(), shotCoordinates[0], shotCoordinates[1]);
        System.out.println("Result: " + shootResult.name() + "!"); // return ShotResult and show it after gameboards
        switch (shootResult) {
            case MISSED -> setState(getState() == GameState.PLAYER1TURN ? GameState.PLAYER2TURN : GameState.PLAYER1TURN);
            case HIT -> {}
            case DESTROYED -> {
                if (isGameOver()) {
                    setState(GameState.FINISHED);
                    setWinner(getPlayer1().areAllShipsSank() ? getPlayer2().getName() : getPlayer1().getName());
                }
            }
            case ERROR -> {}
        }
        return shootResult;
    }

}
