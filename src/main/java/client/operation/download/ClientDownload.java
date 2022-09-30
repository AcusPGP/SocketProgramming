package client.operation.download;

import lombok.extern.slf4j.Slf4j;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Slf4j
public class ClientDownload {
    private final int portFile;
    private final String clientIPAddress;
    private final String rootDownload;
    String listFileToDownload;
    private Socket clientSocketDownload;

    DataOutputStream dosClient;
    DataInputStream disClient;

    public ClientDownload(int portFile, String clientIPAddress, String rootDownload, String listFile) {
        this.portFile = portFile;
        this.clientIPAddress = clientIPAddress;
        this.rootDownload = rootDownload;
        this.listFileToDownload = listFile + ", done";
    }

    public void proceed() {
        try {
            if (clientSocketDownload == null) {
                clientSocketDownload = new Socket(clientIPAddress, portFile);
            }
            dosClient = new DataOutputStream(clientSocketDownload.getOutputStream());
            dosClient.writeUTF(listFileToDownload);
            System.out.println("Downloading.....");
            disClient = new DataInputStream(clientSocketDownload.getInputStream());
            FileOutputStream fos = new FileOutputStream(rootDownload + "/unzipFile.zip");
            byte[] buffer = new byte[4096];
            int count;
            while ((count = disClient.read(buffer)) >= 0) {
                fos.write(buffer, 0, count);
            }
            unzipFile();
            File deleteFile = new File(rootDownload + "/unzipFile.zip");
            deleteFile.delete();
            System.out.println("Downloading successfully.");
        } catch (IOException e) {
            log.info(e.getMessage());
            log.info("<proceed>: can not create the clientSocketDownload");
        } finally {
            try {
                if (clientSocketDownload != null) {
                    clientSocketDownload.close();
                }
                dosClient.close();
                disClient.close();
            } catch (IOException e) {
                log.info(e.getMessage());
                log.info("<proceed>: can not close the clientSocketDownload");
            }
        }
    }

    public void unzipFile() throws IOException {
       String fileZip = rootDownload + "/unzipFile.zip";
       File destDir = new File(rootDownload);
       byte[] buffer = new byte[1024];
        ZipInputStream zis = new ZipInputStream(new FileInputStream(fileZip));
        ZipEntry zipEntry = zis.getNextEntry();
        while (zipEntry != null) {
           File newFile = newFile(destDir, zipEntry);
           if (zipEntry.isDirectory()) {
              if (!newFile.isDirectory() && !newFile.mkdirs()) {
                 throw new IOException("Failed to create directory " + newFile);
              }
           } else {
              // fix for Windows-created archives
              File parent = newFile.getParentFile();
              if (!parent.isDirectory() && !parent.mkdirs()) {
                 throw new IOException("Failed to create directory " + parent);
              }

              // write file content
              FileOutputStream fos = new FileOutputStream(newFile);
              int len;
              while ((len = zis.read(buffer)) > 0) {
                 fos.write(buffer, 0, len);
              }
              fos.close();
           }
           zipEntry = zis.getNextEntry();
        }
       zis.closeEntry();
       zis.close();
    }

   public static File newFile(File destinationDir, ZipEntry zipEntry) throws IOException {
      File destFile = new File(destinationDir, zipEntry.getName());

      String destDirPath = destinationDir.getCanonicalPath();
      String destFilePath = destFile.getCanonicalPath();

      if (!destFilePath.startsWith(destDirPath + File.separator)) {
         throw new IOException("Entry is outside of the target dir: " + zipEntry.getName());
      }

      return destFile;
   }
}
