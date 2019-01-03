CFLAGS:=-c -Wall -Weffc++ -g -std=c++11 -Iinclude
LDFLAGS:=-lboost_system-mt


all: Client
	g++ -o bin/ bin/ConnectionHandler.o bin/Client.o bin/ClientMain.o  $(LDFLAGS)

Client: bin/ConnectionHandler.o bin/Client.o bin/ClientMain.o

bin/ConnectionHandler.o: src/ConnectionHandler.cpp
	g++ $(CFLAGS) -o bin/ConnectionHandler.o src/ConnectionHandler.cpp

bin/Client.o: src/Client.cpp
	g++ $(CFLAGS) -o bin/Client.o src/Client.cpp

bin/ClientMain.o: src/ClientMain.cpp
   	g++ $(CFLAGS) -o bin/ClientMain.o src/ClientMain.cpp

.PHONY: clean
clean:
	rm -f bin/*
