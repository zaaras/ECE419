import java.net.ServerSocket;


public class JobTrackerServer extends Thread {
	Integer Port;
	ServerSocket serverSocket;

	public JobTrackerServer(int i) {
		Port = i;
	}

	@Override
	public void run() {
		try {
			serverSocket = new ServerSocket(Port);
			System.out.println("Server Up waiting for connections");
			while(true){
				new JobTrackerClientHandler(serverSocket.accept()).start();
			}
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

}
