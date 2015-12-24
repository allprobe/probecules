/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package lycus;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.regex.Pattern;

import org.apache.commons.codec.binary.Base64;
import org.snmp4j.PDU;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.VariableBinding;

/**
 *
 * @author Roi
 */
public class GeneralFunctions {

    private static SysLogger logger = new SysLogger();

    public static String Base64Encode(String uri) {

        String s = new String(Base64.encodeBase64(uri.getBytes()));
        return s;
    }

    public static String Base64Decode(String uri) {
		String s = new String(Base64.decodeBase64(uri.getBytes()));
        return s;
    }

    public static byte[] fromStringToBytes(String s, String encoding) {
        Map vars=new HashMap<>();
        try {
            return s.getBytes(encoding);
        } catch (UnsupportedEncodingException ex) {
            vars.put("s", s);
            logger.Record(new Log("Encoding Problem","Encoding","fromStringToBytes",vars,LogType.Debug,ex));
            return null;
        }
    }

    public static Date epuchToDate(long epuch) {
        return new Date(epuch);
    }

    public static long getCurrentTimeSec()
	{
		return System.currentTimeMillis()/1000;
	}
    public static String readFile(String path) 
    		  throws IOException 
    		{
    		  byte[] encoded = Files.readAllBytes(Paths.get(path));
    		  return new String(encoded, StandardCharsets.UTF_8);
    		}
    public static long fromMinutesToSeconds(int minutesNumber)
    {
    	return minutesNumber*60;
    }
    public static List<String> valuesOrdered(String s) {
    	if(s.length()<2)
    		return null;
        List<String> list = new ArrayList<String>();
        String str = s.substring(1, s.length() - 1);
        str = str.replace("\"", "");
        String[] values = str.split(",");
        list.addAll(Arrays.asList(values));
        return list;
    }

    public static void printMap(Map<?, ?> map) {
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            System.out.println("Key: " + entry.getKey() + " --- " + "Value: " + entry.getValue());
        }
    }

    public static List<String> probeKeyFormatOrdered(String s) {
        List<String> list = new ArrayList<String>();
        String str = s.substring(s.indexOf("[") + 1, s.length() - 1);
        String[] values = str.split("~");
        list.addAll(Arrays.asList(values));
        return list;
    }
    public static String valuesFormat(Object result) {
		if (result == null) {
			return "";
		}
		if(!(result instanceof List<?>))
		{
			return "{"+result.toString()+"}";
		}
		else
		{
			List<Object> results=(List<Object>)result;
			if (results.isEmpty()) {
				return "{}";
			} else {
				String returned = "{" + results.get(0);
				for (int i=1;i<results.size();i++) {
					returned += "," + results.get(i);
				}
				returned += "}";
				return returned;
			}
		}

    }
    public static Map<String,Object> fromPDUtoMap(PDU pdu)
    {
    	Map<String,Object> oidsValues=new HashMap<String,Object>(); 
    	for(VariableBinding vb:pdu.getVariableBindings())
    	{
    		oidsValues.put(vb.getOid().toString(), vb.getVariable());
    	}
    	return oidsValues;
    }
    public static OID privOid(OID Oid)
    {
    	String oid=Oid.toString();
    	String[] vars=oid.split("\\.");
    	String lastVar=vars[vars.length-1];
    	int num=Integer.parseInt(lastVar);
    	if(num==0)
    	{
    	return new OID(oid.substring(0, oid.length()-2));	
    	}
    	String newOid="";
    	for(int i=0;i<vars.length-1;i++)
    	{
    	newOid+=vars[i]+".";	
    	}
    	num--;
    	newOid+=num;
    	return new OID(newOid);
    }
    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                                 + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }
    public static String humanReadableByteCount(long bytes) {
        int unit = 1000;
        if (bytes < unit) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(unit));
        String pre = ("kMGTPE").charAt(exp-1) + ("");
        return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
    }
    public static String convertStreamToString(java.io.InputStream is) {
        java.util.Scanner s = new java.util.Scanner(is,"UTF-8").useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }
    
    
}
