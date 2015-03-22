package lsi;

import java.util.HashMap;
import java.util.Set;
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
			resultMap.put(parseEntry[0].trim(), parseEntry[1].trim()+DELIMITER_LEVEL2+parseEntry[2].trim());
		}
		
		return resultMap;
	}
	
	public static String hashMapToString(ConcurrentHashMap<String, String> map){
		
		StringBuilder resultBuilder = new StringBuilder();
		Set<String> resultKeySet = map.keySet();
		int length = 0;
		
		for(String key:resultKeySet){
			resultBuilder.append(key+DELIMITER_LEVEL2+map.get(key)+DELIMITER_LEVEL1);
		}
		
		length = resultBuilder.length();
		return resultBuilder.toString().substring(0, length-1);
	}
	
	public static void iterateHashMap(ConcurrentHashMap<String, String> hMap){
		
		for(String key:hMap.keySet()){
			System.out.println(key + " " + hMap.get(key));
		}
	}
	
	public static ConcurrentHashMap<String, String> mergeViews(ConcurrentHashMap<String, String> mapA, ConcurrentHashMap<String, String> mapB) {
		
		ConcurrentHashMap<String, String> mapM = new ConcurrentHashMap<String, String>();
		Set<String> keysetA = mapA.keySet();
		Set<String> keysetB = mapB.keySet();
		
		for(String keyA:keysetA){
			if (!mapB.containsKey(keyA)) {
				mapM.put(keyA, mapA.get(keyA));
			}
			else {
				String parseValueA = mapA.get(keyA);
				String parseValueB = mapB.get(keyA);
				
				String[] splitValueA = parseValueA.split("_");
				String[] splitValueB = parseValueB.split("_");
				
				if (Long.parseLong(splitValueA[1].trim()) >= Long.parseLong(splitValueB[1].trim())) {
					mapM.put(keyA, parseValueA);
				}
				else {
					mapM.put(keyA, parseValueB);
				}
				
			}
		}
	
		for(String keyB:keysetB){
			if (!mapA.containsKey(keyB)) {
				mapM.put(keyB, mapB.get(keyB));
			}
		}	
		return mapM;
	}
	
	
//	For testing purposes
	public static void main(String[] args) {
		
		ConcurrentHashMap<String, String> demoMap = stringToHashMap("server1_up_123456-server2_down_345678");
//		iterateHashMap(demoMap);
		
		System.out.println(hashMapToString(demoMap));
	}
	 
}
