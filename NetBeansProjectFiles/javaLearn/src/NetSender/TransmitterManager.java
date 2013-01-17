/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package NetSender;

import java.awt.Rectangle;
import java.awt.TrayIcon;
import java.io.*;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;
import NetSender.MessagePack.Type;














/**
 * ���������������������Ϊͼ�ν���͵ײ�ʵ�֣��������ݴ�������֮����м�㣬ͼ�ν���͵ײ�ͨ������������Ϣ��<p>
 * ��������м��Ϊ���ǽ�������Ϣ�Ĵ�����ͼ�ν����и��ֶԿؼ��ķ��Ӳ������뿪��ʹ����Ľṹ��������
 * @author Zero
 */
class TransmitterManager {
    public static JFrame myframe;
    public static FileReceiver fileReceiver = new FileReceiver(4331);
    public static ChatReceiver chatReceiver = new ChatReceiver(5331);
    public static SongSyncReceiver songSyncReceiver = new SongSyncReceiver(6331);
    public static Player myplayer=new Player();
    public static JList fileReceiverList;
    public static JList fileSenderList;
    public static JList songList;
    public static JTextArea playerStatusTextArea;
    public static JTextArea chatTextArea;
    public static JTextArea chatInputTextArea;
    public static JTextArea playerIPTextArea; //��ʱ��Ҫ���IPʱ���ڵĿؼ�û��������Ҫ�Լ���
    public static JSlider songPosBar;
    public static JLabel songTimeLabel;
    public static int playerPort=6331;
    public static void SendFile(String ip,int port,FileSenderListItem item){
            FileSender fst=new FileSender(ip,port);
            item.relatedProgressBar.setString("�ȴ��Է�ͬ���ļ�����...");
            fst.Send(item.file);
    }
    
    private static JProgressBar findRelatedProgressBarByFile(File f,JList list){
        if(list==fileReceiverList){
            for(Object item:((DefaultListModel)list.getModel()).toArray())
                if(((FileReceiverListItem)item).name.equals(f.getName()))
                    return ((FileReceiverListItem)item).relatedProgressBar;
        }else if(list==fileSenderList){
            for(Object item:((DefaultListModel)list.getModel()).toArray())
                if(((FileSenderListItem)item).file.equals(f))
                    return ((FileSenderListItem)item).relatedProgressBar;
        }
        return null;
    }
    static void notifyFileSendingCompleted(File f) {
        findRelatedProgressBarByFile(f,fileSenderList).setString("�ļ�������ɡ�");
    }
    static void notifyFileSendingRequestRejected(File f) {
        findRelatedProgressBarByFile(f,fileSenderList).setString("�ļ�����ʧ�ܣ��Է��ܾ��˽����ļ�");
    }
    static void tellFileSendingProgress(double percent,int speed,File f) {
        findRelatedProgressBarByFile(f,fileSenderList).setValue((int)(percent));
        findRelatedProgressBarByFile(f,fileSenderList).setString("������...("+speed+"KB/s)");
    }
    static void notifySenderFailedToConnect(String nameOfSender) {
        JOptionPane.showMessageDialog(myframe, "����ʧ�ܣ�"+nameOfSender+"�޷�����ָ���ļ������");
    }
    
    static void notifyOtherException(Exception ex) {
        JOptionPane.showMessageDialog(myframe, "���ݴ���������δ֪���쳣��\n"+ex.toString());
    }

    static void notifyReceiverPortAlreadyInUse(String nameOfReceiver) {
        JOptionPane.showMessageDialog(myframe, 
                "���޷�ʹ��"+nameOfReceiver+"���˿ڱ�ռ�á�\n(�Ƿ�����������н������ĳ���)");
    }
    private static JProgressBar makeAProgressBar(JList jList,Object item){
        jList.setSelectedValue(item, true);
        int maxIndex=jList.getSelectedIndex();
        Rectangle cellBounds = jList.getCellBounds(
                maxIndex, maxIndex);
        JProgressBar bar=new JProgressBar(0, 100);
        double proportion=2/3.0;
        int oldx=cellBounds.x,oldwidth=cellBounds.width;
        int newwidth=(int) (oldwidth*proportion),newx=oldx+(oldwidth-newwidth);
        cellBounds.setBounds(newx, cellBounds.y,newwidth, cellBounds.height);
        bar.setBounds(cellBounds);
        bar.setStringPainted(true);
        jList.add(bar);
        jList.repaint();
        return bar;
    }
    
