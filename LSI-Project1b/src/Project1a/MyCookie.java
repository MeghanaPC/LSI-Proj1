package Project1a;
import javax.servlet.http.Cookie;

/**
 * 
 * @author trupti
 * creates a custom cookie that stores cookie name,version,sessionID,locationMatada and has an expiry time
 */
public class MyCookie extends Cookie{

	private static final long serialVersionUID = 5422948150227776777L;
	
	private Integer version=null;
	private String WQAddress=null;
	
	
	public MyCookie(String name, String value_SessionID) {
		super(name, value_SessionID);
		// TODO Auto-generated constructor stub
	}
	
	/**
	 * Create a Cookie with the following values . 
	 * @param name
	 * @param value_SessionID
	 * @param version
	 * @param WQAddress
	 * @param maxAge
	 */
	public MyCookie(String name,String value_SessionID,Integer version,String WQAddress,Integer maxAge)
	{
		super(name,value_SessionID);    //value of the cookie is the sessionID
		this.version=version;
		this.WQAddress=WQAddress;
		this.setMaxAge(maxAge);
		//set version not required
	}
	public int getVersion()
	{
		return this.version;
	}
	public String getLocationMetadata()
	{
		return this.WQAddress;
	}
	public void setVersion(Integer v)
	{
		this.version=v;
	}
	public void invalidate()
	{
		this.setMaxAge(0);
	}
	

}
