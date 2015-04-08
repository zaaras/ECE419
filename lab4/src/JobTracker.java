import java.net.InetAddress;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException.Code;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.Watcher.Event.EventType;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;

public class JobTracker {
	static String primaryJobTracker = "/PrimaryJobTracker";
	private static ZkConnector zkc;
	private static ZooKeeper zk;
	private Watcher watcher, jobsWatcher;
	private JobTrackerServer server;
	private static InetAddress localHost;
	public static LinkedBlockingQueue<String> hashQue = new LinkedBlockingQueue<String>();
	private static List<String> workers = new LinkedList<String>();
	private boolean leader = false;
	private int chunkSize, workerCount, splits=0;

	public static Integer localPort = 4444;

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

		jobsWatcher = new Watcher() {
			@Override
			public void process(WatchedEvent event) {
				handleJobEvent(event);
			}

		};
		zk = zkc.getZooKeeper();
	}

	private void handleJobEvent(WatchedEvent event) {
		// watcher triggered when the JOB que in zookeeper changes
		String path = event.getPath();
		// EventType type = event.getType();

		if (path.equalsIgnoreCase(JobTrackerQueManager.jobPath)) {
			if (leader)
				delegateJob(); // read job from zookeeper que and give it to the workers
		}
		checkJobPath();
	}

	void delegateJob() {
		System.out.println("delegate job");
		
		try {
			Stat stat = zkc.exists(Worker.workerPool, null);
			if (stat == null || stat.getNumChildren() == 0) {
				System.out.println("No workers, try again later.");
			} else {
				
				workerCount = stat.getNumChildren();
				workers = zk.getChildren(Worker.workerPool, false);
				
				chunkSize = FileServer.dictionarySize/workerCount;
				System.out.println("Workers: " + workerCount);

				Iterator<String> workerIterator = workers.iterator();
				String worker ;
				while (workerIterator.hasNext()) {
					worker = workerIterator.next();
					System.out.println(worker);
					String data = Integer.toString(splits) + ";" + Integer.toString(splits+chunkSize+1);
					zk.setData(Worker.workerPool + "/" + worker, data.getBytes() , -1);
					splits = splits+chunkSize+1;
				}
				splits = 0;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		
	}

	private void checkJobPath() {
		zkc.exists(JobTrackerQueManager.jobPath, jobsWatcher);
	}

	public static void main(String[] args) {

		if (args.length != 1) {
			System.out
					.println("Usage: java -classpath lib/zookeeper-3.3.2.jar:lib/log4j-1.2.15.jar:. JobTracker zkServer:clientPort");
			return;
		}

		new JobTrackerQueManager(args[0]).start();

		JobTracker jt = new JobTracker(args[0]);
		jt.checkpath(); // Leader election
		jt.checkJobPath(); // Jobs que on zookeeper

		while (true)
			try {
				Thread.sleep(10);
			} catch (Exception e) {
			}
	}

	private void handleEvent(WatchedEvent event) {
		String path = event.getPath();
		EventType type = event.getType();

		if (path.equalsIgnoreCase(primaryJobTracker)) {
			if (type == EventType.NodeDeleted) {
				// System.out.println(primaryJobTracker +
				// " deleted! Let's go!");
				checkpath(); // try to become the boss
			}
			if (type == EventType.NodeCreated) {
				// System.out.println(primaryJobTracker + " created!");

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
		// leader = false;
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
