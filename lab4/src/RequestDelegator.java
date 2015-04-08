
public class RequestDelegator extends Thread{

	
	@Override
	public void run(){
		while(true){
			FileServer.delegateRequest();
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
