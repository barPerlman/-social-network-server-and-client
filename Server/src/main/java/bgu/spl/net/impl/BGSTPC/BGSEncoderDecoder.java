package bgu.spl.net.impl.BGSTPC;

import bgu.spl.net.Messages.AckMessage;
import bgu.spl.net.Messages.ErrorMessage;
import bgu.spl.net.Messages.NotificationMessage;
import bgu.spl.net.api.MessageEncoderDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;

public class BGSEncoderDecoder implements MessageEncoderDecoder<String> {

    private static final short REGISTER=1,LOGIN=2,LOGOUT=3,FOLLOW=4,POST=5,PM=6,USERLIST=7,STAT=8,NOTIFICATION=9,ACK=10,ERROR=11;

    private byte[] bytes = new byte[1 << 10]; //start with 1k
    private int len = 0;
    private int decodeByteCounter=0;
    private String resha="";
    private boolean isCommandEnded=false;
    private int counterMsg=0;
    private int counterFollow=2;
    private byte[] arrayFollow = new byte[2];
    private boolean byteFollow=false; // if the next byte that we are going to read represents the follow/unfollow byte
    private short numOfSpaces=-1;


    @Override   //TODO complete this
    public String decodeNextByte(byte nextByte) {
        //notice that the top 128 ascii characters have the same representation as their utf-8 counterparts
        //this allow us to do the following comparison
        pushByte(nextByte);
        if(nextByte == '\0' && byteFollow==false)
            counterMsg++;
        byteFollow=false;
        if (decodeByteCounter==1){
            short typeMsg=bytesToShort(bytes);
            switch (typeMsg){
                case REGISTER:
                    resha="REGISTER";
                    break;
                case LOGIN:
                    resha="LOGIN";
                    break;
                case LOGOUT:
                    resha="LOGOUT";
                    break;
                case FOLLOW:
                    resha="FOLLOW";
                    //byteFollow=true;
                    break;
                case POST :
                    resha="POST";
                    break;
                case PM:
                    resha="PM";
                    break;
                case USERLIST:
                    resha="USERLIST";
                    break;
                case STAT:
                    resha="STAT";
                    break;
            }
            bytes = new byte[1 << 10];
            len=0;
        }
        isLastZiro(counterMsg,resha);
        decodeByteCounter++;

        if (isCommandEnded) { // if the command ends
            isCommandEnded=false;
            String temp = resha;
            resha = "";
            if(temp.compareTo("LOGIN")==0||temp.compareTo("LOGOUT")==0||temp.compareTo("REGISTER")==0||temp.compareTo("USERLIST")==0){
                return temp + " " + popStringLoginLogoutRegisterUserlistStat();
            }
            else if(temp.compareTo("FOLLOW")==0){
                return temp+" "+popStringFollow();
            }
            else if(temp.compareTo("POST")==0){
                return temp+" "+popStringPost();
            }
            else if(temp.compareTo("PM")==0){
                return temp+" "+popStringPm();
            }
            else if(temp.compareTo("STAT")==0){
                return temp+" "+popStringLoginLogoutRegisterUserlistStat();
            }
        }

        return null; //not a line yet
    }

    private void isLastZiro(int counterMsg, String resha) {
        switch (resha){
            case "REGISTER":
                if(counterMsg==3)
                    isCommandEnded=true;
                break;
            case "LOGIN":
                if(counterMsg==3)
                    isCommandEnded=true;
                break;
            case "LOGOUT":
                if(counterMsg==1)
                    isCommandEnded=true;
                break;
            case "FOLLOW": // todo fix the counter
                if(decodeByteCounter==1)
                    byteFollow=true;
                if(decodeByteCounter==2)
                    byteFollow=true;
                if(decodeByteCounter==3)
                    byteFollow=true;
                if(counterFollow==4){
                    arrayFollow[0]=bytes[1];
                }
                else if(counterFollow==5){
                    arrayFollow[1]=bytes[2];
                    numOfSpaces=bytesToShort(arrayFollow);
                }
                counterFollow++;

                if(numOfSpaces!=-1 && counterMsg==Integer.parseInt(Short.toString(numOfSpaces))+1)
                    isCommandEnded=true;
                break;
            case "POST":
                if(counterMsg==2)
                    isCommandEnded=true;
                break;
            case "PM":
                if(counterMsg==3)
                    isCommandEnded=true;
                break;
            case "USERLIST":
                if(counterMsg==2)
                    isCommandEnded=true;
                break;
            case "STAT":
                if(counterMsg==2)
                    isCommandEnded=true;
                break;
        }
    }

