import java.net.InetAddress;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException.Code;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.Watcher.Event.EventType;
import org.apache.zookeeper.data.Stat;

public class JobTracker {
	static String primaryJobTracker = "/PrimaryJobTracker";
	static ZkConnector zkc;
	private boolean leader = false;
	private Watcher watcher;
	private JobTrackerServer server;
	private static InetAddress localHost;
	
	public static Integer localPort = 1111;

	public JobTracker(String string) {
		zkc = new ZkConnector();
		try {
			localHost = InetAddress.getLocalHost();
			zkc.connect(string);
			
		} catch (Exception e) {
			System.out.println("Zookeeper connect " + e.getMessage());
		}

		watcher = new Watcher() { // Anonymous Watcher
			@Override
			public void process(WatchedEvent event) {
				handleEvent(event);
			}
		};
	}

	public static void main(String[] args) {

		if (args.length != 1) {
			System.out
					.println("Usage: java -classpath lib/zookeeper-3.3.2.jar:lib/log4j-1.2.15.jar:. JobTracker zkServer:clientPort");
			return;
		}

		JobTracker jt = new JobTracker(args[0]);

		jt.checkpath();
		// Create primary job tracker znode and elect leader

		while (true)
			try {
				Thread.sleep(100);
			} catch (Exception e) {
			}
	}

	private void handleEvent(WatchedEvent event) {
		String path = event.getPath();
		EventType type = event.getType();

		if (path.equalsIgnoreCase(primaryJobTracker)) {
			if (type == EventType.NodeDeleted) {
				//System.out.println(primaryJobTracker + " deleted! Let's go!");
				checkpath(); // try to become the boss
			}
			if (type == EventType.NodeCreated) {
				//System.out.println(primaryJobTracker + " created!");

				try {
					Thread.sleep(500);
				} catch (Exception e) {
				}
				checkpath(); // re-enable the watch
			}
		}
	}

	private void checkpath() {
		Stat stat = zkc.exists(primaryJobTracker, watcher);
		leader = false;
		if (stat == null) { // znode doesn't exist; let's try creating it
			System.out.println("Creating " + primaryJobTracker);
			Code ret = zkc.create(primaryJobTracker, // Path of znode
					localHost.getHostName(), // Data not needed.
					CreateMode.EPHEMERAL // Znode type, set to EPHEMERAL.
					);
			if (ret == Code.OK) {
				System.out.println("Leader");
				leader = true;
				server = new JobTrackerServer(localPort);
				server.start();
			}
		}

	}
}
