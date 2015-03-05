import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;


public class Connection {	
	public Socket client;
	public ObjectOutputStream toClient;
	public ObjectInputStream fromClient;
}