   /* private int zeroesAmountInSentence (){

    }*/

    public short bytesToShort(byte[] byteArr)
    {
        short result = (short)((byteArr[0] & 0xff) << 8);
        result += (short)(byteArr[1] & 0xff);
        return result;
    }

    @Override
    public byte[] encode(String message) {
        String[] splittedMsg=message.split(" ");   //split the message by its spaces
        //get the opCode
        Short opCode=Short.parseShort(splittedMsg[0]);
        byte[] encoded=null;    //holds the encoded message
        switch (opCode){
            case NOTIFICATION:
                encoded=encodeNotificationMsg(splittedMsg);
                break;
            case ACK:
                encoded= encodeAckMsg(splittedMsg);
                break;
            case ERROR:
                encoded= encodeErrorMsg(splittedMsg);
                break;
        }
        return encoded;
    }


    /**
     *encode notification message
     * @param splittedMsg - holds the opCode,type char ,poster,content
     * @return  - the notification message in bytes (encoded)
     */
    private byte[] encodeNotificationMsg(String[] splittedMsg) {
        String contentMsg="";
        for(int i=3;i<splittedMsg.length;i++){//connect the parts of the message
            contentMsg+=splittedMsg[i]+" ";
        }
        Character type=splittedMsg[1].charAt(0);
        byte[] typeByte=(type.toString()).getBytes();


        NotificationMessage Nmsg=new NotificationMessage(typeByte[0],splittedMsg[2],contentMsg);
        byte[] encoded=Nmsg.getBytes();
        return encoded;
    }


    /**
     * encode Ack message
     * @param splittedMsg
     * @return
     */
    private byte[] encodeAckMsg(String[] splittedMsg) {
        short msgOpCode=Short.parseShort(splittedMsg[1]);   //get the message opCode
        byte[] encoded=null;
        AckMessage ackMsg;
        String numOfUsers;
        ArrayList<String> userNameList;
        switch (msgOpCode){
            case STAT:
                ackMsg=new AckMessage(msgOpCode,Short.parseShort(splittedMsg[2]),Short.parseShort(splittedMsg[3]),Short.parseShort(splittedMsg[3]));
                encoded=ackMsg.getBytes();
                break;
            case USERLIST:
                 numOfUsers=splittedMsg[2];
                userNameList=new ArrayList<>();   //get user names into this
                for(int i=3;i<splittedMsg.length;i++){  //build the array of users from splitted message
                    userNameList.add(splittedMsg[i]);
                }
                ackMsg=new AckMessage(msgOpCode,Short.parseShort(numOfUsers),userNameList);
                encoded=ackMsg.getBytes();
                break;
            case FOLLOW:
                 numOfUsers=splittedMsg[2];
                userNameList=new ArrayList<>();   //get user names into this
                for(int i=3;i<splittedMsg.length;i++) {  //build the array of users from splitted message
                    userNameList.add(splittedMsg[i]);
                }
                ackMsg=new AckMessage(msgOpCode,Short.parseShort(numOfUsers),userNameList);
                encoded=ackMsg.getBytes();
                break;
            default:    //any other ack message
                ackMsg=new AckMessage(msgOpCode);
                encoded=ackMsg.getBytes();
                break;
        }
        return encoded;
    }

    /**
     *encode error message
     * @param splittedMsg - holds in this case the opcode in index0 and msg
     *                    opcode inindex 1
     * @return  - the error message in bytes (encoded)
     */
    private byte[] encodeErrorMsg(String[] splittedMsg) {
        ErrorMessage erMsg=new ErrorMessage(Short.parseShort(splittedMsg[1]));
        byte[] encoded=erMsg.getBytes();
        return encoded;
    }

