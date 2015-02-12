import java.io.ObjectOutputStream;
import java.util.Iterator;

public class MazewarBcastThread extends Thread {

	public void run() {
		while (true) {
			if (!MazewarServer.queue.isEmpty()) {
				broadcast();
			}
		}
	}

	public synchronized void broadcast() {

		Iterator<GUIClient> it;
		serverClient temp = new serverClient();
		GUIClient tempgui;

		Connection con;
		ObjectOutputStream toClient;
		EchoPacket p;

		p = MazewarServer.queue.poll();

		if (p != null) {

			// send updates to client side
			for (int i = 0; i < MazewarServer.clients.size(); i++) {
				con = MazewarServer.clients.get(i);
				toClient = con.toClient;
				try {
					toClient.writeObject(p);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					System.exit(1);
				}

			}
			
			it = MazewarServer.client_list.iterator();

			// update client positions on server side
			while (it.hasNext()) {
				tempgui = it.next();
				if (tempgui.getName().equals(p.player)) {
					tempgui.update(p);
					break;

				}
			}
		}
	}

}