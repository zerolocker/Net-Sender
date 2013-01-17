/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package NetSender;

import java.io.*;


/**
 * 半成品文件发送器，默认发送c:\send.txt
 * @author Zero
 */
public class FileSenderTest extends BaseSender{
    public static void main(String [] args){
        FileSenderTest fst=new FileSenderTest("localhost",4331);
        File f = new File("c:\\send.txt");
        fst.Send(f);
    }
    public FileSenderTest(String hostName,int port){super(hostName, port);}
    @Override
    protected void Work(DataInputStream in, DataOutputStream out,Object dataDescription) throws IOException {
        File f=(File)dataDescription;
        byte[] b = new byte[1024];
        out.writeUTF(f.getName());
        out.writeLong(f.length()/1024);

        //对方同意吗？
        String reply = in.readUTF();
        System.out.println("Replys:" + reply);
        if(reply.equals("Rejected."))
            return;

        FileInputStream fin = new FileInputStream(f);
        int len;
        while ((len = fin.read(b)) != -1) {
            out.write(b, 0, len);
        }
    }
}
