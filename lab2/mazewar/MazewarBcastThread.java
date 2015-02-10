import java.io.ObjectOutputStream;


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
	
	public void broadcast(){

		
		Connection con;
	    ObjectOutputStream toClient;	    
	    EchoPacket p;    
	    
	    p = MazewarServer.queue.poll();	    
	    
	    //System.out.println(MazewarServer.queue.toString());
	    
	    if(p!=null){
		    System.out.println(p.player + " " + p.event);
		    
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
	    }
	}

}