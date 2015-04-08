import java.io.ObjectInputStream;
import java.net.Socket;

public class FileServerClientInput extends Thread {

	ObjectInputStream fromClient;
	Socket soc;
	int start, end;
	FileServerClientHandler parent;
	


	public FileServerClientInput(Socket socket,
			FileServerClientHandler fileServerClientHandler) {
		soc = socket;
		parent = fileServerClientHandler;
	}

	@Override
	public void run() {
		String strFromClient;
		try {
			System.out.println("setting up input stream");

			fromClient = new ObjectInputStream(soc.getInputStream());

			while (true) {
				if ((strFromClient = fromClient.readUTF()) != null) {
					parent.clientName = strFromClient.split(";")[0];
					FileServer.requestQue.add(strFromClient);

				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
