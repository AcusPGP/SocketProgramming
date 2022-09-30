package server.operation.download;

import lombok.extern.slf4j.Slf4j;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Slf4j
public class ServerDownload implements Runnable {
    private final int portFile;
    private final String rootFile;
    String listFile;

    DataInputStream disServer;
    DataOutputStream dosServer;

    public ServerDownload(int portFile, String rootFile) {
        this.portFile = portFile;
        this.rootFile = rootFile;
    }

    @Override
    public void run() {
        try {
            ServerSocket downloadServerSocket = new ServerSocket(portFile);
            // Waiting for client's connection
            log.info("Binding to port chat " + portFile + ", please wait......");
            log.info("Command server start: " + downloadServerSocket);
            log.info("Waiting for client's connection......");
            Socket downloadSocket = downloadServerSocket.accept();
            log.info("Client accepted: " + downloadSocket);
            disServer = new DataInputStream(downloadSocket.getInputStream());
            listFile = disServer.readUTF();
            System.out.println("Download stage: [" + listFile + "]");
            String[] list = listFile.split(",");

            FileOutputStream fos = new FileOutputStream(rootFile + "/" + "multiCompressed.zip");
            ZipOutputStream zipOut = new ZipOutputStream(fos);
            for (String fileName : list) {
                if (fileName.trim().equals("done")) {
                    continue;
                }
                File fileToZip = new File(rootFile + "/" + fileName.trim());
                FileInputStream fis = new FileInputStream(fileToZip);
                ZipEntry zipEntry = new ZipEntry(fileToZip.getName());
                zipOut.putNextEntry(zipEntry);

                byte[] bytes = new byte[1024];
                int length;
                while((length = fis.read(bytes)) >= 0) {
                    zipOut.write(bytes, 0, length);
                }
                fis.close();
            }
            zipOut.close();
            fos.close();
            dosServer = new DataOutputStream(downloadSocket.getOutputStream());
            FileInputStream fis = new FileInputStream(rootFile + "/" + "multiCompressed.zip");
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) >= 0) {
                dosServer.write(buffer, 0, bytesRead);
            }
            File deleteFile = new File(rootFile + "/" + "multiCompressed.zip");
            if (deleteFile.delete()) {
                System.out.println("Send and delete complete");
            }
            disServer.close();
            dosServer.close();
            downloadServerSocket.close();
            downloadSocket.close();
        } catch (IOException e) {
            log.info(e.getMessage());
            log.info("<run>: can not create the downloadServerSocket");
        }
    }
}
