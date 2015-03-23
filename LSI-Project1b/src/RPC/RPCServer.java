package RPC;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import Project1a.*;

public class RPCServer implements Runnable {

	private static final int portProj1bRPC = 5300;
	private static final int maxPacketSize = 512;
	private static String DELIMITER = "=";
	private static int OPCODE_READ = 1;
	private static int OPCODE_WRITE = 2;
	private static int OPCODE_VIEW = 3;
	private static String ack = "SUCCESS";
	private static final String upState="UP";
	private static final String downState="DOWN";
	// private static final int timeOut=10000;

	DatagramSocket rpcSocket;

	public RPCServer() throws SocketException {
		rpcSocket = new DatagramSocket(portProj1bRPC);
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		while (true) {
			byte[] inBuf = new byte[maxPacketSize];
			DatagramPacket recvPkt = new DatagramPacket(inBuf, inBuf.length);
			try {
				rpcSocket.receive(recvPkt);

				InetAddress returnAddr = recvPkt.getAddress();
				int returnPort = recvPkt.getPort();
				// here inBuf contains the callID and operationCode

				byte[] outBuf = new byte[512];
				String receivedString=new String(inBuf);
				if(receivedString!=null)
				{
					String result = generateReply(receivedString);
					
					
					//updating entry of the responded to UP of the server from which it is received
					String[] temp = returnAddr.toString().trim().split("/"); 
					List<String> serverIDlist=new ArrayList<String>();
					serverIDlist.add(temp[1].trim());
					lsi.ViewManager.UpdateView(serverIDlist,upState);
					
					outBuf = result.getBytes();
	
					// here outBuf should contain the callID and results of the call
					DatagramPacket sendPkt = new DatagramPacket(outBuf,
							outBuf.length, returnAddr, returnPort);
					rpcSocket.send(sendPkt);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}

	public String generateReply(String data) throws Exception {

		String result = "";
		String[] receivedData = data.split(DELIMITER);
		result += receivedData[0].trim() + DELIMITER;
		int operationCode = Integer.parseInt(receivedData[1].trim());
		if (operationCode == OPCODE_READ) {
			// received format=callID,opcode,sessionID
			SessionInfo sessionObj = SessionTable.sessionMap.get(receivedData[2].trim());
			String sessionString = "";
			if (sessionObj != null) {
				// sending format=callID,version,message,timestamp
				sessionString = sessionObj.getVersion() + DELIMITER
						+ sessionObj.getMessage()+ DELIMITER
						+ sessionObj.getExpirationTime();
			} else {
				sessionString = -1 + DELIMITER + -1
						+ DELIMITER + -1;
			}
			result += sessionString;

		} else if (operationCode == OPCODE_WRITE) {
			// received format=callID,opcode,sessionID,message,version,timestamp
			SessionInfo object = new SessionInfo();
			object.setMessage(receivedData[3].trim());
			object.setVersion(Integer.parseInt(receivedData[4].trim()));
			object.setExpirationTime(Long.parseLong(receivedData[5].trim()));

			// sending format = callID, ack
			SessionTable.sessionMap.put(receivedData[2].trim(), object);
			result += ack;

		} else if (operationCode == OPCODE_VIEW) {
			// received format = callID,opcode, stringOfTuples
			ConcurrentHashMap<String, String> Myview = new ConcurrentHashMap<String, String>(View.ServerView.serverView);
			ConcurrentHashMap<String, String> receivedview = new ConcurrentHashMap<String, String>();
			receivedview = lsi.ViewManager.stringToHashMap(receivedData[2].trim());
			
			ConcurrentHashMap<String, String> mergedView = new ConcurrentHashMap<String, String>();
			mergedView = lsi.ViewManager.mergeViews(Myview, receivedview);
			String sendView = lsi.ViewManager.hashMapToString(Myview);
			// sending format=callID , viewstring
			result += sendView;
			lsi.ViewManager.mergeViewWithSelf(mergedView);

		}
		return result;

	}

}
