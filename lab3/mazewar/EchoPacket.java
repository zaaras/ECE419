import java.io.Serializable;
import java.util.LinkedList;

public class EchoPacket implements Serializable {



	public EchoPacket(EchoPacket in) {
		super();
		this.type = in.type;
		this.event = in.event;
		this.playerCount = in.playerCount;
		this.x = in.x;
		this.y = in.y;
		this.missingIndex = in.missingIndex;
		this.victim = in.victim;
		this.dir = in.dir;
		this.player = in.player;
		this.killer = in.killer;
		this.leader = in.leader;
		this.missingPackOwner = in.missingPackOwner;
		this.packet_id = in.packet_id;
		this.message = in.message;
		
	}
	
	public EchoPacket() {
		super();
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/* define packet formats */
	// This goes into int type
	public static final int ECHO_NULL    = 0;
	public static final int ECHO_REQUEST = 100;
	public static final int ECHO_MISSING = 101;
	public static final int ECHO_NEW = 102;
	public static final int ECHO_REPLY   = 200;
	public static final int ECHO_BYE     = 300;
	public static final int SERVER_LOC   = 301;
	public static final int DISCO   = 302;
	public static final int LEADER   = 303;
	public static final int REQUEST_MISSING   = 305;
	public static final int RESPONSE_MISSING   = 306;

	
	/* the packet payload */
	/* initialized to be a null packet */
	// This goes into int event
	public int type = ECHO_NULL;
	
	public static final int UP = 1;	
	public static final int DOWN = 2;
	public static final int RIGHT = 3;
	public static final int LEFT = 4;
	public static final int FIRE = 5;
	public static final int CONN = 6;
	public static final int DIS = 7;
	public static final int TICK = 8;
	public static final int LOC = 9;
	public static final int KILL = 10;
	public static final int FREEZE   = 11;
	public static final int UNFREEZE   = 12;
	

	int event;
	int playerCount;
	int x,y;
	int missingIndex ;
	int response;
	Direction dir;
	String player;
	String killer;
	String victim;
	String leader;
	String missingPackOwner;
	
	int packet_id;
	
	/* send your message here */
	String message;
	
	LinkedList<serverClient> serverClients;

	@Override
	public String toString() {
		return "EchoPacket [response=" + response + ", player=" + player
				+ ", packet_id=" + packet_id + ", message=" + message + ", missingIndex=" + missingIndex + ", owner=" + missingPackOwner +"]";
	}
	
	
	
}
