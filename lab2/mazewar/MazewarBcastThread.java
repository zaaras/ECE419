import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Iterator;


public class MazewarBcastThread extends Thread {
	
	public void run(){
		while(true){			
			if(!MazewarServer.queue.isEmpty()){
				System.out.println("queue not empty");
				broadcast();
			}else{
				System.out.println("queue empty");
			}
			
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
	}
	
	public void broadcast(){
	
		System.out.print("in broadcast");
		
		Connection con;
	    ObjectOutputStream toClient;	    
	    EchoPacket p;    
	    		
		for(int i = 0; i< MazewarServer.clients.size();i++){
				con = MazewarServer.clients.get(i);
				toClient = con.toClient;
				try {
					toClient.writeObject(MazewarServer.queue.take());
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