#ifndef CLIENT_CLIENT_H
#define CLIENT_CLIENT_H
#include <thread>
#include "ConnectionHandler.h"

class Client{
private:
    ConnectionHandler connectionHandler;
    bool stop;
    int id;
    std::string clientName;
public:
    Client(std::string host, short port, int id);
    //Client & operator=(const Client & other); //copy assignment
    Client(const Client & other); //copy constructor
    ~Client(); //destructor
    //Client(Client && other); //Move constructor
    //Client & operator=(Client && other); //Move assignment
    void runWriter();
    void runReader();
    ConnectionHandler& getConnectionHandler();
    bool getStop();
};

#endif //CLIENT_CLIENT_H
