import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetAddress;
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

public class FileServer {

	private static List<String> workerRequests = new LinkedList<String>();
	private static ZkConnector zkc;
	private static ZooKeeper zk;
	private Watcher watcher, requestsWatcher;
	static String primaryFileServer = "/PrimaryFileServer";
	private static InetAddress localHost;
	private boolean leader = false;
	private FileServerServer server;
	public static List<String> dictionary = new LinkedList<String>();
	public static Integer localPort = 3333;
	public static LinkedBlockingQueue<String> requestQue = new LinkedBlockingQueue<String>();
	public static LinkedBlockingQueue<String> outputQue = new LinkedBlockingQueue<String>();

	public FileServer(String con, String dic) {
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(dic));
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		System.out.println("Building dictionary");
		// Loop over lines in the file and print them.
		while (true) {
			String line = null;
			try {
				line = reader.readLine();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if (line == null) {
				break;
			}
			dictionary.add(line);
		}

		zkc = new ZkConnector();
		try {
			localHost = InetAddress.getLocalHost();
			zkc.connect(con);

		} catch (Exception e) {
			System.out.println("Zookeeper connect " + e.getMessage());
		}

		watcher = new Watcher() { // Anonymous Watcher leader election
			@Override
			public void process(WatchedEvent event) {
				handleEvent(event);
			}
		};

		requestsWatcher = new Watcher() { // queue
			@Override
			public void process(WatchedEvent event) {
				handleJobEvent(event);
			}

		};

		zk = zkc.getZooKeeper();
		// TODO Auto-generated constructor stub
	}

	private void handleJobEvent(WatchedEvent event) {
		// watcher triggered when the JOB que in zookeeper changes
		String path = event.getPath();
		// EventType type = event.getType();

		if (path.equalsIgnoreCase(FileServerQueManager.requestPath)) {
			// if (leader)
			if (event.getType() == EventType.NodeDataChanged)
				delegateRequest(); // read job from zookeeper que and give it to
									// the workers
		}
		checkRequestPath();
	}

	private void checkRequestPath() {
		zkc.exists(FileServerQueManager.requestPath, requestsWatcher);
	}

	void delegateRequest() {
		String list, item;
		LinkedList<String> requests = new LinkedList<String>();
		System.out.println("delegate request");
		try {
			Stat stat = zkc.exists(FileServerQueManager.requestPath, null);
			if (stat != null) {
				list = new String(zk.getData(FileServerQueManager.requestPath,
						false, null));
				requests = JobTrackerQueManager.deserializeList(list);
				item = requests.poll();
				zk.setData(FileServerQueManager.requestPath, requests
						.toString().getBytes(), -1);
				// Add to output que
				outputQue.add(item);
				System.out.println("item from queue: " + item);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void handleEvent(WatchedEvent event) {
		String path = event.getPath();
		EventType type = event.getType();

		if (path.equalsIgnoreCase(primaryFileServer)) {
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
		Stat stat = zkc.exists(primaryFileServer, watcher);
		// leader = false;
		if (stat == null) { // znode doesn't exist; let's try creating it
			System.out.println("Creating " + primaryFileServer);
			Code ret = zkc.create(primaryFileServer, // Path of znode
					localHost.getHostName(), // Data not needed.
					CreateMode.EPHEMERAL // Znode type, set to EPHEMERAL.
					);
			if (ret == Code.OK) {
				System.out.println("Leader");
				leader = true;
				server = new FileServerServer();
				server.start();
			}
		}

	}

	public static void main(String[] args) {

		if (args.length != 2) {
			System.out
					.println("Usage: java -classpath lib/zookeeper-3.3.2.jar:lib/log4j-1.2.15.jar:. JobTracker zkServer:clientPort dic");
			return;
		}

		new FileServerQueManager(args[0]).start();

		FileServer jt = new FileServer(args[0], args[1]);
		jt.checkpath(); // Leader election
		jt.checkRequestPath();

		while (true) {
			;

		}
	}
}
