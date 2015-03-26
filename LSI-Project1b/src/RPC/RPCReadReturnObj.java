package RPC;

/*
 * Defines the object that encapsulates the parameters of an RPC message
 */
public class RPCReadReturnObj {

	String serverID;
	String message;
	int foundVersion;
	Long foundExpiration;
	
	public String getServerID() {
		return serverID;
	}
	public void setServerID(String serverID) {
		this.serverID = serverID;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public int getFoundVersion() {
		return foundVersion;
	}
	public void setFoundVersion(int foundVersion) {
		this.foundVersion = foundVersion;
	}
	public Long getFoundExpiration() {
		return foundExpiration;
	}
	public void setFoundExpiration(Long foundExpiration) {
		this.foundExpiration = foundExpiration;
	}
	
}
