package RPC;

import java.io.IOException;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.UUID;

import Project1a.*;

public class RPCClient {
	private static final int maxPacketSize = 512;
	private static final int portProj1bRPC = 5300;
	private static int callID = 0;
	private static final int timeOut = 10000;
	private static String DELIMITER = "=";
	private static int OPCODE_READ = 1;
	private static int OPCODE_WRITE = 2;
	private static int OPCODE_VIEW = 3;
	private static String ack = "SUCCESS";
	private static final String DELIMITER_LEVEL1= "@";
	private static final String DELIMITER_LEVEL2 = "#";
	//private static final String DELIMITER_SessionID = "_";
	private static final String upState="UP";
	private static final String downState="DOWN";
	

	//
	// SessionReadClient(sessionID)
	// sending to multiple destAddrs, all at port = portProj1bRPC
	// creating new DatagramSocket object rpcSocket
	// and closing it when done
	public static int getCallID()
	{
		callID=callID+1;
		return callID;
	}
	
	//sessionObj should contain stuff from Cookie
	public static SessionInfo SessionReadClient(List<String> destIP,
			String SessionID,SessionInfo sessionObj) throws Exception {
		// format to send = callID, opcode,sessionID
		boolean flag = true;
		DatagramSocket rpcSocket = new DatagramSocket();
		rpcSocket.setSoTimeout(timeOut);
		int callIDLocal=getCallID();
		//UUID callID = UUID.randomUUID();
		
		String dataToSend = callIDLocal + DELIMITER + OPCODE_READ + DELIMITER+ SessionID;
		byte[] outBuf = new byte[maxPacketSize];
		outBuf = dataToSend.getBytes();
		

		for (String host : destIP) {
			InetAddress IP = InetAddress.getByName(host);
			DatagramPacket sendPkt = new DatagramPacket(outBuf, outBuf.length,
					IP, portProj1bRPC);
			rpcSocket.send(sendPkt);
		}
		byte[] inBuf = new byte[maxPacketSize];
		DatagramPacket recvPkt = new DatagramPacket(inBuf, inBuf.length);
		try {
			do {
				recvPkt.setLength(inBuf.length);
				rpcSocket.receive(recvPkt);
				
				
				String receivedString = new String(inBuf);
				
				if (receivedString != null) {
					
					//updating entry of the responded to UP, even if it's version or callID doesn't match
					InetAddress returnAddr = recvPkt.getAddress();
					String[] temp = returnAddr.toString().trim().split("/"); 
					List<String> serverIDlist=new ArrayList<String>();
					serverIDlist.add(temp[1].trim());
					lsi.ViewManager.UpdateView(serverIDlist,upState);
					
					
					// received format=callID,version,message,timestamp
					String[] output = receivedString.split(DELIMITER);
					
					if (checkCallIDVersion(Integer.parseInt(output[1].trim()),sessionObj.getVersion(),Integer.parseInt(output[0].trim()), callIDLocal)) {
						flag = false;

						// only when same version number and callID is matched
						SessionInfo returnObj = new SessionInfo();
						returnObj.setVersion(Integer.parseInt(output[1].trim()));
						returnObj.setMessage(output[2].trim());
						returnObj.setExpirationTime(Long.parseLong(output[3].trim()));
							
						return returnObj;
					}
				}

			} while (flag==true);
		} catch (SocketTimeoutException stoe) {
			// timeout
			//set all to down
			lsi.ViewManager.UpdateView(destIP,downState);
			return null;
		} catch (IOException ioe) {
			// other error
			return null;
		} finally {
			rpcSocket.close();
		}
		return null;
	}

	private static boolean checkCallIDVersion(int recVersion, int currVersion,
			int recCallID, int currCallID) {
		if (recVersion != currVersion)
			return false;
		if (recCallID!=currCallID)
			return false;

		return true;
	}

