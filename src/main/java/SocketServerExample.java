import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.connection.Connection;
import org.bson.Document;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.*;

public class SocketServerExample {
    public static final Thread mainThread = Thread.currentThread();
    private static boolean keepRunning = true;
    private Selector selector;
    private Map<SocketChannel, List> dataMapper;
    private InetSocketAddress listenAddress;
    private ServerSocketChannel serverChannel;
    protected MongoClient dbClient;
    protected MongoDatabase db;
    private static final String MONGO_DB = "health_monitor";

    public SocketServerExample(String address, int port) throws IOException {
        listenAddress = new InetSocketAddress(address, port);
        dataMapper = new HashMap<SocketChannel, List>();
    }



    public static void main(String[] args) throws Exception {
        PropertyFileLoader propertyFileLoader = new PropertyFileLoader();

        final SocketServerExample serverSocket = new SocketServerExample(propertyFileLoader.getProperty("address"),
                Integer.valueOf(propertyFileLoader.getProperty("port")));

        serverSocket.dbClient = new MongoClient(propertyFileLoader.getProperty("mongo_address"),new MongoClientOptions.Builder()
                .maxConnectionIdleTime(Integer.valueOf(propertyFileLoader.getProperty("mongo_connection_idle_time"))).build());
        serverSocket.db = serverSocket.dbClient.getDatabase(MONGO_DB);

        Runnable server = new Runnable() {
            @Override
            public void run() {
                try {
                    serverSocket.startServer();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };

        ArrayList<Thread> listOfThreads = new ArrayList<>();
        listOfThreads.add(new Thread(server));
        listOfThreads.get(0).start();

        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                System.out.println("Shutdown--------------------------");
                keepRunning = false;
                try {
                    try {
                        serverSocket.dbClient.close();
                        serverSocket.serverChannel.close();
                        serverSocket.selector.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    mainThread.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    // create server channel	
    private void startServer() throws IOException {
        this.selector = Selector.open();
        serverChannel = ServerSocketChannel.open();
        serverChannel.configureBlocking(false);

        // retrieve server socket and bind to port
        serverChannel.socket().bind(listenAddress);
        serverChannel.register(this.selector, SelectionKey.OP_ACCEPT);
        System.out.println("Server started...");

        while (true) {
            // wait for events
            this.selector.select();
            //work on selected keys
            Iterator keys = this.selector.selectedKeys().iterator();
            while (keys.hasNext()) {
                SelectionKey key = (SelectionKey) keys.next();
                // this is necessary to prevent the same key from coming up 
                // again the next time around.
                keys.remove();
                if (!key.isValid()) {
                    continue;
                }
                if (key.isAcceptable()) {
                    this.accept(key);
                } else if (key.isReadable()) {
                    this.read(key);
                }
            }
        }
    }

    //accept a connection made to this channel's socket
    private void accept(SelectionKey key) throws IOException {
        ServerSocketChannel serverChannel = (ServerSocketChannel) key.channel();
        SocketChannel channel = serverChannel.accept();
        channel.configureBlocking(false);
        Socket socket = channel.socket();
        SocketAddress remoteAddr = socket.getRemoteSocketAddress();
        System.out.println("Connected to: " + remoteAddr);

        // register channel with selector for further IO
        dataMapper.put(channel, new ArrayList());
        channel.register(this.selector, SelectionKey.OP_READ);
    }

    //read from the socket channel
    private void read(SelectionKey key) throws IOException {
        SocketChannel channel = (SocketChannel) key.channel();
        ByteBuffer buffer = ByteBuffer.allocate(100 * 1024);
        int numRead = -1;
        numRead = channel.read(buffer);

        if (numRead == -1) {
            this.dataMapper.remove(channel);
            Socket socket = channel.socket();
            this.dbClient.close();
            SocketAddress remoteAddr = socket.getRemoteSocketAddress();
            System.out.println("Connection closed by client: " + remoteAddr);
            channel.close();
            key.cancel();
            return;
        }

        byte[] data = new byte[numRead];
        System.arraycopy(buffer.array(), 0, data, 0, numRead);
        String value =  new String(data);
        System.out.println(value);
        //find the topic
       String[] payload= value.split("\n");
        String topic= payload[0];
        String id = payload[1];
        String[] rows = payload[2].split("%");
        for(int i=0;i< rows.length; i++) {
            rows[i] = rows[i].trim();
        }
        List<Document> listOfDocuments = new ArrayList<>();
        MongoCollection<Document> collection = db.getCollection(topic+"_"+id);
        for(String row : rows) {
            String[] values = row.split(",");
            listOfDocuments.add(new Document("id", id).append("pressure", values[0]).append("timestamp", Long.valueOf(values[1])));
        }
        collection.insertMany(listOfDocuments);
    }

    //Write to the client
    private void write(SelectionKey key) {
        SocketChannel channel = (SocketChannel) key.channel();
        ByteBuffer buffer = ByteBuffer.allocate(100 * 1024); //100 KB
        try {
            channel.write(buffer.wrap("hey hello!".getBytes()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}