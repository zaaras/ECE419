import java.net.InetAddress;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException.Code;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.Watcher.Event.EventType;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;

public class JobTracker {
	static String primaryJobTracker = "/PrimaryJobTracker";
	static String resultsFolder = "/Results";
	private static ZkConnector zkc;
	private static ZooKeeper zk;
	private Watcher watcher, jobsWatcher, resultsWatcher;
	private JobTrackerServer server;
	private static InetAddress localHost;
	public static LinkedBlockingQueue<String> hashQue = new LinkedBlockingQueue<String>();
	private static List<String> workers = new LinkedList<String>();
	private boolean leader = false;
	public static HashMap<String, JobTrackerClientHandler> clients = new HashMap<String, JobTrackerClientHandler>();
	public static Lock clientsLock;

	public static Integer localPort = 4444;

	public JobTracker(String string) {
		clientsLock = new ReentrantLock();
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

		resultsWatcher = new Watcher() {
			@Override
			public void process(WatchedEvent event) {
				handleResultEvent(event);
			}

		};

		zk = zkc.getZooKeeper();
	}

	private void handleResultEvent(WatchedEvent event) {
		String resultData = null;
		// PrintWriter out;
		Stat stat = zkc.exists(resultsFolder, resultsWatcher);
		List<String> clientResults;
		String clientName;
		Iterator<String> iterator;
		// leader = false;
		System.out.println("Results changed");

		if (stat == null) { // znode doesn't exist; let's try creating it
			System.out.println("Creating " + resultsFolder);
			Code ret = zkc.create(resultsFolder, // Path of znode
					null, // Data not needed.
					CreateMode.PERSISTENT // Znode type, set to EPHEMERAL.
					);
			if (ret == Code.OK) {
				;
			}
		} else {
			try {
				// if (event.getType() == EventType.NodeChildrenChanged) {
				while(stat.getNumChildren()==0){
					stat = zkc.exists(resultsFolder, resultsWatcher);
				}
				//Thread.sleep(1000);
				clientResults = zk.getChildren(resultsFolder, false);
				iterator = clientResults.iterator();
				System.out.println(clientResults.toString());
				System.out.println(clients.toString());
				while (iterator.hasNext()) {
					clientName = iterator.next();
					JobTrackerClientHandler outputThread = clients.get("/"
							+ clientName);
					// out = new PrintWriter(output);
					System.out.println(clients.toString());
					resultData = new String(zk.getData(resultsFolder + "/"
							+ clientName, false, null));

					if (!resultData.isEmpty()) {
						System.out.println(resultsFolder + "/" + clientName
								+ "    " + resultData);
						outputThread.SendData(resultData);
					}
					zk.delete(resultsFolder + "/" + clientName, -1);
				}

				// }
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

	private void handleJobEvent(WatchedEvent event) {
		// watcher triggered when the JOB que in zookeeper changes
		String path = event.getPath();
		// EventType type = event.getType();

		if (path.equalsIgnoreCase(JobTrackerQueManager.jobPath)) {
			if (leader)
				delegateJob(); // read job from zookeeper que and give it to the
								// workers
		}
		checkJobPath();
	}

	static void delegateJob() {
		String list, item = null;
		LinkedList<String> Jobs = new LinkedList<String>();
		int chunkSize, workerCount, splits = 0;
		// System.out.println("delegate job");

		try {

			Stat stat = zkc.exists(JobTrackerQueManager.jobPath, null);

			if (stat != null) {

				list = new String(zk.getData(JobTrackerQueManager.jobPath,
						false, null));
				Jobs = JobTrackerQueManager.deserializeList(list);

				if (Jobs.isEmpty())
					return;

				item = Jobs.poll();
				zk.setData(JobTrackerQueManager.jobPath, Jobs.toString()
						.getBytes(), -1);

			}

			if (item == null)
				return;
			stat = zkc.exists(Worker.workerPool, null);
			if (stat == null || stat.getNumChildren() == 0) {
				System.out.println("No workers, try again later.");
			} else {

				workerCount = stat.getNumChildren();
				workers = zk.getChildren(Worker.workerPool, false);

				chunkSize = FileServer.dictionarySize / workerCount;
				System.out.println("Workers: " + workerCount);

				Iterator<String> workerIterator = workers.iterator();
				String worker;
				while (workerIterator.hasNext()) {
					worker = workerIterator.next();
					System.out.println(worker);
					String data = Integer.toString(splits) + ";"
							+ Integer.toString(splits + chunkSize + 1) + ";"
							+ item;
					zk.setData(Worker.workerPool + "/" + worker,
							data.getBytes(), -1);
					splits = splits + chunkSize + 1;
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

	private void checkResultPath() {
		Stat stat = zkc.exists(resultsFolder, resultsWatcher);
		// leader = false;
		if (stat == null) { // znode doesn't exist; let's try creating it
			System.out.println("Creating " + resultsFolder);
			Code ret = zkc.create(resultsFolder, // Path of znode
					null, // Data not needed.
					CreateMode.PERSISTENT // Znode type, set to EPHEMERAL.
					);
			if (ret == Code.OK) {
				;
			}
		}
		stat = zkc.exists(resultsFolder, resultsWatcher);
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
		jt.checkResultPath(); // Results

		new JobDelegator().start();

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
