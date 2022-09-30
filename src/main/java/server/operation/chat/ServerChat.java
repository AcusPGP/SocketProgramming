package server.operation.chat;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

@Slf4j
public class ServerChat implements Runnable {
    private final int portChat;

    public ServerChat(int portChat) {
        this.portChat = portChat;
    }

    @Override
    public void run() {
        try {
            ServerSocket chatServerSocket = new ServerSocket(portChat);
            // Waiting for client's connection
            log.info("Binding to port chat " + portChat + ", please wait......");
            log.info("Command server start: " + chatServerSocket);
            log.info("Waiting for client's connection......");
            Socket chatSocket = chatServerSocket.accept();
            log.info("Client accepted: " + chatSocket);
        } catch (IOException e) {
            log.info(e.getMessage());
            log.info("<run>: can not create the chatServerSocket");
        }
    }
}
