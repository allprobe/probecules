/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Utils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;
import org.snmp4j.PDU;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.VariableBinding;

import GlobalConstants.Enums;
import GlobalConstants.Enums.HostType;

/**
 *
 * @author Roi
 */
public class GeneralFunctions {

	public static String Base64Encode(String uri) {

		if(uri==null)
			return null;
		String s = new String(Base64.encodeBase64(uri.getBytes()));
		return s;
	}

	public static String Base64Decode(String uri) {
		if(uri==null)
			return null;
		String s = new String(Base64.decodeBase64(uri.getBytes()));
		return s;
	}

	public static byte[] fromStringToBytes(String s, String encoding) {
		Map vars = new HashMap<>();
		try {
			return s.getBytes(encoding);
		} catch (UnsupportedEncodingException ex) {
			vars.put("s", s);
			Logit.LogInfo("GeneralFunctions - GeneralFunctions, Encoding Problem x\n" + ex.getMessage());
			return null;
		}
	}

	public static Date epuchToDate(long epuch) {
		return new Date(epuch);
	}

	public static long getCurrentTimeSec() {
		return System.currentTimeMillis() / 1000;
	}

	public static String readFile(String path) throws IOException {
		byte[] encoded = Files.readAllBytes(Paths.get(path));
		return new String(encoded, StandardCharsets.UTF_8);
	}

	public static long fromMinutesToSeconds(int minutesNumber) {
		return minutesNumber * 60;
	}

	public static List<String> valuesOrdered(String s) {
		if (s.length() < 2)
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
		if (!(result instanceof List<?>)) {
			return "{" + result.toString() + "}";
		} else {
			List<Object> results = (List<Object>) result;
			if (results.isEmpty()) {
				return "{}";
			} else {
				String returned = "{" + results.get(0);
				for (int i = 1; i < results.size(); i++) {
					returned += "," + results.get(i);
				}
				returned += "}";
				return returned;
			}
		}

	}

	public static Map<String, Object> fromPDUtoMap(PDU pdu) {
		Map<String, Object> oidsValues = new HashMap<String, Object>();
		for (VariableBinding vb : pdu.getVariableBindings()) {
			oidsValues.put(vb.getOid().toString(), vb.getVariable());
		}
		return oidsValues;
	}

	public static OID privOid(OID Oid) {
		String oid = Oid.toString();
		String[] vars = oid.split("\\.");
		String lastVar = vars[vars.length - 1];
		int num = Integer.parseInt(lastVar);
		if (num == 0) {
			return new OID(oid.substring(0, oid.length() - 2));
		}
		String newOid = "";
		for (int i = 0; i < vars.length - 1; i++) {
			newOid += vars[i] + ".";
		}
		num--;
		newOid += num;
		return new OID(newOid);
	}

	public static byte[] hexStringToByteArray(String s) {
		int len = s.length();
		byte[] data = new byte[len / 2];
		for (int i = 0; i < len; i += 2) {
			data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character.digit(s.charAt(i + 1), 16));
		}
		return data;
	}

	public static String humanReadableByteCount(long bytes) {
		int unit = 1000;
		if (bytes < unit)
			return bytes + " B";
		int exp = (int) (Math.log(bytes) / Math.log(unit));
		String pre = ("kMGTPE").charAt(exp - 1) + ("");
		return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
	}

	public static String convertStreamToString(java.io.InputStream is) {
		java.util.Scanner s = new java.util.Scanner(is, "UTF-8").useDelimiter("\\A");
		return s.hasNext() ? s.next() : "";
	}

//	public static String convertHexToString(String hex) {
//
//		StringBuilder sb = new StringBuilder();
//		StringBuilder temp = new StringBuilder();
//
//		// 49204c6f7665204a617661 split into two characters 49, 20, 4c...
//		for (int i = 0; i < hex.length() - 1; i += 2) {
//
//			// grab the hex in pairs
//			String output = hex.substring(i, (i + 2));
//			// convert hex to decimal
//			int decimal = Integer.parseInt(output, 16);
//			// convert the decimal to character
//			sb.append((char) decimal);
//
//			temp.append(decimal);
//		}
//		return sb.toString();
//	}
	
	public static String convertHexToString(String hex) {
	String hexString = 		hex.replace(":","");
   
	byte[] bytes=null;
	try {
		bytes = Hex.decodeHex(hexString.toCharArray());
	} catch (DecoderException e) {
		Logit.LogError("GeneralFunctions - convertHexToString", "failed to convert hex to string! : "+hex+", E: "+e.getMessage());
		return null;
	}
	try {
		String result=new String(bytes, "UTF-8");
		return result;
	} catch (UnsupportedEncodingException e) {
		Logit.LogError("GeneralFunctions - convertHexToString", "failed to convert hex to string! : "+hex+", E: "+e.getMessage());
		return null;
	}
	}
	public static boolean isNullOrEmpty(String str) {
		return str == null || str.isEmpty();
	}

	public static boolean isChanged(String oldStr, String newStr)
	{
		
		return !isNullOrEmpty(newStr) && (oldStr != null ? !oldStr.equals(newStr) : true);
	}
	
	public static boolean isChanged(float oldVal, float newVal)
	{
		return newVal != 0 && oldVal != newVal;
	}
	
	public static boolean isChanged(int oldVal, Integer newVal)
	{
		return newVal != null && oldVal != newVal;
	}
	
	
	public static String getRunnableProbeId(UUID templateId, UUID hostId, String probeId) {
		return templateId.toString() + "@" + hostId.toString() + "@" + probeId;
	}

	public static String getRunnableProbeId(String templateId, String hostId, String probeId) {
		return templateId + "@" + hostId + "@" + probeId;
	}
	
	public int getNumberOfRollupTables(long interval) {
		if (interval < 240)
			return 6;
		if (interval >= 240 && interval < 1200)
			return 5;
		if (interval >= 1200 && interval < 3600)
			return 4;
		if (interval >= 3600 && interval < 21600)
			return 3;
		if (interval >= 21600)
			return 2;
		return 0;
	}

	public static HostType getHostType(String string) {
		if (string.contains("Linux"))
			return Enums.HostType.Linux;
		if (string.contains("Windows"))
			return Enums.HostType.Windows;
		return null;
	}
	

}
