package View;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import javax.media.j3d.View;

public class ServerView {
	
	private final String dELIMITER_STRINGString = "_";
	
	public static HashMap<String,String> serverView;
	
	public ServerView()
	{
		serverView=new HashMap<String,String>();
	}
	public HashMap<String,String> getServerView()
	{
		return serverView;
	}
	public void setServerView(HashMap<String,String> view)
	{
		serverView=view;
	}
	
//	for debugging purposes
	private void iterateHashMap(HashMap<String, String> hMap){
		
		for(String key:hMap.keySet()){
			System.out.println(key + " " + hMap.get(key));
		}
	}
	
//	Merges two views based on latest timestamp policy
	public HashMap<String, String> mergeViews(HashMap<String, String> mapA, HashMap<String, String> mapB) {
		
		HashMap<String, String> mapM = new HashMap<String, String>();
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
