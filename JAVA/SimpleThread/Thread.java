import java.util.*;
import java.io.*;
public class Thread {
    public static int threadCount=4;
    public static int stop= 100;
    public static void main(String [] args){
        System.out.println("Spawning threads....");
        ArrayList<Thread> threads= new ArrayList<Thread>();
        int incrementAmount = stop/threadCount;
        int starting=1;

        for(int i=0; i<threadCount; i++) {
            if (!((i + 1) == threadCount)) {
                threads.add(new Thread(new PrimeThread(starting, starting + incrementAmount, i + ".txt", false)));
            }
            else{
                threads.add(new Thread(new PrimeThread(starting, starting+incrementAmount,i+".txt",true)));
            }
        }
        for(int i=0; i<threads.size();i++){
            threads.get(i).start();
        }
    }
}
