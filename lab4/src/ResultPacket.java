import java.io.Serializable;


public class ResultPacket implements Serializable {
	String password;

	public ResultPacket(String password) {
		super();
		this.password = password;
	}
}
