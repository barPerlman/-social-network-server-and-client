# All Targets
all: BGSclient

# Tool invocations
# Executable "BGSclient" depends on the files inside bin folder
BGSclient: bin/Client.o bin/connectionHandler.o bin/MessageEncDec.o bin/Run.o
	@echo 'Building target: BGSclient'
	@echo 'Invoking: C++ Linker'
	g++ -o bin/BGSclient bin/Client.o bin/connectionHandler.o bin/Run.o bin/MessageEncDec.o -lboost_system -lpthread
	@echo 'Finished building target: BGSclient'
	@echo ' '

# Depends on the source and header files 
bin/Client.o: src/Client.cpp
	g++ -g -Wall -Weffc++ -std=c++11 -c -Iinclude -o bin/Client.o src/Client.cpp

# Depends on the source and header files 
bin/Run.o: src/Run.cpp
	g++ -g -Wall -Weffc++ -std=c++11 -c -Iinclude -o bin/Run.o src/Run.cpp

# Depends on the source and header files 
bin/connectionHandler.o: src/connectionHandler.cpp
	g++ -g -Wall -Weffc++ -std=c++11 -c -Iinclude -o bin/connectionHandler.o src/connectionHandler.cpp

# Depends on the source and header files 
bin/MessageEncDec.o: src/MessageEncDec.cpp
	g++ -g -Wall -Weffc++ -std=c++11 -c -Iinclude -o bin/MessageEncDec.o src/MessageEncDec.cpp

#Clean the build directory
clean: 
	rm -f bin/*
