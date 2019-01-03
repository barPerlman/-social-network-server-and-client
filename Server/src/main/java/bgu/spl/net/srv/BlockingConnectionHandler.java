package bgu.spl.net.srv;

import bgu.spl.net.api.MessageEncoderDecoder;
import bgu.spl.net.api.bidi.BidiMessagingProtocol;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.Socket;

public class BlockingConnectionHandler<T> implements Runnable, java.io.Closeable,ConnectionHandler<T> {

    private final BidiMessagingProtocol<T> protocol;
    private final MessageEncoderDecoder<T> encdec;
    private final Socket sock;
    private BufferedInputStream in;
    private BufferedOutputStream out;
    private volatile boolean connected = true;
    private ConnectionsImpl _connections;
    private  int _conIdToInsert;

    public BidiMessagingProtocol<T> getProtocol() {
        return protocol;
    }

    public BlockingConnectionHandler(Socket sock, MessageEncoderDecoder<T> reader, BidiMessagingProtocol<T> protocol,ConnectionsImpl con) {
        this.sock = sock;
        this.encdec = reader;
        this.protocol = protocol;
        this._connections=con;
    }

    @Override
    public void run() {
        try (Socket sock = this.sock) { //just for automatic closing
            int read;

            in = new BufferedInputStream(sock.getInputStream());
            out = new BufferedOutputStream(sock.getOutputStream());

            //add the connection handler to data structure in connectionsImpl
            _conIdToInsert=_connections.connect(this);
            this.getProtocol().start(_conIdToInsert,_connections);

            while (!protocol.shouldTerminate() && connected && (read = in.read()) >= 0) {
                T nextMessage = encdec.decodeNextByte((byte) read);
                if (nextMessage != null) {
                    protocol.process(nextMessage); //the handler process the message by the protocol

                }
            }

        } catch (IOException ex) {
            ex.printStackTrace();
        }

    _connections.disconnect(_conIdToInsert);
    }



    @Override
    public void close() throws IOException {

        connected = false;
        sock.close();
    }

    @Override
    public void send(T msg) {

        try {
            out.write(encdec.encode(msg));
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
