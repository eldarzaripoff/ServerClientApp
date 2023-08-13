import java.io.*;
import java.net.Socket;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicBoolean;

public class Client2 {

    static String host = "127.0.0.1";// хост моего компьютера
    static int port = 8090;

    private  static  final String EXITCHAT = "/exit";
    private static Socket clientSocket = null;
    private static BufferedReader inMess;
    private static PrintWriter outMess;
    private static Scanner scannerConsole;

    public static void main(String[] args) throws IOException {
        clientSocket = new Socket(host, port);
        outMess = new PrintWriter(clientSocket.getOutputStream(), true);
        inMess = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        scannerConsole = new Scanner(System.in);
        FileWriter fileWriter = new FileWriter("C:\\Users\\zarip\\IdeaProjects\\MyServerTest\\MyServerTest\\src\\main\\resources\\chat_log.txt", true);

        AtomicBoolean flag = new AtomicBoolean(true);

        //поток принимающий сообщения от сервера и печатающий в консоль
        new Thread(() -> {
            try {
                while (true) {
                    if (!flag.get()) {
                        inMess.close();
                        clientSocket.close();
                        break;
                    }
                    if (inMess.ready()) {
                        String messFormServer = inMess.readLine();
                        System.out.println(messFormServer);
                    }
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }).start();


        //поток отправляет сообщения на сервер
        new Thread(() -> {
            while (true) {
                if (scannerConsole.hasNext()) {
                    String mess = scannerConsole.nextLine(); //берем сообщение клиента с консоли
                    if (mess.equalsIgnoreCase(EXITCHAT)) {
                        outMess.println(mess);
                        scannerConsole.close();
                        outMess.close();
                        try {
                            fileWriter.write( mess + "\n");
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
                        fileWriter.write( mess + "\n");
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
        }).start();
    }
}

