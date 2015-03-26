package View;

import java.util.ArrayList;
import java.util.Collections;
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
	private final int GOSSIP_SECS = 60;
	
	@Override
	public void run(){
		ConcurrentHashMap<String,String> destView;
		Double numProbability = 0.0;
		Double randomProbability = 0.0;
		while(true){	
			ConcurrentHashMap<String, String> activeServerViewMap= ViewManager.getActiveServersList(ServerView.serverView);
			System.out.println("Active server view size " + activeServerViewMap.size());
			try {
				numProbability = (double) (1.0/(activeServerViewMap.size()+1.0));  //plus 1 coz self has been excluded but we need this list
				Random random = new Random();
				randomProbability = random.nextDouble();
				System.out.println("Num probab " + numProbability);
				System.out.println("Random probab " + randomProbability);

				System.out.println("View daemon running");
				String[] arr = EnterServlet.serverID.toString().split("/");
				String serverId = arr[arr.length-1];
				ServerView.serverView.put(serverId,upState+DELIMITER_LEVEL2+System.currentTimeMillis());

				if (numProbability < randomProbability) {
					//updating self
					System.out.println("Should send exchange views now");
					//Set<String> serverSet=activeServerViewMap.keySet();
					ArrayList<String> serversList = new ArrayList<String>();
					for(String key : ServerView.serverView.keySet()){
						serversList.add(key);
					}
					serversList.remove(serverId);
					/*java.util.Collections.shuffle((List<?>) serverSet);
					Iterator<String> serverIterator = serverSet.iterator();
					String chosenServer = (String) serverIterator.next();*/
					Collections.shuffle(serversList);
					String chosenServer = serversList.get(0);
					System.out.println("Chosen server " + chosenServer);
					
					destView=new ConcurrentHashMap<String,String>(RPC.RPCClient.ExchangeViewClient(chosenServer, ServerView.serverView));
					ConcurrentHashMap<String,String> mergedView=new ConcurrentHashMap<String,String>(ViewManager.mergeViews(ServerView.serverView,destView));
					ViewManager.mergeViewWithSelf(mergedView);
					
					System.out.println("Gossip with another server");
					
				} else {
//					//call simpleDB
					System.out.println("Gossip with simple DB");
					SimpleDbAccess.gossipWithSimpleDb();
					
				}
				Thread.sleep((GOSSIP_SECS/2) + new Random().nextInt(GOSSIP_SECS));
				
			} catch (Exception e) {
				e.printStackTrace();
			}
			
		}
		
	}
	
}
