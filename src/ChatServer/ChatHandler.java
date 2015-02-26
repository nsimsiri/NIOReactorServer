package ChatServer;

import ReactorFramework2.ChannelFacade;
import ReactorFramework2.InputHandler;

import java.nio.ByteBuffer;

/**
 * Created by NatchaS on 1/26/15.
 */
public class ChatHandler implements InputHandler {
    public ChatHandler(){

    }

    public ByteBuffer nextMessage(ChannelFacade facade){
        return ByteBuffer.allocateDirect(48);
    }

    public void handleInput(ByteBuffer msg, ChannelFacade facade) {

    }
}
