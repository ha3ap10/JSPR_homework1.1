package ru.netology;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class Request {

    private static final String GET = "GET";

    private final String method;
    private final String path;
    private final String version;
    private final Map<String, String> headers;
    private final String body;

    public Request(String method, String path, String version, Map<String, String> headers, String body) {
        this.method = method;
        this.path = path;
        this.version = version;
        this.headers = headers;
        this.body = body;
    }

    public static Request parseRequest(BufferedReader in) throws IOException {

        final var requestLine = in.readLine();
        final var parts = requestLine.split(" ");

        if (parts.length != 3) {
            return null;
        }

        final var method = parts[0];
        final var path = parts[1];
        final var version = parts[2];

        Map<String, String> headers = new HashMap<>();
        String line;
        while (!(line = in.readLine()).equals("")) {
            String[] header = line.split(": ");
            headers.put(header[0], header[1]);
        }

        StringBuilder sb = new StringBuilder();
        if (!method.equals(GET)) {
            while ((line = in.readLine()) != null) {
                sb.append(line);
            }
        }

        final var body = sb.toString();

        return new Request(method, path, version, headers, body);
    }

    public String getMethod() {
        return method;
    }

    public String getPath() {
        return path;
    }

    public String getVersion() {
        return version;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public String getBody() {
        return body;
    }
}
