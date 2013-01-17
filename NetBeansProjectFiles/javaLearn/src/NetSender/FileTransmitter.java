/*
 * 本文件包含了两个数据传输器的类：FileSender,FileReceiver
 */
package NetSender;

import java.io.*;
import java.net.*;
import java.util.Calendar;
import java.util.Date;
import java.util.Scanner;


/**
 * 文件发送器
 * @author Zero
 */
class FileSender extends BaseSender{
    public FileSender(String hostName,int port){super(hostName, port);}
    @Override
    protected void Work(DataInputStream in, DataOutputStream out,Object dataDescription) throws IOException {
        File f=(File)dataDescription;
        byte[] buffer = new byte[1024*64];
        out.writeUTF(f.getName());
        out.writeLong(f.length());

        //对方同意吗？
        String reply = in.readUTF();
        System.out.println("Replys:" + reply);
        if(reply.equals("Rejected."))
        {
            TransmitterManager.notifyFileSendingRequestRejected(f);
            return;
        }

        FileInputStream fin = new FileInputStream(f);
        int len;double percent=0, tot=0,prevtot=0,totlen=f.length()/100;
        long curtime,prevtime=System.currentTimeMillis();int speed=0;
        while ((len = fin.read(buffer)) != -1) {
            out.write(buffer, 0, len);
            tot+=len;percent=tot/totlen;
            curtime=System.currentTimeMillis();
            if(curtime-prevtime>500) {
                speed= (int)(((tot-prevtot))/(curtime-prevtime));
                prevtot=tot;prevtime=curtime;
            }
            TransmitterManager.tellFileSendingProgress(percent,speed,f);
        }
        TransmitterManager.notifyFileSendingCompleted(f);
        fin.close();
    }
    
    @Override
    protected void notifyException(Exception ex){
        if(ex instanceof ConnectException)
            TransmitterManager.notifySenderFailedToConnect("文件发送器");
        else if(ex instanceof UnknownHostException)
            TransmitterManager.notifySenderFailedToConnect("文件发送器");
        else
            TransmitterManager.notifyOtherException(ex);
    }
}

/**
 * 文件接收器
 * @author Zero
 */
class FileReceiver extends BaseReceiver{
    
    public FileReceiver(int port){
        super(port);
    }

    @Override
    protected void notifyException(Exception ex){
        if(ex instanceof BindException)
            TransmitterManager.notifyReceiverPortAlreadyInUse("文件接收器");
        else
            TransmitterManager.notifyOtherException(ex);
    }
    
    @Override
    protected void Work(DataInputStream in, DataOutputStream out)throws IOException {
        //同意接收吗？
        String name = in.readUTF();
        long size = in.readLong();
        File f=TransmitterManager.addFileReceiveReqAndWaitForDecision(name,size);
        boolean user_Authorized=(f==null)?false:true;
        
        if(!user_Authorized){
            out.writeUTF("Rejected.");
        }
        else{
            //开始传输文件
            out.writeUTF("Agreed.");
            byte [] buffer=new byte [1024*64];
            FileOutputStream fileout=new FileOutputStream(f);
            
            int len;double percent=0, tot=0,prevtot=0,totlen=size/100;
            long curtime,prevtime=System.currentTimeMillis();int speed=0;
            File prevFilename=new File(name);
            while((len=in.read(buffer))>0){
                fileout.write(buffer,0,len);
                tot+=len;percent=tot/totlen;
                curtime=System.currentTimeMillis();
                if(curtime-prevtime>500) {
                    speed= (int)(((tot-prevtot))/(curtime-prevtime));
                    prevtot=tot;prevtime=curtime;
                }
                TransmitterManager.tellFileReceivingProgress(percent,speed,prevFilename);
            }
            TransmitterManager.notifyFileReceivingCompleted(prevFilename);
            //经测试，若发送端忘记调用.close()，这种流永远不会有EOF出现。所以这个while无法退出，直到发送端程序结束从而引发I/O异常
            fileout.close();
            System.out.println("--Completed.");
        }
    }
}
