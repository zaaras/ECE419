import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class MazewarClient {

	DatagramSocket echoSocket = null;
	ByteArrayOutputStream byteOutStream = null;
	ObjectOutput objOut = null;

	public MazewarClient(DatagramSocket dtSoc, String name) throws Exception {

		echoSocket = dtSoc;
		byteOutStream = new ByteArrayOutputStream();
		objOut = new ObjectOutputStream(byteOutStream);
		// SendInit(name);
		// SendMsg("Connected: " + name, name);

	}

	public void MultiCastPacket(EchoPacket pack) throws IOException {
		byte[] buf;
		DatagramPacket outPack;
		objOut.writeObject(pack);
		buf = byteOutStream.toByteArray();
		outPack = new DatagramPacket(buf, buf.length,
				InetAddress.getByName(Mazewar.IP), Mazewar.PORT);
		echoSocket.send(outPack);
	}

	public void SendEvent(int event, String name) throws IOException {
		EchoPacket packetToServer = new EchoPacket();
		packetToServer.type = EchoPacket.ECHO_REQUEST;
		packetToServer.player = name;
		packetToServer.event = event;
		MultiCastPacket(packetToServer);
	}

	public void SendInit(String name) throws IOException {
		EchoPacket packetToServer = new EchoPacket();
		packetToServer.type = EchoPacket.ECHO_NEW;
		packetToServer.player = name;
		packetToServer.event = EchoPacket.CONN;
		MultiCastPacket(packetToServer);
	}

	public void SendRequest(int packetid, String name) throws IOException {
		EchoPacket packetToServer = new EchoPacket();
		packetToServer.type = EchoPacket.ECHO_MISSING;
		packetToServer.player = name;
		packetToServer.packet_id = packetid;
		MultiCastPacket(packetToServer);
	}

	public void SendMsg(String msg, String name) throws IOException {
		EchoPacket packetToServer = new EchoPacket();
		packetToServer.type = EchoPacket.ECHO_REQUEST;
		packetToServer.player = name;
		packetToServer.message = msg;
		MultiCastPacket(packetToServer);
	}

	public void SendExit(String name) throws IOException {
		EchoPacket packetToServer = new EchoPacket();
		packetToServer.type = EchoPacket.ECHO_BYE;
		packetToServer.player = name;
		packetToServer.event = EchoPacket.ECHO_BYE;
		MultiCastPacket(packetToServer);
	}

}
