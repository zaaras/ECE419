import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Scanner;


public class ClientDriverInputThread extends Thread{


	volatile ObjectOutputStream toServer;
	String inputHashes;
	private Scanner in = new Scanner(System.in);
	
	public ClientDriverInputThread() {
		toServer = ClientDriver.out;
	}

	@Override
	public void run() {
		while (true) {
			inputHashes = in.nextLine();

			try {
				toServer.writeUTF(inputHashes);
				toServer.flush();
			} catch (IOException e) {
				e.printStackTrace();
			}

		}
	}
}
