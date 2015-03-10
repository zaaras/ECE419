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
import java.util.Queue;

public class ClientQueManager extends Thread {

	MulticastSocket socket = null;
	DatagramPacket inPacket = null;
	InetAddress address = null;
	ObjectInput objIn = null;
	ByteArrayInputStream byteInputStream;
	byte[] inBuf = new byte[Mazewar.PACKET_SIZE];

	public static HashMap<String, Queue<EchoPacket>> remoteQues = new HashMap<String, Queue<EchoPacket>>();
	public static HashMap<String, Integer> remoteQueCounts = new HashMap<String, Integer>();
	public static int localCountQue = 0;

	public ClientQueManager() throws IOException {

		socket = new MulticastSocket(Mazewar.PORT);
		address = InetAddress.getByName(Mazewar.IP);
	}

	public void run() {
		try {
			socket.joinGroup(address);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		// = (EchoPacket) myclient.in.readObject();


		while (true) {

			try {
				EchoPacket fromOthers;
				inPacket = new DatagramPacket(inBuf, inBuf.length);
				socket.receive(inPacket);
				byteInputStream = new ByteArrayInputStream(inBuf);
				objIn = new ObjectInputStream(byteInputStream);
				fromOthers = (EchoPacket) objIn.readObject();

				if (Mazewar.que.add(fromOthers));
				
				if(fromOthers.type == EchoPacket.DISCO)
					continue;
				
				if (Mazewar.playerCount != 0) {

					if (Mazewar.remoteClients.contains(new GUIClient(
							fromOthers.player))
							|| Mazewar.localClient.getName().equals(
									fromOthers.player)) {

						if (remoteQueCounts.get(fromOthers.player) == -1) {
							remoteQueCounts.put(fromOthers.player,
									fromOthers.packet_id);
						} else {

							if (remoteQueCounts.get(fromOthers.player) + 1 == fromOthers.packet_id) {
								// This is the expected msg
								remoteQueCounts.put(fromOthers.player,
										fromOthers.packet_id);
							} else {
								// Missed a package
								requestMissingPacket(
										fromOthers.player,
										remoteQueCounts.get(fromOthers.player) + 1);
							}

							((Queue<EchoPacket>) remoteQues
									.get(fromOthers.player)).add(fromOthers);

						}
					}
				}

				

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.exit(1);
			}
		}
	}

	private void requestMissingPacket(String player, int i) {
		// TODO Auto-generated method stub
		System.out.println("missing packet " + i + " from " + player);

	}
}
