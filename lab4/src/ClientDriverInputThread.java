import java.io.IOException;
import java.util.Scanner;


public class ClientDriverInputThread extends Thread{

	String inputHashes;
	private Scanner in = new Scanner(System.in);


	@Override
	public void run() {
		while (true) {
			inputHashes = in.nextLine();

			try {
				ClientDriver.out.writeUTF(inputHashes);
				ClientDriver.out.flush();
			} catch (IOException e) {
				e.printStackTrace();
			}

		}
	}
}
