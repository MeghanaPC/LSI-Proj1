package View;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class viewDaemon  implements Runnable{
	
	//HashMap<String, String> ServerView;
	
	@Override
	public void run(){
		Double numProbability = 0.0;
		Double randomProbability = 0.0;
		while(true){
			try {
				//take only the up servers
				numProbability = (double) (1/ServerView.serverView.size());
				Random random = new Random();
				randomProbability = random.nextDouble();
				
				if (numProbability < randomProbability) {
					//add self,up,now
					Set<String> serverList = ServerView.serverView.keySet();
					java.util.Collections.shuffle((List<?>) serverList);
					Iterator serverIterator = serverList.iterator();
					String chosenServer = (String) serverIterator.next();
					
//					RPC to chosen server
//					exchange views
//					ServerView.mergeViews(local,exchangedView);
					
				} else {
//					SimpleDB shenanigans
					
				}
				Thread.sleep(new Random().nextLong() * 10000);
				
			} catch (Exception e) {
				e.printStackTrace();
			}
			
		}
		
	}
	
}
