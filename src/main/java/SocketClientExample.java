import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;

public class SocketClientExample {

    public static void main(String[] args) {
        InetSocketAddress hostAddress = new InetSocketAddress("localhost", 8090);
        SocketChannel client = null;
        // ServiceLoader<ExampleClass> loader = ServiceLoader.load(ExampleClass.class);
        // Iterator<ExampleClass> iterator = loader.iterator();
        // String definition = null;
        // ExampleClass ex = iterator.next();
        // definition = ex.getaString();
        // try {
        // while (definition == null && iterator.hasNext()) {
        //      ex = iterator.next();
        //     definition = ex.getaString();
        // }
        // } catch (ServiceConfigurationError serviceError) {
        //     definition = null;
        //     serviceError.printStackTrace();
        // }

        try {
            client = SocketChannel.open(hostAddress);
            System.out.println("Client... started");
            String threadName = Thread.currentThread().getName();


            // PrintWriter out = new PrintWriter(echoSocket.getOutputStream(), true);
            // BufferedReader in = new BufferedReader(new InputStreamReader(echoSocket.getInputStream()));
            BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));

            String userInput;
            // ByteBuffer buffer = ByteBuffer.wrap("hey hi".getBytes());
            while (!((userInput = stdIn.readLine()).equals(""))) {
                ByteBuffer buffer = ByteBuffer.wrap("dimitris,123,3123124454".getBytes());
                client.write(buffer);
                // buffer.clear();
                // int numRead= client.read(buffer);
                // byte[] data = new byte[numRead];
                // System.arraycopy(buffer.array(), 0, data, 0, numRead);
                // String value =  new String(data);
                // System.out.println(value);
            }
            // Send messages to server

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                client.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}