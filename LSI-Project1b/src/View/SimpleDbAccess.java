package View;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import lsi.ViewManager;

import com.amazonaws.services.simpledb.AmazonSimpleDBClient;
import com.amazonaws.services.simpledb.model.*;
import com.amazonaws.auth.BasicAWSCredentials;

public class SimpleDbAccess {

    public static AmazonSimpleDBClient sdb;
    public static String DOMAIN_NAME = "LSIProj1b";
    public static String ITEM_NAME = "ViewItem";


	public static void gossipWithSimpleDb(){
		
		if(sdb == null){
			sdb = new AmazonSimpleDBClient(new BasicAWSCredentials("AKIAIXCNDMOAWSH7JEJA", "EoZEMMGkTgRcYVwn3hUPdz7BRkSXhSUuaczQkOZU"));
		}
		ConcurrentHashMap<String, String> dbView = getViewFromDB();
		if(dbView != null ){
			//mergeViews() DB view with self view
			
			//Send DB the mergedView
			
			//mergeWithSelf(mergedView)
		}
		
	}

	private static ConcurrentHashMap<String, String> getViewFromDB() {
		
		SelectResult result = null;
		SelectRequest sr = new SelectRequest("select * from " + DOMAIN_NAME,
				Boolean.TRUE);
		result = sdb.select(sr);

		for (Item item : result.getItems()) {
			String s = item.getName();
			// System.out.println(s);
			if (s.equals(ITEM_NAME)) {
				List<Attribute> attr = item.getAttributes();
				String viewString = attr.get(0).getValue();
				return (ViewManager.stringToHashMap(viewString));

			}
		}
		return null;
	}

}
