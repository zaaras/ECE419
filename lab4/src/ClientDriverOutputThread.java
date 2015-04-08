import java.io.ObjectInputStream;
import java.net.Socket;

public class ClientDriverOutputThread extends Thread {

	String count = "0";
	Socket soc;
	public ObjectInputStream in = null;
	String fromServer = null;

	public ClientDriverOutputThread(Socket jobTrackerSoc, String currentCount) {
		count = currentCount;
		soc = jobTrackerSoc;
	}

	@Override
	public void run() {
		try {
			in = new ObjectInputStream(soc.getInputStream());
			while (true) {
				if ((fromServer =  in.readUTF()) != null) {
					
					System.out.println(fromServer);

				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
