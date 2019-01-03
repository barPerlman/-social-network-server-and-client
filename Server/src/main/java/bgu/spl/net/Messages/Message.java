package bgu.spl.net.Messages;

import bgu.spl.net.DataBase;

import java.util.Arrays;

public abstract class  Message {

    private short opcode;
    private byte delimeter;

    public Message(short opcode){
        this.opcode=opcode;
        this.delimeter=0;
    }

    public short getOpcode(){return this.opcode;}

    public byte getDelimeter(){return this.delimeter;}

    public short bytesToShort(byte[] byteArr)
    {
        short result = (short)((byteArr[0] & 0xff) << 8);
        result += (short)(byteArr[1] & 0xff);
        return result;
    }

    public byte[] shortToBytes(short num)
    {
        byte[] bytesArr = new byte[2];
        bytesArr[0] = (byte)((num >> 8) & 0xFF);
        bytesArr[1] = (byte)(num & 0xFF);
        return bytesArr;
    }

    public byte[] mergeArrayes(byte[] arr1, byte[] arr2){
        byte[] result = Arrays.copyOf(arr1,arr1.length+arr2.length);
        System.arraycopy(arr2,0,result,arr1.length,arr2.length);
        return result;
    }

    public byte[] includeOneByteToArray(byte[] arr,byte b){
        byte[] result = Arrays.copyOf(arr,arr.length+1);
        result[arr.length]=b;
        return result;
    }
    abstract public String getMsgString();
    abstract public String getMsgStringAsAck();


    abstract public byte[] getBytes();

    //abstract public String sendBytes();

    abstract public short action (DataBase dataBase);

    abstract public Message newMessage(byte b);

}
