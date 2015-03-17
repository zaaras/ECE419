import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

public class DiscoQueManager extends Thread {

	class CheckClients extends TimerTask {

		@Override
		public void run() {
			if (Mazewar.leader != null && Mazewar.localClient != null) {
					if (clientAliveMap != null) {
						

						Set<Entry<String, Integer>> clients = clientAliveMap
								.entrySet();
						Iterator<Entry<String, Integer>> iterator = clients
								.iterator();
						Entry<String, Integer> client;
						while (iterator.hasNext()) {
							client = iterator.next();
							if (client.getValue() == 0) {
								findAndKill(Mazewar.remoteClients,
										client.getKey());
							}
							clientAliveMap.put(client.getKey(), 0);
						}
					}
				//}
			}
		}

	}

	MulticastSocket socket = null;
	DatagramPacket inPacket = null;
	InetAddress address = null;
	ObjectInput objIn = null;
	ByteArrayInputStream byteInputStream;
	byte[] inBuf = new byte[Mazewar.PACKET_SIZE];
	public HashMap<String, Integer> clientAliveMap;// = new HashMap<String,
													// Integer>();
	CheckClients cc = new CheckClients();
	Timer time = new Timer();

	public DiscoQueManager() throws IOException {

		socket = new MulticastSocket(Mazewar.PORT);
		address = InetAddress.getByName(Mazewar.IP);
	}

	private boolean findAndKill(LinkedList<GUIClient> remoteClients,
			String player) {
		// TODO Auto-generated method stub
		Iterator<GUIClient> iterator = remoteClients.iterator();
		GUIClient client;
		while (iterator.hasNext()) {
			client = iterator.next();
			if (client.getName().equals(player)) {
				// Mazewar.maze.removeClient(client);

				// Mazewar.playerCount--;
				Mazewar.localClient.exitServer(client.getName());
				System.out.println(client.getName()
						+ " un-responsive and killed");
				return true;
			}
		}
		return false;
	}

	public void run() {
		time.schedule(cc, 0, 1500);
		clientAliveMap = new HashMap<String, Integer>();

		try {
			socket.joinGroup(address);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		while (true) {

			try {
				EchoPacket fromOthers;
				inPacket = new DatagramPacket(inBuf, inBuf.length);
				socket.receive(inPacket);
				byteInputStream = new ByteArrayInputStream(inBuf);
				objIn = new ObjectInputStream(byteInputStream);
				fromOthers = (EchoPacket) objIn.readObject();

				if (fromOthers.type == EchoPacket.DISCO) {
					if (Mazewar.discoQue.add(fromOthers))
						;

					if (clientAliveMap.get(fromOthers.player) != null) {
						clientAliveMap.put(fromOthers.player,
								clientAliveMap.get(fromOthers.player) + 1);
					} else {
						clientAliveMap.put(fromOthers.player, 1);
					}

				}

				// System.out.println(fromOthers.player + " says " +
				// fromOthers.message );
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.exit(1);
			}
		}
	}
}
