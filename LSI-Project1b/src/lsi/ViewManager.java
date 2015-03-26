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

	/*
	 * Converts a delimited string into a hashmap
	 */
	public static ConcurrentHashMap<String, String> stringToHashMap(String toBeParsed){
		
		ConcurrentHashMap<String, String> resultMap = new ConcurrentHashMap<String, String>();
		String[] tuples = toBeParsed.split(DELIMITER_LEVEL1);
		
		for(String entry:tuples){
			String[] parseEntry = entry.split(DELIMITER_LEVEL2);
			resultMap.put(parseEntry[0].trim(), parseEntry[1].trim()+DELIMITER_LEVEL2+parseEntry[2].trim());
		}
		
		return resultMap;
	}
	
	/*
	 * Converts a hashmap into a delimited string
	 */
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
	
	/*
	 * Merges two views by adding unique entries from both maps and removing older overlapping entries
	 */
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
	
	/*
	 * Updates view table entry to reflect new expiry time
	 */
	public static void UpdateView(List<String> serverID,String state)
	{
		//update self
		String[] arr = EnterServlet.serverID.toString().split("/");
		String serverIdString = arr[arr.length-1];
		ServerView.serverView.put(serverIdString,upState+DELIMITER_LEVEL2+System.currentTimeMillis());
		
		if(state.equals(upState)||state.equals(downState))  //just checking current state is passed
		{
			synchronized(View.ServerView.serverView)
			{
				for(String svr:serverID)
				{
					String newvalue=state+DELIMITER_LEVEL2+System.currentTimeMillis();
					String[] strArr = svr.split("/");
					String svrString = strArr[strArr.length-1];
					View.ServerView.serverView.put(svrString,newvalue);
				}
			}
		}
	}
	
	/*
	 * returns a list of all servers that are currently active (except for the calling server)
	 */
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
		String[] arr = EnterServlet.serverID.toString().split("/");
		String svrString = arr[arr.length-1];
		resultMap.remove(svrString);  //remove self

		return resultMap;
	}
	
	/*
	 * merge a view with the current view
	 */
	public static void mergeViewWithSelf(ConcurrentHashMap<String, String> newMerged) {
		
		synchronized(View.ServerView.serverView)
		{
			View.ServerView.serverView.clear();
			View.ServerView.serverView.putAll(newMerged);
		}
		
	}
		 
}
