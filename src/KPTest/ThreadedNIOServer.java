package KPTest;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.concurrent.*;


public class ThreadedNIOServer {
    ServerSocketChannel ssc;
    ExecutorService es;
    public ThreadedNIOServer() throws IOException {
        this.logger = new KPLogger("Server");
        this.ssc = ServerSocketChannel.open();
        this.es = Executors.newFixedThreadPool(10);
        this.ssc.socket().bind(new InetSocketAddress("localhost", 8080));
        this.logger.log("ready...");

    }

    private KPLogger logger;
    public void start(){
        try{
            while(true){
                final SocketChannel channel = this.ssc.accept();
                this.logger.log(String.format("accepted %s", channel.socket().getRemoteSocketAddress().toString()));
                final ByteBuffer buf = ByteBuffer.allocateDirect(48);
//                process(buf, channel);
                this.es.submit(new Runnable(){
                    @Override public void run() {
                        try {
                            process(buf, channel);
                        } catch (IOException e){}
                    }

                });

            }
        } catch (IOException e){
            logger.log(e.toString());
            try{
                this.ssc.close();
            } catch (IOException _e){}

        }

    }
    public void process(ByteBuffer buf, SocketChannel sc) throws IOException{
        int readFlag = sc.read(buf);
//        KPTest.BufferByteTest.print_bufd(buf);
        while(readFlag!=-1){
            String msg = "";
            buf.flip();
            while(buf.hasRemaining()){
                msg+=String.format("%c", (char)buf.get());
            }
            this.logger.log(msg);

            buf.clear();
            buf.put(msg.toUpperCase().getBytes());
//            KPTest.BufferByteTest.print_bufd(buf);
            buf.flip();
            while(buf.hasRemaining()){
                sc.write(buf);
            }
            buf.clear();
            sc.read(buf);
        }

        sc.close();
    }

    public void shutdown(){
        try{
            this.es.shutdown();
            this.es.awaitTermination(1000, TimeUnit.SECONDS);
            System.out.format("thread pool shutdown");
            this.ssc.close();
        } catch (InterruptedException e){

        } catch (IOException ex){

        }

    }

    public static void main(String[] args) throws IOException{
        ThreadedNIOServer server = new ThreadedNIOServer();
        Runtime.getRuntime().addShutdownHook(new Thread(){
            public void run(){
                System.out.println("on shutdown");
            }
        });

        server.start();
    }

}
