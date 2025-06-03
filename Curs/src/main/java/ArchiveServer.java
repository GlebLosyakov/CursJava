import com.mycompany.curs.NewJFrame;
import java.io.*;
import java.net.*;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;



public class ArchiveServer {
    private static final int PORT = 12345;
    private static final ExecutorService threadPool = Executors.newFixedThreadPool(10);

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Сервер архиватора запущен на порту " + PORT);
            
            while (true) {
                Socket clientSocket = serverSocket.accept();
                threadPool.execute(new ClientHandler(clientSocket));
            }
        } catch (IOException e) {
            System.err.println("Ошибка сервера: " + e.getMessage());
        }
    }

    private static class ClientHandler implements Runnable {
        private final Socket clientSocket;

        public ClientHandler(Socket socket) {
            this.clientSocket = socket;
        }

        @Override
        public void run() {
            try (ObjectInputStream ois = new ObjectInputStream(clientSocket.getInputStream());
                 ObjectOutputStream oos = new ObjectOutputStream(clientSocket.getOutputStream())) {
                
                String command = ois.readUTF();
                byte[] fileData = (byte[]) ois.readObject();
                String fileName = ois.readUTF();
                
                byte[] result;
                String resultExtension;
                
                if ("compress".equals(command)) {
                    result = NewJFrame.HuffmanZipCompressor.compressForNetwork(fileData);
                    resultExtension = ".zip";
                } else if ("decompress".equals(command)) {
                    result = NewJFrame.HuffmanZipDecompressor.decompressFromNetwork(fileData);
                    resultExtension = ".docx";
                } else {
                    throw new IllegalArgumentException("Неизвестная команда: " + command);
                }
                
                oos.writeObject(result);
                oos.writeUTF(fileName.replaceFirst("\\..+$", "") + resultExtension);
                oos.flush();
                
            } catch (Exception e) {
                System.err.println("Ошибка обработки клиента: " + e.getMessage());
            }
        }
    }
}
