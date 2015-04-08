import java.net.Socket;
import java.util.Scanner;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.Watcher.Event.EventType;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;

public class ClientDriver {
	private Watcher watcher;

	public static ZkConnector zkc;
	private static String clientPath = "/Client";
	public static String clientName = "/zumar";
	private String currentCount = "1";
	private ZooKeeper zk;
	private String JobTrackerIp;
	volatile private Socket JobTrackerSoc;
	ClientDriverInputThread cdt;
	ClientDriverOutputThread cdtout;
	public static Scanner in = new Scanner(System.in);
	public static LinkedBlockingQueue<String> inputQue = new LinkedBlockingQueue<String>();


	public ClientDriver(String string) {

		zkc = new ZkConnector();

		try {
			zkc.connect(string);
		} catch (Exception e) {
			System.out.println("Zookeeper connect " + e.getMessage());
		}

		zk = zkc.getZooKeeper();
		watcher = new Watcher() {

			@Override
			public void process(WatchedEvent event) {
				handleEvent(event);

			}

		};

	}

	private void handleEvent(WatchedEvent event) {
		String path = event.getPath();
		EventType type = event.getType();
		System.out.println("watcher");
		try {

			if (path.equalsIgnoreCase(JobTracker.primaryJobTracker)) {
				if (type == EventType.NodeDeleted) {
					System.out.println("node deleted");
					
					cdt.killMe = true;
					cdt.die();
					cdt = null;

					cdtout.die();
					cdtout.killMe = true;
					cdtout = null;

					JobTrackerSoc.close();
					// System.out.println("Closing socket");
					JobTrackerSoc = null;
					zkc.exists(JobTracker.primaryJobTracker, watcher);

				}
				if (type == EventType.NodeCreated) {
					System.out.println("new node");
					checkJobTrackers();
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void checkJobTrackers() {
		Stat stat = zkc.exists(JobTracker.primaryJobTracker, watcher);

		if (stat == null) {
			System.out.println("No JobTrackers spawed, exiting.");
			System.exit(0);
		} else {
			try {
				// if (JobTrackerSoc == null) {

				while (stat.getDataLength() == 0) {
					stat = zkc.exists(JobTracker.primaryJobTracker, watcher);
				}
				Thread.sleep(1000);
				JobTrackerIp = new String(zk.getData(
						JobTracker.primaryJobTracker, false, null));
				System.out.println("Found primary JobTracker at "
						+ JobTrackerIp);
				JobTrackerSoc = new Socket(JobTrackerIp, JobTracker.localPort);
				while (!JobTrackerSoc.isConnected())
					;


			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		cdt = new ClientDriverInputThread(JobTrackerSoc, currentCount);
		cdt.start();

		cdtout = new ClientDriverOutputThread(JobTrackerSoc, currentCount);
		cdtout.start();

	}

	public static void main(String[] args) {

		if (args.length != 1) {
			System.out
					.println("Usage: java -classpath lib/zookeeper-3.3.2.jar:lib/log4j-1.2.15.jar:. ClientDriver zkServer:clientPort");
			return;
		}

		// Get IP for primary job Tracker from zookeeper
		ClientDriver cd = new ClientDriver(args[0]);
		cd.addClient();
		cd.checkJobTrackers();

		
		String input;
		while (true) {
			input = in.nextLine();
			inputQue.add(input);
		}

	}

	private void addClient() {
		Integer tmp;
		try {
			Stat stat = zkc.exists(clientPath, watcher);

			if (stat == null) {
				System.out.println("Creating " + clientPath);
				zk.create(clientPath, // Path of znode
						"1".getBytes(), // Data not needed.
						Ids.OPEN_ACL_UNSAFE, // ACL, set to Completely Open.
						CreateMode.EPHEMERAL // Znode type, set to Persistent.
				);

				if (zkc.exists(clientPath, null) == null) {
					zk.create(clientPath, // Path of znode
							null, // Data not needed.
							Ids.OPEN_ACL_UNSAFE, // ACL, set to Completely Open.
							CreateMode.PERSISTENT // Znode type, set to
													// Persistent.
					);
				}
			} else {
				currentCount = new String(zk.getData(clientPath, false, null));

				tmp = Integer.parseInt(currentCount);
				tmp++;

				currentCount = tmp.toString();
				zk.setData(clientPath, currentCount.getBytes(), -1);
				System.out.println(currentCount);
			}

		} catch (KeeperException e) {
			System.out.println(e.code());
		} catch (Exception e) {
			System.out.println("Make node:" + e.getMessage());
		}

	}
}
