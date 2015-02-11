import java.io.Serializable;

public class EchoPacket implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/* define packet formats */
	public static final int ECHO_NULL    = 0;
	public static final int ECHO_REQUEST = 100;
	public static final int ECHO_MISSING = 101;
	public static final int ECHO_NEW = 102;
	public static final int ECHO_REPLY   = 200;
	public static final int ECHO_BYE     = 300;
	public static final int SERVER_LOC   = 301;
	
	/* the packet payload */
	/* initialized to be a null packet */
	public int type = ECHO_NULL;
	
	public static final int UP = 1;	
	public static final int DOWN = 2;
	public static final int RIGHT = 3;
	public static final int LEFT = 4;
	public static final int FIRE = 5;
	public static final int CONN = 6;
	public static final int DIS = 7;
	public static final int TICK = 8;
	

	int event;
	int x,y;
	String player;
	
	int packet_id;
	
	/* send your message here */
	public String message;
	
}
