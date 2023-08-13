
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

class Client1Test {

    private static final String HOST = "127.0.0.1";
    private static final int PORT = 8090;
    private static final String EXITCHAT = "/exit";

    @Test
    void testClient() throws IOException {
        // Создаем заглушку сервера
        Server server = Mockito.mock(Server.class);

        // Создаем новый поток для запуска клиента
        AtomicBoolean flag = new AtomicBoolean(true);
        Thread clientThread = new Thread(() -> {
            try (Socket clientSocket = new Socket(HOST, PORT);
                 PrintWriter outMess = new PrintWriter(clientSocket.getOutputStream(), true);
                 BufferedReader inMess = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                 Scanner scannerConsole = new Scanner(System.in);
                 FileWriter fileWriter = new FileWriter("chat_log.txt", true)) {

                while (flag.get()) {
                    if (scannerConsole.hasNext()) {
                        String mess = scannerConsole.nextLine(); //берем сообщение клиента с консоли
                        if (mess.equalsIgnoreCase(EXITCHAT)) {
                            outMess.println(mess);
                            scannerConsole.close();
                            outMess.close();
                            try {
                                fileWriter.write(mess + "\n");
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                            try {
                                fileWriter.flush();
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                            flag.set(false);
                            break;
                        }
                        outMess.println(mess); // отправляем серверу
                        try {
                            fileWriter.write(mess + "\n");
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                        try {
                            fileWriter.flush();
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });

        // Запускаем клиента
        clientThread.start();

        // Ждем, пока клиент подключится к серверу
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }

        // Проверяем, что клиент отправляет сообщение на сервер
        Mockito.verify(server).broadcastMessage(Mockito.anyString());

        // Останавливаем клиента и сервер
        flag.set(false);
        clientThread.interrupt();
    }
}
