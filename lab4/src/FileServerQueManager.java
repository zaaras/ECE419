import java.util.LinkedList;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.KeeperException.Code;
import org.apache.zookeeper.data.Stat;


public class FileServerQueManager extends Thread {
	
	private ZkConnector zkc;
	private ZooKeeper zk;
	public static String requestPath = "/Requests";
	private Watcher watcher;
	private LinkedList<String> requestList;
	private String listHolder ;


	
	public FileServerQueManager(String string) {
		
		// TODO Auto-generated constructor stub
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
		String item = null;
		while (true) {
			if(!FileServer.requestQue.isEmpty()){
				item = FileServer.requestQue.poll(); //item = n|s|e				

				Stat stat = zkc.exists(requestPath, watcher);

				if (stat == null) {
					requestList = new LinkedList<String>();
					requestList.add(item);

					Code ret = zkc.create(requestPath, // Path of znode
							requestList.toString(), // Data not needed.
							CreateMode.PERSISTENT // Znode type, set tt //
													// EPHEMERAL.
							);

					if (ret == Code.OK) {
						System.out.println("Jobs node created");
					}
					System.out.println(requestList.toString());
				} else {
					
					try {
						listHolder = new String(zk.getData(requestPath, false, null));
						requestList = JobTrackerQueManager.deserializeList(listHolder);
						requestList.add(item);
						System.out.println(requestList.toString());
						zk.setData(requestPath, requestList.toString().getBytes(), -1);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} 
					//System.out.println("Jobs node already here");
				}

			}
			
		}
		
	}
	

}
