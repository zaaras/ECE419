import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.IOException;

/*
 Copyright (C) 2004 Geoffrey Alan Washburn

 This program is free software; you can redistribute it and/or
 modify it under the terms of the GNU General Public License
 as published by the Free Software Foundation; either version 2
 of the License, or (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307,
 USA.
 */

/**
 * A skeleton for those {@link Client}s that correspond to clients on other
 * computers.
 * 
 * @author Geoffrey Washburn &lt;<a
 *         href="mailto:geoffw@cis.upenn.edu">geoffw@cis.upenn.edu</a>&gt;
 * @version $Id: RemoteClient.java 342 2004-01-23 21:35:52Z geoffw $
 */

public class RemoteClient extends Client implements KeyListener {

	/**
	 * Create a remotely controlled {@link Client}.
	 * 
	 * @param name
	 *            Name of this {@link RemoteClient}.
	 */

	private MazewarClient socket;
	private String name;


	public RemoteClient(String nameIn, MazewarClient conn) {
		super(nameIn);
		socket = conn;
		name = nameIn;
		
	}

	public void keyPressed(KeyEvent e) {
		// If the user pressed Q, invoke the cleanup code and quit.
		if ((e.getKeyChar() == 'q') || (e.getKeyChar() == 'Q')) {
			Mazewar.quit();
			// Up-arrow moves forward.
		} else if (e.getKeyCode() == KeyEvent.VK_UP) {
			toServer(EchoPacket.UP);
			// Down-arrow moves backward.
		} else if (e.getKeyCode() == KeyEvent.VK_DOWN) {
			toServer(EchoPacket.DOWN);
			// Left-arrow turns left.
		} else if (e.getKeyCode() == KeyEvent.VK_LEFT) {
			toServer(EchoPacket.LEFT);
			// Right-arrow turns right.
		} else if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
			toServer(EchoPacket.RIGHT);
			// Spacebar fires.
		} else if (e.getKeyCode() == KeyEvent.VK_SPACE) {
			toServer(EchoPacket.FIRE);
		}
	}

	public void keyReleased(KeyEvent arg0) {

	}

	public void keyTyped(KeyEvent arg0) {

	}
	
	void sendFreeze() {
		try {
			socket.SendFreeze(name);
		} catch (IOException e1) {
			System.err.println("Sending event to server failed.");
			e1.printStackTrace();
		}
	}
	
	
	void sendUnFreeze(String string, Point point, Direction direction) {
		try {
			socket.SendUnFreeze(name, point, direction);
		} catch (IOException e1) {
			System.err.println("Sending event to server failed.");
			e1.printStackTrace();
		}
	}
	
	public void sendKill(EchoPacket killPack) {
		try {
			socket.SendKill(name,killPack);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	void initServer() {
		try {
			socket.SendInit(name);
		} catch (IOException e1) {
			System.err.println("Sending event to server failed.");
			e1.printStackTrace();
		}
	}

	void exitServer() {
		try {
			socket.SendExit(name);
		} catch (IOException e1) {
			System.err.println("Sending event to server failed.");
			e1.printStackTrace();
		}
	}
	
	void toServer(int msg) {
		try {
			socket.SendEvent(msg, name);
		} catch (IOException e1) {
			System.err.println("Sending event to server failed.");
			e1.printStackTrace();
		}
	}
	
	void toServerStr(String msg) {
		try {
			socket.SendMsg(msg, name);
		} catch (IOException e1) {
			System.err.println("Sending event to server failed.");
			e1.printStackTrace();
		}
	}
	
	public void sendTick() {
		try {
			socket.SendTick(name);
		} catch (IOException e1) {
			System.err.println("Sending event to server failed.");
			e1.printStackTrace();
		}
	}

	public void update(EchoPacket message) {
		
		//LinkedList<Integer> missingPackets ;
		//Iterator<Integer> packetid;
		
		
		if(message.player.contains(name)){
			//if (false) {
			//	Mazewar.quit();
			//	// Up-arrow moves forward.
			//} else 
			if (message.event == EchoPacket.UP) {
				forward();
				// Down-arrow moves backward.
			} else if (message.event == EchoPacket.DOWN) {
				backup();
				// Left-arrow turns left.
			} else if (message.event == EchoPacket.LEFT) {
				turnLeft();
				// Right-arrow turns right.
			} else if (message.event == EchoPacket.RIGHT) {
				turnRight();
				// Spacebar fires.
			} else if (message.event == EchoPacket.FIRE) {
				fire();
			}
		}
		
		/*missingPackets = validateQue();
		
		if(missingPackets.size()!=0){
			// some packets dropped, request from server
			packetid = missingPackets.iterator();
			while(packetid.hasNext()){
				System.out.println("missing packets");
				//requestPacket(packetid.next());
			}
		}else{
			/*
		}*/
	}

	public void requestMissingPack(String from, int index) {
		try {
			socket.RequestMissingPack(name, from, index);
		} catch (IOException e1) {
			System.err.println("Sending event to server failed.");
			e1.printStackTrace();
		}
		
	}

	public void SendResponsePack(EchoPacket pack) {
		try {
			socket.SendResponsePack(name,pack);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}






	/*private void requestPacket(Integer id) {
		try {
			socket.SendRequest(id, name);
		} catch (IOException e) {
			System.err.println("Requesting package from server failed.");
			e.printStackTrace();
		}
	}

	private LinkedList<Integer> validateQue() {
		Iterator<EchoPacket> it = que.iterator();
		EchoPacket currentPacket;
		LinkedList<Integer> missingPackets = new LinkedList<Integer>();
		int packetid = 0;
		
		while(it.hasNext()){	
			currentPacket = it.next();
			
			while(currentPacket.packet_id != packetid){
				// packet missing
				missingPackets.add(packetid);
				packetid++;
			}

			packetid++;
		}
		
		return missingPackets;
	}*/

}
