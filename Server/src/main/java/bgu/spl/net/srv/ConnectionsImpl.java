package bgu.spl.net.srv;

import bgu.spl.net.api.bidi.Connections;

import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * implementation class of the Connections interface
 * @param <T>
 */

public class ConnectionsImpl<T> implements Connections<T> {

   private static AtomicInteger _conIdToInsert;  //id of the new active client to add into list




    //list of the new ConnectionHandler interface for  each  active  client
    //it is mapped by the connectionId as key and the its connectionHandler as a value
    private HashMap<Integer, ConnectionHandler<T>> _activeClients;

    //constructor

    public ConnectionsImpl() {
        this._activeClients = new HashMap<>();
        this._conIdToInsert=new AtomicInteger(0);
    }


    //implementations of connections methods:

    public HashMap<Integer, ConnectionHandler<T>> get_activeClients() {
        return _activeClients;
    }


    @Override
    public boolean send(int connectionId, T msg) {

        if(_activeClients.get(connectionId)==null){
            return false;
        }
        _activeClients.get(connectionId).send(msg);
        return true;
    }

    @Override
    public void broadcast(T msg) {

        for(ConnectionHandler<T> activeClient:_activeClients.values()){
            activeClient.send(msg);
        }

    }

    @Override
    public void disconnect(int connectionId) {

        try {
            _activeClients.get(connectionId).close();
            _activeClients.remove(connectionId);

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    //getter for the active clients connectionHandlers list
    public HashMap<Integer,ConnectionHandler<T>> getActiveClientsHandlers(){
        return _activeClients;
    }
    public int connect(ConnectionHandler<T> clientToConnect){
        _activeClients.put(_conIdToInsert.get(),clientToConnect);   //add the new client into connected clients data structure
        return _conIdToInsert.getAndIncrement();

    }

}
