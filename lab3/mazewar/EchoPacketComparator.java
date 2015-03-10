import java.util.Comparator;


public class EchoPacketComparator implements Comparator<EchoPacket>{

	@Override
	public int compare(EchoPacket arg0, EchoPacket arg1) {
		if(((EchoPacket)arg0).packet_id < ((EchoPacket)arg1).packet_id){
			return -1;
		}
		
		if(((EchoPacket)arg0).packet_id > ((EchoPacket)arg1).packet_id){
			return 1;
		}
		
		return 0;
	}

}
