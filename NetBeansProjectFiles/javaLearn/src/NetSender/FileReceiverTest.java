/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package NetSender;

import java.io.*;
import java.util.Scanner;

/** ��������ʵ�ֵ��ļ����������÷���һ��new FileReceiverTest(int)���ɿ�ʼ������<p>
 * ���Ʒ�ļ���������Ĭ�ϰ��ļ����õ�c:\receive.txt
 */
public class FileReceiverTest extends BaseReceiver{
    
    public static void main(String [] args){
        FileReceiverTest FR=new FileReceiverTest(4331);
    }

    public FileReceiverTest(int port)
    {
        super(port);
    }

    @Override
    protected void Work(DataInputStream in, DataOutputStream out)throws IOException {
        boolean user_Authorized=false;
        //�Է�ͬ�������
        String name = in.readUTF();
        long size = in.readLong();
        System.out.println("Request:\n filename: " + name + "\n size: " + size + "KB\n--Agree?(yes/no)");
        //�����ԣ����߳��ϣ�������Scanner������InputStreamReader��BufferedReader�ȵȣ���Ƴ���ʱ
        //�����뱣֤ͬһʱ��û�������߳��ڶ�����block���ŵȴ�System.in��������޷���֪��������������̡߳�
        Scanner s = new Scanner(System.in);
        while (true) {
            String cmd = s.next();
            if (cmd.equals("yes")) {
                out.writeUTF("Agreed.");
                user_Authorized=true;
                break;
            } else if (cmd.equals("no")) {
                out.writeUTF("Rejected.");
                user_Authorized=false;
                break;
            }
        }
        
        if(user_Authorized){
            byte [] buffer=new byte [1024*64];
            //��ʼ�����ļ�
            FileOutputStream fileout=new FileOutputStream("c:\\receive.txt");
            int len;
            while((len=in.read(buffer))>0)
                //System.out.write(buffer,0,len);
                fileout.write(buffer,0,len);
            //�����ԣ������Ͷ����ǵ���.close()����������Զ������EOF���֡��������while�޷��˳���ֱ�����Ͷ˳�������Ӷ�����I/O�쳣
            fileout.close();
            System.out.println("--Completed.");
        }
    }
}