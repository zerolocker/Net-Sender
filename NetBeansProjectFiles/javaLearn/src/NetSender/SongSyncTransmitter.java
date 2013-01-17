/*
 * ���ļ��������������ݴ��������ࣺSongSyncSender,SongSyncReceiver
 */
package NetSender;

import NetSender.MessagePack.Type;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Zero
 */
class SongSyncSender extends BaseSender {
    public SongSyncSender(String hostName,int port){
        super(hostName, port);
    }
    @Override
    protected void notifyException(Exception ex){
        if(ex instanceof ConnectException)
            TransmitterManager.notifySenderFailedToConnect("ͬ������������");
        else if(ex instanceof UnknownHostException)
            TransmitterManager.notifySenderFailedToConnect("ͬ������������");
        else
            TransmitterManager.notifyOtherException(ex);
    }
    @Override
    protected void Work(DataInputStream in, DataOutputStream out, Object dataDescription) throws IOException {
        if(dataDescription instanceof File){
            File f=(File)dataDescription;
            synchronized(f){//���д�����ļ����ճ�˫���Ĳ����б�˳��һ����
                //����һ��һ���������TransmitterManager��addSongsAndSync����
                out.writeUTF("FILE");
                out.writeUTF(f.getName());

                byte [] b=new byte[1024*64];
                FileInputStream fin = new FileInputStream(f);
                int len;
                while ((len = fin.read(b)) != -1) {
                    out.write(b, 0, len);
                }
                TransmitterManager.notifySongSendingComplete(f);
                f.notifyAll();
            }
        }else if(dataDescription instanceof MessagePack){
            MessagePack syncMsg=(MessagePack)dataDescription;
            if(syncMsg.type==Type.PLAYER_PLAY) {
                out.writeUTF("PLAY");
                out.writeUTF(syncMsg.message);//tell which file to play
                String reply=in.readUTF();
                if(reply.equals("Not exists.Send me this song."))
                {
                    TransmitterManager.notifyPeerDontHaveThisSongAndSend(syncMsg.message);
                }else if(reply.equals("I have this song")){
                    TransmitterManager.playSong(syncMsg.message);
                }else
                    System.out.println("----invalid replys in SongSyncSender");
            }
            else if(syncMsg.type==Type.PLAYER_REMOVE) {
                out.writeUTF("REMOVE");
                out.writeUTF(syncMsg.message); //tell which file to remove
            }
            else if(syncMsg.type==Type.PLAYER_SEEK) {
                out.writeUTF("SEEK");
                out.writeDouble(Double.parseDouble(syncMsg.message)); //����Ҫ�������Ž��ȵ������0.5��ʾ��50%
            }
            else if(syncMsg.type==Type.PLAYER_STOP) {
                out.writeUTF("STOP");
            }
        }
        
        
    }
    
}
class SongSyncReceiver extends BaseReceiver {
    public SongSyncReceiver(int port){
        super(port);
    }
    protected void notifyException(Exception ex){
        if(ex instanceof BindException)
            TransmitterManager.notifyReceiverPortAlreadyInUse("ͬ������������");
        else
            TransmitterManager.notifyOtherException(ex);
    }
    @Override
    protected void Work(DataInputStream in, DataOutputStream out) throws IOException {
        String msgType=in.readUTF();
        if("FILE".equals(msgType)){
            String name=in.readUTF();
            File f=new File(TransmitterManager.myplayer.getCachePath()+"\\"+name);
            TransmitterManager.notifySongReceivingStart(f);
            
            byte [] buffer=new byte [1024*64];
            FileOutputStream fileout=new FileOutputStream(f);
            int len;
            while((len=in.read(buffer))>0)
                fileout.write(buffer,0,len);
            fileout.close();
            TransmitterManager.notifySongReceivingCompleteAndAdd(f);
        }else if("PLAY".equals(msgType)){
            String name=in.readUTF();
            if(TransmitterManager.CanSwitchSongFromPeer(name)){
                out.writeUTF("I have this song");
                try {Thread.sleep(MessagePack.guessedDelayms);} catch (InterruptedException ex) {}//С��һ�£���ֹ��̫Զ
                TransmitterManager.playSong(name);
            }else
            {
                out.writeUTF("Not exists.Send me this song.");
            }
        }else if("STOP".equals(msgType)){
            TransmitterManager.processStopSongReq();
        }else if("REMOVE".equals(msgType)){
            String name=in.readUTF();
            TransmitterManager.processRemoveSongReq(name);
        }else if("SEEK".equals(msgType)){
            double rate=in.readDouble();
            TransmitterManager.processSeekSongReq(rate);
        }
    }
    
}