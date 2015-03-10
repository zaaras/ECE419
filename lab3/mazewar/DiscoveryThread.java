import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class DiscoveryThread extends Thread{

	DatagramSocket echoSocket = null;
	String localName;
	
	public DiscoveryThread(DatagramSocket dtSoc, String name) throws IOException {
		echoSocket = dtSoc;
		localName = name;

	}
	
	public void MultiCastPacket(EchoPacket pack) throws IOException {
		byte[] buf;
		ByteArrayOutputStream byteOutStream = new ByteArrayOutputStream();
		ObjectOutput objOut = null;
		objOut = new ObjectOutputStream(byteOutStream);
		DatagramPacket outPack;
		objOut.writeObject(pack);
		buf = byteOutStream.toByteArray();
		outPack = new DatagramPacket(buf, buf.length,
				InetAddress.getByName(Mazewar.IP), Mazewar.PORT);
		echoSocket.send(outPack);
	}

	public void run() {
		EchoPacket discoPack = new EchoPacket();
		discoPack.type = EchoPacket.DISCO;
		discoPack.player = localName;
		discoPack.message = "lets disco";

		while (true) {
			discoPack.x = Mazewar.localClient.getPoint().getX();
			discoPack.y = Mazewar.localClient.getPoint().getY();
			discoPack.dir = Mazewar.localClient.getOrientation();
			try {
				MultiCastPacket(discoPack);
				Thread.sleep(300);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
	}
}
