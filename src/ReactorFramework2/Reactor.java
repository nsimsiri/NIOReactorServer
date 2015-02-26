package ReactorFramework2;

import java.nio.channels.SelectableChannel;

/**
 * Created by NatchaS on 1/22/15.
 */
public interface Reactor {
    public void dispatch(HandlerAdapter adapter);
    public ChannelFacade registerConnection(SelectableChannel sc, InputHandler inputHandler);
    public void unregisterConnection(ChannelFacade channelFacade);
    public String getName();

}
