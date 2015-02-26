package KPTest; /**
 * Created by NatchaS on 1/1/15.
 */
import java.nio.*;

public class BufferByteTest {
    public static void test1(){
        ByteBuffer buf = ByteBuffer.allocateDirect(48);
        print_bufd(buf);
        buf.asCharBuffer().put("Hello");
        print_bufd(buf);
        loop_bufd(buf);


    }

    public static void test2(){
        ByteBuffer buf = ByteBuffer.allocateDirect(48);
        print_bufd(buf);
        String str = "hello world";
        buf.put(str.getBytes());
        print_bufd(buf);
//        buf.flip();
        buf.limit(buf.position()).rewind();
        loop_buf(buf);
        print_bufd(buf);

    }

    public static void print_bufd(ByteBuffer buf){
        System.out.format("\nBUF:\nposition-> %d\n" +
                "limit-> %d\n" +
                "capacity-> %d\n", buf.position(), buf.limit(), buf.capacity());
    }

    public static void loop_bufd(ByteBuffer buf){
        System.out.print("\nprinting buf {\n");
        while(buf.hasRemaining()){
            System.out.print(buf.getChar());
        }
        System.out.print("\n}finished\n");
    }

    public static void loop_buf(ByteBuffer buf){
        System.out.print("\nprinting buf {\n");
        while(buf.hasRemaining()){
            System.out.print((char)buf.get());
        }
        System.out.print("\n}finished\n");
    }


    public static void main(String[] args){
        System.out.println("hello...");
//        test1();
        test2();
    }


}
