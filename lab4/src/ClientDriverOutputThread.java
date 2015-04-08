import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;

public class ClientDriverOutputThread extends Thread {

	String count = "0";
	volatile Socket soc;
	public ObjectInputStream in = null;
	String fromServer = null;
	public volatile boolean killMe = false;

	public ClientDriverOutputThread(Socket jobTrackerSoc, String currentCount) {
		count = currentCount;
		soc = jobTrackerSoc;
	}
	
	public void die(){
		try {
			in.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	@Override
	public void run() {
		try {
			in = new ObjectInputStream(soc.getInputStream());
			while (!killMe) {
				if ((fromServer =  in.readUTF()) != null) {
					
					System.out.println(fromServer);

				}
			}
		}catch (EOFException e) {
			die();
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		} 
	}

}
