package server.operation;

import lombok.extern.slf4j.Slf4j;
import server.operation.chat.ServerChat;
import server.operation.download.ServerDownload;
import server.utilities.ServerUtilities;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.Socket;

@Slf4j
public class ServerCommand implements Runnable {
    // Variables
    private final int portFile;
    private final int portChat;
    private final String serverIPAddress;
    private final String rootFile;
    private final Socket commandSocket;
    String clientName;
    String[] list;

    // Functions
    DataInputStream disServer;
    DataOutputStream dosServer;

    public ServerCommand(int portFile, int portChat, String serverIPAddress, String rootFile, Socket commandSocket) {
        this.portFile = portFile;
        this.portChat = portChat;
        this.serverIPAddress = serverIPAddress;
        this.rootFile = rootFile;
        this.commandSocket = commandSocket;
    }

    /**
     * Stage 3: Receiving the client's command to operate the server's services
     */
    @Override
    public void run() {
        String result = "run";
        receiveClientName();
        sendMenuToClient();
        try {
            // disServer is already declared in receiveClientName()
            // dosServer is already declared in sendMenuToClient()
            do {
                String commandFromClient = disServer.readUTF();
                System.out.println("Command from client " + clientName + ": " + commandFromClient);
                switch (commandFromClient) {
                    case "1" -> {
                        dosServer.writeUTF(ServerUtilities.LISTFILE_1);
                        File file = new File(rootFile);
                        list = file.list();
                        StringBuilder listFiles = new StringBuilder();
                        assert list != null;
                        for (String temp : list) {
                            listFiles.append(temp).append(" ");
                        }
                        dosServer.writeUTF(listFiles.toString());
                        dosServer.writeUTF(ServerUtilities.LISTFILE_2);
                        ServerDownload serverDownload = new ServerDownload(portFile, rootFile);
                        serverDownload.run();
                    }
                    case "2" -> {
                        dosServer.writeUTF("Connecting to chat window......");
                        ServerChat serverChat = new ServerChat(portChat);
                        serverChat.run();
                    }
                    case "3" -> {
                        result = "exit";
                        dosServer.writeUTF("Bye " + clientName);
                    }
                }
            } while (!result.equals("exit"));
        } catch (IOException e) {
            log.info(e.getMessage());
            log.info("<run>: can not receive command from client " + clientName);
        } finally {
            try {
                disServer.close();
                dosServer.close();
                if (result.equals("exit")) {
                    commandSocket.close();
                }
            } catch (IOException e) {
                log.info(e.getMessage());
                log.info("<run>: Can not close the functions");
            }
        }
    }

    public void receiveClientName() {
        try {
            disServer = new DataInputStream(commandSocket.getInputStream());
            clientName = disServer.readUTF();
            log.info("Start receiving command from client " + clientName + " - " + commandSocket.getPort());
        } catch (IOException e) {
            log.info(e.getMessage());
            log.info("<receiveClientName>: can not receive name of the client");
        }
    }

    public void sendMenuToClient() {
        try {
            dosServer = new DataOutputStream(commandSocket.getOutputStream());
            StringBuilder stringBuilder = new StringBuilder();
            for (String temp : ServerUtilities.MENU) {
                stringBuilder.append(temp).append(",");
            }
            dosServer.writeUTF(stringBuilder.toString());
        } catch (IOException e) {
            log.info(e.getMessage());
            log.info("<sendMenuToClient>: can not send the service menu to client");
        }
    }
}
