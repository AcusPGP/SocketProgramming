package server;

import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

@Slf4j
public class ServerConfiguration {
    private static String[] serverConfigValues;

    /**
     * Stage 1 : Get and check server values.
     */
    public static void main(String[] args) {
        getValuesFromConfiguration();
        checkBeforeServerConnection();
        if (!checkConfigurationValues() || !checkPort()) {
            log.info("Please check the server's configuration in the config.properties file again.");
            System.exit(0);
        }
        ServerConnection serverConnection = new ServerConnection(serverConfigValues);
        serverConnection.startServerCommand();
    }

    /**
     * Getting values from the config.properties file for checking stages
     */
    private static void getValuesFromConfiguration() {
        Properties properties = new Properties();
        try {
            properties.load(new FileInputStream("src/main/resources/configuration/config.properties"));
            String portCommand = properties.getProperty("portCommand").trim(); // "6000"
            String portFile = properties.getProperty("portFile").trim(); // "6001"
            String portChat = properties.getProperty("portChat").trim(); // "6002"
            String serverIPAddress = properties.getProperty("serverIPAddress").trim(); // "127.0.0.1"
            File downloadFile = new File(properties.getProperty("rootFile").trim());
            String rootFile = downloadFile.getAbsolutePath(); // "src/server/file"
            serverConfigValues = new String[]{portCommand, portFile, portChat, serverIPAddress, rootFile};
        } catch (IOException e) {
            log.info(e.getMessage());
            log.info("<getValuesFromConfiguration>: Can not read the 'config.properties' file");
        }
    }

    /**
     * Checking stages.
     */
    private static void checkBeforeServerConnection() {
        System.out.println("--------------------------Check the client's configuration-------------------------");
        System.out.println("Make sure the argument values in the configuration is correct.");
        System.out.println("First value - command port (port for sending command to the server): " + serverConfigValues[0]);
        System.out.println("Second value - file ports (port for receiving file from the server) : " + serverConfigValues[1]);
        System.out.println("Third value - chat port (port for chatting with other clients): " + serverConfigValues[2]);
        System.out.println("Fourth value - client's IP address: 'localhost' or your IP address: " + serverConfigValues[3]);
        System.out.println("Fifth value - direction for 'download' folder: " + serverConfigValues[4]);
        System.out.println("-----------------------------------------------------------------------------------");
    }

    // Checking the number of values in the configuration.
    private static boolean checkConfigurationValues() {
        return serverConfigValues.length == 5;
    }

    // Checking the ports if they are numbers.
    private static boolean checkPort() {
        try {
            Integer.parseInt(serverConfigValues[0]);
            Integer.parseInt(serverConfigValues[1]);
            Integer.parseInt(serverConfigValues[2]);
            return true;
        } catch (NumberFormatException e) {
            log.info(e.getMessage());
            log.info("<checkPort>");
        }
        return false;
    }
}
