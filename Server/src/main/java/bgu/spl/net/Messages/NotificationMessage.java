package bgu.spl.net.Messages;

import bgu.spl.net.DataBase;

public class NotificationMessage extends Message {

    private byte notificationType; // '0' if PM message else '1' for Post message
    private String postingUser;
    private String content;

    public NotificationMessage(){
        super((short) 9);
    }

    public NotificationMessage(byte notificationType,String postingUser,String content){
        super((short) 9);
        this.notificationType=notificationType;
        this.postingUser=postingUser;
        this.content=content;
    }

    @Override
    public String getMsgString() {
        return null;
    }

    @Override
    public String getMsgStringAsAck() {
        return null;
    }

    @SuppressWarnings("Duplicates")
    @Override
    public byte[] getBytes() {
        byte[] opcode = this.shortToBytes(getOpcode());
        byte[] notificationByteArray = new byte[1];
        notificationByteArray[0]=notificationType;
        byte[] ans = mergeArrayes(opcode,notificationByteArray);
        ans = mergeArrayes(ans,postingUser.getBytes());
        ans =includeOneByteToArray(ans,getDelimeter());
        ans = mergeArrayes(ans,content.getBytes());
        ans =includeOneByteToArray(ans,getDelimeter());
        return ans;
    }

    @Override
    public short action(DataBase dataBase) {
        return 0;
    }

    @Override
    public Message newMessage(byte b) {
        return null;
    }
}
