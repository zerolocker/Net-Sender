/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package NetSender;

import java.io.*;
import java.util.Scanner;

/** 用命令行实现的文件接收器。用法：一句new FileReceiverTest(int)即可开始工作。<p>
 * 半成品文件接收器，默认把文件放置到c:\receive.txt
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
        //对方同意接受吗？
        String name = in.readUTF();
        long size = in.readLong();
        System.out.println("Request:\n filename: " + name + "\n size: " + size + "KB\n--Agree?(yes/no)");
        //经调试，多线程上，无论用Scanner，还是InputStreamReader，BufferedReader等等，设计程序时
        //都必须保证同一时刻没有两个线程在堵塞（block）着等待System.in。否则会无法获知输入进入了哪条线程。
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
            //开始传输文件
            FileOutputStream fileout=new FileOutputStream("c:\\receive.txt");
            int len;
            while((len=in.read(buffer))>0)
                //System.out.write(buffer,0,len);
                fileout.write(buffer,0,len);
            //经测试，若发送端忘记调用.close()，这种流永远不会有EOF出现。所以这个while无法退出，直到发送端程序结束从而引发I/O异常
            fileout.close();
            System.out.println("--Completed.");
        }
    }
}