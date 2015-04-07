import java.io.ObjectOutputStream;
import java.net.Socket;

public class FileServerClientOutput extends Thread {

	ObjectOutputStream toClient;
	Socket soc;
	int start, end;

	public FileServerClientOutput(Socket socket) {
		soc = socket;
	}

	@Override
	public void run() {
		String item;
		String[] items;
		try {
			System.out.println("setting up output stream");

			toClient = new ObjectOutputStream(soc.getOutputStream());

			while (true) {
				if (!FileServer.outputQue.isEmpty()) {
					item = FileServer.outputQue.poll();
					System.out.println("Somthing on output que "
							+ FileServer.outputQue.peek());
					
					items = item.split(";");
					for (int i = 0; i < items.length; i++) {
						System.out.println(items[i]);
					}
					
					start = Integer.parseInt(items[1]);
					end = Integer.parseInt(items[2]);					

					Thread.sleep(100);
					toClient.writeUTF(FileServer.dictionary.subList(start, end).toString());
					toClient.flush();
					System.out.println("sending");

				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
