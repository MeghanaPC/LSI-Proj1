package Project1a;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 
 * @author trupti
 * a concurrenthasmap that sores <SessionID,sessionState> tuples. 
 * SessionState is a class that has <SessionID,version#,message,expirytime>
 * concurrenthashmap is used because it does not lock the entire table at a time. locks only tuples being operated
 */

public class SessionDataTable extends ConcurrentHashMap<String, SessionState> {

	
	private static final long serialVersionUID = -6014210144348706411L;
	
	/**
	 *  given a sessionID(key) , return its corresponding SessionState(value) 
	 */
	public SessionState getSession(String sessionID)
	{
		//SessionState session=new SessionState();
		if(this.get(sessionID)!=null)
			return this.get(sessionID);
		
		return null;
		
	}
	public String getCurrentMessage(String sessionID)
	{
		return getSession(sessionID).getSessionMessage();
	}
	public Integer getCurrentVersion(String sessionID)
	{
		return getSession(sessionID).getSessionVersion();
	}
	public long getCurrentExpirytime(String sessionID)
	{
		return getSession(sessionID).getExpirationTimeStamp();
	}
	/**
	 * removes the expired sessions . i.e whose expiration time is before current time
	 */
	public void RemoveExpiredSession()
	{
		for(Entry<String, SessionState> e:this.entrySet())
		{
			if(e.getValue().getExpirationTimeStamp() < System.currentTimeMillis())
					this.remove(e.getKey());
		}
	}
	/**
	 * update the expiration time of the session. Also refresh the cookie expiry time.
	 * @param sessionID
	 * @param newExpiry
	 */
	public void refreshTime(String sessionID,long newExpiry)
	{
		SessionState currentstate=get(sessionID);
		currentstate.refresh(newExpiry);
		//replace(sessionID,currentstate);  might get replaced there only
		
	}
	public void invalidate(String sessionID)
	{
		remove(sessionID);
	}
	/**
	 * Whenever the replace button clicked, call this method. updates the message to new message and
	 * also refreshes the expiry time of the corresponsing session 
	 * @param sessionID
	 * @param newmessage
	 * @param newExpiry
	 */
	public void update(String sessionID,String newmessage,long newExpiry)
	{
		SessionState currentstate=get(sessionID);
		currentstate.refresh(newExpiry);
		currentstate.setSessionMessage(newmessage);
		//replace?
	}
	
	

}
