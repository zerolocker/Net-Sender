/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package NetSender;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 一个抽象类，为数据接收提供底层实现。
 * @author Zero
 */
abstract class BaseReceiver {
    protected int port;
    protected ServerSocket server;
    /**由于工作在另一个线程上，异常也发生在这个线程上，不能被类的使用者捕获。<p>
     * 现在设计成每当异常发生时，这个方法都被调用。所以可以通过重写这个方法，让类的使用者自己处理异常。
     */
    protected void notifyException(Exception ex){}

    public BaseReceiver(int port) {
        this.port = port;
        Thread listenerThread = new Thread(new ReceiveReqHandler());
        listenerThread.start();
    }

    private class ReceiveReqHandler implements Runnable {

        @Override
        public void run() {
            try {
                server = new ServerSocket(port);
                while (true) {
                    Socket you = server.accept();
                    new ReceiveHandler(you).start();
                }
            }
            catch (IOException ex) {
                notifyException(ex);
                Logger.getLogger(BaseReceiver.class.getName()).log(Level.SEVERE, null, ex);
            } 
        }
    }

    private class ReceiveHandler extends Thread {

        private Socket you;

        public ReceiveHandler(Socket you) {
            this.you = you;
        }

        @Override
        public void run() {
            DataInputStream in = null;
            DataOutputStream out = null;
            try {
                in = new DataInputStream(you.getInputStream());
                out = new DataOutputStream(you.getOutputStream());
                Work(in, out);
            } catch (IOException ex) {
                notifyException(ex);
                Logger.getLogger(BaseReceiver.class.getName()).log(Level.SEVERE, null, ex);
            }
            //最后必须关闭流。（close()也会抛出I/O异常，所以又要catch）。
            try {
                in.close();out.close();you.close();
            } catch (IOException ex1) {
                notifyException(ex1);
                Logger.getLogger(BaseReceiver.class.getName()).log(Level.SEVERE, null, ex1);
            }
        }
    }
    
    /** 在子类中改写此方法，实现数据传输。 */
    protected abstract void Work(DataInputStream in, DataOutputStream out) throws IOException;

}
