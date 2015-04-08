import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.concurrent.LinkedBlockingQueue;

public class FileServerClientHandler extends Thread {

	private Socket socket = null;
	private ObjectOutputStream toClient = null;
	volatile public String clientName;
	private LinkedBlockingQueue<String> myQue = new LinkedBlockingQueue<String>();

	public FileServerClientHandler(Socket accept) {
		socket = accept;
	}

	public String getSocketName() {
		return clientName;
	}

	@Override
	public void run() {
		System.out.println("Running new client handler");
		try {

			toClient = new ObjectOutputStream(socket.getOutputStream());

			new FileServerClientInput(socket, this).start();

			while (true) {

				FileServer.outputQuelock.lock();
				// if(!FileServer.requestQues.containsKey(getSocketName()))
				// continue;

				// myQue = FileServer.requestQues.get(getSocketName());

				if (!FileServer.outputQue.isEmpty()) {
					if (getSocketName() == null)
						continue;
					
					/*if (!FileServer.outputQue.peek().split(";")[0]
							.equals(getSocketName()))
						continue;*/
					Iterator<String> iterator = null;

					iterator = FileServer.outputQue.iterator();
					while (iterator.hasNext()) {
						
						
						String item;
						String[] items;
						int start, end;

						//item = FileServer.outputQue.poll();
						item = iterator.next();

						items = item.split(";");
						if(!items[0].equals(getSocketName())) continue;
						iterator.remove();

						start = Integer.parseInt(items[1]);
						end = Integer.parseInt(items[2]);

						if (end >= FileServer.dictionarySize)
							end = FileServer.dictionarySize;

						Thread.sleep(100);
						toClient.writeObject(new LinkedList<String>(
								FileServer.dictionary.subList(start, end)));
						toClient.flush();
						System.out.println("Sent to " + getSocketName());
					}
					// }
				}
				FileServer.outputQuelock.unlock();

			}
		} catch (Exception e) {
			// HACK!
			e.printStackTrace();
		}
	}
}
