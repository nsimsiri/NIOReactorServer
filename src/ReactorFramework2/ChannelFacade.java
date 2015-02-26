package ReactorFramework2;

import java.net.InetSocketAddress;

/**
 * Created by NatchaS on 1/21/15.
 */
public interface ChannelFacade {
    // User interacts with this.
    InputQueue inputQueue();
    OutputQueue outputQueue();
    void setHandler(InputHandler handler);
    int getInterestOps();
    void setInterestOps(int ops);
    String getAddress();

}
