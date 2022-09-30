package client.operation;

import client.operation.chat.ClientChat;
import client.operation.download.ClientDownload;
import lombok.extern.slf4j.Slf4j;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

@Slf4j
public class ClientCommand {
    // Variables
    private final int portFile;
    private final int portChat;
    private final String clientIPAddress;
    private final String rootDownload;
    private final Socket clientSocketCommand;
    private final String clientName;
    String receiveFromServer;
    String[] listFile;
    Set<String> listFileToDownload;
    String file;

    // Functions
    Scanner sc = new Scanner(System.in);
    DataInputStream disClient;
    DataOutputStream dosClient;

    public ClientCommand(int portFile, int portChat, String clientIPAddress, String rootDownload, Socket clientSocketCommand, String clientName) {
        this.portFile = portFile;
        this.portChat = portChat;
        this.clientIPAddress = clientIPAddress;
        this.rootDownload = rootDownload;
        this.clientSocketCommand = clientSocketCommand;
        this.clientName = clientName;
    }

    /**
     * Stage 3: Sending command to the server
     */
    public String proceed() {
        String result = "proceed";
        String inputCommand = sc.nextLine().trim();
        inputCommand = inputCommand.toLowerCase().trim();
        try {
            disClient = new DataInputStream(clientSocketCommand.getInputStream());
            dosClient = new DataOutputStream(clientSocketCommand.getOutputStream());
            switch (inputCommand) {
                // case for function "show and download files from the server"
                case "1", "show", "download", "s", "d" -> {
                    dosClient.writeUTF("1");
                    receiveFromServer = disClient.readUTF();
                    System.out.println(receiveFromServer);
                    receiveFromServer = disClient.readUTF();
                    listFile = receiveFromServer.split(" ");
                    int i = 1;
                    for (String file : listFile) {
                        System.out.println(i + ". " + file);
                        i++;
                    }
                    receiveFromServer = disClient.readUTF();
                    System.out.println(receiveFromServer);
                    enterFileName();
                    ClientDownload clientDownload = new ClientDownload(portFile, clientIPAddress, rootDownload, file);
                    clientDownload.proceed();
                }
                // case for function "chat with other clients"
                case "2" -> {
                    dosClient.writeUTF("2");
                    receiveFromServer = disClient.readUTF();
                    System.out.println(receiveFromServer);
                    ClientChat clientChat = new ClientChat(portChat, clientIPAddress, clientName);
                    clientChat.proceed();
                }
                // case for function "quit the program"
                case "3", "quit", "exit", "q", "e" -> {
                    result = "exit";
                    dosClient.writeUTF("3");
                    receiveFromServer = disClient.readUTF();
                    System.out.println(receiveFromServer);
                    disClient.close();
                    dosClient.close();
                }
                default -> System.out.println("Please choose an option to send to the server.");
            }
        } catch (IOException e) {
            log.info(e.getMessage());
            log.info("<proceed>: can not send the command to the server");
        }
        return result;
    }

    public void enterFileName() {
        if (listFileToDownload == null) {
            listFileToDownload = new HashSet<>();
        }
        System.out.print("Please enter file names to download: ");
        String a = sc.nextLine().trim();
        if (a.equals("")) {
            System.out.println("Please enter again!");
            enterFileName();
        } else {
            String[] b = a.split(",");
            for (String temp : b) {
                int check = checkFileExist(temp.trim());
                if (check == 1) {
                    listFileToDownload.add(temp.trim());
                } else {
                    System.out.println("The file name '" + temp.trim() + ", does not exist. Please try again!");
                    enterFileName();
                }
            }
            moreFilesToDownload();
        }
        file = listFileToDownload.toString();
        file = file.substring(1, file.length() - 1);
        if (listFileToDownload.size() == 1) {
            System.out.println("The file you want to download is " + file + ".");
        } else {
            System.out.println("The files you want to download are " + file + ".");
        }
    }

    public int checkFileExist(String temp) {
        int check = 0;
        for (String file : listFile) {
            if (temp.equals(file)) {
                check = 1;
                return check;
            }
        }
        return check;
    }

    public void moreFilesToDownload() {
        System.out.print("Do you want to download more? (Yes/No) ");
        String answer = sc.nextLine().trim();
        switch (answer) {
            case "Yes", "yes", "y" -> enterFileName();
            case "No", "no", "n" -> System.out.print("Please wait to process the download stage. ");
            default -> {
                System.out.println("It is not a right answer. Please try again!");
                moreFilesToDownload();
            }
        }
    }
}
