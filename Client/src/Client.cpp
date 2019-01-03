#include <thread>
#include "../include/ConnectionHandler.h"
#include "../include/Client.h"
#include <boost/asio.hpp>

/**
 * This class represents a client, that sends messages to the server and recives messages from it.
 */

/**
 * Client's constructor.
 *
 * @param host -  the connection handler's host.
 * @param port - the connection handler's port.
 * @param id - the client's ID.
 */
Client::Client(std::string host, short port, int id): connectionHandler(host, port), stop(false), id(id), clientName("CLIENT#" + std::to_string(id)){}

/**
 * The method that the thread threadWrite is responsible for.
 */
void Client::runWriter(){
    while(!this->stop)  {
        if (this->connectionHandler.getIsLoggedOut()==false) {
            const short bufsize(1024);
            char buf[bufsize];
            std::cin.getline(buf, bufsize); // getting a new line from the user
            std::string line(buf);
            int len(line.length());

            if (!connectionHandler.sendLine(line)) { // if it wasn't possible to send the line from the user break;
                // std::cout << "Disconnected. Exiting...\n" << std::endl;
                //this->stop = true;
                //break;
            }
        }
     //   else std::cerr << "terminate run writer" << std::endl;
    }
}

/**
 * The method that the thread threadRead is responsible for.
 */
void Client::runReader(){
    while(!this->stop && this->connectionHandler.getIsLoggedOut()==false) {
        if (this->connectionHandler.getIsLoggedOut()==false){
            std::string answer;

            if (!connectionHandler.getLine(answer)) { // if it wasn't possible to get the answer from the server
                //  std::cout << "Disconnected. Exiting...\n" << std::endl;
                // this->stop = true;
                // break;
            }
            if (answer!=""){
                std::cout << this->clientName+" "+answer <<std::endl;

                std::string::size_type index(answer.find('>', 0));
                std::string s = answer.substr(index+2, index+6);
                if (s.compare("ACK 3") == 0){
                    this->stop = true;
                    this->getConnectionHandler().close();
                }
            }
        }
        //else std::cerr << "terminate run reader" << std::endl;
    }
}

/**
 * This method returns a pointer to the client's connection handler.
 *
 * @return - a pointer to the client's connection handler.
 */
ConnectionHandler& Client::getConnectionHandler(){
    return this->connectionHandler;
}

/**
 * This method returns whether the client's threads need to stop or not.
 *
 * @return - whether the client's threads need to stop or not.
 */
bool Client::getStop(){
    return this->stop;
}


/**
 * Destructor - this method destructs this client.
 */
Client :: ~Client() {
    this->connectionHandler.close();
}

/**
 * Copy constructor - this method makes a copy of this Client and saves it in the given Client "other".
 *
 * @param other - the Client in which the copy of this Client will be saved.
 */
Client :: Client(const Client &other): stop(other.stop), id(other.id), clientName(other.clientName), connectionHandler("11", 1){
}

/**
 * Move constructor - this method makes a copy of this Client, saves it in the given Client "other"
 * and deletes this Client.
 *
 * @param other - the Client in which the copy of this Client will be saved.

Client :: Client(Client&& other): orderPrint(other.orderPrint),capacity(other.capacity),numberTable(other.numberTable),open(other.open),customersList(), orderList(){
    customersList = std:: move(other.customersList);
    orderList = std:: move(other.orderList);
    other.open = false;
}
*/
/**
 * Copy assignment - this method makes a copy of the given Client "other" and saves it in this Client.
 *
 * @param other - the Client that this Client will be identical to.

Client& Client :: operator=(const Client &other) {
    if (this != &other) {
        clear();
        for (Customer *customer:other.customersList)
            this->customersList.push_back(customer->clone());
        for (OrderPair orderPair:other.orderList) {
            OrderPair op(orderPair.first, orderPair.second);
            orderList.push_back(op);
        }
    }
    return *this;

}
*/
/**
 * Move assignment - this method makes a copy of the given Client "other", saves it in this Client
 * and deletes the given Client "other".
 *
 * @param other - the Client that this Client will be identical to.

Client &Client::operator=(Client &&other) {
    if (this != &other) {
        clear();
        customersList = std::move(other.customersList);
        orderList = std::move(other.orderList);
        open = other.open;
        capacity = other.capacity;
        numberTable = other.numberTable;
    }
    return *this;
} */