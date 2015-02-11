import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;

public class MazewarServerHandlerThread extends Thread {

	private Socket socket = null;
	private ObjectInputStream fromClient;

	public MazewarServerHandlerThread(Connection con) {
		super("MazewarServerHandlerThread");
		this.socket = con.client;
		this.fromClient = con.fromClient;
		System.out.println("Created new Thread to handle client");
	}

	public synchronized void increment() {
		MazewarServer.packet_count++;
	}

	public void run() {
		System.out.println("running");
		try {
			//toClient = new ObjectOutputStream(socket.getOutputStream());
			//fromClient = new ObjectInputStream(socket.getInputStream());

			EchoPacket packetFromClient;			

			while ((packetFromClient = (EchoPacket) fromClient.readObject()) != null) {
				System.out.println(packetFromClient.event);
				increment();
				if(packetFromClient.event == EchoPacket.CONN){
					MazewarServer.client_list.add(new GUIClient(packetFromClient.player));
					MazewarServer.maze.addClient(MazewarServer.client_list.getLast());
					// Send to client his location
					packetFromClient.x = MazewarServer.maze.getClientPoint(MazewarServer.client_list.getLast()).getX();
					packetFromClient.y = MazewarServer.maze.getClientPoint(MazewarServer.client_list.getLast()).getY();
					packetFromClient.type = EchoPacket.SERVER_LOC;
					System.out.println("sending loc");
				}
				
				packetFromClient.packet_id = MazewarServer.packet_count;
				MazewarServer.queue.put(packetFromClient);
			}


			fromClient.close();
			socket.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} /*
		 * catch (InterruptedException e) { // TODO Auto-generated catch block
		 * e.printStackTrace(); }
		 */catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}