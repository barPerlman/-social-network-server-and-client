#include "../include/ConnectionHandler.h"
#include <string>
#include <iostream>
#include <boost/asio.hpp>
#include <mutex>

using boost::asio::ip::tcp;
using std::cin;
using std::cout;
using std::cerr;
using std::endl;
using std::string;

/**
 * ConnectionHandler's constructor.
 *
 * @param host - the connection handler's host.
 * @param port - the connection handler's port.
 */
ConnectionHandler::ConnectionHandler(string host, short port): host_(host), port_(port), io_service_(), socket_(io_service_), delimiter('\0'), isLoggedOut(false){}

/**
 * ConnectionHandler's distructor.
 */
ConnectionHandler::~ConnectionHandler() {
    close();
}

/**
 * This method connects the ConnectionHandler.
 *
 * @return whether or not the connection was successful.
 */
bool ConnectionHandler::connect() {
    std::cout << "Starting connect to "
              << host_ << ":" << port_ << std::endl;
    try {
        tcp::endpoint endpoint(boost::asio::ip::address::from_string(host_), port_); // the server endpoint
        boost::system::error_code error;
        socket_.connect(endpoint, error);
        if (error)
            throw boost::system::system_error(error);
    }
    catch (std::exception& e) {
        std::cerr << "Connection failed (Error: " << e.what() << ')' << std::endl;
        return false;
    }
    return true;
}

// ********************** Sending a message to the server **********************

/**
 * This method receives what the user wrote
 * and returns whether or not the string was successfully written to the server.
 *
 * @param line - the string that the user wrote.
 * @return whether the string was successfully sent to the server or not.
 */
bool ConnectionHandler::sendLine(std::string& line) {
    return sendFrameAscii(line, this->delimiter);
}

/**
 * This method encodes the given string and sends it to the server.
 *
 * @param frame - the string that the user wrote.
 * @param delimiter - the delimiter.
 * @return whether the string was successfully sent to the server or not.
 */
bool ConnectionHandler::sendFrameAscii(const std::string& frame, char delimiter) {

    string::size_type indexOfSpace(frame.find(' ', 0)); // the first word is the message's type
    std::string messageTypeName(frame.substr(0, indexOfSpace)); // message's name
    short messageOpcode(messageTypeShort(messageTypeName)); // message's opcode

    char h[2];
    shortToBytes(messageOpcode, h);
    bool result = sendBytes(h, 2); // sending the message's opcode to the server
    if(!result) return false;

    std::string messageContent(frame.substr(indexOfSpace+1, frame.length()-(indexOfSpace+1))); // removing the message's opcode

    if (messageOpcode == 4) { // FOLLOW
        result = followMessage(messageContent);
        if (result == false)
            return false;
    }

    std::string message(changeStringToMessage(messageTypeName, messageContent));
    result = sendBytes(message.c_str(), messageContent.length() ); // sending the encoded message to the server
    if(!result) return false;

    if (messageTypeName == "LOGOUT"){ // in order to make the user wait for the server's response, on whether or not he should terminate
        this->isLoggedOut = true;
    }

    if(messageTypeName!="LOGOUT" && messageTypeName!="USERLIST")
        return sendBytes(&delimiter,1);
}

/**
 * This method returns the opcode of the given string.
 *
 * @param messageTypeName - the type of the message.
 * @return - the opcode of messageTypeName.
 */
short ConnectionHandler::messageTypeShort(std::string messageTypeName){;
    short messageTypeNum;
    if (messageTypeName == "REGISTER")
        messageTypeNum = 1;
    else if (messageTypeName == "LOGIN")
        messageTypeNum = 2;
    else if (messageTypeName == "LOGOUT")
        messageTypeNum = 3;
    else if (messageTypeName == "FOLLOW")
        messageTypeNum = 4;
    else if (messageTypeName == "POST")
        messageTypeNum = 5;
    else if (messageTypeName == "PM")
        messageTypeNum = 6;
    else if (messageTypeName == "USERLIST")
        messageTypeNum = 7;
    else // STAT
        messageTypeNum = 8;
    return messageTypeNum;
}

/**
 * This method changes short to byte.
 *
 * @param num - the number that needs to be translated to bytes.
 * @param bytesArr - the char in which the translated number will be written to.
 */
void ConnectionHandler::shortToBytes(short num, char* bytesArr){
    bytesArr[0] = ((num >> 8) & 0xFF);
    bytesArr[1] = (num & 0xFF);
}

bool ConnectionHandler::followMessage(std::string &messageContent){
    short followOrUnfollow;
    char h[2];
    if (messageContent[0] == '0')
        followOrUnfollow = 0;
    else
        followOrUnfollow = 1;
    shortToBytes(followOrUnfollow, h);
    char j[1];
    j[0]=h[1];
    bool result = sendBytes(j, 1); // sending 0/1 (follow or unfollow)
    if (!result) return false;

    string::size_type indexOfSpace = messageContent.find(' ', 2);

    std::string stringNumOfUsers(messageContent.substr(2, indexOfSpace-2)); // the amount of users
    short numOfUsers = std::stoi(stringNumOfUsers);
    shortToBytes(numOfUsers, h);
    result = sendBytes(h, 2); // sending numOfUsers
    if (!result) return false;

    messageContent = messageContent.substr(indexOfSpace+1, messageContent.length()-indexOfSpace); // removing followOrUnfollow & numOfUsers
    return true;
}

