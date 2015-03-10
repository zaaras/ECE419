import java.io.Serializable;


public class serverClient implements Serializable {
	public serverClient() {

	}
	
	public serverClient(String player, int in_x, int in_y, Direction in_dir) {
		// TODO Auto-generated constructor stub
		name = player;
		x = in_x;
		y = in_y;
		dir = in_dir;
	}
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	int x,y;
	String name;
	Direction dir;
}
