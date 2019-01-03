package bgu.spl.net.Messages;

import bgu.spl.net.DataBase;

public class UserListMessage extends Message{

    public UserListMessage(){
        super((short) 7);
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
        return opcode;
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
