package lsi;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;

public class GeneralUtils {

	/*
	 * Returns the IP address for a server(non AWS version)
	 */
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
	
	/*
	 * Returns the IP address of a server (works with AWS)
	 */
	public static String fetchAWSIP(){
	
		System.out.println("in fetch AWS IP");
		
		StringBuilder resultBuilder = new StringBuilder();
		String commandString ="/opt/aws/bin/ec2-metadata --public-ipv4";
		try {
			Process ipProcess = Runtime.getRuntime().exec(commandString);
			ipProcess.waitFor();
	 
			BufferedReader reader = new BufferedReader(new InputStreamReader(ipProcess.getInputStream()));
	 
			String outputString = "";			
	    
			while ((outputString = reader.readLine())!= null) {
				resultBuilder.append(outputString);
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("Result without parsing " + resultBuilder.toString());
		String parseResult = resultBuilder.toString();
		String[] resultArray = parseResult.split(" ");
		parseResult = resultArray[1];
		System.out.println("Returning AWS IP " + parseResult.trim());
		return parseResult.trim();
	}
	
}
