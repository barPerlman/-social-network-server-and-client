package bgu.spl.net.Messages;

import bgu.spl.net.DataBase;

public class ErrorMessage extends Message {

    private short messageOpcode; //The message opcode the ACK was sent for

    public ErrorMessage(){ super((short)11);}

    public ErrorMessage(short messageOpcode){
        super((short)11);
        this.messageOpcode=messageOpcode;
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
        byte[] ans = mergeArrayes(opcode,this.shortToBytes(messageOpcode));
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
