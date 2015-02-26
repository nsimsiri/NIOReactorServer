package ReactorFramework2;

import KPTest.KPLogger;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.HashMap;
import java.util.concurrent.Callable;

public class HandlerAdapter implements ChannelFacade, Callable<HandlerAdapter> {
    protected InputHandler inputHandler;
    protected InputQueue inputQueue;
    protected OutputQueue outputQueue;
    protected SelectionKey key;
    protected SelectableChannel socketChannel;
    protected Reactor reactor;
    protected int currentInterestOps;
    protected int currentReadyOps;
    protected final Object stateChangeLock = new Object();
    protected volatile boolean isRunning;
    protected String name;
    public KPLogger logger;

    public HandlerAdapter(SelectableChannel socketChannel){
        this(socketChannel, null, null);
    }

    public HandlerAdapter(SelectableChannel socketChannel, Reactor reactor, InputHandler inputHandler){
        this.inputHandler  = inputHandler;
        this.socketChannel = socketChannel;
        this.reactor = reactor;
        this.isRunning = false;
        try{
            if (socketChannel instanceof SocketChannel){
                InetSocketAddress addr = (InetSocketAddress)((SocketChannel)socketChannel).getRemoteAddress();
                this.name = String.format("%s/Handler-%s:%s",reactor.getName(), addr.getAddress(), addr.getPort());
                this.logger = new KPLogger(this.name);
            } else if (socketChannel instanceof ServerSocketChannel){
                this.logger = new KPLogger(reactor.getName());
                this.name = "Reactor";
            } else {
                this.name = "unknown_location";
                this.logger = new KPLogger(String.format("%s/%s", reactor.getName(), this.name));
            }
            this.inputQueue = MessageQueueFactory.createInputQueue();
            this.outputQueue = MessageQueueFactory.createOutputQueue();
        } catch (IOException e) {e.printStackTrace();}
    }
    public HandlerAdapter call(){
        if (this.isRunning){
            try {
                drainOutput(); // write to socket
                fillInput(); // read from socket
                ByteBuffer msg;
                logger.log("in Call");
                while ((msg = this.inputHandler.nextMessage(this)) != null) {
                    this.inputHandler.handleInput(msg, this);
                }
            } catch (IOException e){
                e.printStackTrace();
            } finally {
                synchronized(this.stateChangeLock){
                    this.isRunning = false;
                    if (!outputQueue.isEmpty()) this.currentInterestOps = SelectionKey.OP_WRITE;
                    else this.currentInterestOps = SelectionKey.OP_READ;
                    this.currentReadyOps = 0;
                }
            }
        }
        return this;
    }

    public void prepareToRun(){
        if (!this.key.isValid()) return;
        synchronized(stateChangeLock){
            this.isRunning = true;
            this.currentReadyOps = this.key.readyOps();
            this.currentInterestOps = this.key.interestOps();
        }
    }

    private void drainOutput() throws IOException{
        /* Writing to socket. */
        if ((this.currentReadyOps & SelectionKey.OP_WRITE) != 0 && !this.outputQueue.isEmpty()){
            this.logger.log("Draining Output");
            this.outputQueue.drainTo((WritableByteChannel)this.socketChannel);
        }
    }

    private void fillInput() throws IOException{
//        Reading from socket.
        if ((this.currentReadyOps & SelectionKey.OP_READ) != 0){
            this.logger.log("filling Input");
            this.inputQueue.fillFrom((ReadableByteChannel)this.socketChannel);
        }
    }

    public InputQueue inputQueue(){
        return this.inputQueue;
    }

    public OutputQueue outputQueue(){
        return this.outputQueue;
    }
    public void setHandler(InputHandler handler){
        this.inputHandler = handler;
    }


    public int getInterestOps(){
        return this.currentInterestOps;
    }
    public void setInterestOps(int ops){
        this.currentInterestOps = ops;
    }

    public boolean isConnectionDead(){
        /* TO be implemented
        encapsulate process that verifies client's connection.
        * */
        return false;
    }

    public String getAddress(){
        String addrStr = "";
        try{
            InetSocketAddress addr = (InetSocketAddress)((SocketChannel)socketChannel).getRemoteAddress();
            addrStr = String.format("%s:%s", addr.getAddress(), addr.getPort());
        } catch (IOException e){ e.printStackTrace(); }
        return addrStr;
    }

    public void setInputHandler(InputHandler handler){
        this.inputHandler = handler;
    }

    public InputHandler getInputHandler(){
        return this.inputHandler;
    }

    public void setKey(SelectionKey key){
        this.key = key;
    }

    public SelectionKey getKey(){
        return this.key;
    }

    @Override
    public String toString(){
        HashMap<Integer, String> opsMap = new HashMap<Integer, String>();
        opsMap.put(SelectionKey.OP_READ, "OP_READ");
        opsMap.put(SelectionKey.OP_WRITE, "OP_WRITE");
        opsMap.put(SelectionKey.OP_ACCEPT, "OP_ACCEPT");
        opsMap.put(0, "NO_OP");
        String s = String.format("\n---Adapter---\nName: %s\nq-interestsOps: %s\nq-readyOps: %s\nkey-interestOps: %s\nkey-readyOps: %s\n" +
                        "InputHandler{ %s }\n---\n",
                this.name,
                opsMap.get(this.currentInterestOps),
                opsMap.get(this.currentReadyOps),
                opsMap.get(this.key.interestOps()),
                opsMap.get(this.key.readyOps()),
                this.inputHandler);

        return s;
    }



}
