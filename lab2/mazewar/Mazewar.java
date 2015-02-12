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

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextPane;

/**
 * The entry point and glue code for the game. It also contains some helpful
 * global utility methods.
 * 
 * @author Geoffrey Washburn &lt;<a
 *         href="mailto:geoffw@cis.upenn.edu">geoffw@cis.upenn.edu</a>&gt;
 * @version $Id: Mazewar.java 371 2004-02-10 21:55:32Z geoffw $
 */

public class Mazewar extends JFrame {
	/**
	 * The default width of the {@link Maze}.
	 */
	private final int mazeWidth = 20;

	/**
	 * The default height of the {@link Maze}.
	 */
	private final int mazeHeight = 10;

	/**
	 * The default random seed for the {@link Maze}. All implementations of the
	 * same protocol must use the same seed value, or your mazes will be
	 * different.
	 */
	private final int mazeSeed = 42;

	/**
	 * The {@link Maze} that the game uses.
	 */
	private Maze maze = null;

	/**
	 * The {@link GUIClient} for the game.
	 */
	// private GUIClient guiClient = null;
	private static RemoteClient localClient = null;

	/**
	 * The panel that displays the {@link Maze}.
	 */
	private OverheadMazePanel overheadPanel = null;

	/**
	 * The table the displays the scores.
	 */
	private JTable scoreTable = null;

	/**
	 * Create the textpane statically so that we can write to it globally using
	 * the static consolePrint methods
	 */
	private static final JTextPane console = new JTextPane();

	/**
	 * Write a message to the console followed by a newline.
	 * 
	 * @param msg
	 *            The {@link String} to print.
	 */
	public static synchronized void consolePrintLn(String msg) {
		console.setText(console.getText() + msg + "\n");
	}

	/**
	 * Write a message to the console.
	 * 
	 * @param msg
	 *            The {@link String} to print.
	 */
	public static synchronized void consolePrint(String msg) {
		console.setText(console.getText() + msg);
	}

	/**
	 * Clear the console.
	 */
	public static synchronized void clearConsole() {
		console.setText("");
	}

	/**
	 * Static method for performing cleanup before exiting the game.
	 */
	public static void quit() {
		// Put any network clean-up code you might have here.
		// (inform other implementations on the network that you have
		// left, etc.)
		localClient.exitServer();
		System.exit(0);
	}

	/**
	 * The place where all the pieces are put together.
	 */

	private MazewarClient clientConnection = null;
	private LinkedList<GUIClient> remoteClients = new LinkedList<GUIClient>();
	public static int TOTAL_PLAYERS = 4;
	private int playerCount = 0;
	private Queue<EchoPacket> que;
	private boolean serverAck = false;
	private boolean localClientAdded = false;
	EchoPacket fromServerOutter = null;

