import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.LinkedBlockingQueue;

public class MazewarServer {

	public static int packet_count = 0;
	public static LinkedBlockingQueue<EchoPacket> queue = new LinkedBlockingQueue<EchoPacket>();
	public static ArrayList<Connection> clients = new ArrayList<Connection>();
	public static MazewarBcastThread bt;

	public static void main(String[] args) throws IOException {
		ServerSocket serverSocket = null;
		Socket client;
		Connection con;
		boolean listening = true;

		bt = new MazewarBcastThread();
		bt.start();

		try {
			if (args.length == 1) {
				serverSocket = new ServerSocket(Integer.parseInt(args[0]));
			} else {
				System.err.println("ERROR: Invalid arguments!");
				System.exit(-1);
			}
		} catch (IOException e) {
			System.err.println("ERROR: Could not listen on port!");
			System.exit(-1);
		}

		System.out.println("server started");

		while (listening) {
			con = new Connection();
			con.client = serverSocket.accept();
			con.toClient =  new ObjectOutputStream(con.client.getOutputStream());
			con.fromClient = new ObjectInputStream(con.client.getInputStream());
			
			clients.add(con);
			
			new MazewarServerHandlerThread(con).start();
			// new MazewarServerHandlerThread(queue).start();
		}

		serverSocket.close();
	}

}
