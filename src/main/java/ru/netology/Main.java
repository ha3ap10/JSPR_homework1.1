package ru.netology;

import java.util.Scanner;

public class Main {

    public static Scanner scanner = new Scanner(System.in);
    public static final String SERVER_STOP = "/stop";
    public static final String MSG = "\n\"%s\" - to stop server\n";

    public static void main(String[] args) {

        System.out.printf(MSG, SERVER_STOP);
        final var server = new Server();

        Thread serverThread = new Thread(() -> {
            server.startServer();
        });

        serverThread.start();

        while (true) {

            String command = scanner.nextLine();

            if (SERVER_STOP.equals(command)) {
                server.stopServer();
                break;
            }
        }
    }
}
