import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Scanner;

public class ClientDriverInputThread extends Thread {

	String inputHashes;
	String count = "0";
	Socket soc;
	private Scanner in = new Scanner(System.in);
	public ObjectOutputStream out = null;

	public ClientDriverInputThread(Socket jobTrackerSoc, String currentCount) {
		count = currentCount;
		soc = jobTrackerSoc;
	}

	@Override
	public void run() {
		try {
			out = new ObjectOutputStream(soc.getOutputStream());
			while (true) {
				inputHashes = in.nextLine();

				out.writeUTF(inputHashes + ";" + ClientDriver.clientName
						+ count);
				out.flush();

			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
