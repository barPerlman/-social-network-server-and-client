package bgu.spl.net.impl.newsfeed;

import bgu.spl.net.impl.BGSTPC.BGSEncoderDecoder;
import bgu.spl.net.impl.BGSTPC.BGSProtocol;

import bgu.spl.net.impl.newsfeed.NewsFeed;
import bgu.spl.net.srv.Server;

public class NewsFeedServerMain {

    public static void main(String[] args) {
        NewsFeed feed = new NewsFeed(); //one shared object

// you can use any server... 
//        Server.threadPerClient(
//                7777, //port
//                () -> new RemoteCommandInvocationProtocol<>(feed), //protocol factory
//                ObjectEncoderDecoder::new //message encoder decoder factory
//        ).serve();

   //     Server.reactor(
     //           Runtime.getRuntime().availableProcessors(),
       //         7777, //port
         //       () -> new BGSProtocol(), //protocol factory
      //          BGSEncoderDecoder::new //message encoder decoder factory
       // ).serve();

    }
}
