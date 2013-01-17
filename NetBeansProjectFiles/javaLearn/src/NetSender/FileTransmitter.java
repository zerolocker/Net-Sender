/*
 * ���ļ��������������ݴ��������ࣺFileSender,FileReceiver
 */
package NetSender;

import java.io.*;
import java.net.*;
import java.util.Calendar;
import java.util.Date;
import java.util.Scanner;


/**
 * �ļ�������
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

        //�Է�ͬ����
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
            TransmitterManager.notifySenderFailedToConnect("�ļ�������");
        else if(ex instanceof UnknownHostException)
            TransmitterManager.notifySenderFailedToConnect("�ļ�������");
        else
            TransmitterManager.notifyOtherException(ex);
    }
}

/**
 * �ļ�������
 * @author Zero
 */
class FileReceiver extends BaseReceiver{
    
    public FileReceiver(int port){
        super(port);
    }

    @Override
    protected void notifyException(Exception ex){
        if(ex instanceof BindException)
            TransmitterManager.notifyReceiverPortAlreadyInUse("�ļ�������");
        else
            TransmitterManager.notifyOtherException(ex);
    }
    
    @Override
    protected void Work(DataInputStream in, DataOutputStream out)throws IOException {
        //ͬ�������
        String name = in.readUTF();
        long size = in.readLong();
        File f=TransmitterManager.addFileReceiveReqAndWaitForDecision(name,size);
        boolean user_Authorized=(f==null)?false:true;
        
        if(!user_Authorized){
            out.writeUTF("Rejected.");
        }
        else{
            //��ʼ�����ļ�
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
            //�����ԣ������Ͷ����ǵ���.close()����������Զ������EOF���֡��������while�޷��˳���ֱ�����Ͷ˳�������Ӷ�����I/O�쳣
            fileout.close();
            System.out.println("--Completed.");
        }
    }
}
