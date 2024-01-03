package org.example.dsva.model;

public class Node implements Comparable<Node> {
    private final int id;
    private final String ip;
    private final String port;


    public Node(int id, String ip, String port) {
        this.id = id;
        this.ip = ip;
        this.port = port;
    }

    @Override
    public String toString() {
        return "Node{" + "id=" + id + ", ip='" + ip + '\'' + ", port='" + port + '\'' + '}';
    }

    public String getIp() {
        return this.ip;
    }

    public String getPort() {
        return this.port;
    }

    public int getId() {
        return this.id;
    }

    @Override
    public int compareTo(Node o) {
        return this.id - o.id;
    }
}
