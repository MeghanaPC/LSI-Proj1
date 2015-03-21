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
import java.util.Map.Entry;

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
			  boolean flag=true;
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
				  do{
						  recvPkt.setLength(inBuf.length);
						  rpcSocket.receive(recvPkt);
						  String receivedString=new String(inBuf);
						  if(receivedString!=null)
						  {
							  // received format=callID,sessionID,version,message,timestamp
							  String[] output=receivedString.split(DELIMITER);
							  if(checkCallIDVersion(output[2],callID))
							  {
								  flag=false;
							  
								  //only when same version number and callID is matched
								  SessionState returnObj=new SessionState();
								  returnObj.setExpirationTimeStamp(Long.parseLong(output[4]));
								  returnObj.setSessionID(output[1]);
								  returnObj.setSessionMessage(output[3]);
								  returnObj.setSessionVersion(Integer.parseInt(output[2]));
								  
								  return returnObj;
							  }
						  }
							  
			    } while(true);
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
			return null;
		}
		private boolean checkCallIDVersion(String buff,int callID)
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
					  do{
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
					  }while(k_ack!=destIP.size());
					  
			  }
			  catch(SocketTimeoutException stoe) {
			    // timeout 
				  stoe.printStackTrace();
				  return backups;
			  } catch(IOException ioe) {
			    // other error 
				  ioe.printStackTrace();
				  return backups;
			  }
			  finally
			  {
				  rpcSocket.close();
			  }
			return backups;
			  
		}
		public HashMap<String,String> ExchangeViewClient(String dest,HashMap<String,String> view) throws IOException
		{
			DatagramSocket rpcSocket = new DatagramSocket();
			rpcSocket.setSoTimeout(timeOut);
			callID=callID+1;
			InetAddress IP = InetAddress.getByName(dest);
			String viewString=null;
			for(Entry<String, String> e:view.entrySet())
			{
				viewString+=e.getKey()+"_"+e.getValue()+"-";
			}
			viewString=viewString.substring(0, viewString.length()-1);  
			String dataToSend=callID+DELIMITER+viewString;
			
			 byte[] outBuf = new byte[maxPacketSize];
			 outBuf=dataToSend.getBytes();
			 DatagramPacket sendPkt = new DatagramPacket(outBuf, outBuf.length,IP, portProj1bRPC);
			 rpcSocket.send(sendPkt);
			  
			 byte [] inBuf = new byte[maxPacketSize];
			 DatagramPacket recvPkt = new DatagramPacket(inBuf, inBuf.length);
			 try
			 {
				 //format = CallID, viewString
				 String[] receivedString=null;
				do
				{
					recvPkt.setLength(inBuf.length);
					rpcSocket.receive(recvPkt);
					receivedString=new String(inBuf).split(DELIMITER);
					if(Integer.parseInt(receivedString[0])==callID)
					{
						String ResultviewString=receivedString[1];
						HashMap<String,String> resultview=new HashMap<String,String>();
						String[] tuples=ResultviewString.split("-");
						for(String s:tuples)
						{
							String[] keyValue=s.split("_");
							resultview.put(keyValue[0], keyValue[1]+"_"+keyValue[2]);
						}
						return resultview;
					}
					
				 
				}while(Integer.parseInt(receivedString[0])!=callID);
				return null;
			 }
			 catch(SocketTimeoutException stoe) 
			 {
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
		

}
