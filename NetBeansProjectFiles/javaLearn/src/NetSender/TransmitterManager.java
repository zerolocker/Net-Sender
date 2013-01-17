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
 * 传输器管理器。这个类作为图形界面和底层实现（各种数据传输器）之间的中间层，图形界面和底层通过它来交换信息。<p>
 * 加入这个中间层为的是将交换信息的代码与图形界面中各种对控件的繁杂操作分离开。使程序的结构更清晰。
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
    public static JTextArea playerIPTextArea; //有时需要获得IP时窗口的控件没传过来，要自己来
    public static JSlider songPosBar;
    public static JLabel songTimeLabel;
    public static int playerPort=6331;
    public static void SendFile(String ip,int port,FileSenderListItem item){
            FileSender fst=new FileSender(ip,port);
            item.relatedProgressBar.setString("等待对方同意文件传输...");
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
        findRelatedProgressBarByFile(f,fileSenderList).setString("文件传输完成。");
    }
    static void notifyFileSendingRequestRejected(File f) {
        findRelatedProgressBarByFile(f,fileSenderList).setString("文件传输失败：对方拒绝了接收文件");
    }
    static void tellFileSendingProgress(double percent,int speed,File f) {
        findRelatedProgressBarByFile(f,fileSenderList).setValue((int)(percent));
        findRelatedProgressBarByFile(f,fileSenderList).setString("传输中...("+speed+"KB/s)");
    }
    static void notifySenderFailedToConnect(String nameOfSender) {
        JOptionPane.showMessageDialog(myframe, "传输失败："+nameOfSender+"无法连接指定的计算机。");
    }
    
    static void notifyOtherException(Exception ex) {
        JOptionPane.showMessageDialog(myframe, "数据传输器发生未知的异常：\n"+ex.toString());
    }

    static void notifyReceiverPortAlreadyInUse(String nameOfReceiver) {
        JOptionPane.showMessageDialog(myframe, 
                "将无法使用"+nameOfReceiver+"：端口被占用。\n(是否打开了两个带有接收器的程序？)");
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
    
     /**（注意）曾经出现进度条大小不正确的情况。后来加上synchronized(jList)关键字后解决，
     *  可能是代码中的jList被多个线程同时调用导致。
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
                    item.relatedProgressBar.setString("你拒绝了接收这个文件");
                    return null;
                } else if (s.equals("Done")) {
                    System.out.println("Done");
                    return null;
                } else {
                    return null;
                }
            } catch (InterruptedException ex) {
                item.relatedProgressBar.setString("未知错误：线程等待被终止");
                Logger.getLogger(TransmitterManager.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return null;
    }
    
    static void tellFileReceivingProgress(double percent, int speed, File f) {
        findRelatedProgressBarByFile(f,fileReceiverList).setValue((int)(percent));
        findRelatedProgressBarByFile(f,fileReceiverList).setString("传输中...("+speed+"KB/s)");
    }

    static void notifyFileReceivingCompleted(File f) {
        findRelatedProgressBarByFile(f,fileReceiverList).setString("文件传输完成。");
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
        //并行传多个文件会照成双方的播放列表顺序不一样，所以一个一个传，为避免等，另开线程。另见歌曲发送器的Work函数
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
        },"负责一个一个传输歌曲的线程").start();
    }

    static void notifySongSendingComplete(File f) {
        playerStatusTextArea.append("向对方发送 " +f.getName()+" 完成。\n");
        SongListItem item = findItemInSongList(f.getName());
        item.setDone();
    }

    static void notifySongReceivingCompleteAndAdd(File f) {
        playerStatusTextArea.append("从对方处接收 "+f.getName()+" 完成。\n");
        SongListItem item=new SongListItem(f);
        ((DefaultListModel)(songList.getModel())).addElement(item);
        item.setDone();
    }

    static void notifySongReceivingStart(File f) {
        playerStatusTextArea.append("开始从对方处接收 "+f.getName()+" 。\n");
    }
    
    static SongListItem findItemInSongList(String songname){
        for(Object item:((DefaultListModel)songList.getModel()).toArray())
            if(((SongListItem)item).file.getName().equals(songname))
                return (SongListItem)item;
        return null;
    }

    static void notifyPeerDontHaveThisSongAndSend(String name) {
        playerStatusTextArea.append("对方没有这首歌" +name+ "，将先发送它");
        SongListItem item=findItemInSongList(name);
        item.setUndone();
        new SongSyncSender(playerIPTextArea.getText(), playerPort).Send(item.file);
    }

    static boolean CanSwitchSongFromPeer(String name) {
        SongListItem item = findItemInSongList(name);
        if(item==null || !item.file.exists()){
            playerStatusTextArea.append("对方请求播放"+name+",此歌曲不存在，将先接收\n");
            return false;
        }else{
            playerStatusTextArea.append("对方请求播放"+name+"\n");
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
        try {Thread.sleep(MessagePack.guessedDelayms);} catch (InterruptedException ex) {} //小等一会
        myplayer.seek(rate);
        playerStatusTextArea.append("将对方歌曲进度调整到"+(int)(rate*100)+"%\n");
    }

    static void processSeekSongReq(double rate) {
        playerStatusTextArea.append("对方将歌曲进度调整到"+(int)(rate*100)+"%\n");
        myplayer.processSeek(rate);
    }

    static void processStopSongReq() {
        playerStatusTextArea.append("对方停止了播放歌曲\n");
        myplayer.stop();
    }

    static void sendStopSongMsgAndStop() {
        new SongSyncSender(playerIPTextArea.getText(), playerPort).Send(
                new MessagePack(playerIPTextArea.getText(), Type.PLAYER_STOP, null)
                );
        myplayer.stop();
        playerStatusTextArea.append("发送停止播放请求\n");
    }

    static void playNextSong() {
        songList.setSelectedValue(myplayer.getCurrentSongItem(), true);
        int cur=songList.getSelectedIndex();
        int max=songList.getModel().getSize();
        songList.setSelectedIndex((cur+1)%max);//循环列表
        TransmitterManager.sendPlaySongMsg(((SongListItem)(songList.getSelectedValue())).file);
    }

    static void sendRemoveSongReq(SongListItem item) {
        MessagePack msg=new MessagePack(playerIPTextArea.getText(), MessagePack.Type.PLAYER_REMOVE,item.file.getName());
        new SongSyncSender(playerIPTextArea.getText(), playerPort).Send(msg);
    }

    static void processRemoveSongReq(String name) {
        SongListItem item=findItemInSongList(name);
        ((DefaultListModel)(songList.getModel())).removeElement(item);
        playerStatusTextArea.append("对方从列表中删除了歌曲"+name+(item==null?",但本地列表不存在这首歌":"")+"\n");
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
    /** 猜测的一个网络延时，这个值被Sleep()使用，为了使双方进度尽量接近 */
    public static final int guessedDelayms=0;
    public enum Type{TEXT,PLAYER_PLAY,PLAYER_STOP,PLAYER_SEEK,PLAYER_REMOVE};
    public String srcName;
    public String message;
    public MessagePack.Type type;
    MessagePack(String ip,MessagePack.Type type,String message){
        srcName=ip;this.type=type;this.message=message;
    }
}