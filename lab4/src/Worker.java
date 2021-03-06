import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.KeeperException.Code;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.Watcher.Event.EventType;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;

public class Worker {
	public static String workerPath = "/Worker";
	public static String workerPool = "/WorkerPool";
	private static String workerName = "/guy";
	private String currentCount = "1";
	private static ZooKeeper zk;
	private static ZkConnector zkc;
	private Watcher watcher, connectionWatcher;
	public Socket FileServerSoc;
	private String FileServerIp;
	public ObjectOutputStream toServer = null;
	public ObjectInputStream fromServer = null;
	private int chunkStart, chunkEnd;
	private String[] dataSplits;
	volatile public String Hash,Name;
	

	static Integer tmp;

	public Worker(String string) {
		zkc = new ZkConnector();
		try {
			zkc.connect(string);
		} catch (Exception e) {
			System.out.println("Zookeeper connect " + e.getMessage());
		}

		zk = zkc.getZooKeeper();
		watcher = new Watcher() {// job tracker giving stuff to worker

			@Override
			public void process(WatchedEvent event) {
				String dataIn = "";
				try {
					if (zkc.exists(workerPool + workerName + currentCount, null) != null) {

						EventType type = event.getType();
						if (type == EventType.NodeDataChanged) {
							dataIn = new String(zk.getData(workerPool
									+ workerName + currentCount, false, null));
							System.out.println(dataIn);

							dataSplits = dataIn.split(";");
							chunkStart = Integer.parseInt(dataSplits[0]);
							chunkEnd = Integer.parseInt(dataSplits[1]);
							Hash = dataSplits[2];
							Name = dataSplits[3];
							
							
							dataIn = chunkStart + ";" + chunkEnd;
							toServer.writeUTF(workerName + currentCount + ";"
									+ dataIn);
							toServer.flush();

						}
					}
					zkc.exists(workerPool + workerName + currentCount, watcher);
					// addWorker();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

		};

		connectionWatcher = new Watcher() {

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

			if (path.equalsIgnoreCase(FileServer.primaryFileServer)) {
				if (type == EventType.NodeDeleted) {
					System.out.println("node deleted");
					FileServerSoc.close();
					FileServerSoc = null;
					toServer = null;
					zkc.exists(FileServer.primaryFileServer, connectionWatcher);

				}
				if (type == EventType.NodeCreated) {
					System.out.println("new node");
					checkFileServers();
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	void writeResults(String name, String pswd){
		try {
			zk.setData(JobTracker.resultsFolder,"change".getBytes() , -1);
			zkc.create(JobTracker.resultsFolder + name, pswd, CreateMode.EPHEMERAL);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	}

	private void checkFileServers() {
		Stat stat = zkc.exists(FileServer.primaryFileServer, connectionWatcher);

		if (stat == null) {
			System.out.println("No FileServers spawed, exiting.");
			System.exit(0);
		} else {
			try {
				// if (JobTrackerSoc == null) {

				Thread.sleep(1000);
				FileServerIp = new String(zk.getData(
						FileServer.primaryFileServer, false, null));
				System.out.println("Found primary FileServer at "
						+ FileServerIp);
				FileServerSoc = new Socket(FileServerIp, FileServer.localPort);

				toServer = new ObjectOutputStream(
						FileServerSoc.getOutputStream());

				WorkerInputThread inputThread = new WorkerInputThread(
						FileServerSoc, this);
				inputThread.start();

				// }
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private void addWorker() {

		try {
			Stat stat = zkc.exists(workerPath, watcher);

			if (stat == null) {
				System.out.println("Creating " + workerPath);
				zk.create(workerPath, // Path of znode
						"1".getBytes(), // Data not needed.
						Ids.OPEN_ACL_UNSAFE, // ACL, set to Completely Open.
						CreateMode.EPHEMERAL // Znode type, set to Persistent.
				);

				if (zkc.exists(workerPool, null) == null) {
					zk.create(workerPool, // Path of znode
							null, // Data not needed.
							Ids.OPEN_ACL_UNSAFE, // ACL, set to Completely Open.
							CreateMode.PERSISTENT // Znode type, set to
													// Persistent.
					);
				}
			} else {
				currentCount = new String(zk.getData(workerPath, false, null));

				tmp = Integer.parseInt(currentCount);
				tmp++;

				currentCount = tmp.toString();
				zk.setData(workerPath, currentCount.getBytes(), -1);
				System.out.println(currentCount);
			}

			joinPool();

		} catch (KeeperException e) {
			System.out.println(e.code());
		} catch (Exception e) {
			System.out.println("Make node:" + e.getMessage());
		}

	}

	private void joinPool() throws KeeperException, InterruptedException {

		Code ret = zkc.create(workerPool + workerName + currentCount, // Path of
																		// znode
				null, // Data not needed.
				CreateMode.EPHEMERAL // Znode type, set to Persistent.
				);

		if (ret == Code.OK) {
			System.out.println("Added to pool");
		}

	}

	public static void main(String[] args) {
		if (args.length != 1) {
			System.out
					.println("Usage: java -classpath lib/zookeeper-3.3.2.jar:lib/log4j-1.2.15.jar:. A zkServer:clientPort");
			return;
		}

		Worker w = new Worker(args[0]);
		w.checkFileServers();
		w.addWorker();

		while (true)
			;
	}
}
