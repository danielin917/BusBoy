import org.zeromq.ZMQ;
import org.zeromq.ZMQ.Context;

public class tester {
	public static void main(String [ ] args) {
 		//System.out.println("hiii");
 		ZMQ.Context context = ZMQ.context(1);
 		ZMQ.Socket socket = context.socket(ZMQ.SUB);
 		socket.connect("tcp://127.0.0.1:10000");

 	}
}