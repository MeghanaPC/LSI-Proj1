package View;

import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import lsi.ViewManager;
import Project1a.*;

public class viewDaemon  implements Runnable{
	
	private static final String DELIMITER_LEVEL1= "@";
	private static final String DELIMITER_LEVEL2 = "#";
	private static final String upState="UP";
	private static final String downState="DOWN";
	//	Need to fix this to a value
	private final int GOSSIP_SECS = 10;
	
	@Override
	public void run(){
		ConcurrentHashMap<String,String> destView;
		Double numProbability = 0.0;
		Double randomProbability = 0.0;
		ConcurrentHashMap<String, String> activeServerViewMap= ViewManager.getActiveServersList(ServerView.serverView);
		while(true){
			try {
				numProbability = (double) (1/activeServerViewMap.size()+1);  //plus 1 coz self has been excluded but we need this list
				Random random = new Random();
				randomProbability = random.nextDouble();
				
				if (numProbability < randomProbability) {
					//updating self
					ServerView.serverView.put(EnterServlet.serverID.toString(),upState+DELIMITER_LEVEL2+System.currentTimeMillis());
					
					Set<String> serverSet=activeServerViewMap.keySet();
					java.util.Collections.shuffle((List<?>) serverSet);
					Iterator<String> serverIterator = serverSet.iterator();
					String chosenServer = (String) serverIterator.next();
					
					destView=new ConcurrentHashMap<String,String>(RPC.RPCClient.ExchangeViewClient(chosenServer, ServerView.serverView));
					ConcurrentHashMap<String,String> mergedView=new ConcurrentHashMap<String,String>(ViewManager.mergeViews(ServerView.serverView,destView));
					ViewManager.mergeViewWithSelf(mergedView);
					
				} else {
//					SimpleDB shenanigans
					
				}
				Thread.sleep((GOSSIP_SECS/2) + new Random().nextInt(GOSSIP_SECS));
				
			} catch (Exception e) {
				e.printStackTrace();
			}
			
		}
		
	}
	
}
