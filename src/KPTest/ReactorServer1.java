package KPTest; /**
 * Created by NatchaS on 1/18/15.
 */
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.*;

public class ReactorServer1 implements Runnable{
    // Reactor synonymous with 'Dispatcher'
    Selector selector;
    ServerSocketChannel ssc;
    KPLogger logger;
    public ReactorServer1(int port) throws IOException{
        this.logger = new KPLogger(String.format("ReactorServer@%s", port));
        this.ssc = ServerSocketChannel.open();
        this.ssc.bind(new InetSocketAddress(port));
        this.selector = Selector.open();
        this.ssc.configureBlocking(false);
        logger.log(String.format("started"));

    }

    public void run(){
        try{
            SelectionKey selectionKeyReactor = this.ssc.register(this.selector, SelectionKey.OP_ACCEPT);
            selectionKeyReactor.attach(new Acceptor());
            while(!Thread.interrupted()){
                int numOfReadiedChannels = this.selector.select();
                if (numOfReadiedChannels==0) continue;
                Set<SelectionKey> keys = this.selector.selectedKeys();
                for (SelectionKey key : keys){
                    if (key.isValid()){
                        dispatch(key);
                    }
                }
                keys.clear();
            }
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    public void dispatch(SelectionKey key){
        Runnable handler = (Runnable)key.attachment();
        handler.run();
    }

    // Acceptor
    private class Acceptor implements Runnable{
        private int count;
        public Acceptor(){
            this.count = 0;
        }
        public void run() {
            try{
                SocketChannel acceptedChannel = ssc.accept();
                logger.log(String.format("accepted: %s", acceptedChannel.getRemoteAddress()));
                new EventHandler(acceptedChannel, count);

            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    private class EventHandler implements Runnable{
        private ExecutorService processPool;
        private KPLogger handlerLogger;
        private SocketChannel sc;
        private SelectionKey key;
        static final int READ = 1;
        static final int WRITE = 0;
        int state;
        Queue<String> pendingData;
        public EventHandler(SocketChannel sc, int count){
            this.pendingData = new ConcurrentLinkedQueue<String>();
            try{
                this.processPool = Executors.newFixedThreadPool(3);
                InetSocketAddress addr = (InetSocketAddress)sc.getRemoteAddress();
                this.handlerLogger = new KPLogger(String.format("%s/%s:%s/handler-%d",logger.getName(),addr.getAddress(),addr.getPort(),count));
                this.sc = sc;
                sc.configureBlocking(false);
                this.key = sc.register(selector, SelectionKey.OP_READ);
                this.state = READ;
                key.attach(this);
                selector.wakeup();
            } catch (IOException e){
                e.printStackTrace();
            }
        }

        public void run(){
            if (state==READ) {
                onRead();
                state = WRITE;
                this.key.interestOps(SelectionKey.OP_WRITE);
            } else {
                onWrite();
                state = READ;
                this.key.interestOps(SelectionKey.OP_READ);
            }

        }

        public synchronized void onRead(){
//            this.handlerLogger.log("reading");
            try {
                ByteBuffer buffer = ByteBuffer.allocateDirect(48);
                int bytesRead = this.sc.read(buffer);
                final Map<Integer, String> bufferDataMap = new ConcurrentHashMap<Integer, String>();
                final Queue<String> bufferDataQ = new ConcurrentLinkedQueue<String>();
                String msg = "";
                while(bytesRead > 0){
                    buffer.flip();
                    while(buffer.hasRemaining()){
                        msg+=(char)buffer.get();
                    }
                    buffer.clear();
                    bytesRead = this.sc.read(buffer);

                    handlerLogger.log(msg);
                    bufferDataQ.add(msg);
                }
                String qMsg = "";
                int dataCount = 0;
                Set<Callable<Map<Integer,String>>> futures = new HashSet<Callable<Map<Integer,String>>>();
                while((qMsg=bufferDataQ.poll())!=null){
                    final String qMsg_ = qMsg;
                    final int dataCount_ = dataCount;
                    futures.add(new Callable<Map<Integer, String>>() {
                        @Override
                        public Map<Integer, String> call() throws Exception {
                            Map<Integer, String> futureMap = new ConcurrentHashMap<Integer, String>();
//                            System.out.println("thread" + dataCount_ + "=>" + qMsg_);
                            futureMap.put(dataCount_, qMsg_.toUpperCase());
                            return futureMap;
                        }
                    });
                    dataCount+=1;
                }
               try {
                   List<Future<Map<Integer, String>>> futureMaps = this.processPool.invokeAll(futures);
                   for (Future<Map<Integer, String>> futureMap : futureMaps){
                       bufferDataMap.putAll(futureMap.get());
                   }
               } catch(InterruptedException e) {
                   e.printStackTrace();
               } catch (ExecutionException e){
                   e.printStackTrace();
               }
                for (int i = 0; i < bufferDataMap.size(); i+=1){
//                    System.out.println("loop=>"+bufferDataMap.get(i));
                    this.pendingData.add(bufferDataMap.get(i));
                }



            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void onWrite(){
            ByteBuffer buffer = ByteBuffer.allocateDirect(48);
            String msg="";
            try {
                while((msg=this.pendingData.poll())!=null){
                    buffer.clear();
                    buffer.put(msg.toUpperCase().getBytes());
                    while(buffer.hasRemaining()){
                        buffer.flip();
                        this.sc.write(buffer);
                    }
                }

            } catch (IOException e){
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args){
        try {
            ReactorServer1 reactor = new ReactorServer1(8000);
            new Thread(reactor).start();
        } catch (IOException e){
            e.printStackTrace();
        }


//        Map<Integer, String> pendingDataMap = new ConcurrentHashMap<Integer, String>();
//        pendingDataMap.put(0, "he");
//        pendingDataMap.put(2,"o");
//        pendingDataMap.put(1,"ll");
//        String msg = "";
//        for (int i = 0; i < pendingDataMap.size(); i++){
//            msg+=pendingDataMap.get(i);
//        }
//        System.out.println(msg);


    }
}
