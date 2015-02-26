package KPTest;

import java.net.*;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.HashMap;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

public class SelectorServer {
    ServerSocketChannel scc;
    KPLogger logger;
    static final String SERVER_CHANNEL = "ServerChannel";
    static final String CLIENT_CHANNEL = "ClientChannel";
    public SelectorServer() throws IOException {
        this.logger = new KPLogger("Server");
        this.scc = ServerSocketChannel.open();
        this.scc.socket().bind(new InetSocketAddress(8000));
        logger.log(String.format("started at %s", this.scc.socket().getLocalSocketAddress()));

    }

    public void run(){
        logger.log("running...");
        try {
            this.scc.configureBlocking(false);
            Selector selector = Selector.open();
            SelectionKey serverKey = this.scc.register(selector, SelectionKey.OP_ACCEPT);
            serverKey.attach(SERVER_CHANNEL);
            HashMap<SocketChannel, Queue<String>> pendingDataMap = new HashMap<SocketChannel, Queue<String>>();
            final int bufsize_ = 48;
            ByteBuffer sharedBuf = ByteBuffer.allocate(bufsize_);
            sharedBuf.clear();
            while(true){
                int readyChannels = selector.select();
                if (readyChannels==0) continue;
                if (readyChannels!=0){
                    Set<SelectionKey> keys = selector.selectedKeys();
                    for (SelectionKey key : keys){
                        if (key.isValid()){
                            if (key.isAcceptable() && key.attachment().equals(SERVER_CHANNEL)){
                                /// Accepting new Sockets
                                SocketChannel clientSocketChannel = this.scc.accept();
                                if (clientSocketChannel!=null) {
                                    logger.log(String.format("accepted %s", clientSocketChannel.socket().getRemoteSocketAddress().toString()));
                                    clientSocketChannel.configureBlocking(false);
                                    SelectionKey clientKey = clientSocketChannel.register(selector, SelectionKey.OP_WRITE | SelectionKey.OP_READ);
                                    clientKey.attach(CLIENT_CHANNEL);
                                    pendingDataMap.put(clientSocketChannel, new ConcurrentLinkedQueue<String>());
                                    Queue<String> pendingDataQueue = pendingDataMap.get(clientSocketChannel);
                                    pendingDataQueue.add("> connected to KPTest.SelectorServer\n");
                                }
                            } if (key.isReadable() && key.attachment().equals(CLIENT_CHANNEL)) {
                                //Reading from connection socket.
                                SocketChannel clientSocketChannel = (SocketChannel) key.channel();
                                sharedBuf.clear();
                                int readFlag = clientSocketChannel.read(sharedBuf);
                                sharedBuf.flip();
                                String msg = "";
                                if (readFlag!=-1){
                                    while (sharedBuf.hasRemaining()) {
                                        char c = (char) sharedBuf.get();
                                        msg += c;
                                    }
                                    Queue<String> pendingDataQueue = pendingDataMap.get(clientSocketChannel);
                                    pendingDataQueue.add(msg.toUpperCase());
                                    logger.log(msg);
                                }
                                sharedBuf.clear();
//                                key.interestOps(SelectionKey.OP_WRITE);
                            } if (key.isWritable() && key.attachment().equals(CLIENT_CHANNEL)){
                                SocketChannel clientSocketChannel = (SocketChannel) key.channel();
                                Queue<String> pendingDataQueue = pendingDataMap.get(clientSocketChannel);
                                String msg;
                                while((msg = pendingDataQueue.poll())!=null){
                                    sharedBuf.clear();
                                    sharedBuf.put(msg.getBytes());
                                    while(sharedBuf.hasRemaining()){
                                        sharedBuf.flip();
                                        clientSocketChannel.write(sharedBuf);
                                    }
                                }
                                sharedBuf.clear();
//                                key.interestOps(SelectionKey.OP_READ);
                            }
                        }
                    }
                    selector.selectedKeys().clear();
                }
            }
        } catch (IOException e){
            try {this.scc.close();}
            catch(IOException e_){}
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws IOException{
        SelectorServer server = new SelectorServer();
        server.run();

    }

}

