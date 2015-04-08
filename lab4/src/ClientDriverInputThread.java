import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Scanner;

public class ClientDriverInputThread extends Thread {

	String inputHashes;
	String count = "0";
	volatile Socket soc;

	public volatile ObjectOutputStream out = null;
	public volatile boolean killMe = false;

	

	public ClientDriverInputThread(Socket jobTrackerSoc, String currentCount) {
		count = currentCount;
		soc = jobTrackerSoc;
	}
	
	public void die(){
		try {
			out.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	@Override
	public void run() {
		try {
			out = new ObjectOutputStream(soc.getOutputStream());
			while (!killMe) {
				//inputHashes = ClientDriver.in.nextLine();

				if(!killMe && !ClientDriver.inputQue.isEmpty()){
					inputHashes = ClientDriver.inputQue.poll();
					out.writeUTF(inputHashes + ";" + ClientDriver.clientName
							+ count);
					out.flush();
				}

			}
			System.out.println("Got Killed!!!!!");
		}catch (EOFException e) {
			die();
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
