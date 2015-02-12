

public class ClientQueManager extends Thread{
	private MazewarClient myclient;
	

	public ClientQueManager(MazewarClient clientConnection) {
		myclient = clientConnection;
	}

	public void run() {
		while (true) {
			try {
				EchoPacket fromServer ;//= (EchoPacket) myclient.in.readObject();
				
				while((fromServer = (EchoPacket) myclient.in.readObject()) == null);
				
				/*if (fromServer == null) {
					System.out.println("killed");
				}*/
				
				Mazewar.que.add(fromServer);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.exit(1);
			} 
		}
	}
}
