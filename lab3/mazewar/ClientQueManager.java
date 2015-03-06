import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

public class ClientQueManager extends Thread{

	MulticastSocket socket = null;
	DatagramPacket inPacket = null;
	InetAddress address = null;
	ObjectInput objIn = null;
	ByteArrayInputStream byteInputStream;
	byte[] inBuf = new byte[Mazewar.PACKET_SIZE];
	

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
		EchoPacket fromOthers ;//= (EchoPacket) myclient.in.readObject();
		while (true) {
			try {
				inPacket = new DatagramPacket(inBuf, inBuf.length);
				socket.receive(inPacket);
				byteInputStream =  new ByteArrayInputStream(inBuf);
				objIn = new ObjectInputStream(byteInputStream);
				fromOthers = (EchoPacket) objIn.readObject();
				
				System.out.println("received: " + fromOthers.message +" Name: "+ fromOthers.player);
				
				Mazewar.que.add(fromOthers);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.exit(1);
			} 
		}
	}
}
