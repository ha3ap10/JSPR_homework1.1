package ru.netology;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Scanner;

public class Main {

    final static List<String> validPaths = List.of("/index.html", "/spring.svg", "/spring.png", "/resources.html",
            "/styles.css", "/app.js", "/links.html", "/forms.html", "/classic.html", "/events.html", "/events.js");
    public static Scanner scanner = new Scanner(System.in);
    public static final String SERVER_STOP = "/stop";
    public static final String MSG = "\n\"%s\" - to stop server\n";
    public static final String GET = "GET";
    public static final String POST ="POST";

    public static void main(String[] args) {

        System.out.printf(MSG, SERVER_STOP);
        final var server = new Server();

        for (String path : validPaths) {
            if (path.equals("/classic.html")) {
                server.addHandler(GET, path, new Handler() {
                    @Override
                    public void handle(Request request, BufferedOutputStream responseStream) throws IOException {
                        final var filePath = Path.of(".", "public", path);
                        final var mimeType = Files.probeContentType(filePath);

                        final var template = Files.readString(filePath);
                        final var content = template.replace(
                                "{time}",
                                LocalDateTime.now().toString()
                        ).getBytes();
                        responseStream.write((
                                "HTTP/1.1 200 OK\r\n" +
                                        "Content-Type: " + mimeType + "\r\n" +
                                        "Content-Length: " + content.length + "\r\n" +
                                        "Connection: close\r\n" +
                                        "\r\n"
                        ).getBytes());
                        responseStream.write(content);
                        responseStream.flush();
                    }
                });
            } else {
                server.addHandler(GET, path, new Handler() {
                    @Override
                    public void handle(Request request, BufferedOutputStream responseStream) throws IOException {
                        final var filePath = Path.of(".", "public", path);
                        final var mimeType = Files.probeContentType(filePath);

                        final var length = Files.size(filePath);
                        responseStream.write((
                                "HTTP/1.1 200 OK\r\n" +
                                        "Content-Type: " + mimeType + "\r\n" +
                                        "Content-Length: " + length + "\r\n" +
                                        "Connection: close\r\n" +
                                        "\r\n"
                        ).getBytes());
                        Files.copy(filePath, responseStream);
                        responseStream.flush();
                    }
                });
            }
        }
//        server.addHandler();

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
