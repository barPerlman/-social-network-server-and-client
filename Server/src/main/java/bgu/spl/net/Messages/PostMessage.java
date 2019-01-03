package bgu.spl.net.Messages;

import bgu.spl.net.DataBase;

public class PostMessage extends Message {

    private String content;
    private String senderName;
    public PostMessage(){
        super((short) 5);
    }

    public PostMessage(String content,String sender){
        super((short) 5);
        this.content=content;
        senderName=sender;
    }

    public String getContent() {
        return content;
    }

    @Override
    public String getMsgString() {

        String msg="";
        msg+="POST"+" "+content;
        return msg;
    }

    public String getMsgStringAsAck() {

        String msg="";
        msg+="9"+" 1 "+senderName+" "+content;
        return msg;
    }

    @Override
    public byte[] getBytes() {
        byte[] opcode = this.shortToBytes(getOpcode());
        byte[] ans = mergeArrayes(opcode,content.getBytes());
        ans = includeOneByteToArray(ans,getDelimeter());
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