	public static List<String> SessionWriteClient(List<String> destIP,String sessionID,SessionInfo sessionObj) throws Exception {
		List<String> backups = new ArrayList<String>();
		DatagramSocket rpcSocket = new DatagramSocket();
		try {
			rpcSocket.setSoTimeout(timeOut);
			//UUID callID = UUID.randomUUID();
			int callIDLocal=getCallID();
			String dataToSend = callIDLocal + DELIMITER + OPCODE_WRITE + DELIMITER
					+ sessionID + DELIMITER
					+ sessionObj.getMessage()+ DELIMITER
					+ sessionObj.getVersion() + DELIMITER
					+ sessionObj.getExpirationTime();

			byte[] outBuf = new byte[maxPacketSize];
			outBuf = dataToSend.getBytes();

			for (String host : destIP) {
				InetAddress IP = InetAddress.getByName(host);
				DatagramPacket sendPkt = new DatagramPacket(outBuf,
						outBuf.length, IP, portProj1bRPC);
				rpcSocket.send(sendPkt);
			}
			byte[] inBuf = new byte[maxPacketSize];
			DatagramPacket recvPkt = new DatagramPacket(inBuf, inBuf.length);

			int k_ack = 0;
			do {
				recvPkt.setLength(inBuf.length);
				rpcSocket.receive(recvPkt);
				String receivedString = new String(inBuf);
				if (receivedString != null) {
					// format = CallID , ack
					String[] output = receivedString.split(DELIMITER);
					if(Integer.parseInt(output[0].trim())==callIDLocal)
					{
						if (output[1].trim().equals(ack)) {
							k_ack = k_ack + 1;
							InetAddress returnAddr = recvPkt.getAddress();
							String[] temp = returnAddr.toString().trim().split("/");   //needs to be checked
							backups.add(temp[1].trim());
						}
					}
				}
			} while (k_ack != destIP.size());
			//set servers to up / down
			List<String> downservers=new ArrayList<String>();
			for(String svr:destIP)
			{
				if(!backups.contains(svr))
				{
					downservers.add(svr);
				}
			}
			lsi.ViewManager.UpdateView(backups, upState);
			lsi.ViewManager.UpdateView(downservers, downState);
			
		} catch (SocketTimeoutException stoe) {
			// timeout
			stoe.printStackTrace();
			return backups;
		} catch (IOException ioe) {
			// other error
			ioe.printStackTrace();
			return backups;
		} finally {
			rpcSocket.close();
		}
		return backups;

	}

	public static ConcurrentHashMap<String, String> ExchangeViewClient(String dest,
			ConcurrentHashMap<String, String> view) throws Exception {
		DatagramSocket rpcSocket = new DatagramSocket();
		rpcSocket.setSoTimeout(timeOut);
		try {
			//UUID callID = UUID.randomUUID();
			int callIDLocal=getCallID();
			InetAddress IP = InetAddress.getByName(dest);
			String viewString = null;
			viewString=lsi.ViewManager.hashMapToString(view);
			String dataToSend = callIDLocal + DELIMITER + OPCODE_VIEW + DELIMITER
					+ viewString;

			byte[] outBuf = new byte[maxPacketSize];
			outBuf = dataToSend.getBytes();
			DatagramPacket sendPkt = new DatagramPacket(outBuf, outBuf.length,
					IP, portProj1bRPC);
			rpcSocket.send(sendPkt);

			byte[] inBuf = new byte[maxPacketSize];
			DatagramPacket recvPkt = new DatagramPacket(inBuf, inBuf.length);

			// format = CallID, viewString
			
			String[] receivedString = null;
			do {
				recvPkt.setLength(inBuf.length);
				rpcSocket.receive(recvPkt);
				
				String received = new String(inBuf);
				if (received != null) {
					
				
					//updating responded to upstate
					InetAddress returnAddr = recvPkt.getAddress();
					String[] temp = returnAddr.toString().trim().split("/"); 
					List<String> serverIDlist=new ArrayList<String>();
					serverIDlist.add(temp[1].trim());
					lsi.ViewManager.UpdateView(serverIDlist,upState);
					
					
					receivedString = received.split(DELIMITER);
					if (Integer.parseInt(receivedString[0].trim())==callIDLocal) {
						String ResultviewString = receivedString[1].trim();
						ConcurrentHashMap<String, String> resultview = new ConcurrentHashMap<String, String>();
						
						resultview=lsi.ViewManager.stringToHashMap(ResultviewString);
						
						return resultview;
					}
				}

			} while (Integer.parseInt(receivedString[0].trim())!=callIDLocal);
			return null;
		} catch (SocketTimeoutException stoe) {
			// timeout
			stoe.printStackTrace();
			return null;
		} catch (IOException ioe) {
			// other error
			ioe.printStackTrace();
			return null;
		} finally {
			rpcSocket.close();
		}

	}

}
