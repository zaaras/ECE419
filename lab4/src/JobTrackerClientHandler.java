import java.io.ObjectInputStream;
import java.net.Socket;

public class JobTrackerClientHandler extends Thread {

	private Socket socket = null;
	private ObjectInputStream fromClient = null;
	private String strFromClient;

	public JobTrackerClientHandler(Socket accept) {
		socket = accept;

	}

	@Override
	public void run() {
		System.out.println("Running new client handler");
		try {
			fromClient = new ObjectInputStream(socket.getInputStream());
			while (true) {

				if ((strFromClient = fromClient.readUTF()) != null) {
					JobTracker.hashQue.add(strFromClient);
				}

			}
		} catch (Exception e) {
			// HACK!
			 e.printStackTrace();
		}
	}

}
