import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class JobTrackerClientHandler extends Thread {

	private Socket socket = null;
	private ObjectInputStream fromClient = null;
	private ObjectOutputStream toClient = null;
	private String strFromClient;
	
	public JobTrackerClientHandler(Socket accept) {
		socket = accept;

	}

	public void SendData(String str){
		try {
			toClient.writeUTF(str);
			toClient.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Override
	public void run() {
		System.out.println("Running new client handler");
		try {
			fromClient = new ObjectInputStream(socket.getInputStream());
			toClient = new ObjectOutputStream(socket.getOutputStream());
			while (true) {

				if ((strFromClient = fromClient.readUTF()) != null) {
					
					String name = strFromClient.split(";")[1];
					
					JobTracker.clientsLock.lock();
					JobTracker.hashQue.add(strFromClient);
						
					JobTracker.clients.put(name,this);
					JobTracker.clientsLock.unlock();
					
				}

			}
		} catch (Exception e) {
			// HACK!
			 e.printStackTrace();
		}
	}

}
