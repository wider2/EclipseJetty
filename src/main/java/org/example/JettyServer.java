package org.example;

import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.util.thread.ExecutorThreadPool;
import java.util.logging.Logger;

public class JettyServer {
    private Server server;
    private Logger logger = Logger.getLogger(JettyServer.class.getName());

    public void start() throws Exception {
        ExecutorThreadPool threadPool = new ExecutorThreadPool();
        server = new Server(threadPool);

        ServerConnector serverConnector = new ServerConnector(server);
        serverConnector.setPort(8082);
        Connector[] connectors = {
                serverConnector
        };

        server.setConnectors(connectors);
        server.setHandler(new MyHandler());
        //server.setStopTimeout(30);
        server.start();
        logger.info("Jetty server started");
    }

    public void stop() throws Exception {
        server.stop();
        logger.info("Jetty server stopped");
    }
}
