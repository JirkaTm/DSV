package org.example.dsva.api.model;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class Node {
    private final String ip;
    private final String port;

    boolean isAlive = false;

    public Node(String ip, String port) {
        this.ip = ip;
        this.port = port;
        checkIsAlive();
    }

    @Override
    public String toString() {
        return "Node{" +
                "ip='" + ip + '\'' +
                ", port='" + port + '\'' +
                ", isAlive=" + isAlive +
                '}';
    }

    public String getIp() {
        return this.ip;
    }

    public String getPort() {
        return this.port;
    }

    public boolean getIsAlive() {
        return this.isAlive;
    }

    // Check if node is alive
    public void checkIsAlive() {
        try {
            String url = "http://" + this.ip + ":" + this.port + "/alive";
            System.out.println(url);
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .build();

            client.send(request, HttpResponse.BodyHandlers.ofString());
            this.isAlive = true;
        } catch (Exception e) {
            this.isAlive = false;
        }
    }
}
