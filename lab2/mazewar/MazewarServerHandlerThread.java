import java.net.Socket;


public class MazewarServerHandlerThread extends Thread {
	
	private Socket socket = null;

	public MazewarServerHandlerThread(Socket socket) {
		super("MazewarServerHandlerThread");
		this.socket = socket;
		System.out.println("Created new Thread to handle client");
	}

	public void run() {
		
	}
	
	
}