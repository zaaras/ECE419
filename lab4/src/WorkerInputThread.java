import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;

public class WorkerInputThread extends Thread {

	String dictionary = null;
	Socket soc;
	ObjectInputStream fromServer;

	public WorkerInputThread(Socket fileServerSoc) {
		soc = fileServerSoc;
	}

	@Override
	public void run() {
		try {
			fromServer = new ObjectInputStream(soc.getInputStream());
			System.out.println("Input stream from server up");
			while (true) {

				if ((dictionary = fromServer.readUTF()) != null) {
					System.out.println(dictionary);
				}

			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