/**
 * This method changes the given string messageContent to an encoded string.
 *
 * @param messageTypeName - the type of the message.
 * @param messageContent - the content of the message.
 * @return the encoded given message.
 */
std::string ConnectionHandler::changeStringToMessage(std::string messageTypeName, std::string messageContent){
    std::string message;

    if (messageTypeName == "REGISTER" || messageTypeName == "LOGIN" || messageTypeName == "PM") {
        string::size_type indexOfSpace(messageContent.find(' ', 0)); // where the username ends
        message = messageContent.substr(0,indexOfSpace) +this->delimiter+messageContent.substr(indexOfSpace+1, messageContent.length()-(indexOfSpace+1));
        return message;
    }

    else if (messageTypeName == "LOGOUT" || messageTypeName == "USERLIST")  {
        message = "";
        return message;
    }

    else if (messageTypeName == "FOLLOW"){
        int counter = 0;
        while (counter< messageContent.length()){
            if (messageContent[counter]!=' ')
                message = message + messageContent[counter];
            else
                message = message + delimiter;
            counter++;
        }
        return message;
    }

    else { // if (messageTypeName == "POST" || messageTypeName == "STAT")
        message = messageContent;
        return message;
    }
}

/**
 * This method sends the "bytes" to the server.
 *
 * @param bytes - the array of chars that need to be written to the socket.
 * @param bytesToWrite - the amount of bytes that need to be written to the socket.
 * @return - whether the bytes were successfully written to the socket, or not.
 */
bool ConnectionHandler::sendBytes(const char bytes[], int bytesToWrite) {
    int tmp = 0;
    boost::system::error_code error;
    try {
        while (!error && bytesToWrite > tmp ) {
            tmp += socket_.write_some(boost::asio::buffer(bytes + tmp, bytesToWrite - tmp), error);
        }
        if(error){
            return false; // throw boost::system::system_error(error);
        }

    } catch (std::exception& e) {
        //std::cerr << "recv failed (Error: " << e.what() << ')' << std::endl;
        return false;
    }
    return true;
}

// ********************** Reading a message from the server **********************

/**
 * This method receives a string, in which the server's response will be written to.
 *
 * @param frame - the string in in which the server's response will be written to.
 * @return whether the response was written successfully or not.
 */
bool ConnectionHandler::getLine(std::string& frame) {
    if(!getFrameAscii(frame, this->delimiter)) // saving the message from the server
        return false;
    return true;
}

/**
 * This method makes sure that the server's response, is written in "frame".
 *
 * @param frame - the string in which the server's response is written to.
 * @param delimiter - the delimiter.
 * @return true if the server's response was succsesfully written to "frame", false otherwise.
 */
bool ConnectionHandler::getFrameAscii(std::string& frame, char delimiter) {
    short messageOpcode = getShort(frame, 2); // saving the message's opcode
    if (messageOpcode == -1) // if the opcode wasn't taken succsesfully
        return false;
    std::string messageTypeName = messageTypeString(messageOpcode);

    frame = ""; // removing the opcode that was written to frame

    if (messageTypeName == "NOTIFICATION")
        return createNotification(frame);
    else if (messageTypeName == "ACK")
        return createAck(frame);
    else
        return createError(frame);
}

/**
 * This method reads two bytes from the server, translates them to short & returns that short.
 *
 * @param frame - the string in which the char that represents the returned short is written to.
 * @return - the short representation of the bytes that were read from the server.
 */
short ConnectionHandler::getShort(std::string& frame, int counter){
    char ch;
    int counterWhile = 0;
    try {
        do{
            if(getBytes(&ch, 1)!= false) {
                frame.append(1, ch);
                counterWhile ++;
            }
            else
                return -1;
        } while (counterWhile<counter);
    } catch (std::exception& e) {
        return -1;
    }

    if (counter == 2){
        char messageTypeByte[2];// translating the bytes to char
        messageTypeByte[0] = frame[0];
        messageTypeByte[1] = frame[1];
        return bytesToShort(messageTypeByte); // translating the char to short
    }
    // incase there's only one byte to read
    char messageTypeByte[2];// translating the bytes to char
    messageTypeByte[0] = 0;
    messageTypeByte[1] = frame[0];
    return bytesToShort(messageTypeByte); // translating the char to short
}

/**
 * This method reads the amount "bytesToRead" of bytes from the server & writes them in "bytes"
 *
 * @param bytes - the array of chars, in which the chars that were read from the server, are written to.
 * @param bytesToRead - the amount of bytes to read from the socket.
 * @return - whether the bytes were read succsesfully or not.
 */
bool ConnectionHandler::getBytes(char bytes[], unsigned int bytesToRead) {
    size_t tmp = 0;
    boost::system::error_code error;
    try {
        while (!error && bytesToRead > tmp ) {
            tmp += socket_.read_some(boost::asio::buffer(bytes+tmp, bytesToRead-tmp), error);
        }
        if(error) {
            return false;
        }
    } catch (std::exception& e) {
        return false;
    }
    return true;
}

