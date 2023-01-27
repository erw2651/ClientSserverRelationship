

package server;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;


public class Producer implements Runnable {

    private int maxSleepTime;
    private BlockingQueue<String> buffer;


    private final static AtomicInteger counter = new AtomicInteger(0);


    public Producer(int maxSleepTime, BlockingQueue<String> buffer){
        this.maxSleepTime = maxSleepTime;
        this.buffer = buffer;
    }

    public void safePrintln(String s) {
        synchronized (System.out) {
            System.out.println(s);
        }
    }


    @Override public void run() {
        int counter;

        while(true){
            try {
                counter = Producer.counter.incrementAndGet();
                buffer.add("Res-" + counter + ": "
                        + Thread.currentThread().getName());
                safePrintln(buffer.toString() + " reported by: " + Thread.currentThread().getName());
                Thread.sleep(Math.max(100, maxSleepTime));
            } catch (InterruptedException ie) {}
        }
    }
}