package RPC;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;


public class RPCServer implements Runnable{

		private static final int portProj1bRPC   =   5300;
		private static final int maxPacketSize=512;
		//private static final int timeOut=10000;
	
	  DatagramSocket rpcSocket; 
	  public RPCServer() throws SocketException
	  {
		  rpcSocket=new DatagramSocket(portProj1bRPC);
	  }
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		  while(true) {
		    byte[] inBuf = new byte[maxPacketSize];
		    DatagramPacket recvPkt = new DatagramPacket(inBuf, inBuf.length);
		    try {
				rpcSocket.receive(recvPkt);
			
			    InetAddress returnAddr = recvPkt.getAddress();
			    int returnPort = recvPkt.getPort();
			    // here inBuf contains the callID and operationCode
			  
			    byte[] outBuf = new byte[512];
			    String result=generateReply(new String(recvPkt.getData()));
			    outBuf=result.getBytes();
			    
			    // here outBuf should contain the callID and results of the call
			    DatagramPacket sendPkt = new DatagramPacket(outBuf, outBuf.length,returnAddr, returnPort);
				rpcSocket.send(sendPkt);
			} catch (IOException e) {
				e.printStackTrace();
			}
		  }
		
	}
	public String generateReply(String data)
	{
		 /* int operationCode = ...; // get requested operationCode
		 switch( operationCode ) {
	    	...
	    	case operationSESSIONREAD:
	    		// SessionRead accepts call args and returns call results 
	    		outBuf = SessionRead(recvPkt.getData(), recvPkt.getLength());
	    		break;
	    	...
	    }
		
		 
		 //append with callID*/
		String result=null;
		return result;
		
	}
	
	
}
