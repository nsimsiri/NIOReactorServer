package ReactorFramework2;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by NatchaS on 1/21/15.
 */
public class MessageOutputQueue implements OutputQueue {
    private LinkedBlockingQueue<Byte> bufferQueue;
    private final int _SIZE = 1024;
    public MessageOutputQueue(){
        this.bufferQueue = new LinkedBlockingQueue<Byte>();
    }

    public boolean isEmpty(){
        return this.bufferQueue.isEmpty();
    }

    public int drainTo(WritableByteChannel channel) throws IOException {
        int bytesWritten = 0;
        ByteBuffer buffer = ByteBuffer.allocateDirect(this.bufferQueue.size());
        Byte b;
        while((b = this.bufferQueue.poll())!=null){
            buffer.put(b);
        }
        buffer.flip();
        while(buffer.hasRemaining()){
            bytesWritten = channel.write(buffer);
        }
        return bytesWritten;
    }

    public boolean enqueue(ByteBuffer buffer){
        buffer.flip();
        while(buffer.hasRemaining()){
            try {
                byte b = buffer.get();
//                System.out.print((char)b);
                this.bufferQueue.put(b);
            } catch (InterruptedException e){
                e.printStackTrace();
            }
        }
        return true;
    }

    public static void main(String[] args) throws IOException{
        MessageOutputQueue q = new MessageOutputQueue();
        ByteBuffer buf = ByteBuffer.allocateDirect(20);
        buf.put("hello world".getBytes());
        q.enqueue(buf);
        System.out.println();
    }


}
