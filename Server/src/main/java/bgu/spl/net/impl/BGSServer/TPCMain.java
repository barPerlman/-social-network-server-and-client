package bgu.spl.net.impl.BGSServer;

import bgu.spl.net.impl.Database.Datastructure;
import bgu.spl.net.srv.Server;


public class TPCMain {

    public static void main(String[] args) {

        Datastructure _db=new Datastructure();

        Server.threadPerClient(
                7777, //port
                () -> new BGSProtocol(_db), //protocol factory
                BGSEncoderDecoder::new //message encoder decoder factory
        ).serve();

    }
}
