public class MazewarTickerThread extends Thread {
	
	public void run(){
		while(true){
			EchoPacket p = new EchoPacket();		
			p.event = EchoPacket.TICK;
			
			try {
				MazewarServer.queue.put(p);
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
						
			try {
				Thread.sleep(200);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		
	}

}