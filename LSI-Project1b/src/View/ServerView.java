package View;

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
