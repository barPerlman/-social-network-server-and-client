package bgu.spl.net.Messages;

import bgu.spl.net.DataBase;

import java.util.ArrayList;

public class PmMessage extends Message{
    private String senderName;
    private String receipienttName;
    private String content;

    public PmMessage(){
        super((short) 6);
    }

    public PmMessage(String userName, String content,String sender){
        super((short) 6);
        this.receipienttName=userName;
        this.content=content;
        senderName=sender;
    }

    public String getUserName() {
        return receipienttName;
    }

    public String getContent() {
        return content;
    }

    @Override
    public String getMsgString() {
        String msg="";
        msg+="PM"+" "+receipienttName+" "+content;
        return msg;
    }
    public String getMsgStringAsAck() {

        String msg="";
        msg+="9"+" 0 "+senderName+" "+content;
        return msg;
    }

    @Override
    public byte[] getBytes() {
        byte[] opcode = this.shortToBytes(getOpcode());
        byte[] ans = mergeArrayes(opcode,receipienttName.getBytes());
        ans = includeOneByteToArray(ans,getDelimeter());
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
