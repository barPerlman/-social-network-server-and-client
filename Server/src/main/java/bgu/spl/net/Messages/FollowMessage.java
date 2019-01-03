package bgu.spl.net.Messages;

import bgu.spl.net.DataBase;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class FollowMessage extends Message {

    private byte isFollow;// '0' if follow, else '1'
    private short numOfUsers;
    private ArrayList<String> userNameList;

    public FollowMessage(){
        super((short) 4);
    }

    public ArrayList<String> getUserNameList() {
        return userNameList;
    }

    public byte getIsFollow() {
        return isFollow;
    }

    public short getNumOfUsers() {
        return numOfUsers;
    }

    public FollowMessage(byte isFollow, short numOfUsers, ArrayList<String> userNameList){
        super((short) 4);
        this.isFollow=isFollow;
        this.numOfUsers=numOfUsers;
        this.userNameList=userNameList;
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
        byte[] ans = mergeArrayes(opcode,(isFollow+"").getBytes());
        ans = mergeArrayes(ans,this.shortToBytes(numOfUsers));
        for(String user : userNameList){
            ans = mergeArrayes(ans,user.getBytes());
            ans = includeOneByteToArray(ans,getDelimeter());
        }
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