/**
 * This method translates the given char to short.
 *
 * @param bytesArr - the char that need to be translated to short.
 * @return - the translation of the given char to short.
 */
short ConnectionHandler::bytesToShort(char* bytesArr){
    short result = (short)((bytesArr[0] & 0xff) << 8);
    result += (short)(bytesArr[1] & 0xff);
    return result;
}

/**
 * This method returns the string representation of the given short.
 *
 * @param messageTypeNum - the short that needs to be represented with a string.
 * @return - the string representation of the given short.
 */
std::string ConnectionHandler::messageTypeString(short messageTypeNum){
    std::string messageTypeName;
    if (messageTypeNum == 9)
        messageTypeName = "NOTIFICATION";
    else if (messageTypeNum == 10)
        messageTypeName = "ACK";
    else // 11
        messageTypeName = "ERROR";
    return messageTypeName;
}

/**
 * This method creates a notification message and saves it in the given frame.
 *
 * @param frame - the string in which the message will be saved.
 * @return - whether the notification message was created successfully, or not.
 */
bool ConnectionHandler::createNotification(std::string& frame){
    short messageOpcode = getShort(frame, 1);
    if (messageOpcode == -1)
        return false;

    frame = ""; // clearing messageOpcode from "frame"

    std::vector<char> frameVector;
    if(getString(frameVector) == false)
        return false;

    std::string postingUser (frameVector.data(), frameVector.size());

    frameVector.clear();
    if(getString(frameVector) == false)
        return false;

    std::string content (frameVector.data(), frameVector.size());

    if (messageOpcode==1)  // public
        frame = "> NOTIFICATION Public " + postingUser+content;
    else  // pm
        frame = "> NOTIFICATION PM "+ postingUser+content;
}

/**
 * This method creates an ack message and saves it in the given frame.
 *
 * @param frame - the string in which the message will be saved.
 * @return - whether the ack message was created successfully, or not.
 */
bool ConnectionHandler::createAck(std::string& frame) {
    short messageOpcode = getShort(frame, 2);
    if (messageOpcode == -1)
        return false;

    frame = "";

    if (messageOpcode == 4 || messageOpcode == 7) { // Follow / Unfollow / userlist
        short numOfUsers = getShort(frame, 2);
        if (numOfUsers == -1)
            return false;

        frame = "";

        std::vector<char> frameVector;
        int counter = numOfUsers;
        while (counter>0) {
            if (getString(frameVector) == false)
                return false;
            counter--;
        }

        std::string listOfUsers(frameVector.data(), frameVector.size());
        std::string users (listOfUsers.substr(0, listOfUsers.length()-1));

        frame = "> ACK "+ std::to_string(messageOpcode) +" "+ std::to_string(numOfUsers)+" "+ users;
    }

    else if (messageOpcode == 8){ // stat
        short numPosts = getShort(frame, 2);
        if (numPosts == -1)
            return false;

        frame = "";

        short numFollowers = getShort(frame, 2);
        if (numFollowers == -1)
            return false;

        frame = "";

        short numFollowing = getShort(frame, 2);
        if (numFollowing == -1)
            return false;

        frame = "> ACK "+ std::to_string(messageOpcode) +" "+ std::to_string(numPosts)+" "+ std::to_string(numFollowers)+" "+std::to_string(numFollowing);
    }

    else {// register / login / logout / post / pm
        frame = "> ACK " + std::to_string(messageOpcode);
    }
    return true;
}

/**
 * This method creates an error message and saves it in the given frame.
 *
 * @param frame - the string in which the message will be saved.
 * @return - whether the error message was created successfully, or not.
 */
bool ConnectionHandler::createError(std::string& frame) {
    short messageOpcode = getShort(frame, 2);
    if (messageOpcode == -1)
        return false;

    frame = "> ERROR "+ std::to_string(messageOpcode);

    if (messageOpcode == 3)
        this->isLoggedOut = false;

    return true;
}


/**
 * This method reads bytes from the server & saves them in the given vector.
 *
 * @param frameVector - the string in which the chars that are written to.
 * @return - whether the bytes were read successfully, or not.
 */
bool ConnectionHandler::getString(std::vector<char>& frameVector) {
    char ch;
    // Stop when we encounter the null character.
    // Notice that the null character is not appended to the frame string.
    try {
        do {
            if (getBytes(&ch, 1) != false) {
                if (ch!=this->delimiter)
                    frameVector.push_back(ch);
                else {
                    frameVector.push_back(' ');
                    return true;
                }
            } else
                return false;
        } while (ch != this->delimiter);
    } catch (std::exception &e) {
        return false;
    }
    return true;
}

//*****************************************************************

/**
 * This method closes the ConnectionHandler.
 */
void ConnectionHandler::close() {
    try{
        socket_.close();
    } catch (...) {
        std::cout << "closing failed: connection already closed" << std::endl;
    }
}

bool ConnectionHandler::getIsLoggedOut(){
    return this->isLoggedOut;
}