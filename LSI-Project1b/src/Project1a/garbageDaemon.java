package Project1a;

import java.util.ArrayList;
import java.util.Set;

public class garbageDaemon implements Runnable {
	
	@Override
	public void run(){
		
		System.out.println("In garbage daemon");
		
		ArrayList<String> expiredList = new ArrayList<String>();
		while(true){
			try {
				Set<String> keySet = SessionTable.sessionMap.keySet();
				for(String serverID:keySet){
					SessionInfo tempInfo = SessionTable.sessionMap.get(serverID);
					long tempExpiration = tempInfo.getExpirationTime();
					long currentTime = System.currentTimeMillis();
					if (currentTime > tempExpiration) {
						expiredList.add(serverID);
					}
				}
				
				for(String toBeRemoved:expiredList){
					SessionTable.sessionMap.remove(toBeRemoved);
				}
				expiredList.clear();
				Thread.sleep(10000);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

}
