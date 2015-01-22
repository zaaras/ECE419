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
		
		SendMsg("Connected: " + name);

	}
	
	public void SendMsg(String msg) throws IOException{
		EchoPacket packetToServer = new EchoPacket();
		packetToServer.type = EchoPacket.ECHO_REQUEST;
		packetToServer.message = msg ;
		out.writeObject(packetToServer);
	}

}
