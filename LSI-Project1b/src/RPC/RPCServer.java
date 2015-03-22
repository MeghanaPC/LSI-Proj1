package RPC;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.concurrent.ConcurrentHashMap;

import Project1a.SessionState;
import Project1a.ServletForSession;

public class RPCServer implements Runnable{

		private static final int portProj1bRPC   =   5300;
		private static final int maxPacketSize=512;
		private static String DELIMITER = "|";
		private static int OPCODE_READ = 1;
		private static int OPCODE_WRITE = 2;
		private static int OPCODE_VIEW = 3;
		private static String ack="SUCCESS";
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
		String result="";
		String[] receivedData=data.split(DELIMITER);
		result+=receivedData[0].trim()+DELIMITER;
		 int operationCode =Integer.parseInt(receivedData[1].trim());
		 if(operationCode==OPCODE_READ)
		 {
			 //received format=callID,opcode,sessionID
			 SessionState sessionObj=ServletForSession.sessiontable.get(receivedData[2].trim());
			 String sessionString="";
			 if(sessionObj!=null)
			 {
				// sending format=callID,sessionID,version,message,timestamp
				 sessionString=sessionObj.getSessionID()+DELIMITER+
						 sessionObj.getSessionVersion()+DELIMITER+
						 sessionObj.getSessionMessage()+DELIMITER+
						 sessionObj.getExpirationTimeStamp();
			 }
			 else
			 {
				 sessionString=-1 +DELIMITER+ -1  +DELIMITER+ -1 +DELIMITER+ -1;
			 }
			 result+=sessionString;
			 
		 }
		 else if(operationCode==OPCODE_WRITE)
		 {
			 //received format=callID,opcode,sessionID,message,version,timestamp
			 SessionState object=new SessionState();
			 object.setSessionID(receivedData[2].trim());
			 object.setSessionMessage(receivedData[3].trim());
			 object.setSessionVersion(Integer.parseInt(receivedData[4].trim()));
			 object.setExpirationTimeStamp(Long.parseLong(receivedData[5].trim()));
			 
			 //sending format = callID, ack
			 ServletForSession.sessiontable.put(object.getSessionID(), object);
			 result+=ack;
			 
			 
		 }
		 else if(operationCode==OPCODE_VIEW)
		 {
			 //received format = callID,opcode, stringOfTuples
			 ConcurrentHashMap Myview=new ConcurrentHashMap<String,String>();
			 Myview=View.ServerView.serverView;
			 ConcurrentHashMap receivedview=new ConcurrentHashMap<String,String>();
			 receivedview=lsi.extraUtils.stringToHashMap(receivedData[2].trim());
			 ConcurrentHashMap mergedView=new ConcurrentHashMap<String,String>();
			 lsi.extraUtils.mergeViews(Myview,receivedview);
			 String sendView=lsi.extraUtils.hashMapToString(Myview);
			 //sending format=callID , viewstring
			 result+=sendView;
			 
		 }
		 return result;
		 
	
		
	}
	
	
}
