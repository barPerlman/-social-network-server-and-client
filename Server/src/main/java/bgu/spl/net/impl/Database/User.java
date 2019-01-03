package bgu.spl.net.impl.Database;

import bgu.spl.net.Messages.Message;

import java.util.ArrayList;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.LinkedBlockingQueue;

public class User {
    private ArrayList<String> _followersList;   //holds users who follows me
    private ArrayList<String> _followingList;   //holds users who follows me
    private String _user_name;
    private String _password;
    private boolean _isLoggedIn;
    private int _connectionId;      //connection id which fits the connection handle datastructure connection id
    //note: connectionId =-1 means that the user has been never logged in
    private int _first_connectionId;    //holds the first connection id received in aim to be possible to sort by registered time
    private LinkedBlockingQueue<Message> _awaitingMessages;     //holds the messages sent to the user since last log out

    public int getNumOfPosts() {
        return numOfPosts;
    }

    public void increaseNumOfPosts() {
        this.numOfPosts++;
    }

    private int numOfPosts; //amount of posts this user published



    public User(String _user_name, String _password, int connectionId) {
        this._user_name = _user_name;
        this._password = _password;
        _followersList=new ArrayList<>();
        _connectionId=connectionId;
        _awaitingMessages=new LinkedBlockingQueue<>();
        numOfPosts=0;
        _followingList=new ArrayList<>();
    }

    //getters

    public int get_first_connectionId() {
        return _first_connectionId;
    }

    public int get_amountOfFollowing() {
        return _followingList.size();
    }
    public int get_amountOfFollowers(){
        return _followersList.size();
    }

    public ArrayList<String> get_followingList() {
        return _followingList;
    }

    public LinkedBlockingQueue<Message> get_awaitingMessages() {
        return _awaitingMessages;
    }

    public ArrayList<String> get_followersList() {
        return _followersList;
    }

    public String get_user_name() {
        return _user_name;
    }

    public String get_password() {
        return _password;
    }

    public int get_connectionId() {
        return _connectionId;
    }

    public boolean is_isLoggedIn() {
        return _isLoggedIn;
    }
    //raise by 1 when another new user is following this user
    //add user to follow this user
    public void addFollower(String userName){
        _followersList.add(userName);
    }
    //remove follower from list
    public void removeFollower(String userName){
        _followersList.remove(userName);
    }
    //set the status of loggin
    public void set_isLoggedIn(boolean loggedInStatus) {
        this._isLoggedIn = loggedInStatus;
    }
    //update the connection id of the handler associated to this user (Client)
    public void set_connectionId(int _connectionId) {

      //if it's the first connection of the user update the first connection id field
        if(this._connectionId==-1){
            _first_connectionId=_connectionId;
        }
        this._connectionId = _connectionId;
    }

    public void addFollowing(String userToFollow){
        _followingList.add(userToFollow);
    }
    public void stopFollowing(String userToUnFollow){
        _followingList.remove(userToUnFollow);
    }

    /**
     * checks if the received user is in the following list
     * @param userName - to check if exist
     * @return true if exist, else return false
     */
    public boolean checkIfUserExistInFollowing(String userName){
        boolean existUser=false;    //the returned value
        if(_followingList!=null) {
            for (String currUsr : _followingList) {
                if (currUsr.equals(userName)) {
                    existUser = true;
                }
            }
        }

        return existUser;
    }


}
