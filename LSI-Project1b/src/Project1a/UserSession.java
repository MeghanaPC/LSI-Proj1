package Project1a;
import java.util.Calendar;
import java.util.Date;

/**
 * 
 * @author trupti
 * UserSession class is like a session per user. It has the sessionstate object and a cookie
 * associated with the session. 
 *
 */
public class UserSession {
	SessionState sessionstate;
	MyCookie mycookie;
	public static int COOKIE_AGE = 60;
	
	public void createCookie(String sessionID, Integer sessionVersion, String location)
	{
		mycookie = new MyCookie("CS5300PROJ1SESSION",sessionID, sessionVersion,location,COOKIE_AGE);
	}
	public MyCookie createNewUserSession()
	{
		sessionstate=new SessionState();
		createCookie(sessionstate.getSessionID(),sessionstate.getSessionVersion(),"Default location");
		setExpirationTimestamp();
		
		return mycookie;
		
	}
	/**
	 * get the current time and add "COOKIE_AGE" amount of time to it to get new expiry time
	 */
	public void setExpirationTimestamp()
	{
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.SECOND, COOKIE_AGE); 
		Date expirytime=cal.getTime();
		sessionstate.setExpirationTimeStamp(expirytime);
	}
	/**
	 * set maxage to 0 so that the cookie will be removed by browser
	 */
	public void invalidate()
	{
		mycookie.setMaxAge(0);
	}
	public MyCookie getCookie()
	{
		return this.mycookie;
	}
	public SessionState getSessionState()
	{
		return this.sessionstate;
	}
	/**
	 * refresh behavior - increase version of cookie, change max_age to new max_age 
	 * and also change session expiration timestamp
	 * @return
	 */
	public MyCookie refresh()
	{
		
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.SECOND, COOKIE_AGE);
		sessionstate.setExpirationTimeStamp(cal.getTime());
		mycookie.setVersion(sessionstate.incrementVersion());
		mycookie.setMaxAge(COOKIE_AGE);
		return mycookie;
	}

}
