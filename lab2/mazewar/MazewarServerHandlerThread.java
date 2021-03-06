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
		System.out.println("New client");
	}

	public void run() {
		
		System.out.println("running");
		try {
			//toClient = new ObjectOutputStream(socket.getOutputStream());
			//fromClient = new ObjectInputStream(socket.getInputStream());

			EchoPacket packetFromClient;			

			while ((packetFromClient = (EchoPacket) fromClient.readObject()) != null) {
				MazewarServer.increment();			
				packetFromClient.packet_id = MazewarServer.packet_count;
				if(packetFromClient.event == EchoPacket.CONN){
					handleMsg(packetFromClient);
				}else if(packetFromClient.event == EchoPacket.ECHO_BYE){
					MazewarServer.queue.put(packetFromClient);
					break;
				}
				else{
					MazewarServer.queue.put(packetFromClient);
				}
			}

			removeClient(socket,packetFromClient.player);
			fromClient.close();
			socket.close();
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 

	}

	private void removeClient(Socket socket, String name) {
		System.out.println("Removing client " + name);
		Connection con;
		//send updates to client side
		for(int i = 0; i< MazewarServer.clients.size();i++){
				con = MazewarServer.clients.get(i);
				if(con.client.equals(socket)){
					MazewarServer.clients.remove(i);
				}
		}
		
		for(int i = 0; i < MazewarServer.client_list.size();i++){
			if(MazewarServer.client_list.get(i).getName().equals(name)){
				MazewarServer.negativeIncrement();
				MazewarServer.maze.removeClient(MazewarServer.client_list.get(i));
				MazewarServer.client_list.remove(i);

			}
		}
		
	}

	private synchronized void handleMsg(EchoPacket packetFromClient) throws InterruptedException {
		
		LinkedList<serverClient> holder = new LinkedList<serverClient>();
		serverClient temp = new serverClient();
		GUIClient tempgui;
		Iterator<GUIClient> it ;//= MazewarServer.client_list.iterator();
		 
		packetFromClient.dir = Direction.North;
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
			temp.dir = tempgui.getOrientation();
			//packetFromClient.dir = temp.dir;
			System.out.println("Dir of client: " + temp.dir);
			holder.add(temp);
		}
		

		packetFromClient.type = EchoPacket.SERVER_LOC;
		packetFromClient.serverClients = holder;
		MazewarServer.queue.put(packetFromClient);	
	}
}