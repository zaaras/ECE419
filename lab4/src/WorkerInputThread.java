import java.io.ObjectInputStream;
import java.net.Socket;
import java.util.Iterator;
import java.util.LinkedList;

public class WorkerInputThread extends Thread {

	Object dictionaryHolder = null;
	LinkedList<String> dictionary;
	Socket soc;
	ObjectInputStream fromServer;
	Worker parent;
	boolean found = false;
	

	public WorkerInputThread(Socket fileServerSoc, Worker worker) {
		soc = fileServerSoc;
		parent = worker;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void run() {
		try {
			fromServer = new ObjectInputStream(soc.getInputStream());
			System.out.println("Input stream from server up");
			while (true) {

				if ((dictionaryHolder = fromServer.readObject()) != null) {
					if (dictionaryHolder != null) {
						dictionary = (LinkedList<String>) dictionaryHolder;
						System.out.println(dictionary.size());
						System.out.println("Hash: " + parent.Hash);
						Iterator<String> iterator = dictionary.iterator();
						while (iterator.hasNext()) {

							String pswd = iterator.next(), pswdHash;
							pswdHash = MD5Test.getHash(pswd);

							if (parent.Hash.equals(pswdHash)) {
								System.out.println(pswd + " for " + parent.Name);
								parent.writeResults(parent.Name, pswd);
								found = true;
								break;
							}

						}
						if(!found){
							parent.writeResults(parent.Name, "No password found");
						}
					}
				}
				
				

			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
