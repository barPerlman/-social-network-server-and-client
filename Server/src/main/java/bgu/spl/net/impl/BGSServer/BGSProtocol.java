package bgu.spl.net.impl.BGSServer;

import bgu.spl.net.Messages.Message;
import bgu.spl.net.Messages.PmMessage;
import bgu.spl.net.Messages.PostMessage;
import bgu.spl.net.api.bidi.BidiMessagingProtocol;
import bgu.spl.net.api.bidi.Connections;
import bgu.spl.net.impl.Database.Datastructure;
import bgu.spl.net.impl.Database.User;
import bgu.spl.net.srv.ConnectionsImpl;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.concurrent.LinkedBlockingQueue;

public class BGSProtocol implements BidiMessagingProtocol<String> {

    private boolean shouldTerminate;
    private ConnectionsImpl<String> _activeCon;
    private int _CurrConID;
    private Datastructure _db;  //data base which holds the registered users and messages
    //synchronize objects
    private Object _msgSyncObject;  //lock on messages DS
    private Object _usersLock;      //lock on users DS

    public BGSProtocol(Datastructure db){
        shouldTerminate = false;
        _db=db;
        _msgSyncObject=new Object();
        _usersLock=new Object();
    }
    @Override
    public void start(int connectionId, Connections<String> connections) {
       _activeCon= (ConnectionsImpl<String>)connections;
        _CurrConID=connectionId;
    }

    @Override
    public void process(String msg) {
        String[] parts = msg.split(" ");
        String resha=parts[0];  //get the command
        switch (resha){
            case "REGISTER":
                handleRegister(parts);
                break;
            case "LOGIN":
                handleLogin(parts);
                break;
            case "LOGOUT":
                handleLogout();
                break;
            case "FOLLOW":
                handleFollow(parts);
                break;
            case "POST":
                handlePost(parts);
                break;
            case "PM":
                handlePM(parts);
                break;
            case "USERLIST":
                handleUserlist();
                break;
            case "STAT":
                handleStat(parts);
                break;
            default:
                System.out.println("illegal command");
                break;
        }
    }

