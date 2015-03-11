import java.util.Iterator;

public class LeaderElection extends Thread {

	public void run() {
		Iterator<GUIClient> clientIt;
		GUIClient client;
		String minName, clientName;
		minName = Mazewar.localClient.getName();
		Mazewar.leader = minName;

		while (true) {

			if (Mazewar.remoteClients.isEmpty()) {
				minName = Mazewar.localClient.getName();
				Mazewar.leader = minName;
			} else if (!(Mazewar.remoteClients.contains(new GUIClient(
					Mazewar.leader)))) {
				if(Mazewar.localClient.getName().equals(Mazewar.leader)){
				;	
				}else{
					minName = Mazewar.localClient.getName();
					Mazewar.leader = minName;
				}
			}

			clientIt = Mazewar.remoteClients.iterator();
			while (clientIt.hasNext()) {

				client = clientIt.next();
				clientName = client.getName();
				if (clientName.compareTo(minName) <= 0) {
					minName = clientName;
					Mazewar.leader = minName;

				}

			}
			 System.out.println("My leader: " + Mazewar.leader);
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}
}
