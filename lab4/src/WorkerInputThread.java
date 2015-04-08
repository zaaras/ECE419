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

				
				//if ((dictionary = fromServer.readUTF()) != null) {
				if ((dictionaryHolder = fromServer.readObject()) != null) {
					if(dictionaryHolder!=null){
					//System.out.println("got something");
					dictionary = (LinkedList<String>)dictionaryHolder;
					System.out.println(dictionary.size());
					System.out.println("Hash: " + parent.Hash);
					Iterator<String> iterator = dictionary.iterator();
					while(iterator.hasNext()){
						
						String pswd = iterator.next(), pswdHash;
						pswdHash = MD5Test.getHash(pswd);
						
						if(parent.Hash.equals(pswdHash)){
							System.out.println(pswd);
							break;
						}
						
						
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
