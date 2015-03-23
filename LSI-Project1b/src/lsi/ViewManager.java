package lsi;

import java.util.List;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import Project1a.*;
import View.ServerView;


public class ViewManager {
	
	private static final String DELIMITER_LEVEL1= "@";
	private static final String DELIMITER_LEVEL2 = "#";
	private static final String upState="UP";
	private static final String downState="DOWN";

	public static ConcurrentHashMap<String, String> stringToHashMap(String toBeParsed){
		
		ConcurrentHashMap<String, String> resultMap = new ConcurrentHashMap<String, String>();
		String[] tuples = toBeParsed.split(DELIMITER_LEVEL1);
		
		for(String entry:tuples){
			String[] parseEntry = entry.split(DELIMITER_LEVEL2);
			resultMap.put(parseEntry[0].trim(), parseEntry[1].trim()+DELIMITER_LEVEL2+parseEntry[2].trim());
		}
		
		return resultMap;
	}
	
	public static String hashMapToString(ConcurrentHashMap<String, String> map){
		
		StringBuilder resultBuilder = new StringBuilder();
		Set<String> resultKeySet = map.keySet();
		int length = 0;
		
		for(String key:resultKeySet){
			resultBuilder.append(key+DELIMITER_LEVEL2+map.get(key)+DELIMITER_LEVEL1);
		}
		
		length = resultBuilder.length();
		return resultBuilder.toString().substring(0, length-1);
	}
	
	/*public static void iterateHashMap(ConcurrentHashMap<String, String> hMap){
		
		for(String key:hMap.keySet()){
			System.out.println(key + " " + hMap.get(key));
		}
	}*/
	
	public static ConcurrentHashMap<String, String> mergeViews(ConcurrentHashMap<String, String> mapA, ConcurrentHashMap<String, String> mapB) {
		
		ConcurrentHashMap<String, String> mapM = new ConcurrentHashMap<String, String>();
		Set<String> keysetA = mapA.keySet();
		Set<String> keysetB = mapB.keySet();
		
		for(String keyA:keysetA){
			if (!mapB.containsKey(keyA)) {
				mapM.put(keyA, mapA.get(keyA));
			}
			else {
				String parseValueA = mapA.get(keyA);
				String parseValueB = mapB.get(keyA);
				
				String[] splitValueA = parseValueA.split(DELIMITER_LEVEL2);
				String[] splitValueB = parseValueB.split(DELIMITER_LEVEL2);
				
				if (Long.parseLong(splitValueA[1].trim()) >= Long.parseLong(splitValueB[1].trim())) {
					mapM.put(keyA, parseValueA);
				}
				else {
					mapM.put(keyA, parseValueB);
				}
				
			}
		}
	
		for(String keyB:keysetB){
			if (!mapA.containsKey(keyB)) {
				mapM.put(keyB, mapB.get(keyB));
			}
		}	
		return mapM;
	}
	public static void UpdateView(List<String> serverID,String state)
	{
		//update self
		ServerView.serverView.put(EnterServlet.serverID.toString(),upState+DELIMITER_LEVEL2+System.currentTimeMillis());
		
		if(state.equals(upState)||state.equals(downState))  //just checking currect state is passed
		{
			synchronized(View.ServerView.serverView)
			{
				for(String svr:serverID)
				{
					String newvalue=state+DELIMITER_LEVEL2+System.currentTimeMillis();
					View.ServerView.serverView.put(svr,newvalue);
				}
			}
		}
	}
	
	public static ConcurrentHashMap<String, String>  getActiveServersList(ConcurrentHashMap<String, String> map){
		//get all active servers except yourself
		ConcurrentHashMap<String, String>  resultMap = new ConcurrentHashMap<String,String>(map);
		for(String server:map.keySet()){
			String tupleString = map.get(server);
			String[] parseTuple = tupleString.split(DELIMITER_LEVEL2);
			if (parseTuple[0].equals(downState)) {
				resultMap.remove(server);
			}
		}
		resultMap.remove(EnterServlet.serverID.toString());  //remove self

		return resultMap;
	}
public static void mergeViewWithSelf(ConcurrentHashMap<String, String> newMerged) {
		
		synchronized(View.ServerView.serverView)
		{
			View.ServerView.serverView.clear();
			View.ServerView.serverView.putAll(newMerged);
		}
		
	}
	
//	For testing purposes
	/*public static void main(String[] args) {
		
		ConcurrentHashMap<String, String> demoMap = stringToHashMap("server1_up_123456-server2_down_345678");
//		iterateHashMap(demoMap);
		
		System.out.println(hashMapToString(demoMap));
	}*/
	
	 
}