    /**
     * get a data about the received user
     * the data is num of posts,num of followers and following
     * @param parts
     */
    private void handleStat(String[] parts) {
        synchronized (_usersLock) {
            String userToget = parts[1];  //get the user name to get his data
            User interestUser = _db.getRegisteredUsersHM().get(userToget);   //the user we wants his data
            User currUser = _db.getUserByConId(_CurrConID);             //the current user who is request
            if (currUser != null && currUser.is_isLoggedIn()) {   //curr user is logged in
                if (interestUser != null) { //the user we want his data is registered
                    //send ack
                    _activeCon.send(_CurrConID, "10" + " 8 " + interestUser.getNumOfPosts() + " " + interestUser.get_amountOfFollowers() + " " + interestUser.get_amountOfFollowing());
                } else {//the user we want his data is not registered
                    _activeCon.send(_CurrConID, "11" + " 8");  //error message
                }
            } else {   //the requesting user is not logged in
                _activeCon.send(_CurrConID, "11" + " 8");  //error message
            }

        }
    }
    /**
     * send registered users list
     */
    private void handleUserlist() {
        synchronized (_usersLock) {
            User user;  //current client
            ArrayList<User> regUsersList = new ArrayList<>();   //holds the registerd users to order by connection id
            if ((user = _db.getUserByConId(_CurrConID)) != null && user.is_isLoggedIn()) {    //user is logged in
                for (User regUser : _db.getRegisteredUsersHM().values()) { //insert registered users to list
                    regUsersList.add(regUser);
                }
                //order the list of users
                Collections.sort(regUsersList, new Comparator<User>() {
                    @Override   //define compare method for comperator by connection id
                    public int compare(User user1, User user2) {
                        if (user1.get_first_connectionId() == user2.get_first_connectionId())
                            return 0;
                        return user1.get_first_connectionId() < user2.get_first_connectionId() ? -1 : 1;
                    }
                });
                int numberOfUsers = regUsersList.size();
                String usersList = "";
                for (int i = 0; i < numberOfUsers; i++) {   //insert the sorted user names into string
                    usersList += regUsersList.get(i).get_user_name() + " ";
                }
                //send ack message
                _activeCon.send(_CurrConID, "10" + " 7" + " " + numberOfUsers + " " + usersList);
            } else {   //curr user is not logged in
                //send error message
                _activeCon.send(_CurrConID, "11" + " 7");
            }
        }
    }
    /**
     * send private message to another user
     * @param parts parts of the message content
     */
    private void handlePM(String[] parts) {
        synchronized (_msgSyncObject) {
            String targetUser = parts[1]; //the user wee need to send the message
            targetUser=targetUser.substring(1);
            String contentMsg = ""; //the message to send
            for (int i = 2; i < parts.length; i++) {    //unseparate the msg
                contentMsg += parts[i] + " ";
            }
            User user;
            //the post user is logged in so we can send his message
            if ((user = _db.getUserByConId(_CurrConID)) != null && user.is_isLoggedIn()) {
                User dstUser;
                if ((dstUser = _db.getRegisteredUsersHM().get(targetUser)) != null) {    //the reciepient is registered
                    //add the message to the mesages data structure
                    Timestamp currTime;
                    _db.getSentMessages().putIfAbsent(currTime = new Timestamp(System.currentTimeMillis()), new LinkedBlockingQueue<>());  //create queue for the timestamp if not exist
                    _db.getSentMessages().get(currTime).add(new PmMessage(targetUser, contentMsg, user.get_user_name()));
                    boolean dstUserStatus = dstUser.is_isLoggedIn();
                    //the dstUser is logged in
                    if (dstUserStatus) {
                        int dstUserConId = dstUser.get_connectionId();    //get the dst user connection id
                        //send the message to the destination user
                        _activeCon.send(dstUserConId, "9" + " 0 " + _db.getUserByConId(_CurrConID).get_user_name() + " " + contentMsg);//SEND THE MESSAGE as notification
                    } else {    //the dstUser is logged out then send the message to his waiting messages list
                        dstUser.get_awaitingMessages().add(new PmMessage(targetUser, contentMsg, user.get_user_name()));
                    }
                    //send ack message
                    _activeCon.send(_CurrConID, "10" + " 6");
                } else {// the reciepient is not registered send error message
                    _activeCon.send(_CurrConID, "11" + " 6");
                }
            } else {//the sender is not logged in so send error
                _activeCon.send(_CurrConID, "11" + " 6");
            }
        }
    }
    /**
     * send post message to the followers of this user in case they are logged in
     * otherwise add the message to their waiting messages list
     * @param parts
     */
    private void handlePost(String[] parts) {
        synchronized (_msgSyncObject) {
            String contentMsg = "";
            for (int i = 1; i < parts.length; i++) {
                contentMsg += parts[i] + " ";
            }
            //first check the user can post (is loggedin)
            User user;
            //the post user is logged in so we can post his message
            if ((user = _db.getUserByConId(_CurrConID)) != null && user.is_isLoggedIn()) {
                //save the message in the messages data structure
                Timestamp currTime;
                _db.getSentMessages().putIfAbsent(currTime = new Timestamp(System.currentTimeMillis()), new LinkedBlockingQueue<>());  //create queue for the timestamp if not exist
                _db.getSentMessages().get(currTime).add(new PostMessage(contentMsg, user.get_user_name()));
                //publish the post to followers or mentioned by @ tag
                publishPost(user, contentMsg);    //publish the msg to the logged in required or add to their wait list if loggedout
                user.increaseNumOfPosts();  //update amount of published posts by the user
                //send ack msg
                _activeCon.send(_CurrConID, "10" + " 5");
            } else {//the user doesn't exist or logged out
                //send error message
                _activeCon.send(_CurrConID, "11" + " 5");
            }
        }
    }
    /**
     * publish the post to the required users
     * @param user posting user
     * @param contentMsg -the received message
     */
    private void publishPost(User user, String contentMsg) {
        String[] splittedMsg=contentMsg.split(" ");
        ArrayList<String> personalTargetUsers=new ArrayList<>();    //holds the users mentioned in the message content
        for(String isUser:splittedMsg){     //iterate parts of message to find the mentioned users
            if(isUser.contains("@")){ //this is a mentioned user
                //add to mentioned list in case:
                //1. user is registered
                //2. user is not following the curr user
                //3.user is not already inserted to list of mentioned
                String userToCheck=(isUser.substring((isUser.indexOf("@"))+1));   //add the user without the @ sign
                if((_db.getRegisteredUsersHM().get(userToCheck)!=null)&&!user.get_followersList().contains(userToCheck)&&!personalTargetUsers.contains(userToCheck)){
                    personalTargetUsers.add(userToCheck);
                }
            }
        }
        //publish to the mentioned users
        publishPostToMentioned(personalTargetUsers,contentMsg);
        //publish to the followers
        publishPostToFollowers(contentMsg);
    }
    private void publishPostToFollowers(String contentMsg) {
        User currUser=_db.getUserByConId(_CurrConID);
        for(String follower:currUser.get_followersList()){  //publish the post to all of the user followers
            User followerUser;
            if((followerUser=_db.getRegisteredUsersHM().get(follower))!=null){//check the follower is registered
                boolean followerStatus=followerUser.is_isLoggedIn();
                if(followerStatus){    //the follower is logged in so send him post
                    int followerConId=followerUser.get_connectionId();    //get the follower connection id
                    _activeCon.send(followerConId,"9"+" 1 " +_db.getUserByConId(_CurrConID).get_user_name()+" "+contentMsg);//SEND THE MESSAGE as notification
                }
                else{   //the target follower is logged out so put in his waiting messages queue
                    followerUser.get_awaitingMessages().add(new PostMessage(contentMsg,currUser.get_user_name()));
                }
            }
        }
    }
    /**
     * publish post to users who mentioned in the content of message
     * @param personalTargetUsers-users to send to
     * @param contentMsg -the message content
     */
    private void publishPostToMentioned(ArrayList<String> personalTargetUsers, String contentMsg) {
      User dsUser;
        for(String user:personalTargetUsers){
            if((dsUser=_db.getRegisteredUsersHM().get(user))!=null){   //if the target user is registered to the system
                boolean userLoginStatus=dsUser.is_isLoggedIn(); //holds login status
                if(userLoginStatus){    //target is logged in
                    int targetUserConID=dsUser.get_connectionId();  //get the user connectionHandler connection id
                    _activeCon.send(targetUserConID,"9"+" 1 " +_db.getUserByConId(_CurrConID).get_user_name()+" "+contentMsg);//SEND THE MESSAGE as notification
                }
                else{   //the target is logged out
                    //add the msg to the waiting list of the target
                    dsUser.get_awaitingMessages().add(new PostMessage(contentMsg,_db.getUserByConId(_CurrConID).get_user_name()));
                }
            }
        }
    }

