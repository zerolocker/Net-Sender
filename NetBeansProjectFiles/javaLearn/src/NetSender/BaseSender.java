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
 * һ�������࣬Ϊ���ݷ����ṩ�ײ�ʵ�֡�
 * @author Zero
 */
abstract class BaseSender {    
    String hostName;int port;
    public BaseSender(String hostName,int port){this.hostName = hostName;this.port = port;}
    public void Send(Object dataDescription){
        new SendHandler(dataDescription).start();
    }
    
    /**���ڹ�������һ���߳��ϣ��쳣Ҳ����������߳��ϣ����ܱ����ʹ���߲���<p>
     * ������Ƴ�ÿ���쳣����ʱ����������������á����Կ���ͨ����д��������������ʹ�����Լ������쳣��
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
            //������ر�������close()Ҳ���׳�I/O�쳣��������Ҫcatch����
            try {
                in.close();out.close();mysocket.close();
            } catch (IOException ex1) {
                notifyException(ex1);
                Logger.getLogger(BaseReceiver.class.getName()).log(Level.SEVERE, null, ex1);
            }
        }
    }
    
    /** �������и�д�˷�����ʵ�����ݴ��䡣 */
    protected abstract void Work(DataInputStream in, DataOutputStream out,Object dataDescription) throws IOException;

}
