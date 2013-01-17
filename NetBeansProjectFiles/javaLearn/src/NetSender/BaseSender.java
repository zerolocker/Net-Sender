/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package NetSender;

import java.io.*;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 一个抽象类，为数据发送提供底层实现。
 * @author Zero
 */
abstract class BaseSender {    
    String hostName;int port;
    public BaseSender(String hostName,int port){this.hostName = hostName;this.port = port;}
    public void Send(Object dataDescription){
        new SendHandler(dataDescription).start();
    }
    
    /**由于工作在另一个线程上，异常也发生在这个线程上，不能被类的使用者捕获。<p>
     * 现在设计成每当异常发生时，这个方法都被调用。所以可以通过重写这个方法，让类的使用者自己处理异常。
    */
    protected void notifyException(Exception ex){}
    
    private class SendHandler extends Thread {
        Object dataDescription;
        
        SendHandler(Object dataDescription) {
            this.dataDescription = dataDescription;
        }

        @Override
        public void run() {
            DataInputStream in=null; DataOutputStream out =null;Socket mysocket=null;
            try {
                mysocket = new Socket(hostName, port);
                in = new DataInputStream(mysocket.getInputStream());
                out = new DataOutputStream(mysocket.getOutputStream());

                Work(in, out, dataDescription);

            } catch (IOException ex) {
                notifyException(ex);
                Logger.getLogger(BaseReceiver.class.getName()).log(Level.SEVERE, null, ex);
            }            
            //最后必须关闭流。（close()也会抛出I/O异常，所以又要catch）。
            try {
                in.close();out.close();mysocket.close();
            } catch (IOException ex1) {
                notifyException(ex1);
                Logger.getLogger(BaseReceiver.class.getName()).log(Level.SEVERE, null, ex1);
            }
        }
    }
    
    /** 在子类中改写此方法，实现数据传输。 */
    protected abstract void Work(DataInputStream in, DataOutputStream out,Object dataDescription) throws IOException;

}
