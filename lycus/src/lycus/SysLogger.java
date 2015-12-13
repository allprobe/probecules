/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package lycus;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.productivity.java.syslog4j.Syslog;
import org.productivity.java.syslog4j.SyslogIF;

public class SysLogger {

	private static boolean debug;
	private static boolean development;
	private static SyslogIF syslogInterface;
	private static SimpleDateFormat dateFormat;
	private static File proberLog;
	private static String grep;

	public static boolean Init() {
		
		syslogInterface = Syslog.getInstance("udp");
		syslogInterface.getConfig().setHost(Global.getSyslogHost());
		syslogInterface.getConfig().setPort(514);
		syslogInterface.getConfig().setFacility("local3");
		syslogInterface.getConfig().setLocalName("PROBECULES");
		syslogInterface.getConfig().setIdent(Global.getDataCenterID() + "_" + Global.getThisHostToken());

		debug = Global.getDebug();
		development = Global.getDevelopment();
		dateFormat = new SimpleDateFormat(Global.getDateFormat());
		setGrep(null);
		if (!development) {
			proberLog = new File("/var/log/allprobe/probecules-" + Global.getDataCenterID() + "_"
					+ Global.getThisHostToken() + ".log");
			try {
				FileUtils.write(proberLog, "---Start---", true);
			} catch (IOException ex) {
				System.out.println("unable to write log file!");
				return false;
			}
		}
		return true;

	}

	public static String getGrep() {
		return grep;
	}

	public static void setGrep(String grep) {
		SysLogger.grep = grep;
	}

	public static void Record(Log log) {
		if (getGrep() != null)
			if (!log.getInfo().contains(getGrep()))
				return;
		
		String fullMessage="";
		
		Throwable t=log.getException();
		if (t != null) {
			fullMessage+="Exception message: "+t.getMessage()+" \n";
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			t.printStackTrace(pw);
			fullMessage+="Exception stacktrace: "+sw.toString()+" \n";
		}
		fullMessage+="Log info: "+log.getInfo();
		
		switch (log.getLogType()) {
		case Info:
			if (Global.getSyslogHost() == null)
				RecordInfo(log);
			else
			{	fullMessage=BashColor.GREEN+fullMessage+BashColor.NO_COLOR;
				syslogInterface.info(fullMessage);
			}
			break;
		case Warn:
			if (Global.getSyslogHost() == null)
				RecordWarn(log);
			else{
				fullMessage=BashColor.YELLOW+fullMessage+BashColor.NO_COLOR;
				syslogInterface.warn(fullMessage);
			}break;
		case Error:
			if (Global.getSyslogHost() == null)
				RecordError(log);
			else
			{	fullMessage=BashColor.RED+fullMessage+BashColor.NO_COLOR;
				syslogInterface.error(fullMessage);
			}break;
		case Debug:
			if (Global.getSyslogHost() == null)
				RecordDebug(log);
			else
			{	fullMessage=BashColor.PURPLE+fullMessage+BashColor.NO_COLOR;
				syslogInterface.debug(fullMessage);
			}break;
		}
	}

	private static void RecordInfo(Log log) {
		String s = dateFormat.format(Calendar.getInstance().getTime());
		s += " [I]";
		s += " <-> " + log.getInfo() + "\r\n";
		if (development) {
			System.out.print(s);
		} else {
			try {
				FileUtils.write(proberLog, s, true);
			} catch (IOException ex) {
				System.out.println("unable to write log file!");
			}
		}
	}

	private static void RecordWarn(Log log) {
		String s = dateFormat.format(Calendar.getInstance().getTime());
		s += " [W]";
		s += " <-> " + log.getInfo() + "\r\n";
		if (development) {
			System.out.print(s);
		} else {
			try {
				FileUtils.write(proberLog, s, true);
			} catch (IOException ex) {
				System.out.println("unable to write log file!");
			}
		}
	}

	private static void RecordError(Log log) {
		String s = dateFormat.format(Calendar.getInstance().getTime());
		s += " [E]";
		s += " <-> " + log.getInfo();
		if (log.getException() != null)
			s += " ==ex==>> " + log.getException().getMessage();
		s += "\r\n";
		if (development) {
			System.out.print(s);
		} else {
			try {
				FileUtils.write(proberLog, s, true);
			} catch (IOException ex) {
				System.out.println("unable to write log file!");
			}
		}
	}

	private static void RecordDebug(Log log) {
		String s = dateFormat.format(Calendar.getInstance().getTime());
		s += " [D]";
		s += " <-> " + log.getInfo() + "\r\n";
		if (debug) {
			if (development) {
				System.out.print(s);
			} else {
				try {
					FileUtils.write(proberLog, s, true);
				} catch (IOException ex) {
					System.out.println("unable to write log file!");
				}
			}
		}
	}

	private static void RecordMessage(String s) {
		if (debug) {
			if (development) {
				System.out.println(s);
			} else {
				try {
					FileUtils.write(proberLog, s, true);
				} catch (IOException ex) {
					System.out.println("unable to write log file!");
				}
			}
		}
	}

}
