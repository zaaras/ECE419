import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.concurrent.LinkedBlockingQueue;

public class MazewarServer {

	private final static int mazeWidth = 20;
	private final static int mazeHeight = 10;
	private final static int mazeSeed = 42;

	public static int packet_count = 0;
	public static LinkedBlockingQueue<EchoPacket> queue = new LinkedBlockingQueue<EchoPacket>();
	public static ArrayList<Connection> clients = new ArrayList<Connection>();
	public static MazewarBcastThread bt;
	//public static MazewarTickerThread tt;
	public static int clientCount = 0;

	public static Maze maze = new MazeImpl(new Point(mazeWidth, mazeHeight),
			mazeSeed);
	
	public static LinkedList<GUIClient> client_list = new LinkedList<GUIClient>(); 

	public static void main(String[] args) throws IOException {
		ServerSocket serverSocket = null;
		Connection con;
		boolean listening = true;

		bt = new MazewarBcastThread();
		bt.start();

		//tt = new MazewarTickerThread();
		//tt.start();

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
			con.toClient = new ObjectOutputStream(con.client.getOutputStream());
			con.fromClient = new ObjectInputStream(con.client.getInputStream());

			clients.add(con);
			clientCount++;

			new MazewarServerHandlerThread(con).start();
		}

		serverSocket.close();
	}

}