     /**��ע�⣩�������ֽ�������С����ȷ���������������synchronized(jList)�ؼ��ֺ�����
     *  �����Ǵ����е�jList������߳�ͬʱ���õ��¡�
     */
    static File addFileReceiveReqAndWaitForDecision(String name, long size) {
        FileReceiverListItem item=new TransmitterManager.FileReceiverListItem(name, size);
        synchronized(fileReceiverList){
        ((DefaultListModel)(fileReceiverList.getModel())).addElement(item);
        item.relatedProgressBar=makeAProgressBar(fileReceiverList,item);
        item.relatedProgressBar.setString("");
        }
        synchronized(item){
            try {
                item.wait();
                String s = item.getStatus();
                if (s.equals("Accepted")) {
                    return new File(item.getSavePath());
                } else if (s.equals("Rejected")) {
                    item.relatedProgressBar.setStringPainted(true);
                    item.relatedProgressBar.setString("��ܾ��˽�������ļ�");
                    return null;
                } else if (s.equals("Done")) {
                    System.out.println("Done");
                    return null;
                } else {
                    return null;
                }
            } catch (InterruptedException ex) {
                item.relatedProgressBar.setString("δ֪�����̵߳ȴ�����ֹ");
                Logger.getLogger(TransmitterManager.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return null;
    }
    
    static void tellFileReceivingProgress(double percent, int speed, File f) {
        findRelatedProgressBarByFile(f,fileReceiverList).setValue((int)(percent));
        findRelatedProgressBarByFile(f,fileReceiverList).setString("������...("+speed+"KB/s)");
    }

    static void notifyFileReceivingCompleted(File f) {
        findRelatedProgressBarByFile(f,fileReceiverList).setString("�ļ�������ɡ�");
    }

    static void addFileSendingReq(File[] selectedFiles) {
        for(File f : selectedFiles){
            FileSenderListItem item=new TransmitterManager.FileSenderListItem(f);
            ((DefaultListModel)(fileSenderList.getModel())).addElement(item);
            item.relatedProgressBar=makeAProgressBar(fileSenderList,item);
            item.relatedProgressBar.setString("");
        }
    }

    static void showChatMessage(MessagePack pack) {
        try {
            chatTextArea.append(pack.srcName + ":  " + pack.message + "\n" );
            chatTextArea.setCaretPosition(chatTextArea.getText().length());
            String old=myframe.getTitle();myframe.requestFocus();
            Thread.sleep(500);myframe.setTitle("(New Msg!)"+old);
            Thread.sleep(500);myframe.setTitle("(        )"+old);
            Thread.sleep(500);myframe.setTitle("(New Msg!)"+old);
            Thread.sleep(500);myframe.setTitle("(        )"+old);
            Thread.sleep(500);myframe.setTitle("(New Msg!)"+old);
            Thread.sleep(500);old=old.replace("(New Msg!)", "");old=old.replace("(        )", "");
            myframe.setTitle(old);
        } catch (InterruptedException ex) {}
    }

    static void sendChatText(String ip, int port, String str) {
        try {
            String myNickname = InetAddress.getLocalHost().getHostName();
            new ChatSender(ip, port).Send(new MessagePack(myNickname, MessagePack.Type.TEXT, str));
        } catch (UnknownHostException ex) {
            Logger.getLogger(TransmitterManager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    static void addSongsAndSync(final String ip, final int port,final File[] selectedFiles) {   
        //���д�����ļ����ճ�˫���Ĳ����б�˳��һ��������һ��һ������Ϊ����ȣ����̡߳����������������Work����
        new Thread(new Runnable() {
            public void run() {
                for(File f : selectedFiles){
                    synchronized(f){
                        SongListItem item=new SongListItem(f);
                        ((DefaultListModel)(songList.getModel())).addElement(item);
                        new SongSyncSender(ip, port).Send(f); 
                        try {f.wait();} catch (InterruptedException ex) {
                            Logger.getLogger(TransmitterManager.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                }
            }
        },"����һ��һ������������߳�").start();
    }

    static void notifySongSendingComplete(File f) {
        playerStatusTextArea.append("��Է����� " +f.getName()+" ��ɡ�\n");
        SongListItem item = findItemInSongList(f.getName());
        item.setDone();
    }

    static void notifySongReceivingCompleteAndAdd(File f) {
        playerStatusTextArea.append("�ӶԷ������� "+f.getName()+" ��ɡ�\n");
        SongListItem item=new SongListItem(f);
        ((DefaultListModel)(songList.getModel())).addElement(item);
        item.setDone();
    }

    static void notifySongReceivingStart(File f) {
        playerStatusTextArea.append("��ʼ�ӶԷ������� "+f.getName()+" ��\n");
    }
    
    static SongListItem findItemInSongList(String songname){
        for(Object item:((DefaultListModel)songList.getModel()).toArray())
            if(((SongListItem)item).file.getName().equals(songname))
                return (SongListItem)item;
        return null;
    }

    static void notifyPeerDontHaveThisSongAndSend(String name) {
        playerStatusTextArea.append("�Է�û�����׸�" +name+ "�����ȷ�����");
        SongListItem item=findItemInSongList(name);
        item.setUndone();
        new SongSyncSender(playerIPTextArea.getText(), playerPort).Send(item.file);
    }

    static boolean CanSwitchSongFromPeer(String name) {
        SongListItem item = findItemInSongList(name);
        if(item==null || !item.file.exists()){
            playerStatusTextArea.append("�Է����󲥷�"+name+",�˸��������ڣ����Ƚ���\n");
            return false;
        }else{
            playerStatusTextArea.append("�Է����󲥷�"+name+"\n");
            return true;
        }
    }

    static void playSong(String name) {
        SongListItem item = findItemInSongList(name);
        if(item.getStatus()=="Done")
            myplayer.play(item);
    }
    static void sendPlaySongMsg(File f){
        MessagePack msg=new MessagePack(playerIPTextArea.getText(), MessagePack.Type.PLAYER_PLAY,f.getName());
        new SongSyncSender(playerIPTextArea.getText(), playerPort).Send(msg);
    }

    static void tellSongPos(int secondsAmount, int totalsec,double progress) {
        SimpleDateFormat f = new SimpleDateFormat("mm:ss");
        Calendar d1=Calendar.getInstance();
        d1.set(0, 0, 0, 0, 0, secondsAmount);
        Calendar d2=Calendar.getInstance();
        d2.set(0, 0, 0, 0, 0, totalsec);
        songTimeLabel.setText(f.format(d1.getTime())+"/"+f.format(d2.getTime()));
        if(songPosBar.getValueIsAdjusting()==false)
            songPosBar.setValue((int)(progress*100));
    }

    static void sendSongSeekReqAndSeek(double rate) {
        new SongSyncSender(playerIPTextArea.getText(), playerPort).Send(
                new MessagePack(playerIPTextArea.getText(), Type.PLAYER_SEEK, String.valueOf(rate))
                );
        try {Thread.sleep(MessagePack.guessedDelayms);} catch (InterruptedException ex) {} //С��һ��
        myplayer.seek(rate);
        playerStatusTextArea.append("���Է��������ȵ�����"+(int)(rate*100)+"%\n");
    }

    static void processSeekSongReq(double rate) {
        playerStatusTextArea.append("�Է����������ȵ�����"+(int)(rate*100)+"%\n");
        myplayer.processSeek(rate);
    }

    static void processStopSongReq() {
        playerStatusTextArea.append("�Է�ֹͣ�˲��Ÿ���\n");
        myplayer.stop();
    }

    static void sendStopSongMsgAndStop() {
        new SongSyncSender(playerIPTextArea.getText(), playerPort).Send(
                new MessagePack(playerIPTextArea.getText(), Type.PLAYER_STOP, null)
                );
        myplayer.stop();
        playerStatusTextArea.append("����ֹͣ��������\n");
    }

    static void playNextSong() {
        songList.setSelectedValue(myplayer.getCurrentSongItem(), true);
        int cur=songList.getSelectedIndex();
        int max=songList.getModel().getSize();
        songList.setSelectedIndex((cur+1)%max);//ѭ���б�
        TransmitterManager.sendPlaySongMsg(((SongListItem)(songList.getSelectedValue())).file);
    }

    static void sendRemoveSongReq(SongListItem item) {
        MessagePack msg=new MessagePack(playerIPTextArea.getText(), MessagePack.Type.PLAYER_REMOVE,item.file.getName());
        new SongSyncSender(playerIPTextArea.getText(), playerPort).Send(msg);
    }

    static void processRemoveSongReq(String name) {
        SongListItem item=findItemInSongList(name);
        ((DefaultListModel)(songList.getModel())).removeElement(item);
        playerStatusTextArea.append("�Է����б���ɾ���˸���"+name+(item==null?",�������б��������׸�":"")+"\n");
    }
    
    static class FileReceiverListItem{
        public JProgressBar relatedProgressBar=null;
        public String name;
        public long len;
        private  String status="Not Determined";
        private String savePath=null;
        public FileReceiverListItem(String filename,long length){
            name=filename;len=length;
        }
        public void setAccepted(String savePath){status="Accepted";this.savePath=savePath;}
        public void setRejected(){status="Rejected";}
        public void setDone(){status="Done";}
        public String getStatus(){return status;}
        public String getSavePath(){return savePath;}
        @Override
        public String toString(){
            return name+"("+len/1000+"KB)";
        }
    }
    static class FileSenderListItem{
        public JProgressBar relatedProgressBar=null;
        public File file;
        private  String status="Not determined";
        public FileSenderListItem(File file){
            this.file=file;
        }
        public void setDone(){status="Done";}
        public String getStatus(){return status;}
        @Override
        public String toString(){
            return file.getName()+"("+file.length()/1000+"KB)";
        }
    }
    static class SongListItem{
        boolean isPlaying=false;
        public File file;
        private String status="Synchronizing";
        public SongListItem(File file){
            this.file=file;
        }
        public void setDone(){status="Done";}
        public void setUndone(){status="Synchronizing";}
        public String getStatus(){return status;}
        @Override
        public String toString(){
            return file.getName();
        }
    }
    
}

class MessagePack{
    /** �²��һ��������ʱ�����ֵ��Sleep()ʹ�ã�Ϊ��ʹ˫�����Ⱦ����ӽ� */
    public static final int guessedDelayms=0;
    public enum Type{TEXT,PLAYER_PLAY,PLAYER_STOP,PLAYER_SEEK,PLAYER_REMOVE};
    public String srcName;
    public String message;
    public MessagePack.Type type;
    MessagePack(String ip,MessagePack.Type type,String message){
        srcName=ip;this.type=type;this.message=message;
    }
}