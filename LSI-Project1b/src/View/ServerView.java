package View;

import java.util.concurrent.ConcurrentHashMap;

/*
 * Definition of a server view table object along with setters and getters
 */
public class ServerView {
	
	
	public static ConcurrentHashMap<String,String> serverView;
	
	public ServerView()
	{
		serverView=new ConcurrentHashMap<String,String>();
		System.out.println("Inside - servewView constructor");
	}
	public ConcurrentHashMap<String,String> getServerView()
	{
		System.out.println("ServerView getter");
		return serverView;
	}
	public void setServerView(ConcurrentHashMap<String,String> view)
	{
		serverView=view;
		System.out.println("ServerView setter");
	}
}
