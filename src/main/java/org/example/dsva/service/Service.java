package org.example.dsva.service;

import jakarta.annotation.PreDestroy;
import org.example.dsva.DsVaApplication;
import org.example.dsva.model.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CompletableFuture;

import static java.lang.Integer.parseInt;

@org.springframework.stereotype.Service
public class Service {
    private List<Node> nodes;
    private Node leader;
    private final int ID;
    private static final Logger logger = LoggerFactory.getLogger(Service.class);

    private boolean forceExit = false;

    private ArrayList<String> cachedMessages = new ArrayList<>();

    private int delay = 0;


    public Service() {
        ID = DsVaApplication.cwsNum;
        logger.info("Node ID: " + ID);
    }


    @EventListener(ApplicationReadyEvent.class)
    public void init() {
        logger.info("Loading environment configuration");
        nodes = readConfiguredNodes();
        start_election();
    }

    @PreDestroy
    public void destroy() {
        if (!forceExit){
            logger.warn("Logging out");
            if (leader == null) {
                start_election_on_disconnect();
            }
        }
    }

    // Read nodes and ports from resources/static/connections.txt
    public List<Node> readConfiguredNodes() {
        try {
            List<Node> nodeList = new ArrayList<>();
            File myObj = new File("src/main/resources/connections.txt");
            Scanner myReader = new Scanner(myObj);
            while (myReader.hasNextLine()) {
                String data = myReader.nextLine();

                // Split line by space and create new node
                String[] nodeData = data.split(" ");
                Node node = new Node(parseInt(nodeData[0]), nodeData[1], nodeData[2]);

                if (node.getId() == ID) {
                    continue;
                }
                nodeList.add(node);
            }
            logger.info("Configurations loaded from connections.txt: " + nodeList);
            myReader.close();
            return nodeList;
        } catch (FileNotFoundException e) {
            logger.error("Connections.txt file not found! " + e);
            System.exit(1);
        }
        return null;
    }

    public void start_election() {
        List<Node> higherNodes = new ArrayList<>();
        boolean potentialLeader = false;

        for (Node node : nodes) {
            if (node.getId() > ID) {
                higherNodes.add(node);
            }
        }


        if (!higherNodes.isEmpty()) {
            List<CompletableFuture<Integer>> futures = new ArrayList<>();
            for (Node node : higherNodes) {
                CompletableFuture<Integer> future = CompletableFuture.supplyAsync(() -> {
                    logger.info("Sending election request to node " + node.getId());
                    try {
                        return sendRestElection(node);
                    } catch (IOException e) {
                        logger.warn("Exception while sending election request to node " + node.getId() + ", " + e);
                        return -1;
                    }
                });
                futures.add(future);
            }
            if (futures.stream().map(CompletableFuture::join).anyMatch(status -> status == 200)) {
                potentialLeader = true;
            }
        }
        if (!potentialLeader) {
            logger.info("This node (" + ID + ") is now the leader!");
            leader = null;
            for (Node node : nodes) {
                logger.info("Sending victory message to node " + node.getId());
                CompletableFuture.runAsync(() -> {
                    try {
                        waitForDelay();
                        URL url = new URL("http://" + node.getIp() + ":" + node.getPort() + "/victory?id=" + ID);
                        HttpURLConnection con = (HttpURLConnection) url.openConnection();
                        con.setConnectTimeout(1000);
                        con.setRequestMethod("POST");
                        int status = con.getResponseCode();
                        if (status == 200) {
                            logger.info("Received reply from node " + node.getId() + ": Status: " + status);
                        } else {
                            logger.error("Received reply from node " + node.getId() + ": Status: " + status);
                        }
                    } catch (IOException e) {
                        logger.warn("Exception while sending victory message to node " + node.getId() + ", " + e);
                    }
                });
            }
            sendCachedMessages();
        }
    }

    public void start_election_on_disconnect() {
        nodes.sort(Collections.reverseOrder());
        for (Node node : nodes) {
            logger.info("Sending election request to node " + node.getId());

            CompletableFuture.runAsync(() -> {
                try {
                    sendRestElection(node);
                } catch (IOException e) {
                    logger.warn("Exception while sending election request to node " + node.getId() + ", " + e);
                }
            });
        }
    }

