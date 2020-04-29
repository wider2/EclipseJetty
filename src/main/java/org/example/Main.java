package org.example;

import java.util.logging.Logger;

public class Main {

    private static Logger logger = Logger.getLogger(Main.class.getName());

    public static void main(String[] args) throws Exception {

        logger.info("Logger main app");

        JettyServer server = new JettyServer();
        server.start();
    }
    
}
