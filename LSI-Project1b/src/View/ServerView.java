package View;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;


public class ServerView {
	
	
	public static ConcurrentHashMap<String,String> serverView;
	
	public ServerView()
	{
		serverView=new ConcurrentHashMap<String,String>();
	}
	public ConcurrentHashMap<String,String> getServerView()
	{
		return serverView;
	}
	public void setServerView(ConcurrentHashMap<String,String> view)
	{
		serverView=view;
	}
	
//	for debugging purposes
	public void iterateHashMap(ConcurrentHashMap<String, String> hMap){
		
		for(String key:hMap.keySet()){
			System.out.println(key + " " + hMap.get(key));
		}
	}
	
//	Merges two views based on latest timestamp policy 
	public ConcurrentHashMap<String, String> mergeViews(ConcurrentHashMap<String, String> mapA, ConcurrentHashMap<String, String> mapB) {
		
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
				
				String[] splitValueA = parseValueA.split("_");
				String[] splitValueB = parseValueB.split("_");
				
				if (Long.parseLong(splitValueA[1]) >= Long.parseLong(splitValueB[1])) {
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
	
//	Tested and working
	/*
	public static void main(String[] args){
		
		HashMap<String,String> mapA = new HashMap<String, String>();
		HashMap<String,String> mapB = new HashMap<String, String>();
		
		long currentTime = System.currentTimeMillis();
		mapA.put("abds2334", "up_" + String.valueOf(currentTime));
		currentTime += 300;
		mapB.put("abcs2334", "up_" + String.valueOf(currentTime));
		
		ServerView sView = new ServerView();
		sView.iterateHashMap(mapA);
		sView.iterateHashMap(mapB);

		sView.iterateHashMap(sView.mergeViews(mapA, mapB));
		
	}
	*/

}
