import java.net.ServerSocket;


public class FileServerServer extends Thread{
	ServerSocket serverSocket;
	

	@Override
	public void run() {
		try {
			serverSocket = new ServerSocket(FileServer.localPort);
			System.out.println("Server Up waiting for connections");
			while(true){
				new FileServerClientHandler(serverSocket.accept()).start();
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}

}
