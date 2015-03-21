package View;

import java.util.HashMap;

public class ServerView {

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
	
}
