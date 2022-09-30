package server;

import lombok.extern.slf4j.Slf4j;
import server.operation.ServerCommand;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
public class ServerConnection {
    // Variables
    private final int portCommand; // 6000
    private final int portFile; // 6001
    private final int portChat; // 6002
    private final String serverIPAddress; // "127.0.0.1"
    private final String rootFile; // "src/server/file"
    private static final int NUM_OF_THREAD = 4;

    public ServerConnection(String[] serverConfigValues) {
        this.portCommand = Integer.parseInt(serverConfigValues[0]);
        this.portFile = Integer.parseInt(serverConfigValues[1]);
        this.portChat = Integer.parseInt(serverConfigValues[2]);
        this.serverIPAddress = serverConfigValues[3];
        this.rootFile = serverConfigValues[4];
    }

    /**
     * Stage 2: Starting a command stage to receive client's message
     */
    public synchronized void startServerCommand() {
        ExecutorService executorService = Executors.newFixedThreadPool(NUM_OF_THREAD);
        ServerSocket commandServerSocket = null;
        try {
            // Waiting for client's connection
            log.info("Binding to port command " + portCommand + ", please wait......");
            commandServerSocket = new ServerSocket(portCommand);
            log.info("Command server start: " + commandServerSocket);
            log.info("Waiting for client's connection......");
            while (true) {
                try {
                    // Accepting the client's connection
                    Socket commandSocket = commandServerSocket.accept();
                    log.info("Client accepted: " + commandSocket);
                    // Creating a thread for each client's connection, maximum is serving 4 clients at a time
                    ServerCommand serverCommand = new ServerCommand(portFile, portChat, serverIPAddress, rootFile, commandSocket);
                    executorService.execute(serverCommand);
                } catch (IOException e) {
                    log.info(e.getMessage());
                    log.info("<startServerCommand>: Can not connect to the client");
                }
            }
        } catch (IOException e) {
            log.info(e.getMessage());
            log.info("<startServerCommand>: Can not create the commandServerSocket");
        } finally {
            executorService.shutdown();
            try {
                if (commandServerSocket != null) {
                    commandServerSocket.close();
                }
            } catch (IOException e) {
                log.info(e.getMessage());
                log.info("<startServerCommand>: Can not close the commandServerSocket");
            }
        }
    }
}
