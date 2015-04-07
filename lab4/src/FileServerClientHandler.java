import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;


public class FileServerClientHandler extends Thread{
	
	private Socket socket = null;
	private ObjectInputStream fromClient = null;
	public static ObjectOutputStream toClient = null;
	private String strFromClient;

	public FileServerClientHandler(Socket accept) {
		// TODO Auto-generated constructor stub
		socket = accept;
	}

	@Override
	public void run() {
		System.out.println("Running new client handler");
		try {
			fromClient = new ObjectInputStream(socket.getInputStream());
			
			new FileServerClientOutput(socket).start();

			while (true) {

				if ((strFromClient = fromClient.readUTF()) != null) {
					FileServer.requestQue.add(strFromClient);
				}
				
			}
		} catch (Exception e) {
			// HACK!
			 e.printStackTrace();
		}
	}
}
