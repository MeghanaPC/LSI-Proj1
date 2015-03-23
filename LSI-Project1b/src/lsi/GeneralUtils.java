package lsi;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;

public class GeneralUtils {

	public static String fetchIP(){
		
		String resultIP = null;
		try {
	        Enumeration<NetworkInterface> interfaceList = NetworkInterface.getNetworkInterfaces();
	        
	        while (interfaceList.hasMoreElements()) {
	            NetworkInterface interfaceItem = interfaceList.nextElement();
	            if (interfaceItem.isLoopback() || !interfaceItem.isUp())
	                continue;

	            Enumeration<InetAddress> addressList = interfaceItem.getInetAddresses();
	            
	            while(addressList.hasMoreElements()) {
	                InetAddress addr = addressList.nextElement();
	                //there are many interfaces with several IPs each - so just took the 1st one thats IPv4
	                if (addr instanceof Inet4Address) {
	                	resultIP = addr.getHostAddress();
//		                System.out.println(interfaceItem.getDisplayName() + " " + resultIP);
		                break;
					}
	                
	            }
	        }
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
		return resultIP;
	}
	
	public static void main(String[] args) {
		System.out.println(fetchIP());
	}
	
}
