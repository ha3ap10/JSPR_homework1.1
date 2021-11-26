package ru.netology;

import java.io.*;
import java.net.Socket;
import java.util.Map;

public class ClientConnection implements Runnable {

    private final Socket socket;
    private final Map<String, Map<String, Handler>> handlersMap;

    public ClientConnection(Socket socket, Map<String, Map<String, Handler>> handlersMap) {
        this.socket = socket;
        this.handlersMap = handlersMap;
    }

    @Override
    public void run() {

        try (socket;
             final var in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             final var out = new BufferedOutputStream(socket.getOutputStream())) {

            final var request = Request.parseRequest(in);
            final var method = request.getMethod();
            final var path = request.getPath();

            Handler handler = handlersMap.get(method).get(path);
            if (handler == null) {
               handler = notFoundHandler();
            }
            handler.handle(request, out);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Handler notFoundHandler() {
        return new Handler() {
            @Override
            public void handle(Request request, BufferedOutputStream responseStream) throws IOException {
                responseStream.write((
                        "HTTP/1.1 404 Not Found\r\n" +
                                "Content-Length: 0\r\n" +
                                "Connection: close\r\n" +
                                "\r\n"
                ).getBytes());
                responseStream.flush();
            }
        };
    }
}
