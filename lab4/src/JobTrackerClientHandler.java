import java.net.Socket;


public class JobTrackerClientHandler extends Thread{
	
	private Socket socket = null;

	public JobTrackerClientHandler(Socket accept) {
		socket = accept;
		System.out.println("Created new client handler");
	}
	
	@Override
	public void run() {
		
	}

}
