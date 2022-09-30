package client;

import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.Scanner;

@Slf4j
public class ClientConfiguration {
    static Scanner sc = new Scanner(System.in);
    private static String[] clientConfigValues;
    private static String clientName;

    /**
     * Stage 1 : Get and check client values.
     */
    public static void main(String[] args) {
        getValuesFromConfiguration();
        checkFolderExist(clientConfigValues[4]);
        checkBeforeClientConnection();
        String inputCommand = sc.nextLine().trim();
        if (inputCommand.equals("")) {
            //Checking stage
            if (!checkConfigurationValues() || !checkPort()) {
                log.info("Please check the client's configuration in the config.properties file again.");
                System.exit(0);
            }
            clientName = enterClientName();
            ClientConnection clientConnection = new ClientConnection(clientConfigValues, clientName);
            clientConnection.connectToServerCommand();
        } else {
            main(args);
        }
    }

    /**
     * Getting values from the config.properties file for checking stages.
     */
    private static void getValuesFromConfiguration() {
        Properties properties = new Properties();
        try {
            properties.load(new FileInputStream("src/main/resources/configuration/config.properties"));
            String portCommand = properties.getProperty("portCommand").trim(); // "6000"
            String portFile = properties.getProperty("portFile").trim(); // "6001"
            String portChat = properties.getProperty("portChat").trim(); // "6002"
            String clientIPAddress = properties.getProperty("clientIPAddress").trim(); // "127.0.0.1"
            File fileDownload = new File(properties.getProperty("rootDownload"));
            String rootDownload = fileDownload.getAbsolutePath(); // "download" folder
            clientConfigValues = new String[]{portCommand, portFile, portChat, clientIPAddress, rootDownload};
        } catch (IOException e) {
            log.info(e.getMessage());
            log.info("<getValuesFromConfiguration>: Can not read the 'config.properties' file");
        }
    }

    /**
     * Checking stages.
     */
    private static void checkBeforeClientConnection() {
        System.out.println("--------------------------Check the client's configuration-------------------------");
        System.out.println("Make sure the argument values in the configuration is correct.");
        System.out.println("First value - command port (port for sending command to the server): " + clientConfigValues[0]);
        System.out.println("Second value - file ports (port for receiving file from the server) : " + clientConfigValues[1]);
        System.out.println("Third value - chat port (port for chatting with other clients): " + clientConfigValues[2]);
        System.out.println("Fourth value - client's IP address: 'localhost' or your IP address: " + clientConfigValues[3]);
        System.out.println("Fifth value - direction for 'download' folder: " + clientConfigValues[4]);
        System.out.println("-----------------------------------------------------------------------------------");
        System.out.println("If the config.properties is correct, please press 'enter' to continue.");
    }

    // Entering the user name
    public static String enterClientName() {
        System.out.print("Enter your name here to connect to the server: ");
        clientName = sc.nextLine().trim();
        if (clientName.equals("")) {
            System.out.print("Please try again!.");
            enterClientName();
        }
        return clientName;
    }

    // Checking if the "download" folder exists.
    private static void checkFolderExist(String rootDownload) {
        File file = new File(rootDownload);
        if (file.mkdir()) {
            System.out.println("Folder created: " + file.getName());
        }
    }

    // Checking the number of values in the configuration.
    private static boolean checkConfigurationValues() {
        return clientConfigValues.length == 5;
    }

    // Checking the ports if they are numbers.
    private static boolean checkPort() {
        try {
            Integer.parseInt(clientConfigValues[0]);
            Integer.parseInt(clientConfigValues[1]);
            Integer.parseInt(clientConfigValues[2]);
            return true;
        } catch (NumberFormatException e) {
            log.info(e.getMessage());
            log.info("<checkPort>");
        }
        return false;
    }
}