    /**
     * perform follow after the users mentioned in the string
     * @param parts
     */
    private void handleFollow(String[] parts) {
        synchronized (_usersLock) {
            User userInDS = _db.getUserByConId(_CurrConID);
            if (userInDS != null && userInDS.is_isLoggedIn()) {  //this user is logged in so we can check insertion to his followers
                String opCode = parts[1];
                ArrayList<String> followersToCheckAndAdd = new ArrayList<>(); //holds the inserted followers in temporary list
                String numOfUsers = parts[2]; //amount of users to read into list
                int intNumOfUsers = Integer.parseInt(numOfUsers);
                for (int i = 3; i < intNumOfUsers + 3; i++) {
                    followersToCheckAndAdd.add(parts[i]);
                }
                //here we check if we need to try follow or unfollow this list and act accordingly
                if (Integer.parseInt(opCode) == 0) {
                    tryFollow(userInDS, followersToCheckAndAdd);
                } else {  //this opCode ==1 so try unfollow
                    tryUnfollow(userInDS, followersToCheckAndAdd);
                }
            } else {//this user is not logged in return error message
                _activeCon.send(_CurrConID, "11" + " 4");
            }
        }
    }
    /**
     * remove users in the array list from following list
     * @param userInDS  - the user which the list of following belongs to
     * @param unFollowingToCheck - the list with users to try to remove
     */
    private void tryUnfollow(User userInDS, ArrayList<String> unFollowingToCheck) {

        int succeededAmount=0;  //holds amount of success unfollows
        ArrayList<String> refOfFollowingList=userInDS.get_followingList();
        String successfullyUnFollowed="";
        for(String toUnFollow:unFollowingToCheck){    //this try to remove
            boolean currUsrExist=userInDS.checkIfUserExistInFollowing(toUnFollow);    //holds the result of if the iterated user is in following list
            if(currUsrExist){  //'userInDS' is  following ' toUnfollow' so remove from following list
                userInDS.stopFollowing(toUnFollow);
                succeededAmount++;
                //get the user "to Follow"
                User toUnFollowUser=_db.getRegisteredUsersHM().get(toUnFollow);
                toUnFollowUser.removeFollower(userInDS.get_user_name());                     //add the follower to followers list of the 'to follow' followers list
                //add to string of followed
                successfullyUnFollowed+=toUnFollow+" ";
            }

        }
        //check for command success
        if(succeededAmount==0){ //follow failed
            //send error message
            _activeCon.send(_CurrConID,"11"+" 4");
        }
        else{   //send ack msg
            _activeCon.send(_CurrConID,"10"+" 4"+" "+succeededAmount+" "+successfullyUnFollowed);
        }
    }

