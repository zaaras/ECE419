import java.net.Socket;
import java.util.Scanner;

import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.Watcher.Event.EventType;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;

public class ClientDriver {
	private Watcher watcher;
	private static Scanner in = new Scanner(System.in);
	private static String inputHashes;
	private ZkConnector zkc;
	private ZooKeeper zk;
	private String JobTrackerIp;
	private Socket JobTrackerSoc;

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

		if (path.equalsIgnoreCase(JobTracker.primaryJobTracker)) {
			if (type == EventType.NodeDeleted) {

			}
			if (type == EventType.NodeCreated) {
				checkJobTrackers();
			}
		}
	}

	private void checkJobTrackers() {
		Stat stat = zkc.exists(JobTracker.primaryJobTracker, watcher);

		if (stat == null) {
			System.out.println("No JobTrackers spawed, exiting.");
			System.exit(0);
		} else {
			try {
				JobTrackerIp = new String(zk.getData(JobTracker.primaryJobTracker, false, null));
				System.out.println("Found primary JobTracker at " + JobTrackerIp);
				JobTrackerSoc = new Socket(JobTrackerIp, JobTracker.localPort);
			} catch (Exception e) {
			}			
		}
	}

	public static void main(String[] args) {

		if (args.length != 1) {
			System.out
					.println("Usage: java -classpath lib/zookeeper-3.3.2.jar:lib/log4j-1.2.15.jar:. ClientDriver zkServer:clientPort");
			return;
		}

		// Get IP for primary job Tracker from zookeeper
		ClientDriver cd = new ClientDriver(args[0]);
		cd.checkJobTrackers();
		
		while (true) {
			inputHashes = in.nextLine();
			System.out.println(inputHashes);
			
		}

	}
}