    private int sendRestElection(Node node) throws IOException {
        waitForDelay();
        URL url = new URL("http://" + node.getIp() + ":" + node.getPort() + "/election");
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setConnectTimeout(1000);
        con.setRequestMethod("POST");
        int status = con.getResponseCode();
        if (status == 200) {
            logger.info("Received reply from node " + node.getId() + ": Status: " + status);
        }
        else {
            logger.error("Received reply from node " + node.getId() + ": Status: " + status);
        }
        return status;
    }


    public boolean setLeader(int id) {
        for (Node node : nodes) {
            if (node.getId() == id) {
                if (id < ID) {
                    logger.error("ERROR occurred in architecture restructuring! ");
                    logger.warn("Restarting election process...");
                    start_election();
                    return false;
                }
                leader = node;
                logger.info("Node " + id + " is now the leader!");
                sendCachedMessages();
                return true;
            }
        }
        return false;
    }

    public void spreadMessage(Integer id, String message) {
        if (leader == null) {

            logger.info("Spreading message from node " + id + " to all nodes");
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
            LocalDateTime now = LocalDateTime.now();
            String time = dtf.format(now);
            message = time + " Node " + id + " says: " + message;
            printMessageToConsole(message);

            for (Node node : nodes) {
                try {
                    int status = sendRestMessage(id, message, node);
                    if (status == 200){
                        logger.info("Received reply from node " + node.getId() + ": Status: " + status);
                    } else {
                        logger.error("Received reply from node " + node.getId() + ": Status: " + status);
                    }
                } catch (IOException e) {
                    logger.warn("Exception while sending message to node " + node.getId() + ", " + e);
                }
            }
        } else {
            printMessageToConsole(message);
        }
    }

    private int sendRestMessage(Integer id, String message, Node node) throws IOException {
        waitForDelay();
        URL url = new URL("http://" + node.getIp() + ":" + node.getPort() + "/message?id=" + id);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setConnectTimeout(1000);
        con.setRequestMethod("POST");
        con.setRequestProperty("Content-Type", "text/plain");
        con.setDoOutput(true);
        con.getOutputStream().write(message.getBytes(StandardCharsets.UTF_8));
        return con.getResponseCode();
    }

    private void waitForDelay() {
        if (delay > 0) {
            logger.info("Waiting for delay...");
            try {
                Thread.sleep(delay);
            } catch (InterruptedException e) {
                logger.warn("Exception while sleeping, " + e);
            }
            logger.info("Delay finished");
        }
    }
    public boolean sendMessage(String input) {
        if (leader == null) {
            spreadMessage(ID, input);
            return true;
        } else {
            logger.info("Sending message to leader node " + leader.getId());
            try {
                int status = sendRestMessage(ID, input, leader);
                if (status == 200) {
                    logger.info("Received reply from leader node " + leader.getId() + ": Status: " + status);
                    return true;
                } else {
                    cachedMessages.add(input);
                    logger.error("Received reply from leader node " + leader.getId() + ": Status: " + status);
                    logger.info("Message cached");
                    logger.warn("Restarting election process...");
                    start_election();
                    return false;
                }

            } catch (IOException e) {
                cachedMessages.add(input);
                logger.error("Error while sending message to node " + leader.getId() + ", " + e);
                logger.info("Message cached");
                logger.warn("Restarting election process...");
                start_election();
                return false;
            }
        }
    }

    private void printMessageToConsole(String message) {

        if ("L". equals(message.split(" ")[5])) {
            if (leader == null) {
                System.out.println(message.split(" ")[1] + " The Leader is this node (" + ID + ")");
            }
            else {
                System.out.println(message.split(" ")[1] + " The Leader is: " + leader.getId());
            }
        }
        else {
            System.out.println(message);
        }
    }

    public void setForceExit(boolean force_exit) {
        this.forceExit = force_exit;
    }

    private void sendCachedMessages() {
        if (cachedMessages.isEmpty()) {
            return;
        }
        ArrayList<String> newCache = new ArrayList<>();
        logger.info("Sending cached messages");
        for (String message : cachedMessages) {
            if(!sendMessage(message)){
                newCache.add(message);
            }
        }
        if (!newCache.isEmpty()) {
            logger.warn("Some cached messages failed to send. Retrying...");
            cachedMessages = newCache;
        }
        else {
            logger.info("All cached messages sent successfully");
            cachedMessages.clear();
        }
    }

public void setDelay(int delay) {
        this.delay = delay;
    }
}

