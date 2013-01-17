import java.util.Date;
public class PrintTime extends Thread{
    public static void main(String [] args){
        PrintTime p=new PrintTime();
        Supervisor s=new Supervisor(p);
        s.start();
        try {sleep(0);} catch (InterruptedException ex) {}
        p.start();
        //while(!p.isAlive()) continue; //等待p启动s再启动
    }
    public void run(){
        for(int i=0;i<10;i++){
            System.out.println("Timer:\"It is："+new Date() +", I shall sleep for 5s\"");
            try {
                sleep(5000);
            } catch (InterruptedException ex) {
                System.out.println("Supervisor:\"Wake up!!!!\"  Timer:\"I was woken up. :(\"");
            }
        }
    }
}
class Supervisor extends Thread{
    private PrintTime p;
    public Supervisor(PrintTime p){this.p=p;}
    public void run(){
        while(p.isAlive()){
            p.interrupt();
            try {sleep(1000);} catch (InterruptedException ex) {}
        }
    }
}