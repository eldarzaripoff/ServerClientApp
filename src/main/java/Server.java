import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Scanner;

public class Server {

    private static Map<Integer, User> users = new HashMap<>();

    public static void main(String[] args) throws FileNotFoundException {
        System.out.println("Start server");
        Properties props = new Properties();
        try(InputStream inputStream = new FileInputStream("MyServerTest/src/main/resources/settings.txt")) {
            props.load(inputStream);
            int port = Integer.parseInt(props.getProperty("port"));
            try (ServerSocket serverSocket = new ServerSocket(port)) {
                while (true) {
                    try {
                        Socket clientSocket = serverSocket.accept();
                        sendMessToAll("К чату подключился новый участник: " + clientSocket.getPort());
                        new Thread(() -> {
                            try (PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)) { // канал записи в сокет
                                User user = new User(clientSocket, out);
                                users.put(clientSocket.getPort(), user);
                                System.out.println(user);
                                waitMessAndSend(clientSocket);
                            } catch (IOException e) {
                                e.printStackTrace();
                            } finally {
                                try {
                                    clientSocket.close(); // закрываем сокет клиента
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }).start();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }


    }

    public static synchronized void sendMessToAll(String mess) {
        for (Map.Entry<Integer, User> entry : users.entrySet()) {
            entry.getValue().getOut().println(mess);
            System.out.println("Отправлено");
        }
    }

    public static void waitMessAndSend(Socket clientSocket) {
        String timestamp = LocalDateTime.now().toString();
        try (Scanner inMess = new Scanner(clientSocket.getInputStream())) {

            users.get(clientSocket.getPort()).getOut().println("Введите ваше имя:");
            String name = inMess.nextLine();
            users.get(clientSocket.getPort()).setName(name);

            while (true) {
                if (inMess.hasNext()) {
                    String mess = inMess.nextLine();
                    switch (mess) {
                        default:
                            sendMessToAll(users.get(clientSocket.getPort()).getName() + ": " + mess + " : " + timestamp);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
