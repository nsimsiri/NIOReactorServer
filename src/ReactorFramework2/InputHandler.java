package ReactorFramework2;

import java.nio.ByteBuffer;

/**
 * Created by NatchaS on 1/21/15.
 */
public interface InputHandler {
    public ByteBuffer nextMessage(ChannelFacade facade);
    public void handleInput(ByteBuffer msg, ChannelFacade facade);

}
