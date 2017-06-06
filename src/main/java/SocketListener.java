import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;


public class SocketListener extends Thread {
    private static final int SOCKET_PORT = 9099;
    private ServerSocket service = null;
    private Socket socket = null;
    private ArrayList<Runnable> connectedClients;

    public SocketListener() {
        try {
            service = new ServerSocket(SOCKET_PORT);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static void main(String[] args) {
        SocketListener myService = new SocketListener();
        try {
            myService.listen();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public synchronized void start() {
        super.start();
    }

    @Override
    public void run() {
        try (PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream())); )
        {
            String inputLine, outputLine;
            while ((inputLine = in.readLine()) != null) {
                out.println(inputLine);
                if (inputLine.equals("Bye."))
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void listen() throws IOException {
        System.out.println("Listening on port " + SOCKET_PORT);
        // while (true) {
        socket = service.accept();

    }
}
