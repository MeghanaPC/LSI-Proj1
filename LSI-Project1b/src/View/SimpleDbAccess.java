package View;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import lsi.ViewManager;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.simpledb.AmazonSimpleDBClient;
import com.amazonaws.services.simpledb.model.*;
import com.amazonaws.auth.BasicAWSCredentials;

public class SimpleDbAccess {

	public static AmazonSimpleDBClient sdb;
	public static final String DOMAIN_NAME = "LSIProj1b";
	public static final String ITEM_NAME = "ViewItem";
	public static final String ATTR_NAME = "ATTR";

	// returns true if the domain was created
	public static boolean createSimpleDbDomainIfNotExists() {

		if (sdb == null) {
			sdb = new AmazonSimpleDBClient(new BasicAWSCredentials(
					"AKIAIXCNDMOAWSH7JEJA",
					"EoZEMMGkTgRcYVwn3hUPdz7BRkSXhSUuaczQkOZU"));
			sdb.setEndpoint("sdb.amazonaws.com");
		}
		
		ArrayList<String> existingDomains = new ArrayList<String>();
		String nextToken = null;
		do {
			ListDomainsRequest ldr = new ListDomainsRequest();
			ldr.setNextToken(nextToken);

			ListDomainsResult result = sdb.listDomains(ldr);
			existingDomains.addAll(result.getDomainNames());

			nextToken = result.getNextToken();
		} while (nextToken != null);

		if (!existingDomains.contains(DOMAIN_NAME)) {
			CreateDomainRequest crd = new CreateDomainRequest();
			crd.setDomainName(DOMAIN_NAME);
			try {
				sdb.createDomain(crd);
			} catch (Exception e) {
				return false;
			}
			System.out.println("New domain created");
			return true;
		}
		return false;

	}

	public static void gossipWithSimpleDb() {

		String dbViewString = null;

		/*
		if (sdb == null) {
			sdb = new AmazonSimpleDBClient(new BasicAWSCredentials(
					"AKIAIXCNDMOAWSH7JEJA",
					"EoZEMMGkTgRcYVwn3hUPdz7BRkSXhSUuaczQkOZU"));
		}
		*/
		try {
			dbViewString = getViewFromDB();
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
		if (dbViewString != null) {
			
			System.out.println("Merge branch - simple DB");
			System.out.println("View String in DB " + dbViewString);
			ConcurrentHashMap<String, String> dbView = ViewManager
					.stringToHashMap(dbViewString);
			// mergeViews() DB view with self view
			ConcurrentHashMap<String, String> mergedView = ViewManager
					.mergeViews(dbView, ServerView.serverView);
			// Send DB the mergedView
			String mergedViewString = ViewManager.hashMapToString(mergedView);
			System.out.println("Merged view string " + mergedViewString);
			putViewToDB(dbViewString, mergedViewString);

			ViewManager.mergeViewWithSelf(mergedView);
			// mergeWithSelf(mergedView)

		} else {
			// Assuming that DB is empty as no server has yet merged its view
			// with DB
			System.out.println("Empty DB branch");
			String serverViewString = ViewManager
					.hashMapToString(ServerView.serverView);
			putFirstTimeViewToDB(serverViewString);
		}

	}

	private static void putFirstTimeViewToDB(String serverViewString) {

		PutAttributesRequest putAttributesRequest = new PutAttributesRequest();
		putAttributesRequest.setDomainName(DOMAIN_NAME);
		List<ReplaceableAttribute> data = new ArrayList<ReplaceableAttribute>();
		data.add((new ReplaceableAttribute().withName(ATTR_NAME)
				.withValue(serverViewString).withReplace(true)));
		putAttributesRequest.setItemName(ITEM_NAME);
		putAttributesRequest.setAttributes(data);
		try {
			sdb.putAttributes(putAttributesRequest);
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("FirstTime View to DB " + serverViewString);

	}

	private static void putViewToDB(String oldDbView, String updatedDbView) {

		PutAttributesRequest putAttributesRequest = new PutAttributesRequest();
		putAttributesRequest.setDomainName(DOMAIN_NAME);
		List<ReplaceableAttribute> data = new ArrayList<ReplaceableAttribute>();
		data.add((new ReplaceableAttribute().withName(ATTR_NAME)
				.withValue(updatedDbView).withReplace(true)));
		putAttributesRequest.setItemName(ITEM_NAME);
		putAttributesRequest.setAttributes(data);
		UpdateCondition condition = new UpdateCondition(ATTR_NAME, oldDbView,
				true);
		putAttributesRequest.setExpected(condition);
		try {
			sdb.putAttributes(putAttributesRequest);
		} catch (AmazonServiceException e) {
			e.printStackTrace();
			if (e.getErrorCode().equals("409")) {
				// retry
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("put view in DB " + updatedDbView);

	}

	private static String getViewFromDB() throws Exception {

		SelectResult result = null;
		SelectRequest sr = new SelectRequest("select * from " + DOMAIN_NAME,
				Boolean.TRUE);

		result = sdb.select(sr);
		System.out.println("Fetching view from DB");

		for (Item item : result.getItems()) {
			String s = item.getName();
			// System.out.println(s);
			if (s.equals(ITEM_NAME)) {
				List<Attribute> attr = item.getAttributes();
				String viewString = attr.get(0).getValue();
				return viewString.trim();

			}
		}
		return null;
	}

}
