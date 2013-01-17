


public class RndAndCheckPrime{
    public static void main(String [] args)
    {
        System.out.println((byte)8569);
        System.out.println("����0��99���ڵ��������");
        rndGenThread t1=new rndGenThread();
        t1.start();   
        rndChkPrimeThread t2=new rndChkPrimeThread(t1);
        while(!t1.isAlive()) continue;  //��t1��ʼ���ٿ�ʼ����ȷ������������һ�������
        t2.start();
        
        //���t1���ɵ������curNum����90����t1ֹͣ�����������t2Ҳ�����ֹͣ��
        while(t1.curNum<=90){
            try {Thread.sleep(100);} catch (InterruptedException ex) {}
        }
        t1.exitFlag=true;
        System.out.println("���ɵ��������"+t1.curNum+"������90,�����˳�");
    }
}
class rndGenThread extends Thread{
    public boolean exitFlag=false;
    public int curNum=0;
    public void run(){
        while(!exitFlag){
            System.out.println(curNum);
            curNum=(int)(100*Math.random()); //����0��100�������
            try {sleep(1000);} catch (InterruptedException ex) {}
        }
    }
}
class rndChkPrimeThread extends Thread{
    private rndGenThread generator;
    public rndChkPrimeThread(rndGenThread generator){ //�ڹ��캯���л���Ǹ��������������̵߳�����
        this.generator=generator;
    }
    public void run(){
        while(!generator.exitFlag)
        {
            boolean f=true;int cur=generator.curNum;
            for(int i=2;f && i<Math.sqrt(cur);i++)
                if(cur%i==0) f=false;
            System.out.println("���ɵ������"+cur+(f?"��":"����")+"����");
            try {sleep(1000);} catch (InterruptedException ex) {}
        }
    }
}