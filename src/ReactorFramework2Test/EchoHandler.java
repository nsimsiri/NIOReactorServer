package ReactorFramework2Test;

import KPTest.KPLogger;
import ReactorFramework2.ChannelFacade;
import ReactorFramework2.InputHandler;
import ReactorFramework2.InputQueue;
import ReactorFramework2.OutputQueue;

import java.nio.ByteBuffer;

/**
 * Created by NatchaS on 1/24/15.
 */
public class EchoHandler implements InputHandler {
    KPLogger logger;
    boolean recievedAddress = false;
    public EchoHandler(){
        logger = new KPLogger("EchoHandler");
    }

    public ByteBuffer nextMessage(ChannelFacade facade){
        if (!recievedAddress){
            String addrStr = facade.getAddress();
            this.logger.setName(this.logger.getName()+"@"+facade.getAddress());
            recievedAddress = true;
        }

        InputQueue inputQueue = facade.inputQueue();
        int nl_pos = inputQueue.indexOf((byte)'\n');
        if (nl_pos == -1) return null;
        return inputQueue.dequeueBytes(nl_pos);
    }

    public void handleInput(ByteBuffer buffer, ChannelFacade facade){
        String msg = "";
        buffer.flip();
        while(buffer.hasRemaining()){
            msg+=(char)buffer.get();
            this.logger.log(msg);
        }
        logger.log("received = "+msg);
        OutputQueue outputQueue = facade.outputQueue();
        buffer.clear();
        buffer.put(new byte[buffer.limit()]);
        buffer.clear();
        buffer.put(msg.toUpperCase().getBytes());
        outputQueue.enqueue(buffer);
    }

    @Override
    public String toString(){
        String s = logger.getName();
        return s;
    }
}