	public Mazewar() {
		super("ECE419 Mazewar");
		consolePrintLn("ECE419 Mazewar started!");

		// Create the maze
		maze = new MazeImpl(new Point(mazeWidth, mazeHeight), mazeSeed);
		que = new LinkedList<EchoPacket>();
		assert (maze != null);

		// Have the ScoreTableModel listen to the maze to find
		// out how to adjust scores.
		ScoreTableModel scoreModel = new ScoreTableModel();
		assert (scoreModel != null);
		maze.addMazeListener(scoreModel);

		// Throw up a dialog to get the GUIClient name.
		String name = JOptionPane.showInputDialog("Enter your name");
		if ((name == null) || (name.length() == 0)) {
			Mazewar.quit();
		}

		// You may want to put your network initialization code somewhere in
		// here.
		String[] args = new String[2];
		args[0] = "localhost";//"ug147.eecg.utoronto.ca";//"localhost";
		args[1] = "1111";

		try {
			clientConnection = new MazewarClient(args, name);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			assert (false);
		}
		assert (clientConnection != null);

		localClient = new RemoteClient(name, clientConnection);
		Iterator<serverClient> it;
		serverClient temp;
		int localx = 0, localy = 0;
		Direction localdir = Direction.North;

		try {
			
			localClient.initServer();// sends init to server
			fromServerOutter = (EchoPacket) clientConnection.in.readObject();
			
			it = fromServerOutter.serverClients.iterator();
			
			for(int i = 0; i<fromServerOutter.serverClients.size();i++){
				temp = fromServerOutter.serverClients.get(i);
				if (!temp.name.equals(localClient.getName())) {
					remoteClients.add(new GUIClient(temp.name));
					maze.addClient(remoteClients.getLast(), new Point(temp.x,temp.y),temp.dir);
					playerCount++;
				}else{
					localx = temp.x;
					localy = temp.y;
					localdir = temp.dir;
				}
			}
			
			
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		maze.addClient(localClient, new Point(localx,localy),localdir);
		this.addKeyListener(localClient);

		// Create the GUIClient and connect it to the KeyListener queue

		// guiClient = new GUIClient(name);
		// maze.addClient(guiClient);
		// this.addKeyListener(guiClient);

		// Use braces to force constructors not to be called at the beginning of
		// the
		// constructor.
		{
			// maze.addClient(new RobotClient("Norby"));
			// maze.addClient(new RobotClient("Robbie"));
			// maze.addClient(new RobotClient("Clango"));
			// maze.addClient(new RobotClient("Marvin"));

		}

		// Create the panel that will display the maze.
		overheadPanel = new OverheadMazePanel(maze, localClient);
		assert (overheadPanel != null);
		maze.addMazeListener(overheadPanel);

		// Don't allow editing the console from the GUI
		console.setEditable(false);
		console.setFocusable(false);
		console.setBorder(BorderFactory.createTitledBorder(BorderFactory
				.createEtchedBorder()));

		// Allow the console to scroll by putting it in a scrollpane
		JScrollPane consoleScrollPane = new JScrollPane(console);
		assert (consoleScrollPane != null);
		consoleScrollPane.setBorder(BorderFactory.createTitledBorder(
				BorderFactory.createEtchedBorder(), "Console"));

		// Create the score table
		scoreTable = new JTable(scoreModel);
		assert (scoreTable != null);
		scoreTable.setFocusable(false);
		scoreTable.setRowSelectionAllowed(false);

		// Allow the score table to scroll too.
		JScrollPane scoreScrollPane = new JScrollPane(scoreTable);
		assert (scoreScrollPane != null);
		scoreScrollPane.setBorder(BorderFactory.createTitledBorder(
				BorderFactory.createEtchedBorder(), "Scores"));

		// Create the layout manager
		GridBagLayout layout = new GridBagLayout();
		GridBagConstraints c = new GridBagConstraints();
		getContentPane().setLayout(layout);

		// Define the constraints on the components.
		c.fill = GridBagConstraints.BOTH;
		c.weightx = 1.0;
		c.weighty = 3.0;
		c.gridwidth = GridBagConstraints.REMAINDER;
		layout.setConstraints(overheadPanel, c);
		c.gridwidth = GridBagConstraints.RELATIVE;
		c.weightx = 2.0;
		c.weighty = 1.0;
		layout.setConstraints(consoleScrollPane, c);
		c.gridwidth = GridBagConstraints.REMAINDER;
		c.weightx = 1.0;
		layout.setConstraints(scoreScrollPane, c);

		// Add the components
		getContentPane().add(overheadPanel);
		getContentPane().add(consoleScrollPane);
		getContentPane().add(scoreScrollPane);

		// Pack everything neatly.
		pack();

		// Let the magic begin.
		setVisible(true);
		overheadPanel.repaint();
		this.requestFocusInWindow();

		Iterator<GUIClient> itgui;
		GUIClient tempgui;
		int i;
		while (true) {
			try {
				EchoPacket fromServer = (EchoPacket) clientConnection.in
						.readObject();
				
				if(fromServer == null){
					System.out.println("killed");
				}
				
				//System.out.println(fromServer.player + " " + fromServer.event
				//		+ " " + fromServer.type);
				
				que.add(fromServer);
				
				if(fromServer.event == EchoPacket.TICK){
					maze.missleTick();
					continue;
				}

				if (fromServer.player.equals(localClient.getName())) {
					localClient.update(fromServer);

				} else {
					if (fromServer.type == EchoPacket.SERVER_LOC) {
						// Some one joined the game

						it = fromServer.serverClients.iterator();
						while (it.hasNext()) {
							temp = it.next();
							if (!temp.name.equals(localClient.getName())) {
								System.out
										.println("<<<<--------Adding cilent " + temp.name +" X: " + temp.x +" Y: "+temp.y);
								remoteClients.add(new GUIClient(temp.name));
								maze.addClient(remoteClients.get(playerCount), new Point(temp.x,temp.y),temp.dir);
								playerCount++;
							}
						}
					} else {

						
						 itgui = remoteClients.iterator(); 
						 while(itgui.hasNext()) { 
							 tempgui = itgui.next(); 
							 if(tempgui.getName().equals(fromServer.player))
								 	tempgui.update(fromServer);
						 }
						 

						/*for (i = 0; i < TOTAL_PLAYERS; i++) {
							if (remoteClients.get(i).getName().equals(fromServer.player)) {
								remoteClients.get(i).update(fromServer);
								break;
							}
						}*/
						
					}
				}

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.exit(1);
			}
		}
	}

	/**
	 * Entry point for the game.
	 * 
	 * @param args
	 *            Command-line arguments.
	 */
	public static void main(String args[]) {

		/* Create the GUI */
		new Mazewar();

	}
}
