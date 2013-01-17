/*
 * ���ļ��������������ݴ��������ࣺChatSender,ChatReceiver
 */
package NetSender;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.*;
import java.util.logging.Level;
import java.util.logging.Logger;


class ChatSender extends BaseSender {
    ChatSender(String ip,int port){
        super(ip,port);
    }
    @Override
    protected void Work(DataInputStream in, DataOutputStream out, Object dataDescription) throws IOException {
        MessagePack pack=(MessagePack)dataDescription;
        out.writeUTF(pack.srcName);
        out.writeUTF(pack.type.name());
        out.writeUTF(pack.message);
    }
    @Override
    protected void notifyException(Exception ex){
        if(ex instanceof ConnectException)
            TransmitterManager.notifySenderFailedToConnect("������Ϣ������");
        else if(ex instanceof UnknownHostException)
            TransmitterManager.notifySenderFailedToConnect("������Ϣ������");
        else
            TransmitterManager.notifyOtherException(ex);
    }
}

class ChatReceiver extends BaseReceiver {
    
    ChatReceiver(int port) {
        super(port);
    }
    @Override
    protected void Work(DataInputStream in, DataOutputStream out) throws IOException {
        String ip=in.readUTF();
        String type=in.readUTF();
        String message=in.readUTF();
        MessagePack pack=new MessagePack(ip,MessagePack.Type.valueOf(type),message);
        if(pack.type==MessagePack.Type.TEXT)
            TransmitterManager.showChatMessage(pack);
    }
    protected void notifyException(Exception ex){
        if(ex instanceof BindException)
            TransmitterManager.notifyReceiverPortAlreadyInUse("������Ϣ������");
        else
            TransmitterManager.notifyOtherException(ex);
    }
}