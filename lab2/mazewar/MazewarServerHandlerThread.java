import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;
import java.util.Iterator;
import java.util.LinkedList;

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
		Iterator<GUIClient> it ;
		serverClient temp = new serverClient();
		GUIClient tempgui;
		
		System.out.println("running");
		try {
			//toClient = new ObjectOutputStream(socket.getOutputStream());
			//fromClient = new ObjectInputStream(socket.getInputStream());

			EchoPacket packetFromClient;			

			while ((packetFromClient = (EchoPacket) fromClient.readObject()) != null) {
				System.out.println(packetFromClient.event);
				increment();				
				packetFromClient.packet_id = MazewarServer.packet_count;
				if(packetFromClient.event == EchoPacket.CONN){
					handleMsg(packetFromClient);
				}else{
					
					it = MazewarServer.client_list.iterator();
					
					//update client positions
					while(it.hasNext()){
						tempgui = it.next();
						if(tempgui.getName().equals(packetFromClient.player)){
							tempgui.update(packetFromClient);
							System.out.println(tempgui.getPoint().getX());
							break;
							
						}
					}
					
					MazewarServer.queue.put(packetFromClient);
				}
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

	private synchronized void handleMsg(EchoPacket packetFromClient) throws InterruptedException {
		
		LinkedList<serverClient> holder = new LinkedList<serverClient>();
		serverClient temp = new serverClient();
		GUIClient tempgui;
		Iterator<GUIClient> it ;//= MazewarServer.client_list.iterator();
		int i;
		
		System.out.println("Processing connection for " + packetFromClient.player);
		
		if(packetFromClient.event == EchoPacket.CONN){
			tempgui = new GUIClient(packetFromClient.player);
			MazewarServer.maze.addClient(tempgui);
			MazewarServer.client_list.add(tempgui);
			//client_list.getCliet(umar).forward.
		}
		
		it = MazewarServer.client_list.iterator();
		
		while(it.hasNext()){
			tempgui = it.next();
			temp = new serverClient();
			temp.name = tempgui.getName();
			temp.x = tempgui.getPoint().getX();
			temp.y = tempgui.getPoint().getY();
			holder.add(temp);
		}
		
		packetFromClient.type = EchoPacket.SERVER_LOC;
		packetFromClient.serverClients = holder;
		MazewarServer.queue.put(packetFromClient);	
	}
}