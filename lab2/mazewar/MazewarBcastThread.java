import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;


public class MazewarBcastThread extends Thread {
	
	private LinkedBlockingQueue<EchoPacket> queue;
	private ArrayList<Socket> clients;
	
	public MazewarBcastThread(LinkedBlockingQueue<EchoPacket> q,  ArrayList<Socket> clients) {
		this.queue = q;
		this.clients = clients;
	}
	
	public void run(){
		ObjectOutputStream toClient;
		Socket client;
		for(int i = 0; i<clients.size();i++){
				client = clients.get(i);
				try {
					toClient = new ObjectOutputStream(client.getOutputStream());
					toClient.writeObject(queue.take());
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
		}
	}

}
