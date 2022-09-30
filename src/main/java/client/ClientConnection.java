package client;

import client.operation.ClientCommand;
import lombok.extern.slf4j.Slf4j;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

@Slf4j
public class ClientConnection {
    // Variables
    private final int portCommand; // 6000
    private final int portFile; // 6001
    private final int portChat; // 6002
    private final String clientIPAddress; // "127.0.0.1"
    private final String rootDownload; // "download"
    private final String clientName; // "clientName"
    private Socket clientSocketCommand;
    String receiveFromServer;

    // Functions
    DataInputStream disClient;
    DataOutputStream dosClient;

    public ClientConnection(String[] clientConfigValues, String clientName) {
        this.portCommand = Integer.parseInt(clientConfigValues[0]);
        this.portFile = Integer.parseInt(clientConfigValues[1]);
        this.portChat = Integer.parseInt(clientConfigValues[2]);
        this.clientIPAddress = clientConfigValues[3];
        this.rootDownload = clientConfigValues[4];
        this.clientName = clientName;
    }

    /**
     * Stage 2: Starting a command stage for sending client's command to the server
     */
    public void connectToServerCommand() {
        try {
            // Creating a clientSocketCommand to connect to the server
            if (clientSocketCommand == null) {
                clientSocketCommand = new Socket(clientIPAddress, portCommand);
            }
            System.out.println("Connected: " + clientSocketCommand + " - Client name: " + clientName);
            // Sending the user name to the server
            sendClientNameToServer();
            // Displaying the server's service menu
            receiveMenuFromServer();
            while (true) {
                clientMenu();
                ClientCommand clientCommand = new ClientCommand(portFile, portChat, clientIPAddress, rootDownload, clientSocketCommand, clientName);
                String result = clientCommand.proceed();
                if (result.equals("exit")) {
                    break;
                }
            }
        } catch (IOException e) {
            log.info(e.getMessage());
            log.info("<connectToServerCommand>: Can not connect to the sever");
        } finally {
            try {
                dosClient.close();
                disClient.close();
                if (clientSocketCommand != null) {
                    try {
                        clientSocketCommand.close();
                    } catch (IOException e) {
                        log.info(e.getMessage());
                        log.info("<connectToServerCommand>: Can not close the clientSocketCommand");
                    }
                }
            } catch (IOException e) {
                log.info(e.getMessage());
                log.info("<connectToServerCommand>: Can not close the disClient, dosClient");
            }
        }
    }

    // Sending the client name to the server
    public void sendClientNameToServer() {
        try {
            dosClient = new DataOutputStream(clientSocketCommand.getOutputStream());
            dosClient.writeUTF(clientName);
        } catch (IOException e) {
            log.info(e.getMessage());
            log.info("<sendClientNameToServer>: can not send the client name to the server");
        }
    }

    // Receiving menu from the server
    public void receiveMenuFromServer() {
        try {
            disClient = new DataInputStream(clientSocketCommand.getInputStream());
            receiveFromServer = disClient.readUTF();
        } catch (IOException e) {
            log.info(e.getMessage());
            log.info("<receiveMenuFromServer>: can not receive the service menu");
        }
    }

    public void clientMenu() {
        String[] menu = receiveFromServer.split(",");
        for (int i = 0; i < menu.length - 1; i++) {
            System.out.println(menu[i]);
        }
        System.out.print(menu[menu.length - 1]);
    }
}
