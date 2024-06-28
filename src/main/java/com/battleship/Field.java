package com.battleship;

import com.battleship.DataTypes.FieldState;

import static com.battleship.DataTypes.FieldState.*;

public class Field {
    private FieldState fieldState;

    public Field(FieldState fieldState) {
        this.fieldState = fieldState;
    }

    public FieldState getFieldState() {
        return fieldState;
    }

    public void setFieldState(FieldState fieldState) {
        this.fieldState = fieldState;
    }

    @Override
    public String toString() {
        return switch (fieldState) {
            case EMPTY ->  " ";
            case SHIP -> "O";
            case MISS -> ".";
            case HIT -> "X";
        };
    }

    public static FieldState valueOf(String s) {
        return switch (s) {
            case " " -> EMPTY;
            case "O" -> SHIP;
            case "." -> MISS;
            case "X" -> HIT;
            default -> throw new IllegalStateException("Unexpected value: " + s);
        };
    }
}
