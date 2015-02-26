package ReactorFramework2;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;

/**
 * Created by NatchaS on 1/21/15.
 */
public interface InputQueue {
    int fillFrom(ReadableByteChannel channel) throws IOException;
    boolean isEmpty();
    int indexOf(int b);
    ByteBuffer dequeueBytes(int count);
    void discardBytes(int count);
}
