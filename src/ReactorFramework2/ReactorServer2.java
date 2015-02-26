package ReactorFramework2;

import KPTest.KPLogger;

import java.text.SimpleDateFormat;
import java.util.*;
import java.net.*;
import java.io.*;
import java.nio.channels.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;

/* JOBS - SERVER
* - manages/dispatches io events -> aka does multiplexing
* - manages Adapter
* - manages syncing of Adapter's interestOps with adapter's key's interest op
*
* TO D0:
* - multi-threading
* - disconnection detection
* */

public class ReactorServer2 implements Runnable, Reactor{
    public static final int _DEFAULT_PORT = 8000;
    private int port;
    private Selector selector;
    private ServerSocketChannel ssc;
    private BlockingQueue<HandlerAdapter> adapterInterestQueue;
    private KPLogger logger;
    private String name;

    HandlerAdapter acceptorHandler;

    public ReactorServer2(int port, Acceptable clientAcceptor) {
        this.name = String.format("Reactor2/Server@%s", port);
        this.logger = new KPLogger(this.name);
//        this.logger.withDate(true);
        this.logger.log("started...");

        try{
            this.port = port;
            this.ssc = ServerSocketChannel.open();
            this.ssc.bind(new InetSocketAddress(port));
            this.ssc.configureBlocking(false);
            this.selector = Selector.open();
            HandlerAdapter acceptorHandler = new AcceptorHandlerAdapter(this.ssc, this, clientAcceptor);
            this.acceptorHandler = acceptorHandler;
            SelectionKey acceptorKey = this.ssc.register(this.selector, SelectionKey.OP_ACCEPT, acceptorHandler);
            acceptorHandler.setKey(acceptorKey);
            acceptorHandler.setInterestOps(acceptorKey.interestOps());
            this.adapterInterestQueue = new LinkedBlockingQueue<HandlerAdapter>();


        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void run(){
        try {
            //dispatcher
            while(!Thread.interrupted()){
                renewAdapterInterestQueue();
                int numOfKeys = this.selector.select();
                Set<SelectionKey> keys = this.selector.selectedKeys();
                // renews channel operation
                if (numOfKeys==0) continue;
                for(SelectionKey key : keys){
                    HandlerAdapter adapter = (HandlerAdapter)key.attachment();
                    logger.log(adapter.toString());
                    dispatch(adapter);
                }
                keys.clear();
            }

        } catch (IOException e){
            e.printStackTrace();
        }
    }

    public void dispatch(HandlerAdapter adapter){
        // thread pool spawn

        adapter.prepareToRun();
        adapter.getKey().interestOps(0);
        adapter.call(); // -> shit happens with user
        // wrap with FutureTask + use ThreadPool to dispatch
        // after finishes - future returns adapter that will be added to the interestQueue

        try{
            this.adapterInterestQueue.put(adapter);
        } catch (InterruptedException e){ e.printStackTrace(); }

    }

    public void renewAdapterInterestQueue(){
//        logger.log("size-q: " + this.adapterInterestQueue.size());
        HandlerAdapter adapter;
        while((adapter = this.adapterInterestQueue.poll()) != null){
            logger.log(adapter.toString());
            if (adapter.isConnectionDead()){
                // unregister channel + adapter
            } else {
                SelectionKey key = adapter.getKey();
                if (key.isValid()){
                    key.interestOps(adapter.getInterestOps());
                }
            }
        }
    }

    public ChannelFacade registerConnection(SelectableChannel socketChannel, InputHandler inputHandler){
        HandlerAdapter adapter = new HandlerAdapter(socketChannel, this, inputHandler);
        try {
            SelectionKey key = socketChannel.register(this.selector, SelectionKey.OP_READ, adapter);
            adapter.setKey(key);
            adapter.setInterestOps(key.interestOps());
            adapter.logger.log("accepted connection => {" + adapter + "}");
            return adapter;
        } catch (IOException e){
            e.printStackTrace();
        } finally {
            return null;
        }
    }

    public void unregisterConnection(ChannelFacade channel){
        /* To be implemented*/
    }

    public String getName(){
        return this.name;
    }



    public static void main(String[] args){

    }
}