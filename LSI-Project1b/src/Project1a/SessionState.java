package Project1a;
//import java.util.Date;
import java.util.UUID;

/**
 * 
 * @author trupti
 *	session state contains <sessionID, version, message, expirationtimestamp> and is 
 *	stored in sessiondatatable as the value corresponding to key sessionID
 */
public class SessionState {
	private String sessionID;
	private String sessionMessage;
	private Integer sessionVersion;
	//private Date creationTime;
	private long expirationTimeStamp;
	
	//private static int Cookie_Age=120;

	public SessionState()
	{
		setSessionID(UUID.randomUUID().toString());   //generates globally unique ID
		setSessionMessage("Hello User");
		setSessionVersion(1);
		setExpirationTimeStamp(System.currentTimeMillis());  //initial expiry time set as current date
		
	}
	public Integer incrementVersion()
	{
		return ++sessionVersion;
	}

	public String getSessionID() {
		return sessionID;
	}

	public void setSessionID(String sessionID) {
		this.sessionID = sessionID;
	}

	public String getSessionMessage() {
		return sessionMessage;
	}

	public void setSessionMessage(String sessionMessage) {
		this.sessionMessage = sessionMessage;
	}

	public Integer getSessionVersion() {
		return sessionVersion;
	}

	public void setSessionVersion(Integer sessionVersion) {
		this.sessionVersion = sessionVersion;
	}


	public Long getExpirationTimeStamp() {
		return expirationTimeStamp;
	}

	public void setExpirationTimeStamp(long expirationTimeStamp) {
		this.expirationTimeStamp = expirationTimeStamp;
	}
	public void refresh(long newExpiry)
	{
		//Calendar cal = Calendar.getInstance();
		//cal.add(Calendar.SECOND, UserSession.COOKIE_AGE);
		this.setExpirationTimeStamp(newExpiry);
	}
	public boolean isExpired()
	{
		if(this.expirationTimeStamp < System.currentTimeMillis())
			return true;
		
		return false;
	}
	
}
