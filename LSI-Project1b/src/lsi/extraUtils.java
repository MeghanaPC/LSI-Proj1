package lsi;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

import javax.swing.text.StyledEditorKit.ForegroundAction;

import View.*;


public class extraUtils {
	
	private static final String DELIMITER_LEVEL1= "-";
	private static final String DELIMITER_LEVEL2 = "_";

	public static ConcurrentHashMap<String, String> stringToHashMap(String toBeParsed){
		
		ConcurrentHashMap<String, String> resultMap = new ConcurrentHashMap<String, String>();
		String[] tuples = toBeParsed.split(DELIMITER_LEVEL1);
		
		for(String entry:tuples){
			String[] parseEntry = entry.split(DELIMITER_LEVEL2);
			resultMap.put(parseEntry[0], parseEntry[1]+DELIMITER_LEVEL2+parseEntry[2]);
		}
		
		return resultMap;
	}
	
	public static String hashMapToString(ConcurrentHashMap<String, String> map){
		
		String result = "blah";
		//StringBuilder
		
		return result;
	}
	
//	For testing purposes
	/*
	public static void main(String[] args) {
		
		extraUtils eutils = new extraUtils();
		ConcurrentHashMap<String, String> demoMap = eutils.stringToHashMap("server1_up_123456-server2_down_345678");
		View.ServerView testView = new View.ServerView();
		testView.iterateHashMap(demoMap);
	}
	*/ 
}
