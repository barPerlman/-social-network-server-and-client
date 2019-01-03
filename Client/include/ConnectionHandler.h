#ifndef CONNECTION_HANDLER__
#define CONNECTION_HANDLER__

#include <string>
#include <iostream>
#include <boost/asio.hpp>
#include <mutex>
#include <condition_variable>

using boost::asio::ip::tcp;

class ConnectionHandler {
private:
    char delimiter;
    const std::string host_;
    const short port_;
    boost::asio::io_service io_service_;   // Provides core I/O functionality
    tcp::socket socket_;
    bool isLoggedOut;

    // ********************** Sending a message to the server **********************
    bool sendFrameAscii(const std::string& frame, char delimiter);
    short messageTypeShort(std::string messageTypeName);
    void shortToBytes(short num, char* bytesArr);
    bool followMessage(std::string &messageContent);
    std::string changeStringToMessage(std::string messageTypeName, std::string messageContent);
    bool sendBytes(const char bytes[], int bytesToWrite);

    // ********************** Reading a message from the server **********************
    bool getFrameAscii(std::string &frame, char delimiter);
    short getShort(std::string &frame, int counter);
    bool getBytes(char bytes[], unsigned int bytesToRead);
    short bytesToShort(char *bytesArr);
    std::string messageTypeString(short messageTypeNum);
    bool createNotification(std::string &frame);
    bool createAck(std::string &frame);
    bool createError(std::string &frame);
    bool getString(std::vector<char> &frameVector);

public:
    bool getIsLoggedOut();
    ConnectionHandler(std::string host, short port);
    virtual ~ConnectionHandler();

    // Connect to the remote machine
    bool connect();

    // Read an ascii line from the server
    // Returns false in case connection closed before a newline can be read.
    bool getLine(std::string& frame);

    // Send an ascii line from the server
    // Returns false in case connection closed before all the data is sent.
    bool sendLine(std::string& line);

    // Close down the connection properly.
    void close();

}; //class ConnectionHandler

#endif