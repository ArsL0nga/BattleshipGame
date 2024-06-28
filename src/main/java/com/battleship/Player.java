package com.battleship;

import com.battleship.DataTypes.FieldState;
import com.battleship.DataTypes.PlayerType;
import com.battleship.DataTypes.ShipDirection;

import java.util.*;

import static com.battleship.DataTypes.FieldState.EMPTY;
import static com.battleship.Game.GAMEBOARD_SIZE;
import static java.util.Map.entry;

public class Player {
    private String name;
    private PlayerType type;
    private ArrayList<Ship> ships = new ArrayList<>();

    public Player(String name) {
        this.name = name;
        this.type = null;
    }

    public Player(String name, PlayerType type) {
        this.name = name;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public PlayerType getType() {
        return type;
    }

    public ArrayList<Ship> getShips() {
        return ships;
    }

    public boolean placeShips(ArrayList<ArrayList<Field>> gameboard) {
        Map<Integer, Integer> unplaced_ships; // Map with unplaced ships (size, quantity)
        unplaced_ships = new HashMap<>(Map.ofEntries(entry(6, 1), entry(5, 2), entry(4, 3),
                entry(3, 4), entry(2, 5), entry(1, 6)));

        if (type == PlayerType.BOT) {
            autoFill(unplaced_ships,gameboard) ;
            return true;
        }

        int autoFillChoice = isAutoFillChosen();
        if (autoFillChoice == -1) {
            System.out.println("Wrong value!");
            return false;
        } else if (autoFillChoice == 2) {
            System.out.println("Exit chosen");
            return false;
        }
        else if (autoFillChoice == 1) {
            autoFill(unplaced_ships, gameboard);
            showGameboards(gameboard, null);
        } else {
            showGameboards(gameboard, null);
            while (!unplaced_ships.isEmpty()) {
                Ship ship = getInfoFromUser(unplaced_ships);
                if (ship == null) continue;
                if (ship.placeShip(gameboard)) {
                    ships.add(ship);
                    unplaced_ships.put(ship.getSize(), unplaced_ships.get(ship.getSize()) - 1); // lessen the quantity of unplaced ships
                    if (unplaced_ships.get(ship.getSize()) == 0) unplaced_ships.remove(ship.getSize()); // all ships of such size are placed - remove them from the map
                } else {
                    System.out.println("The ship cannot be placed here!");
                }
                showGameboards(gameboard, null);
            }
        }
        return true;
    }

    public void showGameboards(String gameboardStr, boolean isReverse, boolean isRighBoardHidden) {
        // show gameboards from string
        for (int n = 0; n < 2; n++) {
            System.out.print(n == 0 ? "    " : "              ");
            for (char i = 'A'; i <= 'P'; i++) {
                System.out.printf("%3c", i);
            }
        }
        System.out.println("\n");
        for (int i = 1; i <= GAMEBOARD_SIZE; i++) {
            // show left board which represents current player
            System.out.printf("%2d  ", i);
            for (int j = (isReverse ? GAMEBOARD_SIZE : 0); j < (isReverse ? 2*GAMEBOARD_SIZE : GAMEBOARD_SIZE); j++) {
                System.out.printf("%3s", gameboardStr.charAt((i-1)*GAMEBOARD_SIZE*2 + j));
            }

            // show right board. ships are hidden for players and visible for spectators
            System.out.print("          ");
            System.out.printf("%2d  ", i);
            for (int j = (isReverse ? 0 : GAMEBOARD_SIZE); j < (isReverse ? GAMEBOARD_SIZE : 2*GAMEBOARD_SIZE); j++) {
                char field = gameboardStr.charAt((i-1)*GAMEBOARD_SIZE*2 + j);
                System.out.printf("%3s", field == 'O' && isRighBoardHidden ? " " : field);
            }
            System.out.println();
        }
    }

    public void showGameboards(ArrayList<ArrayList<Field>> player1Gameboard, ArrayList<ArrayList<Field>> player2Gameboard) {
        for (int n = 0; n < 2; n++) {
            System.out.print(n == 0 ? "    " : "              ");
            for (char i = 'A'; i <= 'P'; i++) {
                System.out.printf("%3c", i);
            }
        }
        System.out.println("\n");

        for (int i = 1; i <= GAMEBOARD_SIZE; i++) {
            // show left board
            System.out.printf("%2d  ", i);
            for (Field field : player1Gameboard.get(i-1)) {
                System.out.printf("%3s", field.toString());
            }

            // show right board
            System.out.print("          ");
            System.out.printf("%2d  ", i);
            if (player2Gameboard == null) {
                for (int j = 0; j <= GAMEBOARD_SIZE; j++) {
                    System.out.printf("%3s", new Field(EMPTY));
                }
                System.out.println();
            } else {
                for (Field field : player2Gameboard.get(i-1)) {
                    System.out.printf("%3s", field.getFieldState() == FieldState.SHIP ? new Field(EMPTY).toString() : field.toString());
                }
                System.out.println();
            }
        }
    }

    // TODO exit only on 3. everything else is error and try again
    private int isAutoFillChosen() {
        System.out.println("Place ships! Do you want to use autofill?\n1 - Yes\n2 - No\n3 - For exit press any other key!");
        Scanner scanner = new Scanner(System.in);
        if (!scanner.hasNextInt()) {
            return -1;
        }
        switch (scanner.nextInt()) {
            case 1 -> {
                return 1;
            }
            case 2 -> {
                return 0;
            }
            default -> {
                return 2;
            }
        }
    }

    private void autoFill(Map<Integer, Integer> unplaced_ships, ArrayList<ArrayList<Field>> gameboard) {
        while (!unplaced_ships.isEmpty()) {
            int size = Collections.max(unplaced_ships.keySet());
            Ship ship = null;
            int attempts = 0;
            do {
                ShipDirection direction = ShipDirection.values()[(int) (Math.random() * 2)];
                int start_x = (int) (Math.random() * 16);
                int start_y = (int) (Math.random() * 16);
                ship = new Ship(direction, size, start_x, start_y);
                attempts++;
            } while (!ship.placeShip(gameboard) && attempts < 2000);
            ships.add(ship); // 2000 attempts should be 99% enough to place the ship
            unplaced_ships.put(ship.getSize(), unplaced_ships.get(ship.getSize()) - 1);
            if (unplaced_ships.get(ship.getSize()) == 0) unplaced_ships.remove(ship.getSize());
        }
    }

    private Ship getInfoFromUser(Map<Integer, Integer> unplaced_ships) {
        int size = -1;
        ShipDirection direction;
        int x = -1;
        int y = -1;

        // get ship size
        System.out.println("Choose the size of the ship! Enter number from 1 to 6!");
        Scanner scanner = new Scanner(System.in);
        if (!scanner.hasNextInt()) {
            System.out.println("Wrong value! Try again!");
            return null;
        }
        size = scanner.nextInt();
        if (!unplaced_ships.containsKey(size)) {
            System.out.println("There are no unplaced ships of such size!");
            return null;
        }

        // get ship direction
        System.out.println("Choose the direction of the ship!\n1 - Horizontal\n2 - Vertical");
        if (!scanner.hasNext()) {
            System.out.println("Wrong value! Try again!");
            return null;
        }
        switch (scanner.next()) {
            case "1" -> direction = ShipDirection.HORIZONTAL;
            case "2" -> direction = ShipDirection.VERTICAL;
            default -> {
                System.out.println("Wrong value! Try again!");
                return null;
            }
        }

        // get start coordinates
        System.out.println("Enter the x coordinate for the left field if the ship is horizontal or upper field if the ship is vertical! (Value from A to P)");
        if (!scanner.hasNext()) {
            System.out.println("Wrong value! Try again!");
            return null;
        }
        x = Character.getNumericValue(scanner.next().charAt(0)) - 10;
        if (x < 0 || x > 15) {
            System.out.println("Wrong value! Try again!");
            return null;
        }

        System.out.println("Enter the y coordinate for the left field if the ship is horizontal or upper field if the ship is vertical! (Value from 1 to 16)");
        if (!scanner.hasNextInt()) {
            System.out.println("Wrong value! Try again!");
            return null;
        }
        y = scanner.nextInt() - 1;
        if (y < 0 || y > 15) {
            System.out.println("Wrong value! Try again!");
            return null;
        }
        return new Ship(direction, size, x, y);
    }

    public boolean getShotPositionFromUser(int[] shotCoordinates) {
        // get x
        System.out.println("Enter the x value of the shot! (from A to P)");
        Scanner scanner = new Scanner(System.in);
        if (!scanner.hasNext()) {
            System.out.println("Wrong value! Try again!");
            return false;
        }
        shotCoordinates[0] = Character.getNumericValue(scanner.next().charAt(0)) - 10;
        if (shotCoordinates[0] < 0 || shotCoordinates[0] > 15) {
            System.out.println("Wrong value! Try again!");
            return false;
        }

        // get y
        System.out.println("Enter the y value of the shot! (from 1 to 16)");
        if (!scanner.hasNextInt()) {
            System.out.println("Wrong value! Try again!");
            return false;
        }
        shotCoordinates[1] = scanner.nextInt() - 1;
        if (shotCoordinates[1] < 0 || shotCoordinates[1] > 15) {
            System.out.println("Wrong value! Try again!");
            return false;
        }
        return true;
    }

    public boolean areAllShipsSank() {
        for (Ship ship : ships) {
            if (!ship.isSank()) return false;
        }
        return true;
    }
}
