package system.ticketing.utilities.util;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Created by Ncube on 2/5/17.
 */
public class MapUtil {
    public static final String EQUALS ="==";
    public static final String SEPERATOR ="!!";

    public static String convertAttributesMapToString(Map<String,String> attributesMap){
        StringBuffer sb = new StringBuffer();
        for (String key : attributesMap.keySet()) {
            sb.append(key + EQUALS + attributesMap.get(key) + SEPERATOR );
        }
        return sb.toString();
    }

    public static Map<String,String> convertUserInfoListToMap(List<String> stringList, String seperator){
        Map<String,String> result = new TreeMap<String, String>();
        String[] sa ;
        for(String string : stringList){
            sa= string.split("\\"+seperator);
            result.put(sa[0], sa[1]);
        }
        return result;
    }

    public static Map<String,String> convertAttributesStringToMap(String attributesString){
        if(attributesString == null){
            return null;
        }
        Map<String, String> attributesMap = new HashMap<String, String>();
        int startIndex = 0;
        int endIndex =0;
        endIndex=attributesString.indexOf(EQUALS,startIndex);
        String key,value;
        while(endIndex >= 0){

            key = attributesString.substring(startIndex,endIndex);
            startIndex = endIndex + EQUALS.length();
            endIndex=attributesString.indexOf(SEPERATOR,startIndex);
            value =attributesString.substring(startIndex,endIndex);
            startIndex = endIndex + SEPERATOR.length();

            attributesMap.put(key, value);
            endIndex=attributesString.indexOf(EQUALS,startIndex);
        }
        return attributesMap;
    }

    public static String mapToString(Map<String, String> map) {
        StringBuilder stringBuilder = new StringBuilder();

        for (String key : map.keySet()) {
            if (stringBuilder.length() > 0) {
                stringBuilder.append("&");
            }
            String value = map.get(key);
            try {
                stringBuilder.append((key != null ? URLEncoder.encode(key, "UTF-8") : ""));
                stringBuilder.append("=");
                stringBuilder.append(value != null ? URLEncoder.encode(value, "UTF-8") : "");
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException("This method requires UTF-8 encoding support", e);
            }
        }

        return stringBuilder.toString();
    }

    public static Map<String, String> stringToMap(String input) {
        Map<String, String> map = new HashMap<String, String>();

        String[] nameValuePairs = input.split("&");
        for (String nameValuePair : nameValuePairs) {
            String[] nameValue = nameValuePair.split("=");
            try {
                map.put(URLDecoder.decode(nameValue[0], "UTF-8"), nameValue.length > 1 ? URLDecoder.decode(
                        nameValue[1], "UTF-8") : "");
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException("This method requires UTF-8 encoding support", e);
            }
        }

        return map;
    }

    public static void main(String[] args){
        Map<String, String> attributesMap = new HashMap<String, String>();
        attributesMap.put("name", "prince");
        attributesMap.put("surname", "kaguda");
        String str =mapToString(attributesMap);
        System.out.println(">>>>>>>>>>>>> "+stringToMap(str).get("name"));


    }
}
