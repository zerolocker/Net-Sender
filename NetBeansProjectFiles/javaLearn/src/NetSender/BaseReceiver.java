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
 * һ�������࣬Ϊ���ݽ����ṩ�ײ�ʵ�֡�
 * @author Zero
 */
abstract class BaseReceiver {
    protected int port;
    protected ServerSocket server;
    /**���ڹ�������һ���߳��ϣ��쳣Ҳ����������߳��ϣ����ܱ����ʹ���߲���<p>
     * ������Ƴ�ÿ���쳣����ʱ����������������á����Կ���ͨ����д��������������ʹ�����Լ������쳣��
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
            //������ر�������close()Ҳ���׳�I/O�쳣��������Ҫcatch����
            try {
                in.close();out.close();you.close();
            } catch (IOException ex1) {
                notifyException(ex1);
                Logger.getLogger(BaseReceiver.class.getName()).log(Level.SEVERE, null, ex1);
            }
        }
    }
    
    /** �������и�д�˷�����ʵ�����ݴ��䡣 */
    protected abstract void Work(DataInputStream in, DataOutputStream out) throws IOException;

}
