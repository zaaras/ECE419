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
import java.util.Queue;

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

	public static LinkedList<missingPacket> missingPacks = new LinkedList<missingPacket> ();
	
	private class missingPacket{
		
		public missingPacket(final int index, final String from){
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

				System.out.println("Type: " + fromOthers.message);
				

				if(fromOthers.type == EchoPacket.REQUEST_MISSING && fromOthers.missingPackOwner.equals(Mazewar.localClient.getName())){
					System.out.println("here");
					EchoPacket lookedup = lookupIndex(MazewarClient.sentPackets, fromOthers.missingIndex);
					if(lookedup == null){
						System.out.println("Look up failed");
						System.exit(1);
					}else{
						System.out.println("Look up success");
					}
					Mazewar.localClient.SendResponsePack(lookedup);
				}
				
				if(fromOthers.type == EchoPacket.RESPONSE_MISSING){
					System.out.println("reponse missing "+ fromOthers.player);
					lookupRemove(fromOthers.packet_id);
					PriorityQueue<EchoPacket> tempQue = remoteQues.get(fromOthers.player);
					tempQue.add(fromOthers);
					remoteQues.put(fromOthers.player, tempQue);
				}
				
				if(fromOthers.event == EchoPacket.FREEZE || fromOthers.event == EchoPacket.UNFREEZE )
					if (Mazewar.que.add(fromOthers));
				
				if(fromOthers.type == EchoPacket.DISCO)
					continue;
				
				if (Mazewar.playerCount != 0) {

					
					if (Mazewar.alreadyConnected(Mazewar.remoteClients, fromOthers.player)
							|| Mazewar.localClient.getName().equals(
									fromOthers.player)) {

						if (remoteQueCounts.get(fromOthers.player) == -1) {
							remoteQueCounts.put(fromOthers.player,
									fromOthers.packet_id);
						} else {

							//if (remoteQueCounts.get(fromOthers.player) + 1 == fromOthers.packet_id) {
							int pack = checkQue(fromOthers.player, remoteQueCounts.get(fromOthers.player));
							if (pack==-1) {								
								// This is the expected msg
								;
							} else {
								// Missed a package
								System.out.println("Missing pack from " + fromOthers.player + " " + pack );
								//missingPacks.add(new missingPacket(remoteQueCounts.get(fromOthers.player) + 1, fromOthers.player));
								missingPacks.add(new missingPacket(pack, fromOthers.player));
								
								Iterator<missingPacket> missingIterator = missingPacks.iterator();
								missingPacket missing;
								while(missingIterator.hasNext()){
									missing = missingIterator.next();
									requestMissingPacket(missing.missingFrom,missing.missingPack);
								}
								
							}

							//remoteQueCounts.put(fromOthers.player, fromOthers.packet_id);
							PriorityQueue<EchoPacket> tempQue = remoteQues.get(fromOthers.player);
							tempQue.add(fromOthers);
							remoteQues.put(fromOthers.player, tempQue);
							
							Collection<Entry<String, PriorityQueue<EchoPacket>>> Pques = remoteQues.entrySet();
							Iterator<Entry<String, PriorityQueue<EchoPacket>>> queIterator = Pques.iterator();
							Entry<String, PriorityQueue<EchoPacket>> que;
							EchoPacket packet;
							
							while(queIterator.hasNext()){
								que = queIterator.next();
							
								if(missingPacks.isEmpty()){
									System.out.println("missing packs in empty");
									if(que.getValue().peek()!=null){
										packet = que.getValue().poll();
										if (Mazewar.que.add(packet));
									}
								}
								
							}
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

	private synchronized int checkQue(String player, int startPoint) {
		// TODO Auto-generated method stub
		PriorityQueue<EchoPacket> searchq = remoteQues.get(player);
		Iterator<EchoPacket> Packiterator = searchq.iterator();
		int count = startPoint;
		EchoPacket pack;
		while(Packiterator.hasNext()){
			pack = Packiterator.next();
			if(pack.packet_id == count){
				count++;
			}else{
				return count;
			}
		}
		
		return -1;
	}

	private void lookupRemove(int packet_id) {
		Iterator<missingPacket> it = missingPacks.iterator();
		while(it.hasNext()){
			missingPacket tmp = it.next();
			if(tmp.missingPack == packet_id){
				it.remove();
				
			}
		}
	}
	private EchoPacket lookupIndex(LinkedList<EchoPacket> sentPackets,
			int missingIndex) {
		
		Iterator<EchoPacket> it = sentPackets.iterator();
		EchoPacket tmp, ret;
		
		
		while(it.hasNext()){
			
			tmp = it.next();
			
			if(tmp.packet_id == missingIndex){
				System.out.println("from lookup pack found");
				ret = new EchoPacket(tmp);
				return ret;
			}
			
			
		}
		return null;
		
	}

	private void requestMissingPacket(String player, int i) {
		// TODO Auto-generated method stub
		//System.out.println("missing packet " + i + " from " + player);
		Mazewar.localClient.sendMissingPack(player,i);

	}
}
