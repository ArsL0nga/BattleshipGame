package com.battleship;

import com.battleship.DataTypes.PlayerType;

import java.io.IOException;
import java.net.ConnectException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

public class Application {
    private static String userName = "test";

    public static void main(String[] args) throws IOException {
        System.out.println("Enter the name:");
        userName = new Scanner(System.in).nextLine();
        mainMenu();
    }

    public static void mainMenu() throws IOException {
        clearScreen();
        showUI();
        if (userName.equalsIgnoreCase("admin")) readAdminChoice();
        else handleUserChoice();
    }

    private static void showUI() {
        String options = userName.equalsIgnoreCase("admin") ? "Choose action:\n1 - Show all games\n2 - Show game by id\n3 - Delete game\n4 - Exit\n" :
                "Choose action:\n1 - Play with a bot\n2 - Play with a friend\n3 - Exit";
        System.out.println(options);
    }

    private static void handleUserChoice() throws IOException {
        Scanner scanner = new Scanner(System.in);
        if (!scanner.hasNextInt()) {
            System.out.println("Wrong value! Try again!");
            mainMenu();
            return;
        }
        switch (scanner.nextInt()) {
            case 1 -> {
                startSoloGame();
            }
            case 2 -> {
                startMultiplayerGame();
            }
            case 3 -> {
                // do nothing and end the process
            }
            default -> {
                System.out.println("Wrong value! Try again!");
                mainMenu();
            }
        }
    }

    private static void readAdminChoice() throws IOException {
        Scanner scanner = new Scanner(System.in);
        if (!scanner.hasNextInt()) {
            System.out.println("Wrong value! Try again!");
            mainMenu();
            return;
        }
        switch (scanner.nextInt()) {
            case 1 -> {
                // show all games
            }
            case 2 -> {
                // get game by id
            }
            case 3 -> {
                // delete game by id
            }
            case 4 -> {
                // do nothing and end the process
            }
            default -> {
                System.out.println("Wrong value! Try again!");
                mainMenu();
            }
        }
    }

    public static void clearScreen() {
        try {
            new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void startSoloGame() {
        new SoloGame(new Player(userName, PlayerType.PLAYER));
    }

    private static void startMultiplayerGame() throws IOException {
        switch (isServer()) {
            case 1 -> {
                createServer();
                createClient();
            }
            case 2 -> createClient();
            case 3 -> mainMenu();
            default -> mainMenu();
        }
    }

    private static int isServer() {
        System.out.println("Choose action:\n1 - Create new game\n2 - Connect to the existing game\n3 - Cancel");
        Scanner scanner = new Scanner(System.in);
        if (!scanner.hasNextInt()) {
            System.out.println("Wrong value! Try again!");
            return -1;
        }
        int choice = scanner.nextInt();
        return (choice < 1 || choice > 3 ? -1 : choice);
    }

    private static void createServer() throws IOException {
        ServerSocket serverSocket = new ServerSocket(3333);
        Thread gameServer = new Thread(new GameServer(serverSocket));
        gameServer.start();
    }

    private static void createClient() throws IOException {
        try {
            Socket socket = new Socket("localhost", 3333);
            GameClient gameClient = new GameClient(socket, userName);
            gameClient.listenForMessage();
        } catch (ConnectException e) {
            System.out.println("Connection has failed!");
            mainMenu();
        }
    }
}