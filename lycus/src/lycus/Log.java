/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package lycus;

import java.util.Map;

/**
 *
 * @author Roi
 */
public class Log {
    private String Info;
    private String Class;
    private String Function;
    private Map<String,Object> Variables;//name - value
    private LogType logType;
    private Exception exception;
    
    // <editor-fold defaultstate="collapsed" desc="Constructors">
    public Log(String Info,String Class, String Function,Map<String,Object> Vars,LogType type,Exception e)
    {
    this.Info=Info;
    this.Class=Class;
    this.Function=Function;
    this.Variables=Vars;
    this.logType=type;
    this.exception=e;
    }
    public Log(String Info,String Class, String Function,Map<String,Object> Vars,LogType type)
    {
    this.Info=Info;
    this.Class=Class;
    this.Function=Function;
    this.Variables=Vars;
    this.logType=type;
    this.exception=null;
    }
    public Log(String Info,String Class, String Function,LogType type)
    {
    this.Info=Info;
    this.Class=Class;
    this.Function=Function;
    this.Variables=null;
    logType=type;
    this.exception=null;
    }
    public Log(String Info,String Class, String Function,LogType type,Exception e)
    {
    this.Info=Info;
    this.Class=Class;
    this.Function=Function;
    this.Variables=null;
    logType=type;
    this.exception=e;
    }
     public Log(String Info,LogType type)
    {
    this.Info=Info;
    this.Class="Unknown";
    this.Function="Unknown";
    this.Variables=null;
    logType=type;
    this.exception=null;
    }
     public Log(String Info,LogType type,Exception e)
    {
    this.Info=Info;
    this.Class="Unknown";
    this.Function="Unknown";
    this.Variables=null;
    logType=type;
    this.exception=e;
    }
         // </editor-fold>
     
    // <editor-fold defaultstate="collapsed" desc="Properties">

    /**
     * @return the Info
     */
    public String getInfo() {
        return Info;
    }

    /**
     * @param Info the Info to set
     */
    public void setInfo(String Info) {
        this.Info = Info;
    }

    /**
     * @return the Class
     */
    public String get_Class() {
        return Class;
    }

    /**
     * @param Class the Class to set
     */
    public void setClass(String Class) {
        this.Class = Class;
    }

    /**
     * @return the Function
     */
    public String getFunction() {
        return Function;
    }

    /**
     * @param Function the Function to set
     */
    public void setFunction(String Function) {
        this.Function = Function;
    }

    /**
     * @return the Variables
     */
    public Map<String,Object> getVariables() {
        return Variables;
    }

    /**
     * @param Variables the Variables to set
     */
    public void setVariables(Map<String,Object> Variables) {
        this.Variables = Variables;
    }

    /**
     * @return the logType
     */
    public LogType getLogType() {
        return logType;
    }

    /**
     * @param logType the logType to set
     */
    public void setLogType(LogType logType) {
        this.logType = logType;
    }
/**
     * @return the exception
     */
    public Exception getException() {
        return exception;
    }

    /**
     * @param exception the exception to set
     */
    public void setException(Exception exception) {
        this.exception = exception;
    }

    // </editor-fold>

    
    
}
