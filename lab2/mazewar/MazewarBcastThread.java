import java.io.ObjectOutputStream;
import java.util.Iterator;


public class MazewarBcastThread extends Thread {
	
	public void run(){
		while(true){			
			if(!MazewarServer.queue.isEmpty()){
				//System.out.println("queue not empty");
				//System.out.println(MazewarServer.queue.peek().message);
				broadcast();
			}else{
				//System.out.println("queue empty");
			}
			
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
	}
	
	public synchronized void broadcast(){
		
		Iterator<GUIClient> it ;
		serverClient temp = new serverClient();
		GUIClient tempgui;
		
		Connection con;
	    ObjectOutputStream toClient;	    
	    EchoPacket p;    
	    
	    p = MazewarServer.queue.poll();	    
	    
	    
		it = MazewarServer.client_list.iterator();
		
	    
	    if(p!=null){
		   // System.out.println(p.player + " " + p.event);
		    
		    
			//send updates to client side
			for(int i = 0; i< MazewarServer.clients.size();i++){
					con = MazewarServer.clients.get(i);
					toClient = con.toClient;
					try {
						toClient.writeObject(p);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}		
				
			}
			
			//update client positions on server side
			while(it.hasNext()){
				tempgui = it.next();
				if(tempgui.getName().equals(p.player)){
					tempgui.update(p);
					break;
					
				}
			}
	    }
	}

}