    private void pushByte(byte nextByte) {
        if (len >= bytes.length) {
            bytes = Arrays.copyOf(bytes, len * 2);
        }

        bytes[len++] = nextByte;
    }

    @SuppressWarnings("Duplicates")
    private String popStringLoginLogoutRegisterUserlistStat() {
        String result="", partOfTheResult="";
        //notice that we explicitly requesting that the string will be decoded from UTF-8
        //this is not actually required as it is the default encoding in java.
        int indexPrevSpace=-1;// the index of the previous space;
        // handle the translation of the bytes until the last space
        for(int i=0; i<=decodeByteCounter-2;i++){
            if(bytes[i]==0){//TODO pay attention that it can be 00
                partOfTheResult = new String(bytes, indexPrevSpace+1, i-(indexPrevSpace+1), StandardCharsets.UTF_8); // translating until the space
                result += partOfTheResult; // adds the translation
                result+=" "; // adds the space
                indexPrevSpace=i; // resets the last index of thr previous space
            }
        }
        //String result = new String(bytes, 0, len, StandardCharsets.UTF_8);
        len = 0;
        resetStatics();
        return result;
    }


    @SuppressWarnings("Duplicates")
    private String popStringFollow() {

        String result="";
        //get the follow unfollow op code
       // byte[] opArr=new byte[1];
       // opArr[0]=bytes[0];
      //  Short followOp=bytesToShort(opArr);
        result+=bytes[0];
        result+=" ";
        //get the number of followers
        byte[] opArr=new byte[2];
        opArr[0]=bytes[1];
        opArr[1]=bytes[2];
        Short numOfUsers=bytesToShort(opArr);
        result+=numOfUsers;
        result+=" ";
       // int num_of_users=Short.toUnsignedInt(numOfUsers);
        String partOfTheResult="";
        int indexPrevSpace=2;// the index of the previous space;
        // handle the translation of the bytes until the last space
        for(int i=3; i<=decodeByteCounter-2;i++){
            if(bytes[i]==0){
                partOfTheResult = new String(bytes, indexPrevSpace+1, i-(indexPrevSpace+1), StandardCharsets.UTF_8); // translating until the space
                result += partOfTheResult; // adds the translation
                result+=" "; // adds the space
                indexPrevSpace=i; // resets the last index of thr previous space
            }
        }

        len = 0;
        resetStatics();
        return result;
    }

    @SuppressWarnings("Duplicates")
    private String popStringPost() {

       String result="";
        // int num_of_users=Short.toUnsignedInt(numOfUsers);
        String partOfTheResult="";
        int indexPrevSpace=-1;// the index of the previous space;
        // handle the translation of the bytes until the last space
        for(int i=0; i<=decodeByteCounter-2;i++){
            if(bytes[i]==0){
                partOfTheResult = new String(bytes, indexPrevSpace+1, i-(indexPrevSpace+1), StandardCharsets.UTF_8); // translating until the space
                result += partOfTheResult; // adds the translation
                result+=" "; // adds the space
                indexPrevSpace=i; // resets the last index of thr previous space
            }
        }

        len = 0;
        resetStatics();
        return result;

    }
    @SuppressWarnings("Duplicates")
    private String popStringPm() {
        String result="";
        // int num_of_users=Short.toUnsignedInt(numOfUsers);
        String partOfTheResult="";
        int indexPrevSpace=-1;// the index of the previous space;
        // handle the translation of the bytes until the last space
        for(int i=0; i<=decodeByteCounter-2;i++){
            if(bytes[i]==0){
                partOfTheResult = new String(bytes, indexPrevSpace+1, i-(indexPrevSpace+1), StandardCharsets.UTF_8); // translating until the space
                result += partOfTheResult; // adds the translation
                result+=" "; // adds the space
                indexPrevSpace=i; // resets the last index of thr previous space
            }
        }
        len = 0;
        resetStatics();
        return result;
    }



    private void resetStatics(){
          len = 0;
          decodeByteCounter=0;
          counterMsg=0;
        counterFollow=2;
         arrayFollow = new byte[2];
        bytes = new byte[1 << 10];
    }
}
