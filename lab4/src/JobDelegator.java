
public class JobDelegator extends Thread {
	
	@Override
	public void run(){
		while(true){
			JobTracker.delegateJob();
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

}
