package bgu.spl.net.Messages;

import bgu.spl.net.DataBase;

public class StatMessage extends Message {

    private String userName;

    public StatMessage(){
        super((short) 8);
    }

    public StatMessage(String userName){
        super((short) 8);
        this.userName=userName;
    }

    public String getUserName() {
        return userName;
    }

    @Override
    public String getMsgString() {
        return null;
    }

    @Override
    public String getMsgStringAsAck() {
        return null;
    }

    @Override
    public byte[] getBytes() {
        byte[] opcode = this.shortToBytes(getOpcode());
        byte[] ans = mergeArrayes(opcode,userName.getBytes());
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
