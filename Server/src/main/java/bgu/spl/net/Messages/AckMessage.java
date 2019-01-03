package bgu.spl.net.Messages;

import bgu.spl.net.DataBase;

import java.util.ArrayList;

public class AckMessage extends Message {

    private short messageOpcode; //The message opcode the ACK was sent for
    private boolean isFollowOrUserlist;

    // fields for follow message or UserList message
    private short numOfUsers; // for the ack of the follow message,UserList message
    private ArrayList<String> userNameList;  // for the ack of the follow message,UserList message

    // fields for stat message
    private short numPosts;
    private short numFollowers;
    private short numFollowing;

    public AckMessage(short messageOpcode){
        super((short) 10);
        this.messageOpcode=messageOpcode;
    }

    // a constructor for follow message or UserList message
    public AckMessage(short messageOpcode,short numOfUsers,ArrayList<String> userNameList){
        super((short) 10);
        this.messageOpcode=messageOpcode;
        this.numOfUsers=numOfUsers;
        this.userNameList=userNameList;
        this.isFollowOrUserlist=true;
    }
    //stat message ack constructor
    public AckMessage(short messageOpcode,short numPosts,short numFollowers,short numFollowing){
        super((short) 10);
        this.numPosts=numPosts;
        this.numFollowers=numFollowers;
        this.numFollowing=numFollowing;
        this.isFollowOrUserlist=false;
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
        if(this.isFollowOrUserlist){ // its a foolow / userlist message
            byte[] opcode = this.shortToBytes(getOpcode());
            byte[] ans = mergeArrayes(opcode,this.shortToBytes(messageOpcode));
            ans = mergeArrayes(ans,this.shortToBytes(numOfUsers));
            for(String user : userNameList){
                ans = mergeArrayes(ans,user.getBytes());
                ans = includeOneByteToArray(ans,getDelimeter());
            }
            return ans;
        }
        else if(this.messageOpcode==8){ // its a stat message
            byte[] opcode = this.shortToBytes(getOpcode());
            byte[] ans = mergeArrayes(opcode,this.shortToBytes(messageOpcode));
            ans = mergeArrayes(ans,this.shortToBytes(numPosts));
            ans = mergeArrayes(ans,this.shortToBytes(numFollowers));
            ans = mergeArrayes(ans,this.shortToBytes(numFollowing));
            return ans;
        }
        else {  //it's any other message so get the regular ack bytes representation
            byte[] opcode = this.shortToBytes(getOpcode());
            byte[] ans = mergeArrayes(opcode,this.shortToBytes(messageOpcode));
            return ans;
        }
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