    /**
     * this try to add to user following list
     * @param follower  - the user which is following to the users in the following list
     * @param followingToCheckAndAdd - items to add to the list
     */
    private void tryFollow(User follower, ArrayList<String> followingToCheckAndAdd) {
        int succeededAmount=0;  //holds amount of success follows
        ArrayList<String> refOfFollowingList=follower.get_followingList();
        String successfullyFollowed="";
        for(String toFollow:followingToCheckAndAdd){    //this try to add
            boolean currUsrExist=follower.checkIfUserExistInFollowing(toFollow);    //holds the result of if the iterated user is in following list
            User toFollowUser=_db.getRegisteredUsersHM().get(toFollow);
            if(!currUsrExist&&toFollowUser!=null){  //'follower' is not following 'to follow' so add to following list and the one to follow is exist
                follower.addFollowing(toFollow);
                succeededAmount++;
                //get the user "to Follow"
                toFollowUser.addFollower(follower.get_user_name());                     //add the follower to followers list of the 'to follow' followers list
                //add to string of followed
                successfullyFollowed+=toFollow+" ";
            }
        }
        //check for command success
        if(succeededAmount==0){ //follow failed
            //send error message
            _activeCon.send(_CurrConID,"11"+" 4");
        }
        else{   //send ack msg
            _activeCon.send(_CurrConID,"10"+" 4"+" "+succeededAmount+" "+successfullyFollowed);
        }
    }
    /**
     * logout a user out of the server
     */
    private void handleLogout() {
        synchronized (_msgSyncObject) {
            //there's no such user or he is not logged in
            User user;
            if ((user = _db.getUserByConId(_CurrConID)) == null || !user.is_isLoggedIn()) {
                //return error msg
                _activeCon.send(_CurrConID, "11" + " 3"); //we sign the separation between opCodes by whitespace
            } else {   //perform logout
                user.set_isLoggedIn(false);                     //update status in data structures
                _activeCon.send(_CurrConID, "10" + " 3");     //return an ACK message to Client
                shouldTerminate = true;       //verify its the right place to put it!!!
            }
        }
    }
    /**
     * login a user into the server
     * @param parts - parts of the message command
     */
    private void handleLogin(String[] parts) {
        synchronized (_usersLock) {
            String user_name = parts[1];
            String password = parts[2];
            //check if there is a user with received user name
            //and the password match
            //and this user is not already logged in
            //and there is no user taken the current id means that client occupied by another login
            User user;
            if ((user = _db.getRegisteredUsersHM().get(user_name)) != null &&
                    password.equals(user.get_password()) &&
                    !user.is_isLoggedIn()&&
            _db.getUserByConId(_CurrConID)==null) {
                //perform login
                user.set_isLoggedIn(true);

                user.set_connectionId(_CurrConID);  //verify the connection id at the user meets the conId of the connection handler

                //send ACK msg
                _activeCon.send(_CurrConID, "10" + " 2");
                //  reading of awaiting messages queue
                LinkedBlockingQueue<Message> awaitedMessages = _db.getRegisteredUsersHM().get(user_name).get_awaitingMessages();
                for (Message msg : awaitedMessages) {
                    String notification = msg.getMsgStringAsAck(); //get the actual string for the message
                    _activeCon.send(_CurrConID, notification);   //accomplish the send of the waited message to its user dest
                }
            } else {   //logging in failed
                _activeCon.send(_CurrConID, "11" + " 2"); //we sign the separation between opCodes by whitespace
            }
        }
    }
    /**
     * handle register request
     * @param parts - parts of the message command
     */
    private void handleRegister(String[] parts) {
        synchronized (_usersLock) {
            User userCheck=null;
            String user_name = parts[1];
            String password = parts[2];
            //check if doesn't exist and im not logged ins
            if (_db.getRegisteredUsersHM().get(user_name) == null&&((userCheck=_db.getUserByConId(_CurrConID))==null||!userCheck.is_isLoggedIn())) {

                //add a user with the following name and password to the Users data structure
                //connectionid=-1 means that this user has been never logged in
                    _db.getRegisteredUsersHM().put(user_name, new User(user_name, password,-1));

                //send ACK msg in return
                _activeCon.send(_CurrConID, "10" + " 1"); //we sign the separation between opCodes by whitespace
            } else {   //user with this user name is already exist. return error msg
                //11=opcode for error, 1=opCode for register
                _activeCon.send(_CurrConID, "11" + " 1"); //we sign the separation between opCodes by whitespace
            }

        }
    }
    @Override
    public boolean shouldTerminate() {
        return shouldTerminate;
    }
}
