


public class RndAndCheckPrime{
    public static void main(String [] args)
    {
        System.out.println((byte)8569);
        System.out.println("生成0到99以内的随机数：");
        rndGenThread t1=new rndGenThread();
        t1.start();   
        rndChkPrimeThread t2=new rndChkPrimeThread(t1);
        while(!t1.isAlive()) continue;  //等t1开始后再开始，可确保至少生成了一个随机数
        t2.start();
        
        //如果t1生成的随机数curNum大于90，让t1停止工作（按设计t2也会跟着停止）
        while(t1.curNum<=90){
            try {Thread.sleep(100);} catch (InterruptedException ex) {}
        }
        t1.exitFlag=true;
        System.out.println("生成的随机数是"+t1.curNum+"，大于90,程序退出");
    }
}
class rndGenThread extends Thread{
    public boolean exitFlag=false;
    public int curNum=0;
    public void run(){
        while(!exitFlag){
            System.out.println(curNum);
            curNum=(int)(100*Math.random()); //生成0到100的随机数
            try {sleep(1000);} catch (InterruptedException ex) {}
        }
    }
}
class rndChkPrimeThread extends Thread{
    private rndGenThread generator;
    public rndChkPrimeThread(rndGenThread generator){ //在构造函数中获得那个被检查随机数的线程的引用
        this.generator=generator;
    }
    public void run(){
        while(!generator.exitFlag)
        {
            boolean f=true;int cur=generator.curNum;
            for(int i=2;f && i<Math.sqrt(cur);i++)
                if(cur%i==0) f=false;
            System.out.println("生成的随机数"+cur+(f?"是":"不是")+"素数");
            try {sleep(1000);} catch (InterruptedException ex) {}
        }
    }
}