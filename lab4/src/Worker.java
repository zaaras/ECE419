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
	private static String currentCount = "1";
	private static ZooKeeper zk;
	private static ZkConnector zkc;
	private Watcher watcher;

	static Integer tmp;

	public Worker(String string) {
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
				// String path = event.getPath();
				try {
					if (zkc.exists(workerPool + workerName + currentCount, null) != null) {

						EventType type = event.getType();
						if (type == EventType.NodeDataChanged) {
							System.out.println("data changed:");
							System.out.println(new String(zk.getData(workerPool
									+ workerName + currentCount, false, null)));
						}
					}
					addWatcher();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

		};

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

	public static void main(String[] args) {
		if (args.length != 1) {
			System.out
					.println("Usage: java -classpath lib/zookeeper-3.3.2.jar:lib/log4j-1.2.15.jar:. A zkServer:clientPort");
			return;
		}

		Worker w = new Worker(args[0]);
		w.addWatcher();

		while (true)
			;
	}

	private void addWatcher() {
		zkc.exists(workerPool + workerName + currentCount, watcher);
	}

	private static void joinPool() throws KeeperException, InterruptedException {

		Code ret = zkc.create(workerPool + workerName + currentCount, // Path of
																		// znode
				null, // Data not needed.
				CreateMode.EPHEMERAL // Znode type, set to Persistent.
				);

		if (ret == Code.OK) {
			System.out.println("Added to pool");
		}

	}
}