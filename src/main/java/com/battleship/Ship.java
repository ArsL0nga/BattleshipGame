package com.battleship;

import com.battleship.DataTypes.FieldState;
import com.battleship.DataTypes.ShipDirection;

import java.util.ArrayList;

public class Ship {
    private ShipDirection direction;
    private int size;
    private int startX;
    private int startY;
    private boolean[] shipFieldsHit;

    public Ship(ShipDirection direction, int size, int startX, int startY) {
        this.direction = direction;
        this.size = size;
        this.startX = startX;
        this.startY = startY;
        this.shipFieldsHit = new boolean[size];
    }

    public ShipDirection getDirection() {
        return direction;
    }
    public int getSize() {
        return size;
    }
    public int getStartX() {
        return startX;
    }
    public int getStartY() {
        return startY;
    }

    public boolean placeShip(ArrayList<ArrayList<Field>> gameboard) {
        if (direction == ShipDirection.HORIZONTAL) {
            if (startX + size > 16) return false;
            // check if ship fields and fields around are empty
            for (int i = Math.max(startX - 1, 0); i < Math.min(startX + size + 1, 16) ; i++) {
                for (int j = Math.max(startY - 1, 0); j <= Math.min(startY + 1, 15); j++) {
                    if (gameboard.get(j).get(i).getFieldState() != FieldState.EMPTY) return false;
                }
            }
            // if ship can be placed then place it
            for (int i = startX; i < startX + size; i++) {
                gameboard.get(startY).get(i).setFieldState(FieldState.SHIP);
            }
            return true;
        } else { // same as above but for vertical ships
            if (startY + size > 16) return false;
            for (int i = Math.max(startX - 1, 0); i <= Math.min(startX + 1, 15) ; i++) {
                for (int j = Math.max(startY - 1, 0); j < Math.min(startY + size + 1, 16); j++) {
                    if (gameboard.get(j).get(i).getFieldState() != FieldState.EMPTY) return false;
                }
            }
            for (int i = startY; i < startY + size; i++) {
                gameboard.get(i).get(startX).setFieldState(FieldState.SHIP);
            }
        }
        return true;
    }

    public boolean isHit(int x, int y) {
        if (direction == ShipDirection.HORIZONTAL) {
            if (y == startY && x >= startX && x <= startX + size) {
                shipFieldsHit[x - startX] = true;
                return true;
            }
        } else {
            if (x == startX && y >= startY && y <= startY + size) {
                shipFieldsHit[y - startY] = true;
                return true;
            }
        }
        return false;
    }

    public boolean isSank() {
        for (boolean isFieldHit : shipFieldsHit) {
            if (!isFieldHit) return false;
        }
        return true;
    }
}
