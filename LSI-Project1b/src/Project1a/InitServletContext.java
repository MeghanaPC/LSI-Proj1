package Project1a;

import java.net.SocketException;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import lsi.GeneralUtils;
import RPC.RPCServer;
import View.ServerView;
import View.SimpleDbAccess;
import View.viewDaemon;

public class InitServletContext implements ServletContextListener{

	@Override
	public void contextDestroyed(ServletContextEvent arg0) {
		System.out.println("CONTEXT DESTROYED");
		//SimpleDbAccess.removeIPFromDB(EnterServlet.serverID);
		
	}

	/*
	 * Performs initial application setup - starts daemons and adds server entry to its view table
	 */
	@Override
	public void contextInitialized(ServletContextEvent arg0) {
		// TODO Auto-generated method stub
		System.out.println("CONTEXT INIT");
		EnterServlet.serverID = (GeneralUtils.fetchAWSIP());
		SimpleDbAccess.createSimpleDbDomainIfNotExists();

		ServerView.serverView = new ConcurrentHashMap<String,String>();
		String[] arr = EnterServlet.serverID.toString().split("/");
		String svrString = arr[arr.length-1];
		ServerView.serverView.put(svrString, EnterServlet.upState
				+ EnterServlet.DELIMITER_LEVEL2 + System.currentTimeMillis());

		System.out.println("Starting daemons");
		Thread rpcServerThread = null;
		try {
			rpcServerThread = new Thread(new RPCServer());
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		rpcServerThread.setDaemon(true);
		rpcServerThread.start();

		Thread viewDaemonThread = new Thread(new viewDaemon());
		viewDaemonThread.setDaemon(true);
		viewDaemonThread.start();

		Thread garbageDaemonThread = new Thread(new garbageDaemon());
		garbageDaemonThread.setDaemon(true);
		garbageDaemonThread.start();
		System.out.println("Started all daemons");


	}

}
