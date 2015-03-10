public class MazewarTickerThread extends Thread {
	
	public void run(){
		while (true){
			if(Mazewar.leader.equals(Mazewar.localClient.getName())){
			
			//MazewarServer.increment();			
			//EchoPacket p = new EchoPacket();		
			//p.event = EchoPacket.TICK;
			//p.packet_id = MazewarServer.packet_count;
			
			
			/*try {
				MazewarServer.queue.put(p);
				MazewarServer.maze.missleTick();
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}*/
			
			Mazewar.localClient.sendTick();
						
			try {
				Thread.sleep(200);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
	}
		
	}
}
