package org.example.dsva.service;


import jakarta.annotation.PostConstruct;
import org.example.dsva.api.model.Node;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

@Service
public class EnvNodesService {
    List<Node> nodes;

    public EnvNodesService() {
        nodes = new ArrayList<>();
    }

    @PostConstruct
    public void init() {
        // TODO
        System.out.println("Environment init");
        readConfiguredNodes();
    }

    // Read nodes and ports from resources/static/connections.txt
    public void readConfiguredNodes() {
        try {
            // TODO Change path to connections.txt
            File myObj = new File("C:\\Users\\jirka\\JProjects\\DSVa\\src\\main\\resources\\connections.txt");
            Scanner myReader = new Scanner(myObj);
            while (myReader.hasNextLine()) {
                String data = myReader.nextLine();

                // Split line by space and create new node
                String[] nodeData = data.split(" ");
                Node node = new Node(nodeData[0], nodeData[1]);
                nodes.add(node);

                System.out.println(node);
            }
            myReader.close();
        } catch (FileNotFoundException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
    }
}

