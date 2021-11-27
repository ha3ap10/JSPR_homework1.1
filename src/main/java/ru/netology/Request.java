package ru.netology;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Request {

    private static final String GET = "GET";
    private static final String DELIMITER = "?";
    private static final String CONTENT_TYPE = "Content-Type";
    private static final String URLENCODED = "application/x-www-form-urlencoded";

    private final String method;
    private final String path;
    private final String version;
    private final Map<String, String> headers;
    private final Map<String, String> queryParams;
    private final Map<String, List<String>> postParams;
    private final String body;

    public Request(String method, String path, String version, Map<String, String> headers, Map<String, String> queryParams, Map<String, List<String>> postParams, String body) {
        this.method = method;
        this.path = path;
        this.version = version;
        this.headers = headers;
        this.queryParams = queryParams;
        this.postParams = postParams;
        this.body = body;
    }

    public static Request parseRequest(BufferedReader in) throws IOException {

        final var requestLine = in.readLine();
        final var parts = requestLine.split(" ");

        if (parts.length != 3) {
            return null;
        }

        final var method = parts[0];
        String path;
        Map<String, String> queryParams = new HashMap<>();
        if (parts[1].contains(DELIMITER)) {
            int endPath = parts[1].indexOf(DELIMITER);
            path = parts[1].substring(0, endPath);
            queryParams = setQueryParams(parts[1].substring(endPath + 1));
        } else {
            path = parts[1];
        }
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

        Map<String, List<String>> postParams = new HashMap<>();
        if (headers.get(CONTENT_TYPE).equals(URLENCODED)) postParams = setPostParams(body);

        return new Request(method, path, version, headers, queryParams, postParams, body);
    }

    private static Map<String, String> setQueryParams(String requestLinePath) {
        Map<String, String> queryParams = new HashMap<>();
        List<NameValuePair> list = URLEncodedUtils.parse(requestLinePath, StandardCharsets.UTF_8);
        for (NameValuePair valuePair : list) {
            queryParams.put(valuePair.getName(), valuePair.getValue());
        }
        return queryParams;
    }

    public Map<String, String> getQueryParams() {
        return queryParams;
    }

    public String getQueryParam(String name) {
        return queryParams.get(name);
    }

    public static Map<String, List<String>> setPostParams(String body) {
        Map<String, List<String>> postParams = new HashMap<>();
        List<NameValuePair> list = URLEncodedUtils.parse(body, StandardCharsets.UTF_8);
        for (NameValuePair valuePair : list) {
            List<String> valuesList = postParams.getOrDefault(valuePair.getName(), new ArrayList<>());
            valuesList.add(valuePair.getValue());
            postParams.put(valuePair.getName(), valuesList);
        }
        return postParams;
    }

    public Map<String, List<String>> getPostParams() {
        return postParams;
    }

    public List<String> getPostParam(String name) {
        return postParams.get(name);
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
