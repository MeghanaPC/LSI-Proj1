package RPC;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import Project1a.SessionState;
public class RPCClient {
		private static final int maxPacketSize=512;
		private static final int portProj1bRPC   =   5300;
		private static int callID=0;
		private static final int timeOut=10000;
		private static String DELIMITER = "|";
		private static int OPCODE_READ = 1;
		private static int OPCODE_WRITE = 2;
		private static int OPCODE_VIEW = 3;
		private static String ack="SUCCESS";
	  //
	  // SessionReadClient(sessionID)
	  //   sending to multiple destAddrs, all at port = portProj1bRPC
	 //   creating new DatagramSocket object rpcSocket
	  //   and closing it when done
	  //
		public SessionState SessionReadClient(List<String> destIP,SessionState sessionObj) throws IOException
		{
			  
			  DatagramSocket rpcSocket = new DatagramSocket();
			  rpcSocket.setSoTimeout(timeOut);
			  callID = callID+1;
			  String dataToSend=callID+
								  DELIMITER+sessionObj.getSessionID()+
								  DELIMITER+OPCODE_READ;
			  byte[] outBuf = new byte[maxPacketSize];
			  outBuf=dataToSend.getBytes();
			 // DatagramPacket sendPkt = new DatagramPacket(outBuf, outBuf.length, IP, portProj1bRPC);
			 // fill outBuf with [ callID, operationSESSIONREAD, sessionID ]
			  
			  for(String host:destIP) {
				 InetAddress IP= InetAddress.getByName(host);
			     DatagramPacket sendPkt = new DatagramPacket(outBuf, outBuf.length,IP, portProj1bRPC);
			     rpcSocket.send(sendPkt);
			  }
			  byte [] inBuf = new byte[maxPacketSize];
			  DatagramPacket recvPkt = new DatagramPacket(inBuf, inBuf.length);
			  try {
					  for(int i=0;i<destIP.size();i++)
					  {
						  recvPkt.setLength(inBuf.length);
						  rpcSocket.receive(recvPkt);
						  String receivedString=new String(inBuf);
						  if(receivedString!=null)
						  {
							  // received format=callID,sessionID,version,message,timestamp
							  String[] output=receivedString.split(DELIMITER);
							  if(checkCallIDVersion(output[2],callID))
							  {
								  //only when same version number and callID is matched
								  SessionState returnObj=new SessionState();
								  returnObj.setExpirationTimeStamp(Date.parse(output[4]));
								  returnObj.setSessionID(output[1]);
								  returnObj.setSessionMessage(output[3]);
								  returnObj.setSessionVersion(Integer.parseInt(output[2]));
								  
								  return returnObj;
							  }
						  }
			    } 
			  } catch(SocketTimeoutException stoe) {
			    // timeout 
			    return null;
			  } catch(IOException ioe) {
			    // other error 
				  return null;
			  }
			  finally
			  {
				  rpcSocket.close();
			  }
		}
		private boolean checkCallIDVersion(String inBuf,int callID)
		{
			//need to implement
			return true;
		}
		public List<String> SessionWriteClient(List<String> destIP,SessionState sessionObj) throws IOException
		{
			List<String> backups=new ArrayList<String>();
			DatagramSocket rpcSocket = new DatagramSocket();
			rpcSocket.setSoTimeout(timeOut);
			  callID = callID+1;
			String dataToSend =callID + DELIMITER +
					OPCODE_WRITE + DELIMITER +
					sessionObj.getSessionID() + DELIMITER +
					sessionObj.getSessionMessage() + DELIMITER +
					sessionObj.getSessionVersion() + DELIMITER +
					sessionObj.getExpirationTimeStamp();
			
			
			 byte[] outBuf = new byte[maxPacketSize];
			  outBuf=dataToSend.getBytes();
			 
			  for(String host:destIP) {
				 InetAddress IP= InetAddress.getByName(host);
			     DatagramPacket sendPkt = new DatagramPacket(outBuf, outBuf.length,IP, portProj1bRPC);
			     rpcSocket.send(sendPkt);
			  }
			  byte [] inBuf = new byte[maxPacketSize];
			  DatagramPacket recvPkt = new DatagramPacket(inBuf, inBuf.length);
			  try {
				  
				  	  int k_ack=0;
					  for(int i=0;i<destIP.size();i++)
					  {
						  recvPkt.setLength(inBuf.length);
						  rpcSocket.receive(recvPkt);
						  String receivedString=new String(inBuf);
						  if(receivedString!=null)
						  {
							  //format = CallID , ack
							  String[] output=receivedString.split(DELIMITER);
							 
							  if(output[1].equals(ack))
							  {
								  k_ack=k_ack+1;
								  InetAddress returnAddr = recvPkt.getAddress();
								  String[] temp=returnAddr.toString().split("/");
								  backups.add(temp[1]);
							  } 
						  }
					  }
					  if(backups.size()!=0)
						  return backups;
					  else
						  return null;
			  }
			  catch(SocketTimeoutException stoe) {
			    // timeout 
				  stoe.printStackTrace();
				  return null;
			  } catch(IOException ioe) {
			    // other error 
				  ioe.printStackTrace();
				  return null;
			  }
			  finally
			  {
				  rpcSocket.close();
			  }
			  
		}
		public HashMap<String,String> ExchangeViewClient(String dest,HashMap<String,String> view) throws UnknownHostException
		{
			callID=callID+1;
			InetAddress IP = InetAddress.getByName(dest);
			String viewString=null;
			for(EntrySet)
			 DatagramSocket rpcSocket = new DatagramSocket();
				rpcSocket.setSoTimeout(timeOut);
				  callID = callID+1;
				String dataToSend =callID + DELIMITER +
			 
		}
		

}
