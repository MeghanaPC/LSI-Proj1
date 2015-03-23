package Project1a;

import java.util.Date;

public class SessionInfo {
	private String message;
	private int version;
	private Date expirationTime;
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public int getVersion() {
		return version;
	}
	public void setVersion(int version) {
		this.version = version;
	}
	public Date getExpirationTime() {
		return expirationTime;
	}
	public void setExpirationTime(Date expirationTime) {
		this.expirationTime = expirationTime;
	}
	

}
