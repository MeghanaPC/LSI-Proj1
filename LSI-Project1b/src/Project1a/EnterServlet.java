package Project1a;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
//import javax.servlet.annotation.WebServlet;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lsi.ViewManager;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.http.HttpRequest;
import com.amazonaws.http.HttpResponse;
import com.amazonaws.services.simpledb.AmazonSimpleDBClient;

import View.*;
import Project1a.*;
import RPC.*;

/**
 * Servlet implementation class EnterServlet
 */
// @WebServlet("/EnterServlet")
public class EnterServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	private static final String COOKIE_NAME = "CS5300PROJ1ACOOKIE";

	private static final String defaultMessage = "Hello User!";

	private static final String location = "localhost";
	private static final String DELIMITER_LEVEL2 = "#";
	private static final String upState = "UP";
	private static final String downState = "DOWN";
	public static InetAddress serverID;
	public static final int K_RESILIENCY_K_VALUE = 1;

	private static final String COOKIE_DELIMITER_1 = "-";
	private static final String COOKIE_DELIMITER_2 = "_";

	private static final long SESSION_TIMEOUT_SECS = 60;

	private static final long DELTA_MILLISECS = 4000;

	private static final int STARTING_VERSION = 1;

	private static final String SERVER_ID_NULL = "NULL";

	private static int sessionNumber = 0;

	public static int getSessionNumber() {
		sessionNumber = sessionNumber + 1;
		return sessionNumber;
	}

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public EnterServlet() {
		super();
		// TODO Auto-generated constructor stub

		try {
			// CHANGE TO CALL SCRIPT
			serverID = InetAddress.getByName("127.0.0.1");

			SimpleDbAccess.createSimpleDbDomainIfNotExists();

			ServerView.serverView.put(serverID.toString(), upState
					+ DELIMITER_LEVEL2 + System.currentTimeMillis());

			Thread rpcServerThread = new Thread(new RPCServer());
			rpcServerThread.setDaemon(true);
			rpcServerThread.start();

			Thread viewDaemonThread = new Thread(new viewDaemon());
			viewDaemonThread.setDaemon(true);
			viewDaemonThread.start();

			Thread garbageDaemonThread = new Thread(new garbageDaemon());
			garbageDaemonThread.setDaemon(true);
			garbageDaemonThread.start();

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {

		Cookie[] cookies = request.getCookies();
		Boolean firstTimeAccess = true;
		String cookieValue = null;

		if (cookies != null) {
			for (Cookie cookie : cookies) {
				if (cookie.getName().equals(COOKIE_NAME)) {
					cookieValue = cookie.getValue();
					firstTimeAccess = false;
				}
			}
		}
		if (firstTimeAccess) {
			System.out.println("First time access");
			createNewSession(request, response);

		} else {

			// validate cookie by calling session read
			String[] cookieParts = cookieValue.split(COOKIE_DELIMITER_1);
			SessionInfo sessionInfo = SessionTable.sessionMap
					.get(cookieParts[0].trim());
			int cookieVersionNumber = Integer.parseInt(cookieParts[1].trim());
			
			if (sessionInfo != null
					&& sessionInfo.getVersion() == cookieVersionNumber) {
				// local server has the latest session details
				// check if session is expired
				if (sessionInfo.getExpirationTime() < System
						.currentTimeMillis()) {
					createNewSession(request, response);
				} else {

					processAfterSessionDataFound(request, response,
							cookieParts, sessionInfo, "Found locally in server " + serverID.toString());

				}
			} else {
				// Session read from primary and backups in cookie to find
				// session details
				String[] locationArray = cookieParts[2]
						.split(COOKIE_DELIMITER_2);
				
				String primaryAddress = locationArray[0];
				ArrayList<String> primaryList = new ArrayList<>();
				primaryList.add(primaryAddress);
				
				RPCReadReturnObj returnObj = null;
				
				String serverIdWithSessionInfo = null;
				String placeFound = null;
				try {
					returnObj = RPCClient.SessionReadClient(primaryList, cookieParts[0].trim(), cookieVersionNumber);
					if(returnObj != null){
						serverIdWithSessionInfo = returnObj.getServerID();
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				if(serverIdWithSessionInfo == null){
					ArrayList<String> backupList = new ArrayList<>();
					for(int i = 1; i < locationArray.length; i++){
						if(!locationArray[i].equals(SERVER_ID_NULL)){
							backupList.add(locationArray[i]);
						}
					}
					
					try {
						returnObj = null;
						returnObj = RPCClient.SessionReadClient(backupList, cookieParts[0].trim(), cookieVersionNumber);
						if(returnObj != null){
							serverIdWithSessionInfo = returnObj.getServerID();
						}
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
				}else{
					placeFound = "Found in server " + serverIdWithSessionInfo + "which was Primary";
				}
				
				if(serverIdWithSessionInfo == null){
					
					response.setContentType("text/plain");
					PrintWriter writer = response.getWriter();
					writer.println("Session info not found at server");
				
				}else{
					placeFound = "Found in server " + serverIdWithSessionInfo + "which was Backup";

					//constructing sessionInfo from returnObj 
					SessionInfo sessionInfoFromReturnObj = new SessionInfo();
					sessionInfoFromReturnObj.setMessage(returnObj.getMessage());
					sessionInfoFromReturnObj.setExpirationTime(returnObj.getFoundExpiration());
					sessionInfoFromReturnObj.setVersion(returnObj.getFoundVersion());
					
					processAfterSessionDataFound(request, response, cookieParts, sessionInfoFromReturnObj, placeFound);
				}
				

			}
		}

	}

	private void processAfterSessionDataFound(HttpServletRequest request,
			HttpServletResponse response, String[] cookieParts,
			SessionInfo sessionInfo,  String placeFound) throws ServletException, IOException {

		SessionInfo sessionObj = new SessionInfo();
		Date discardTime = new Date(System.currentTimeMillis()
				+ SESSION_TIMEOUT_SECS * 1000 + DELTA_MILLISECS);
		Date expirationTime = new Date(System.currentTimeMillis()
				+ SESSION_TIMEOUT_SECS * 1000);

		// create new sessionObj
		sessionObj.setExpirationTime(discardTime.getTime());
		sessionObj.setMessage(sessionInfo.getMessage());
		sessionObj.setVersion(sessionInfo.getVersion() + 1);

		if (request.getParameter("replace") != null) {
			String text = request.getParameter("textbox");
			sessionObj.setMessage(text);

		} else if (request.getParameter("logout") != null) {
			createNewSession(request, response);

		} else {
			// new tab in browser or refresh button
		}
		// send RPC write, send back cookie
		ConcurrentHashMap<String, String> serversUp = ViewManager
				.getActiveServersList(ServerView.serverView);

		ArrayList<String> serversList = new ArrayList<String>();
		// find k backups
		if (serversUp != null) {
			for (String serverIDInMap : serversUp.keySet()) {
				if (!serverIDInMap.equals(serverID.toString())) {
					serversList.add(serverIDInMap);
				}
			}
		}

		Collections.shuffle(serversList);
		ArrayList<String> destIPsList = new ArrayList<>();

		if (serversList.size() >= K_RESILIENCY_K_VALUE) {
			destIPsList = (ArrayList<String>) serversList.subList(0,
					K_RESILIENCY_K_VALUE);
		} else {
			destIPsList = (ArrayList<String>) serversList.subList(0,
					serversList.size());
		}

		// Updating local table
		String sessionID = cookieParts[0].trim();
		SessionTable.sessionMap.put(sessionID, sessionObj);

		List<String> backups;

		backups = RPCClient.SessionWriteClient(destIPsList, sessionID,
				sessionObj);

		String cookieLocationMetdaData = serverID + COOKIE_DELIMITER_2;
		String backupServerString = "";

		int serversNotReplied = destIPsList.size() - backups.size();

		for (String backup : backups) {
			cookieLocationMetdaData = cookieLocationMetdaData + backup
					+ COOKIE_DELIMITER_2;
			backupServerString = backupServerString + backup + ", ";
		}

		for (int i = 0; i < serversNotReplied; i++) {
			cookieLocationMetdaData = cookieLocationMetdaData + SERVER_ID_NULL
					+ COOKIE_DELIMITER_2;
			backupServerString = backupServerString + SERVER_ID_NULL + ",";
		}

		backupServerString = backupServerString.substring(0,
				backupServerString.length() - 1);

		cookieLocationMetdaData = cookieLocationMetdaData.substring(0,
				cookieLocationMetdaData.length() - 1);

		Cookie cookie = new Cookie(COOKIE_NAME, sessionID + COOKIE_DELIMITER_1
				+ sessionObj.getVersion() + COOKIE_DELIMITER_1
				+ cookieLocationMetdaData);

		cookie.setMaxAge((int) SESSION_TIMEOUT_SECS);

		response.addCookie(cookie);

		String message = sessionObj.getMessage();
		RequestDispatcher dispatcher = request
				.getRequestDispatcher("EnterServlet.jsp");

		request.setAttribute("serverID", serverID);
		request.setAttribute("placeFound", placeFound);
		request.setAttribute("primary", serverID);
		request.setAttribute("backup", backupServerString);
		request.setAttribute("sessionExpiryTime", expirationTime);
		request.setAttribute("discardTime", discardTime);

		request.setAttribute("viewString",
				ViewManager.hashMapToString(ServerView.serverView));

		request.setAttribute("message", message);
		/*
		 * request.setAttribute("expiration",expirationTime.toString());
		 * request.setAttribute("cookie", cookie.getValue());
		 */
		dispatcher.forward(request, response);

	}

	private void createNewSession(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {

		int sessionNumber = getSessionNumber();
		String sessionID = sessionNumber + COOKIE_DELIMITER_2
				+ this.serverID.toString();

		ConcurrentHashMap<String, String> serversUp = ViewManager
				.getActiveServersList(ServerView.serverView);

		ArrayList<String> serversList = new ArrayList<String>();
		// find k backups
		if (serversUp != null) {
			for (String serverIDInMap : serversUp.keySet()) {
				if (!serverIDInMap.equals(serverID.toString())) {
					serversList.add(serverIDInMap);
				}
			}
		}

		Collections.shuffle(serversList);
		ArrayList<String> destIPsList = new ArrayList<>();

		if (serversList.size() >= K_RESILIENCY_K_VALUE) {
			destIPsList = (ArrayList<String>) serversList.subList(0,
					K_RESILIENCY_K_VALUE);
		} else {
			destIPsList = (ArrayList<String>) serversList.subList(0,
					serversList.size());
		}

		Date discardTime = new Date(System.currentTimeMillis()
				+ SESSION_TIMEOUT_SECS * 1000 + DELTA_MILLISECS);
		Date expirationTime = new Date(System.currentTimeMillis()
				+ SESSION_TIMEOUT_SECS * 1000);
		SessionInfo sessionObj = new SessionInfo();
		sessionObj.setExpirationTime(discardTime.getTime());
		sessionObj.setMessage(defaultMessage);
		sessionObj.setVersion(STARTING_VERSION);

		List<String> backups;

		backups = RPCClient.SessionWriteClient(destIPsList, sessionID,
				sessionObj);

		String cookieLocationMetdaData = serverID + COOKIE_DELIMITER_2;
		String backupServerString = "";

		int serversNotReplied = destIPsList.size() - backups.size();

		for (String backup : backups) {
			cookieLocationMetdaData = cookieLocationMetdaData + backup
					+ COOKIE_DELIMITER_2;
			backupServerString = backupServerString + backup + ", ";
		}

		for (int i = 0; i < serversNotReplied; i++) {
			cookieLocationMetdaData = cookieLocationMetdaData + SERVER_ID_NULL
					+ COOKIE_DELIMITER_2;
			backupServerString = backupServerString + SERVER_ID_NULL + ",";
		}

		backupServerString = backupServerString.substring(0,
				backupServerString.length() - 1);

		cookieLocationMetdaData = cookieLocationMetdaData.substring(0,
				cookieLocationMetdaData.length() - 1);

		Cookie cookie = new Cookie(COOKIE_NAME, sessionID + COOKIE_DELIMITER_1
				+ STARTING_VERSION + COOKIE_DELIMITER_1
				+ cookieLocationMetdaData);

		cookie.setMaxAge((int) SESSION_TIMEOUT_SECS);

		SessionTable.sessionMap.put(sessionID.toString(), sessionObj);

		response.addCookie(cookie);

		String message = defaultMessage;
		RequestDispatcher dispatcher = request
				.getRequestDispatcher("EnterServlet.jsp");

		request.setAttribute("serverID", serverID);
		request.setAttribute("placeFound", "New session created at " + serverID);
		request.setAttribute("primary", serverID);
		request.setAttribute("backup", backupServerString);
		request.setAttribute("sessionExpiryTime", expirationTime);
		request.setAttribute("discardTime", discardTime);

		request.setAttribute("viewString",
				ViewManager.hashMapToString(ServerView.serverView));

		request.setAttribute("message", message);
		/*
		 * request.setAttribute("expiration",expirationTime.toString());
		 * request.setAttribute("cookie", cookie.getValue());
		 */
		dispatcher.forward(request, response);

	}

	/*
	private void logoutAction(HttpServletRequest request,
			HttpServletResponse response, String cookieValue)
			throws ServletException, IOException {

		String[] strArray = cookieValue.split("_");

		String oldsessionId = strArray[0];
		Integer oldVersionNumber = Integer.parseInt(strArray[1]);
		Integer newVersionNumber = oldVersionNumber + 1;
		// Date expirationTime = new Date();

		SessionInfo oldSessionInfo = SessionTable.sessionMap.get(oldsessionId);
		if (oldSessionInfo == null) {
			// CLIENT HAS ALTERED COOKIE
			response.setContentType("text/plain");
			PrintWriter writer = response.getWriter();
			writer.println("Session info not found at server");
		} else {

			Date expirationTime = new Date(System.currentTimeMillis() + 60000);
			UUID sessionId = UUID.randomUUID();
			SessionInfo sessionInfo = new SessionInfo();
			sessionInfo.setExpirationTime(expirationTime.getTime());
			sessionInfo.setVersion(1);
			sessionInfo.setMessage(defaultMessage);
			SessionTable.sessionMap.put(sessionId.toString(), sessionInfo);

			Cookie cookie = new Cookie(COOKIE_NAME, sessionId + "_" + "1" + "_"
					+ location);
			cookie.setMaxAge(60);
			response.addCookie(cookie);

			String message = defaultMessage;
			RequestDispatcher dispatcher = request
					.getRequestDispatcher("EnterServlet.jsp");

			request.setAttribute("serverID", serverID);
			request.setAttribute("placeFound", serverID);
			request.setAttribute("primary", serverID);
			request.setAttribute("backup", serverID);
			request.setAttribute("sessionExpiryTime", serverID);
			request.setAttribute("discardTime", serverID);

			request.setAttribute("viewString", serverID);

			request.setAttribute("message", message);
			request.setAttribute("expiration", expirationTime.toString());
			request.setAttribute("cookie", cookie.getValue());
			dispatcher.forward(request, response);

		}

	}

	private void refreshAction(HttpServletRequest request,
			HttpServletResponse response, String cookieValue)
			throws ServletException, IOException {

		String[] strArray = cookieValue.split("_");
		System.out.println(strArray.length + strArray[0] + strArray[1]);

		String sessionId = strArray[0];
		Integer oldVersionNumber = Integer.parseInt(strArray[1]);
		Integer newVersionNumber = oldVersionNumber + 1;

		SessionInfo oldSessionInfo = SessionTable.sessionMap.get(sessionId);
		if (oldSessionInfo == null) {
			// CLIENT HAS ALTERED COOKIE
			response.setContentType("text/plain");
			PrintWriter writer = response.getWriter();
			writer.println("Session info not found at server");
		} else {
			Date expirationTime = new Date(System.currentTimeMillis() + 60000);

			SessionInfo sessionInfo = new SessionInfo();
			sessionInfo.setExpirationTime(expirationTime.getTime());
			sessionInfo.setVersion(newVersionNumber);
			sessionInfo.setMessage(oldSessionInfo.getMessage());
			SessionTable.sessionMap.put(sessionId.toString(), sessionInfo);

			Cookie cookie = new Cookie(COOKIE_NAME, sessionId + "_"
					+ newVersionNumber + "_" + location);
			cookie.setMaxAge(60);
			response.addCookie(cookie);

			String message = oldSessionInfo.getMessage();
			RequestDispatcher dispatcher = request
					.getRequestDispatcher("EnterServlet.jsp");
			request.setAttribute("message", message);
			request.setAttribute("expiration", expirationTime.toString());
			request.setAttribute("cookie", cookie.getValue());
			dispatcher.forward(request, response);
		}

	}

	private void replaceAction(HttpServletRequest request,
			HttpServletResponse response, String cookieValue)
			throws ServletException, IOException {

		String[] strArray = cookieValue.split("_");
		System.out.println(strArray.length + strArray[0] + strArray[1]);
		String sessionId = strArray[0];
		Integer oldVersionNumber = Integer.parseInt(strArray[1]);
		Integer newVersionNumber = oldVersionNumber + 1;
		String text = request.getParameter("textbox");

		SessionInfo oldSessionInfo = SessionTable.sessionMap.get(sessionId);
		if (oldSessionInfo == null) {
			// CLIENT HAS ALTERED COOKIE
			response.setContentType("text/plain");
			PrintWriter writer = response.getWriter();
			writer.println("Session info not found at server");
		} else {
			Date expirationTime = new Date(System.currentTimeMillis() + 60000);

			SessionInfo sessionInfo = new SessionInfo();
			sessionInfo.setExpirationTime(expirationTime.getTime());
			sessionInfo.setVersion(newVersionNumber);
			sessionInfo.setMessage(text);
			SessionTable.sessionMap.put(sessionId.toString(), sessionInfo);

			Cookie cookie = new Cookie(COOKIE_NAME, sessionId + "_"
					+ newVersionNumber + "_" + location);
			cookie.setMaxAge(60);
			response.addCookie(cookie);

			String message = text;
			RequestDispatcher dispatcher = request
					.getRequestDispatcher("EnterServlet.jsp");
			request.setAttribute("message", message);
			request.setAttribute("expiration", expirationTime.toString());
			request.setAttribute("cookie", cookie.getValue());
			dispatcher.forward(request, response);
		}

	}

	private void removeStaleEntries() {

		Date currentTime = new Date();
		ArrayList<String> keysToBeRemoved = new ArrayList<String>();
		for (Map.Entry<String, SessionInfo> entry : SessionTable.sessionMap
				.entrySet()) {
			if (entry.getValue().getExpirationTime() < System
					.currentTimeMillis()) {
				keysToBeRemoved.add(entry.getKey());
			}
		}
		for (String sessionId : keysToBeRemoved) {
			SessionTable.sessionMap.remove(sessionId);
		}
		for (Map.Entry<String, SessionInfo> entry : SessionTable.sessionMap
				.entrySet()) {
			System.out.println(entry.getKey() + " " + entry.getValue());
		}
	}

	/*
	 * private String getFirstResponseString() { String response =
	 * "<%@ page language=\"java\" contentType=\"text/html; charset=UTF-8\"\n" +
	 * "pageEncoding=\"UTF-8\"%\n" +
	 * "<!DOCTYPE html PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\" \"http://www.w3.org/TR/html4/loose.dtd\">\n"
	 * + "<html>\n" + "<head>\n" +
	 * "<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\">\n"
	 * + "<title>LSI project 1a</title>\n" + "</head>\n" + "<body>\n" +
	 * "<h1>Hello User</h1>\n" + "<form action=EnterServlet method=\"get\">\n" +
	 * "<br/>\n" + "<br/>\n" +
	 * "<input type=\"submit\" name=\"replace\" value=\"Replace\"/>\n" +
	 * "<input type=\"text\" name=\"textbox\" value=\"\"/>\n" + "<br/>\n" +
	 * "<input type=\"submit\" name=\"refresh\" value=\"Refresh\"/>\n" +
	 * "<br/>\n" + "<input type=\"submit\" name=\"logout\" value=\"Logout\"/>\n"
	 * 
	 * 
	 * + "</form>\n" + "</body>\n" + "</html>\n";
	 * 
	 * return response; }
	 */

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}

}
