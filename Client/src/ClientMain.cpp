#include <stdlib.h>
#include "../include/ConnectionHandler.h"
#include "../include/Client.h"
#include <thread>

int main (int argc, char **argv) {
    if (argc < 3) {
        std::cerr << "Usage: " << argv[0] << " host port" << std::endl << std::endl;
        return -1;
    }
    std::string host = argv[1];
    short port = atoi(argv[2]);

    int id = 0; // *********************
    Client client(host, port, id); // creating a new client

    if (!client.getConnectionHandler().connect()) {
        std::cerr << "Cannot connect to " << host << ":" << port << std::endl;
        return 1;
    }

    std::thread threadWrite (&Client::runWriter, &client);
    std::thread threadRead (&Client::runReader, &client);

    threadRead.join();
    threadWrite.join();
    if (client.getStop() == true) {
        client.getConnectionHandler().close();
    }

    return 0;
}