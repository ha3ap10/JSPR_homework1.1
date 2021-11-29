package ru.netology;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Stream;

public class Main {

    final static List<String> validPaths = List.of("/index.html", "/spring.svg", "/spring.png", "/resources.html",
            "/styles.css", "/app.js", "/links.html", "/forms.html", "/classic.html", "/events.html", "/events.js");
    //http://localhost:9999/facts.html?count=3
    private static final String FACTS = "/facts";
    private static final String QUERY_NAME = "count";

    public static Scanner scanner = new Scanner(System.in);
    public static final String SERVER_STOP = "/stop";
    public static final String MSG = "\n\"%s\" - to stop server\n";
    public static final String GET = "GET";
    public static final String POST ="POST";

    public static void main(String[] args) {

        System.out.printf(MSG, SERVER_STOP);
        final var server = new Server();

        server.addHandler(GET, FACTS + ".html", new Handler() {
            @Override
            public void handle(Request request, BufferedOutputStream responseStream) throws IOException {
                StringBuilder sb = new StringBuilder();
                final var filePathTxt = Path.of(".", "public", FACTS + ".txt");
                final var filePath = Path.of(".", "public", FACTS + ".html");
                final var mimeType = Files.probeContentType(filePath);

                final var count = request.getQueryParam(QUERY_NAME);

                File file = new File(filePathTxt.toString());
                long lineCount;
                try (Stream<String> stream = Files.lines(filePathTxt, StandardCharsets.UTF_8)) {
                    lineCount = stream.count();
                }
                long msgs = (count != null) ? Integer.parseInt(count) : lineCount;

                try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                    for (int i = 0; i < msgs; i++) {
                        sb
                                .append(br.readLine())
                                .append("\n<br>");
                    }
                }
                final var out = sb.toString();

                final var template = Files.readString(filePath);
                final var content = template.replace(
                        "{facts}",
                        out
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
