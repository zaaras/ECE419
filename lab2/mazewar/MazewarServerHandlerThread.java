import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;


public class MazewarServerHandlerThread extends Thread {
	
	private Socket socket = null;
	private BlockingQueue<EchoPacket> queue;

	public MazewarServerHandlerThread(Socket socket, BlockingQueue<EchoPacket> q) {
		super("MazewarServerHandlerThread");
		this.socket = socket;
		this.queue = q;
		System.out.println("Created new Thread to handle client");
	}

	public void run() {
		System.out.println("running");
			try {
				ObjectInputStream fromClient = new ObjectInputStream(socket.getInputStream());
				EchoPacket packetFromClient;
				
				while (( packetFromClient = (EchoPacket) fromClient.readObject()) != null) {
					System.out.println(packetFromClient.event);
					//queue.put(packetFromClient);					
				}
				
				fromClient.close();
				socket.close();				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} /*catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}		*/
			
			
	}	
}