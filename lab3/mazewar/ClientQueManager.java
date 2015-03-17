import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.PriorityQueue;

public class ClientQueManager extends Thread {

	MulticastSocket socket = null;
	DatagramPacket inPacket = null;
	InetAddress address = null;
	ObjectInput objIn = null;
	ByteArrayInputStream byteInputStream;
	byte[] inBuf = new byte[Mazewar.PACKET_SIZE];

	public static HashMap<String, PriorityQueue<EchoPacket>> remoteQues = new HashMap<String, PriorityQueue<EchoPacket>>();
	public static HashMap<String, Integer> remoteQueCounts = new HashMap<String, Integer>();
	public static int localCountQue = 0;

	public static LinkedList<missingPacket> missingPacks = new LinkedList<missingPacket>();

	int once = 1;

	private class missingPacket {

		public missingPacket(final int index, final String from) {
			missingPack = index;
			missingFrom = from;
		}

		int missingPack;
		String missingFrom;
	}

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

				/*if (fromOthers.packet_id%50 == 0 && once == 1
						&& fromOthers.player.equals("b")) {
					System.out.println("Packet DROP");
					once = 1;
					// fromOthers.packet_id = 10;
					continue;
				}*/

				if (fromOthers.type == EchoPacket.DISCO)
					continue;

				//System.out.println("Type: " + fromOthers.toString());

				if (fromOthers.type == EchoPacket.REQUEST_MISSING
						&& fromOthers.missingPackOwner
								.equals(Mazewar.localClient.getName())) {
					// System.out.println("Looking for pack: "
					// + fromOthers.missingIndex);
					EchoPacket lookedup = lookupIndex(
							MazewarClient.sentPackets, fromOthers.missingIndex);
					if (lookedup == null) {
						System.out.println("Look up failed");
						System.exit(1);
					} else {
						// System.out.println("Look up success");
					}
					lookedup.missingPackOwner = fromOthers.player;
					Mazewar.localClient.SendResponsePack(lookedup);
					continue;
				}

				// if (fromOthers.type == EchoPacket.RESPONSE_MISSING) {
				if (fromOthers.response == 1
						&& fromOthers.missingPackOwner
								.equals(Mazewar.localClient.getName())) {

					// System.out.println("response: " + fromOthers.packet_id);

					lookupRemove(fromOthers.packet_id);
					PriorityQueue<EchoPacket> tempQue = remoteQues
							.get(fromOthers.player);
					tempQue.add(fromOthers);
					remoteQues.put(fromOthers.player, tempQue);
					remoteQueCounts
							.put(fromOthers.player, fromOthers.packet_id);
					continue;
				} else if (fromOthers.response == 1) {
					continue;
				}

				if (fromOthers.event == EchoPacket.FREEZE
						|| fromOthers.event == EchoPacket.UNFREEZE) {
					if (Mazewar.que.add(fromOthers))
						;
					continue; // We added this <-------------------------------
				}

