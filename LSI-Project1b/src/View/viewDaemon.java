package View;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class viewDaemon  implements Runnable{
	
	HashMap<String, String> ServerView;
	
	public viewDaemon(HashMap<String, String> setView) {
		ServerView = setView;
	}
	
	@Override
	public void run(){
		Double numProbability = 0.0;
		Double randomProbability = 0.0;
		while(true){
			try {
				numProbability = (double) (1/ServerView.size());
				Random random = new Random();
				randomProbability = random.nextDouble();
				
				if (numProbability > randomProbability) {
					
					Set<String> serverList = ServerView.keySet();
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
