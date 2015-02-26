package KPTest; /**
 * Created by NatchaS on 1/1/15.
 */
import java.util.concurrent.*;
import java.util.Random;

public class ExecutorSample {
    ExecutorService exc;
    public ExecutorSample(){
        this.exc = Executors.newFixedThreadPool(10);

    }

    public void test1() throws InterruptedException{
        final Random ran = new Random();
        for(int i= 0 ; i < 10; i ++){
            final int val = i;
            this.exc.submit(new Runnable() {
                @Override
                public void run() {
                    int _time = ran.nextInt(10000);
                    System.out.format(">>>>starting-> %d in %d\n", val, _time);
                    try {
                        Thread.sleep(_time);
                    } catch (InterruptedException e) {
                    }
                    System.out.format("thread-> %d\n", val);
                }
            });
        }
        this.exc.shutdown();
        this.exc.awaitTermination(1000, TimeUnit.SECONDS);
        System.out.println("\nFin.");


    }


    public static void main(String[] args) throws InterruptedException{
        ExecutorSample ex = new ExecutorSample();
        ex.test1();

    }


}
