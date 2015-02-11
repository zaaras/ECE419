import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;

public class MazewarClient {

	Socket echoSocket = null;
	ObjectOutputStream out = null;
	ObjectInputStream in = null;
	
	public MazewarClient(String[] args, String name) throws IOException,
			ClassNotFoundException {
		
		try {
			/* variables for hostname/port */
			String hostname = "localhost";
			int port = 4444;

			if (args.length == 2) {
				hostname = args[0];
				port = Integer.parseInt(args[1]);
			} else {
				System.err.println("ERROR: Invalid arguments!");
				System.exit(-1);
			}
			echoSocket = new Socket(hostname, port);

			out = new ObjectOutputStream(echoSocket.getOutputStream());
			in = new ObjectInputStream(echoSocket.getInputStream());

		} catch (UnknownHostException e) {
			System.err.println("ERROR: Don't know where to connect!!");
			System.exit(1);
		} catch (IOException e) {
			System.err.println("ERROR: Couldn't get I/O for the connection.");
			System.exit(1);
			
		}
		//SendInit(name);
		//SendMsg("Connected: " + name, name);

	}

	public void SendEvent(int event, String name) throws IOException{
		EchoPacket packetToServer = new EchoPacket();
		packetToServer.type = EchoPacket.ECHO_REQUEST;
		packetToServer.player = name;
		packetToServer.event = event ;
		out.writeObject(packetToServer);
	}
	
	public void SendInit(String name) throws IOException{
		EchoPacket packetToServer = new EchoPacket();
		packetToServer.type = EchoPacket.ECHO_NEW;
		packetToServer.player = name;
		packetToServer.event = EchoPacket.CONN;
		out.writeObject(packetToServer);
	}
	
	public void SendRequest(int packetid, String name) throws IOException{
		EchoPacket packetToServer = new EchoPacket();
		packetToServer.type = EchoPacket.ECHO_MISSING;
		packetToServer.player = name;
		packetToServer.packet_id = packetid ;
		out.writeObject(packetToServer);
	}
	
	public void SendMsg(String msg, String name) throws IOException{
		EchoPacket packetToServer = new EchoPacket();
		packetToServer.type = EchoPacket.ECHO_REQUEST;
		packetToServer.player = name;
		packetToServer.message = msg ;
		out.writeObject(packetToServer);
	}

}
