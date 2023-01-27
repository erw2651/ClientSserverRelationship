package clients;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;


public class Client implements Runnable {

    private String hosName, thrName;
    private Socket clientSocket;
    private static Random random;
    private final static AtomicInteger TOTAL_RESOURCES;

    private int port, maxSleepTime, maxConnectTime, anInt;

    static {
        random = new Random();
        TOTAL_RESOURCES = new AtomicInteger(0);
    }


    public Client(String hosName, int port, int maxSleepTime, int maxConnectTime){
        this.hosName = hosName;
        this.port = port;
        this.maxSleepTime = maxSleepTime;
        this.maxConnectTime = maxConnectTime;
        anInt = 0;
    }


    public void connection() throws UnknownHostException, IOException{
        clientSocket = new Socket(hosName, port);
    }


    public void getResource() throws IOException{
        String resource;
        InputStreamReader reader = new InputStreamReader(clientSocket.getInputStream());
        BufferedReader bufferedReader = new BufferedReader(reader);

        while ((resource = bufferedReader.readLine()) != null) {
            safePrint("'"+resource + "' taken by "
                    + thrName + " at "
                    + System.currentTimeMillis());
            anInt++;
        }
    }


    public void askResource() throws IOException{
        OutputStreamWriter outReader = new OutputStreamWriter(clientSocket.getOutputStream());
        BufferedWriter writer = new BufferedWriter(outReader);
        writer.write("GET");
        writer.newLine();
        writer.flush();
    }


    public void safePrint(String s) {
        synchronized (System.out) {
            System.out.println(s);
        }
    }


    @Override public void run() {

        thrName = Thread.currentThread().getName();
        long startTime = System.currentTimeMillis();
        int total;

        while(true){
            try {
                safePrint(thrName + " is connecting for "
                        + "new resource at " + System.currentTimeMillis());

                connection();
                askResource();
                getResource();

                if ((System.currentTimeMillis() - startTime) >= maxConnectTime){
                    total = TOTAL_RESOURCES.getAndAdd(anInt);
                    safePrint(thrName + " is done and consumed " + anInt
                            + " / " + total + " resources.");
                    break;
                }
                safePrint(thrName + " is sleeping");
                Thread.sleep(Math.max(100, random.nextInt(maxSleepTime)));
            } catch (UnknownHostException e) {
                System.err.println("Unknown Host");
                break;
            } catch (IOException e) {
                System.err.println("Cannot establish connection: "+e.getMessage());
                break;
            } catch (InterruptedException ex) {

            }
        }
    }

    public static void main(String arg[]){

        String client1 = arg[0];
        int port = Integer.parseInt(arg[1]);
        int numConsumers = Integer.parseInt(arg[2]);
        int maxSleepTime = Integer.parseInt(arg[3]);
        int i1;

        for (int i = 0; i < numConsumers; i++){

            i1 = Math.max(5000, random.nextInt(Integer.parseInt(arg[4])));

            Client client = new Client(client1, port, maxSleepTime, i1);

            Thread threadClient = new Thread(client);
            threadClient.start();
        }

    }
}
