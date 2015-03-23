package Project1a;


import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
//import javax.servlet.annotation.WebServlet;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import View.*;

/**
 * @author trupti
 */



/**
 * Servlet implementation class ServletForSession
 */
//@WebServlet("/ServletForSession")
public class ServletForSession extends HttpServlet{
	private static final long serialVersionUID = 1L;
	private static boolean flag=false;
    public static SessionDataTable sessiontable=new SessionDataTable();
    public static UserSession usersession=new UserSession();
    public static InetAddress serverID;
    /**
     * @see HttpServlet#HttpServlet()
     */
    public ServletForSession() {
        super();
        ServerView.serverView =new ConcurrentHashMap<String,String>();
        try {
			serverID=InetAddress.getByName("127.0.0.1");
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
        
    }

    /**
     * Clicking a link, clicking a bookmark, entering raw URL in browser address bar, etcetera will all 
     * fire a HTTP GET request. If a Servlet is listening on the URL in question, then its doGet() method will be called. 
     * The HTTP POST requests are usually only fired by a <form> whose method attribute is set to post
     * --> reference quote from Stackoverflow (when to use GET and PUT requests)
     */
    
    //checks if the sessiontable is empty. If yes, start new session else just refresh behavior
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		sessiontable.RemoveExpiredSession();
		flag=false;
		Cookie[] cookie_array = request.getCookies();
		MyCookie cookie;
		String sessionID=getSessionID(cookie_array);
		if(sessionID==null)
		{
			cookie=usersession.createNewUserSession();
			sessionID=cookie.getValue(); 
			sessiontable.put(sessionID, usersession.getSessionState());
		}
		else
		{
				cookie=usersession.refresh();   //cookie with new time 
				sessiontable.refreshTime(sessionID,usersession.sessionstate.getExpirationTimeStamp());   //update expiration timestamp
		}
			
		String message = usersession.sessionstate.getSessionMessage();
		//add the cookie to request, add other values to be displayed
		response.addCookie(cookie);
		request.setAttribute("message", message);
		String cookie_value ="SessionID="+cookie.getValue()+" "+"Version="+cookie.getVersion()+" "+"Location="+cookie.getLocationMetadata();
		request.setAttribute("cookievalue", cookie_value);
		request.setAttribute("expirationtimestamp", usersession.sessionstate.getExpirationTimeStamp());
		RequestDispatcher dispatcher = request.getRequestDispatcher("FormProcess.jsp");
		dispatcher.forward(request, response);
		
	}
	protected String getSessionID(Cookie[] cookieArray)
	{
		String sessionID = null;
		if(cookieArray!=null)
		{
			for(int i =0 ; i<cookieArray.length;i++)
			{
				Cookie cookie = cookieArray[i];
			
				if(cookie.getName().equals("CS5300PROJ1SESSION"))
				{
					sessionID = cookie.getValue();
					break;
				}
			}
		}
		return sessionID;
	}
	/**
	 * doPost called when we click on either of the three buttons. Functionality performed
	 * based on the type of button clicked. Also session expiry and logout handled.
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		sessiontable.RemoveExpiredSession();
		String clickedbutton = request.getParameter("hiddenbutton");
		if(flag)
		{
			doGet(request,response);
			return;
		}
		Cookie[] cookies = request.getCookies();
		MyCookie cookie = null;
		String sessionID=usersession.getCookie().getValue();
		String message=null;
		
		
		if(cookies!=null)
		{
			if(sessiontable.get(sessionID)!=null)
			{
			
				if(clickedbutton.equals("replace"))
				{
					String newMessage=request.getParameter("replacemessage");
					cookie=usersession.refresh();
					sessiontable.update(sessionID, newMessage,usersession.sessionstate.getExpirationTimeStamp());
					
				}
				else if(clickedbutton.equals("refresh"))
				{
					cookie=usersession.refresh();   //cookie with new time 
					sessiontable.refreshTime(sessionID,usersession.sessionstate.getExpirationTimeStamp());   //update expiration timestamp
					//print both times and see
				}
				else if(clickedbutton.equals("logout"))
				{
					usersession.mycookie.invalidate();
					cookie=usersession.mycookie;
					sessiontable.invalidate(sessionID);
					//cookie=usersession.mycookie;      //mycookie maxage set to 0
					sessionID=null;
					response.addCookie(cookie);
				
					PrintWriter out=response.getWriter();
					out.println("Logged out . Please Click on URL Bar/Broswer Refresh button to Start New Session");
					//create new user session
				/*	cookie=usersession.createNewUserSession();
					sessionID=cookie.getValue(); 
					sessiontable.put(sessionID, usersession.getSessionState());*/
					flag=true;
				}
				
				
			}
			else
			{
				//session expired
				PrintWriter out=response.getWriter();
				out.println("User Session Expired. Please Click on URL Bar/Browser Refresh Button to Start New Session");
				usersession.mycookie.invalidate();
				sessiontable.invalidate(sessionID);
				//cookie=usersession.mycookie;      //mycookie maxage set to 0;
				//usersession.mycookie.setValue(null);
				sessionID=null;
				flag=true;
					
				
			}
		}
		else
		{
			cookie=usersession.refresh();   //cookie with new time 
			sessiontable.refreshTime(sessionID,usersession.sessionstate.getExpirationTimeStamp());   //update expiration timestamp
		}
		if(sessionID!=null)
		{
			message=usersession.sessionstate.getSessionMessage();
			String cookie_value ="SessionID="+cookie.getValue()+" "+"Version="+cookie.getVersion()+" "+"Location="+cookie.getLocationMetadata();
			request.setAttribute("cookievalue", cookie_value);
			request.setAttribute("expirationtimestamp", usersession.sessionstate.getExpirationTimeStamp());
		
		
		
		response.addCookie(cookie);
		request.setAttribute("message", message);
		RequestDispatcher dispatcher = request.getRequestDispatcher("FormProcess.jsp");
		dispatcher.forward(request, response);
		}
		
	}
	

}
