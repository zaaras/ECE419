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
		// TODO Auto-generated method stub

	}

	public void keyTyped(KeyEvent arg0) {
		// TODO Auto-generated method stub

	}

	void toServer(int msg) {
		try {
			socket.SendEvent(msg, name);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}

	public void update(String message) {
		
		if(message.contains("mov:")){
			if (message.contains("mov:q")) {
				Mazewar.quit();
				// Up-arrow moves forward.
			} else if (message.contains("mov:u")) {
				forward();
				// Down-arrow moves backward.
			} else if (message.contains("mov:b")) {
				backup();
				// Left-arrow turns left.
			} else if (message.contains("mov:l")) {
				turnLeft();
				// Right-arrow turns right.
			} else if (message.contains("mov:r")) {
				turnRight();
				// Spacebar fires.
			} else if (message.contains("mov:s")) {
				fire();
			}
		}
	}

	/**
	 * May want to fill in code here.
	 */
}
