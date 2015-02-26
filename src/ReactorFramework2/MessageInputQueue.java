package ReactorFramework2;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.util.ArrayList;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;


/**
 * Created by NatchaS on 1/21/15.
 */
public class MessageInputQueue implements InputQueue {
    private LinkedBlockingQueue<Byte> bufferQueue;
    private final int _SIZE = 1024;
    public MessageInputQueue(){

        this.bufferQueue = new LinkedBlockingQueue<Byte>();
    }

    public int fillFrom(ReadableByteChannel channel) throws IOException {
        ByteBuffer buffer = ByteBuffer.allocateDirect(_SIZE);
        int byteCount = channel.read(buffer);
        int totalByteCount = byteCount;
        buffer.flip();
        while(byteCount>0){
            while(buffer.hasRemaining()){
                try{
                    this.bufferQueue.put(buffer.get());
                } catch (InterruptedException e){
                    e.printStackTrace();
                }
            }
            System.out.println(buffer.limit());
            buffer.clear();
            byteCount = channel.read(buffer);
            totalByteCount += byteCount;
            buffer.flip();
        }
        buffer.clear();
        return totalByteCount;
    }

    public int fillFrom(ByteBuffer buffer){
        buffer.flip();
        while(buffer.hasRemaining()){
            try{
                this.bufferQueue.put(buffer.get());
            } catch (InterruptedException e){
                e.printStackTrace();
            }
        }
        buffer.clear();
        return this.bufferQueue.size();
    }

    public boolean isEmpty(){
        return this.bufferQueue.isEmpty();
    }

    public int indexOf(int b){
        ArrayList<Byte> bytes = new ArrayList<Byte>(this.bufferQueue);
        for(int i = 0; i < this.bufferQueue.size(); i++){
            if (b==bytes.get(i)) return i;
        }
        return -1;
    }

    public ByteBuffer dequeueBytes(int count){
        int i = 0;
        Byte b;
        ByteBuffer buf = ByteBuffer.allocateDirect(this.bufferQueue.size());
        ArrayList<Byte> bs = new ArrayList<Byte>(this.bufferQueue);
        if (!this.bufferQueue.isEmpty()){
            while((b = this.bufferQueue.poll())!=null){
                buf.put(b);
                if (i==count) break;
                i++;
            }
        }
        return buf;
    }

    public void discardBytes(int count){
        this.dequeueBytes(count);
    }

    @Override
    public String toString(){
        String s = "MessageInputQueue(";
        for (Byte b : this.bufferQueue){
            s+=String.format("[%s]", (char)b.intValue());
        }
        s+=")";
        return s;
    }

    public static void __test1(){
        ByteBuffer buf = ByteBuffer.allocateDirect(48);
        buf.put("hello world".getBytes());
        MessageInputQueue iQ = new MessageInputQueue();
        iQ.fillFrom(buf);
        System.out.println(iQ);
        int index = iQ.indexOf('e');
        System.out.println("index=> " + index + "\n" + iQ);
        ByteBuffer res = iQ.dequeueBytes(index);
        res.flip();
        System.out.print(">> ");
        for (int i = 0; i < res.limit(); i++){
            System.out.print((char) res.get() + (" "));
        }
        System.out.println("\n"+iQ);
        iQ.discardBytes(iQ.indexOf('r'));
        System.out.println("\n"+iQ);
    }


    public static void main(String[] args){

        MessageInputQueue.__test1();

    }



}
