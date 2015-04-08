import java.io.ObjectInputStream;
import java.net.Socket;
import java.util.LinkedList;

public class WorkerInputThread extends Thread {

	Object dictionaryHolder = null;
	LinkedList<String> dictionary;
	Socket soc;
	ObjectInputStream fromServer;

	public WorkerInputThread(Socket fileServerSoc) {
		soc = fileServerSoc;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void run() {
		try {
			fromServer = new ObjectInputStream(soc.getInputStream());
			System.out.println("Input stream from server up");
			while (true) {

				
				//if ((dictionary = fromServer.readUTF()) != null) {
				if ((dictionaryHolder = fromServer.readObject()) != null) {
					if(dictionaryHolder!=null){
					//System.out.println("got something");
					dictionary = (LinkedList<String>)dictionaryHolder;
					System.out.println(dictionary.size());
					Worker.dataReceivedSignal.countDown();
					}
				}

			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
