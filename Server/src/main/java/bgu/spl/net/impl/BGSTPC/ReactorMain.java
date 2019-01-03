package bgu.spl.net.impl.BGSTPC;

import bgu.spl.net.impl.Database.Datastructure;
import bgu.spl.net.srv.Server;

public class ReactorMain {
    public static void main(String[] args) throws Exception{

        Datastructure _db=new Datastructure();
        int port=7777;
        int threadsAmount=0;
        if(args.length>1){
            port=Integer.parseInt(args[0]);
            threadsAmount=Integer.parseInt(args[1]);
        }
        else throw new Exception("not enough arguments!");
        Server.reactor(
                           threadsAmount,
                         port, //port
                       () -> new BGSProtocol(_db), //protocol factory
                          BGSEncoderDecoder::new //message encoder decoder factory
                 ).serve();
    }
}