				if (Mazewar.playerCount != 0) {

					if (Mazewar.alreadyConnected(Mazewar.remoteClients,
							fromOthers.player)
							|| Mazewar.localClient.getName().equals(
									fromOthers.player)) {

						if (remoteQueCounts.get(fromOthers.player) == -1) {
							remoteQueCounts.put(fromOthers.player,
									fromOthers.packet_id);
							System.out.println("init " + fromOthers.packet_id
									+ " local " + fromOthers.player);
						} // else {

						PriorityQueue<EchoPacket> tempQue = remoteQues
								.get(fromOthers.player);
						tempQue.add(fromOthers);
						remoteQues.put(fromOthers.player, tempQue);

						// System.out.println(tempQue.toString());

						// if (remoteQueCounts.get(fromOthers.player) + 1 ==
						// fromOthers.packet_id) {
						int pack = checkQue(fromOthers.player,
								remoteQueCounts.get(fromOthers.player));
						if (pack == -1) {
							// This is the expected msg

						} else if (!inMissingList(new missingPacket(pack,
								fromOthers.player))) {
							// Missed a package

							System.out.println("Missing pack from "
									+ fromOthers.player + " " + pack + " "
									+ Mazewar.localClient.getName());

							missingPacks.add(new missingPacket(pack,
									fromOthers.player));

							Iterator<missingPacket> missingIterator = missingPacks
									.iterator();
							missingPacket missing;
							while (missingIterator.hasNext()) {
								
									if(missingPacks.size() == 0)
										break;
									missing = missingIterator.next();
									requestMissingPacket(missing.missingFrom,
											missing.missingPack);
								
								{
									while (true) {
										if(missing.missingFrom.equals(Mazewar.localClient.getName())){
											break;
										}
										
										inPacket = new DatagramPacket(inBuf,
												inBuf.length);
										socket.receive(inPacket);
										byteInputStream = new ByteArrayInputStream(
												inBuf);
										objIn = new ObjectInputStream(
												byteInputStream);
										fromOthers = (EchoPacket) objIn
												.readObject();
										
										//System.out.println("Type: " + fromOthers.toString());

										if (fromOthers.response == 1
												&& fromOthers.missingPackOwner
														.equals(Mazewar.localClient
																.getName())) {

											// System.out.println("response: " +
											// fromOthers.packet_id);

											lookupRemove(fromOthers.packet_id);
											PriorityQueue<EchoPacket> tempQueBla = remoteQues
													.get(fromOthers.player);
											tempQueBla.add(fromOthers);
											remoteQues.put(fromOthers.player,
													tempQueBla);
											remoteQueCounts.put(
													fromOthers.player,
													fromOthers.packet_id);
											break;
										} else if (fromOthers.response == 1) {
											continue;
										}
									}

								}
								}


						}

						// remoteQueCounts.put(fromOthers.player,
						// fromOthers.packet_id);

						Collection<Entry<String, PriorityQueue<EchoPacket>>> Pques = remoteQues
								.entrySet();
						Iterator<Entry<String, PriorityQueue<EchoPacket>>> queIterator = Pques
								.iterator();
						Entry<String, PriorityQueue<EchoPacket>> que;
						EchoPacket packet;

						while (queIterator.hasNext()) {
							que = queIterator.next();

							if (missingPacks.isEmpty()) {

								while (que.getValue().peek() != null) {
									packet = que.getValue().poll();

									remoteQueCounts.put(que.getKey(),
											packet.packet_id + 1);

									if (Mazewar.que.add(packet))
										;
								}
							}

						}
						// }
					}
				}

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.exit(1);
			}
		}
	}

	private boolean inMissingList(missingPacket pack) {
		Iterator<missingPacket> iterator = missingPacks.iterator();
		missingPacket item;

		while (iterator.hasNext()) {
			item = iterator.next();

			if (pack.missingFrom.equals(item.missingFrom)
					&& (pack.missingPack == item.missingPack)) {
				return true;
			}
		}

		return false;

	}

	private synchronized int checkQue(String player, int startPoint) {
		// TODO Auto-generated method stub
		PriorityQueue<EchoPacket> searchq = remoteQues.get(player);
		Iterator<EchoPacket> Packiterator = searchq.iterator();
		int count = startPoint;
		EchoPacket pack;
		// System.out.println(searchq.toString());
		while (Packiterator.hasNext()) {
			pack = Packiterator.next();
			if (pack.packet_id == count) {
				count++;
			} else {
				return count;
			}
		}

		return -1;
	}

	private void lookupRemove(int packet_id) {
		Iterator<missingPacket> it = missingPacks.iterator();
		while (it.hasNext()) {
			missingPacket tmp = it.next();
			if (tmp.missingPack == packet_id) {

				it.remove();
				// System.out.println("REMOVED " + missingPacks.size());
			}
		}
	}

	private EchoPacket lookupIndex(LinkedList<EchoPacket> sentPackets,
			int missingIndex) {

		Iterator<EchoPacket> it = sentPackets.iterator();
		EchoPacket tmp, ret;

		while (it.hasNext()) {

			tmp = it.next();

			if (tmp.packet_id == missingIndex) {
				ret = new EchoPacket(tmp);
				return ret;
			}

		}
		return null;

	}

	private void requestMissingPacket(String player, int i) {
		// TODO Auto-generated method stub
		// System.out.println("missing packet " + i + " from " + player);
		Mazewar.localClient.requestMissingPack(player, i);

	}
}
