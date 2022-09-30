package client.operation.chat;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.Socket;

@Slf4j
public class ClientChat {
    // Variables
    private final int portChat;
    private final String clientIPAddress;
    private final String clientName;
    private Socket clientSocketChat = null;

    public ClientChat(int portChat, String clientIPAddress, String clientName) {
        this.portChat = portChat;
        this.clientIPAddress = clientIPAddress;
        this.clientName = clientName;
    }

    public void proceed() {
        try {
            // Creating a clientSocketChat to connect to the server
            if (clientSocketChat == null) {
                clientSocketChat = new Socket(clientIPAddress, portChat);
            }
        } catch (IOException e) {
            log.info(e.getMessage());
            log.info("<proceed>: can not create the clientSocketChat");
        }
    }
}
