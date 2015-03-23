package Project1a;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

import javax.media.j3d.View;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
//import javax.servlet.annotation.WebServlet;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import View.*;
import Project1a.*;
import RPC.*;
/**
 * Servlet implementation class EnterServlet
 */
//@WebServlet("/EnterServlet")
public class EnterServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	private static final String COOKIE_NAME = "CS5300PROJ1ACOOKIE";

	private static final String defaultMessage = "Hello User!";
	
	private static final String location = "localhost";
	public static InetAddress serverID;

       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public EnterServlet() {
        super();
        // TODO Auto-generated constructor stub
        try {
			serverID=InetAddress.getByName("127.0.0.1");
			
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
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		Cookie[] cookies = request.getCookies();
		Boolean firstTimeAccess = true;
		String cookieValue = null;
		
		if(SessionTable.sessionMap.size() >= SessionTable.mapSizeThresholdForPurge){
			System.out.println("Calling removeStaleEntries");
			removeStaleEntries();
		}
		
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
			
			Date expirationTime = new Date(System.currentTimeMillis() + 60000);
			UUID sessionId = UUID.randomUUID();
			SessionInfo sessionInfo = new SessionInfo();
			sessionInfo.setExpirationTime(expirationTime.getTime());
			sessionInfo.setVersion(1);
			sessionInfo.setMessage(defaultMessage);
			SessionTable.sessionMap.put(sessionId.toString(),sessionInfo);
			
			Cookie cookie = new Cookie(COOKIE_NAME, sessionId + "_" + "1" + "_" + location);
			cookie.setMaxAge(60);
			response.addCookie(cookie);

			String message = defaultMessage;
			RequestDispatcher dispatcher = request.getRequestDispatcher("EnterServlet.jsp");
			request.setAttribute("message",message);
			request.setAttribute("expiration",expirationTime.toString());
			request.setAttribute("cookie", cookie.getValue());
			dispatcher.forward(request, response);

		}else{
			if(request.getParameter("replace")!= null){
				replaceAction(request, response, cookieValue);
			}else if(request.getParameter("refresh")!= null){
				refreshAction(request, response, cookieValue);
			}else if(request.getParameter("logout")!= null){
				logoutAction(request, response, cookieValue);
			}else{
				//new tab in browser
				refreshAction(request, response, cookieValue);
			}			
		}

	}

	private void logoutAction(HttpServletRequest request,
			HttpServletResponse response, String cookieValue) throws ServletException, IOException {
		
		String[] strArray = cookieValue.split("_");

		String oldsessionId = strArray[0];
		Integer oldVersionNumber = Integer.parseInt(strArray[1]);
		Integer newVersionNumber = oldVersionNumber + 1;
		//Date expirationTime = new Date();

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
			SessionTable.sessionMap.put(sessionId.toString(),sessionInfo);
			
			Cookie cookie = new Cookie(COOKIE_NAME, sessionId + "_" + "1" + "_" + location);
			cookie.setMaxAge(60);
			response.addCookie(cookie);

			String message = defaultMessage;
			RequestDispatcher dispatcher = request.getRequestDispatcher("EnterServlet.jsp");
			request.setAttribute("message",message);
			request.setAttribute("expiration",expirationTime.toString());
			request.setAttribute("cookie", cookie.getValue());
			dispatcher.forward(request, response);

			
		}


	}

	private void refreshAction(HttpServletRequest request,
			HttpServletResponse response, String cookieValue) throws ServletException, IOException {

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
			request.setAttribute("message",message);
			request.setAttribute("expiration",expirationTime.toString());
			request.setAttribute("cookie", cookie.getValue());
			dispatcher.forward(request, response);
		}

	}

	private void replaceAction(HttpServletRequest request,
			HttpServletResponse response, String cookieValue) throws ServletException, IOException {

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
			request.setAttribute("message",message);
			request.setAttribute("expiration",expirationTime.toString());
			request.setAttribute("cookie", cookie.getValue());
			dispatcher.forward(request, response);
		}
		
	}
	

	private void removeStaleEntries() {

		Date currentTime = new Date();
		ArrayList<String> keysToBeRemoved = new ArrayList<String>();
		for(Map.Entry<String,SessionInfo> entry : SessionTable.sessionMap.entrySet()){
			if(entry.getValue().getExpirationTime() < System.currentTimeMillis()){
				keysToBeRemoved.add(entry.getKey());
			}
		}
		for(String sessionId : keysToBeRemoved){
			SessionTable.sessionMap.remove(sessionId);
		}
		for(Map.Entry<String,SessionInfo> entry : SessionTable.sessionMap.entrySet()){
			System.out.println(entry.getKey() + " " + entry.getValue());
		}
	}

	/*
	private String getFirstResponseString() {
		String response = "<%@ page language=\"java\" contentType=\"text/html; charset=UTF-8\"\n"
			    + "pageEncoding=\"UTF-8\"%\n"
		+ "<!DOCTYPE html PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\" \"http://www.w3.org/TR/html4/loose.dtd\">\n"
		+ "<html>\n"
		+ "<head>\n"
		+ "<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\">\n"
		+ "<title>LSI project 1a</title>\n"
		+ "</head>\n"
		+ "<body>\n"
		+ "<h1>Hello User</h1>\n"
		+ "<form action=EnterServlet method=\"get\">\n"
		+ "<br/>\n"
		+ "<br/>\n"
			+ "<input type=\"submit\" name=\"replace\" value=\"Replace\"/>\n"
			+ "<input type=\"text\" name=\"textbox\" value=\"\"/>\n"
			+ "<br/>\n"
			+ "<input type=\"submit\" name=\"refresh\" value=\"Refresh\"/>\n"
			+ "<br/>\n"
			+ "<input type=\"submit\" name=\"logout\" value=\"Logout\"/>\n"
			

		+ "</form>\n"
		+ "</body>\n"
		+ "</html>\n";
		
		return response;
	}
	*/

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}

}
