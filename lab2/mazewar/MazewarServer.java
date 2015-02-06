import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import sun.misc.Queue;


public class MazewarServer {
	
    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = null;
        Socket client;
        boolean listening = true;
        
        LinkedBlockingQueue<EchoPacket> queue = new LinkedBlockingQueue<EchoPacket>();
        
       // BlockingQueue<EchoPacket> queue = new LinkedBlockingQueue<>();
        ArrayList<Socket> clients = new ArrayList<Socket>();
        
//        new MazewarBcastThread(queue,clients).start();

        try {
        	if(args.length == 1) {
        		serverSocket = new ServerSocket(Integer.parseInt(args[0]));
        	} else {
        		System.err.println("ERROR: Invalid arguments!");
        		System.exit(-1);
        	}
        } catch (IOException e) {
            System.err.println("ERROR: Could not listen on port!");
            System.exit(-1);
        }

        System.out.println("server started");
        
        while (listening) {
        	//client = serverSocket.accept();
        //	clients.add(client);
        	new MazewarServerHandlerThread(serverSocket.accept(), queue).start();
        	//new MazewarServerHandlerThread(queue).start();
        }

        serverSocket.close();
    }

}
