package ru.netology;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {

    private static final int PORT = 9999;
    private static final int CONNECTIONS = 64;
    private static final String TIME_OUT = "Accept timed out";

    private Map<String, Map<String, Handler>> handlersMap = new ConcurrentHashMap<>();
    private ExecutorService executorService;
    private Thread serverThread;


    public void startServer() {
        serverThread = Thread.currentThread();
        executorService = Executors.newFixedThreadPool(CONNECTIONS);

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            serverSocket.setSoTimeout(2000);
            while (!serverThread.isInterrupted()) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    System.out.println("New connection: " + clientSocket.getInetAddress());
                    executorService.execute(new ClientConnection(clientSocket, handlersMap));
                } catch (IOException e) {
                    if (!e.getMessage().equals(TIME_OUT)) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void stopServer() {
        executorService.shutdown();
        serverThread.interrupt();
        System.out.println("Server stopped");
    }

    public void addHandler(String method, String path, Handler handler) {
        Map<String, Handler> handlerMap = handlersMap.getOrDefault(method, new ConcurrentHashMap<>());
        handlerMap.putIfAbsent(path, handler);
        handlersMap.putIfAbsent(method, handlerMap);
    }
}
