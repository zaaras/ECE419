import java.util.LinkedList;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException.Code;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;

public class JobTrackerQueManager extends Thread {

	private ZkConnector zkc;
	private ZooKeeper zk;
	public static String jobPath = "/Jobs";
	private String fromClient;
	private Watcher watcher;
	private LinkedList<String> jobList;
	private String listHolder ;

	public JobTrackerQueManager(String string) {
		super();
		zkc = new ZkConnector();
		

		try {
			zkc.connect(string);
		} catch (Exception e) {
			System.out.println("Zookeeper connect " + e.getMessage());
		}

		watcher = new Watcher() {

			@Override
			public void process(WatchedEvent event) {
				//System.out.println("Something happened to the jobs");
			}

		};
		zk = zkc.getZooKeeper();
	}

	@Override
	public void run() {
		while (true) {
			if (!JobTracker.hashQue.isEmpty()) {
				fromClient = JobTracker.hashQue.poll();

				Stat stat = zkc.exists(jobPath, watcher);

				if (stat == null) {
					jobList = new LinkedList<String>();
					jobList.add(fromClient);

					Code ret = zkc.create(jobPath, // Path of znode
							jobList.toString(), // Data not needed.
							CreateMode.PERSISTENT // Znode type, set tt //
													// EPHEMERAL.
							);

					if (ret == Code.OK) {
						System.out.println("Jobs node created");
					}
					System.out.println(jobList.toString());
				} else {
					
					try {
						listHolder = new String(zk.getData(jobPath, false, null));
						jobList = deserializeList(listHolder);
						jobList.add(fromClient);
						System.out.println(jobList.toString());
						zk.setData(jobPath, jobList.toString().getBytes(), -1);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} 
					//System.out.println("Jobs node already here");
				}

			}
		}
	}

	private LinkedList<String> deserializeList(String string) {
		String[] hashes;
		string = string.substring(1, string.length() - 1);
		LinkedList<String> ret = new LinkedList<String>();
		hashes = string.split(", ");
		for (int i = 0; i < hashes.length; i++) {
			ret.add(hashes[i]);
		}
		return ret;
	}
}
