import java.io.ObjectOutputStream;
import java.net.Socket;

import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.Watcher.Event.EventType;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;

public class ClientDriver {
	private Watcher watcher;

	public static ZkConnector zkc;
	private ZooKeeper zk;
	private String JobTrackerIp;
	private Socket JobTrackerSoc;
	public static ObjectOutputStream out = null;

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
					JobTrackerSoc.close();
					JobTrackerSoc = null;
					out = null;
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
				
				Thread.sleep(1000);
				JobTrackerIp = new String(zk.getData(
						JobTracker.primaryJobTracker, false, null));
				System.out.println("Found primary JobTracker at "
						+ JobTrackerIp);
				JobTrackerSoc = new Socket(JobTrackerIp, JobTracker.localPort);
				out = new ObjectOutputStream(JobTrackerSoc.getOutputStream());
				// }
			} catch (Exception e) {
				e.printStackTrace();
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

		ClientDriverInputThread cdt = new ClientDriverInputThread();
		cdt.start();

		while (true) {
			;
		}

	}
}
