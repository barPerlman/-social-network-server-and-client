package bgu.spl.net.impl.Database;

import bgu.spl.net.Messages.Message;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.concurrent.LinkedBlockingQueue;

public class Datastructure {
    private  HashMap<String,User> registeredUsersHM;   //holds the registered users
    private  HashMap<Timestamp, LinkedBlockingQueue<Message>> sentMessages;    //holds post and pm messages sent with their timestamp


    public Datastructure(){
        registeredUsersHM=new HashMap<>();
        sentMessages=new HashMap<>();
    }


    /**
     * this returns a user with conID or null if can't fimd
     * @param conID integer represents the user connection id
     * @return
     */
    public  User getUserByConId(int conID){
        User usrToReturn=null;
        for(User currUser:registeredUsersHM.values()){
            if(conID==currUser.get_connectionId()){
                usrToReturn=currUser;
            }
        }
        return usrToReturn;
    }

    public HashMap<String, User> getRegisteredUsersHM() {
        return registeredUsersHM;
    }

    public HashMap<Timestamp, LinkedBlockingQueue<Message>> getSentMessages() {
        return sentMessages;
    }
}
