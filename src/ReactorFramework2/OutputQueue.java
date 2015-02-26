package ReactorFramework2;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;

/**
 * Created by NatchaS on 1/21/15.
 */
public interface OutputQueue {
    int drainTo(WritableByteChannel channel) throws IOException;
    boolean isEmpty();
    boolean enqueue(ByteBuffer buffer);


}
