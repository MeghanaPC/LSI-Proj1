package RPC;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import Project1a.SessionState;

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

	//
	// SessionReadClient(sessionID)
	// sending to multiple destAddrs, all at port = portProj1bRPC
	// creating new DatagramSocket object rpcSocket
	// and closing it when done
	//
	public SessionState SessionReadClient(List<String> destIP,
			SessionState sessionObj) throws Exception {
		// format to send = callID, opcode,sessionID
		boolean flag = true;
		DatagramSocket rpcSocket = new DatagramSocket();
		rpcSocket.setSoTimeout(timeOut);
		callID = callID + 1;
		String dataToSend = callID + DELIMITER + OPCODE_READ + DELIMITER
				+ sessionObj.getSessionID();
		byte[] outBuf = new byte[maxPacketSize];
		outBuf = dataToSend.getBytes();
		// DatagramPacket sendPkt = new DatagramPacket(outBuf, outBuf.length,
		// IP, portProj1bRPC);
		// fill outBuf with [ callID, operationSESSIONREAD, sessionID ]

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
					// received
					// format=callID,sessionID,version,message,timestamp
					String[] output = receivedString.split(DELIMITER);
					if (checkCallIDVersion(Integer.parseInt(output[2].trim()),
							sessionObj.getSessionVersion(),
							Integer.parseInt(output[0].trim()), callID)) {
						flag = false;

						// only when same version number and callID is matched
						SessionState returnObj = new SessionState();
						returnObj.setExpirationTimeStamp(Long
								.parseLong(output[4].trim()));
						returnObj.setSessionID(output[1].trim());
						returnObj.setSessionMessage(output[3].trim());
						returnObj.setSessionVersion(Integer.parseInt(output[2]
								.trim()));

						return returnObj;
					}
				}

			} while (true);
		} catch (SocketTimeoutException stoe) {
			// timeout
			return null;
		} catch (IOException ioe) {
			// other error
			return null;
		} finally {
			rpcSocket.close();
		}
	}

	private boolean checkCallIDVersion(int recVersion, int currVersion,
			int recCallID, int currCallID) {
		if (recVersion != currVersion)
			return false;
		if (recCallID != currCallID)
			return false;

		return true;
	}

	public List<String> SessionWriteClient(List<String> destIP,
			SessionState sessionObj) throws Exception {
		List<String> backups = new ArrayList<String>();
		DatagramSocket rpcSocket = new DatagramSocket();
		try {
			rpcSocket.setSoTimeout(timeOut);
			callID = callID + 1;
			String dataToSend = callID + DELIMITER + OPCODE_WRITE + DELIMITER
					+ sessionObj.getSessionID() + DELIMITER
					+ sessionObj.getSessionMessage() + DELIMITER
					+ sessionObj.getSessionVersion() + DELIMITER
					+ sessionObj.getExpirationTimeStamp();

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

					if (output[1].trim().equals(ack)) {
						k_ack = k_ack + 1;
						InetAddress returnAddr = recvPkt.getAddress();
						String[] temp = returnAddr.toString().split("/");
						backups.add(temp[1].trim());
					}
				}
			} while (k_ack != destIP.size());

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

	public HashMap<String, String> ExchangeViewClient(String dest,
			HashMap<String, String> view) throws Exception {
		DatagramSocket rpcSocket = new DatagramSocket();
		rpcSocket.setSoTimeout(timeOut);
		try {
			callID = callID + 1;
			InetAddress IP = InetAddress.getByName(dest);
			String viewString = null;
			for (Entry<String, String> e : view.entrySet()) {
				viewString += e.getKey() + "_" + e.getValue() + "-";
			}
			viewString = viewString.substring(0, viewString.length() - 1);
			String dataToSend = callID + DELIMITER + OPCODE_VIEW + DELIMITER
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
				receivedString = new String(inBuf).split(DELIMITER);
				if (Integer.parseInt(receivedString[0].trim()) == callID) {
					String ResultviewString = receivedString[1].trim();
					HashMap<String, String> resultview = new HashMap<String, String>();
					String[] tuples = ResultviewString.split("-");
					for (String s : tuples) {
						String[] keyValue = s.split("_");
						resultview.put(keyValue[0].trim(), keyValue[1].trim()
								+ "_" + keyValue[2].trim());
					}
					return resultview;
				}

			} while (Integer.parseInt(receivedString[0].trim()) != callID);
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
