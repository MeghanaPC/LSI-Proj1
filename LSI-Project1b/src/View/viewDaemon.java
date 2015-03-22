package View;

import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;

import lsi.ViewManager;

public class viewDaemon  implements Runnable{
	
//	Need to fix this to a value
	private final int GOSSIP_SECS = 10;
	
	@Override
	public void run(){
		Double numProbability = 0.0;
		Double randomProbability = 0.0;
		Set<String> serverList = ViewManager.getActiveServersList(ServerView.serverView);
		while(true){
			try {
				//take only the up servers
				numProbability = (double) (1/serverList.size());
				Random random = new Random();
				randomProbability = random.nextDouble();
				
				if (numProbability < randomProbability) {
					//add self,up,now
					
					java.util.Collections.shuffle((List<?>) serverList);
					Iterator<String> serverIterator = serverList.iterator();
					String chosenServer = (String) serverIterator.next();
					
//					RPC to chosen server
//					exchange views
//					ServerView.mergeViews(local,exchangedView);
					
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
