package Project1a;

import java.util.concurrent.ConcurrentHashMap;

public class SessionTable {

	public static int mapSizeThresholdForPurge = 3;
	public static ConcurrentHashMap<String, SessionInfo> sessionMap = new ConcurrentHashMap<String, SessionInfo>();
}
