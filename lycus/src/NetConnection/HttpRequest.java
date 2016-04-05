/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package NetConnection;

import java.io.IOException;
import java.io.StringWriter;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.HttpClientBuilder;

import Utils.GeneralFunctions;

/**
 *
 * @author Roi
 */
enum RequestTypes {

    POST, GET
}

public class HttpRequest {

    private String url;
    private RequestTypes type;
    private int timeOut;
    private String credentials;//credentials format User:Pass
    private HttpClient httpClient;
    private HttpRequestBase request;
    private HttpResponse response;
    private RequestConfig requestConfig;
    private int responseStatusCode;
    private long responseQueryTime;
    private HttpEntity responseEntity;
    private String responsePageContent;
    private boolean isTimeOut = false;

    public HttpRequest(String url, RequestTypes type, int timeOut) {
        this.url = url;
        this.type = type;
        this.timeOut = timeOut;
        //set time out to http client
        this.requestConfig = RequestConfig.custom().setConnectTimeout(timeOut).build();
      
        this.httpClient = HttpClientBuilder.create().setDefaultRequestConfig(requestConfig).build();
        
        //initialize request
        this.request = (type == RequestTypes.POST) ? new HttpPost(url) : new HttpGet(url);
    }

    public HttpRequest(String url, RequestTypes type, int timeOut, String credentials) {
        this.url = url;
        this.type = type;
        this.timeOut = timeOut;
        this.credentials = credentials;
        //set time out to http client
        this.requestConfig = RequestConfig.custom().setConnectTimeout(timeOut).build();
        this.httpClient = HttpClientBuilder.create().setDefaultRequestConfig(requestConfig).build();
        //initialize request
        this.request = (type == RequestTypes.POST) ? new HttpPost(url) : new HttpGet(url);
            this.request.setHeader("Authorization", "Basic " + GeneralFunctions.Base64Encode(credentials));
        
    }

    //#region Getters/Setters
    /**
     * @return the url
     */
    public String getUrl() {
        return url;
    }

    /**
     * @param url the url to set
     */
    public void setUrl(String url) {
        this.url = url;
    }

    /**
     * @return the type
     */
    public RequestTypes getType() {
        return type;
    }

    /**
     * @param type the type to set
     */
    public void setType(RequestTypes type) {
        this.type = type;
    }

    /**
     * @return the timeOut
     */
    public long getTimeOut() {
        return timeOut;
    }

    /**
     * @param timeOut the timeOut to set
     */
    public void setTimeOut(int timeOut) {
        this.timeOut = timeOut;
    }

    /**
     * @return the credentials
     */
    public String getCredentials() {
        return credentials;
    }

    /**
     * @param credentials the credentials to set
     */
    public void setCredentials(String credentials) {
        this.credentials = credentials;
    }
    public int getResponseStatusCode() {
        return responseStatusCode;
    }

    public long getResponseQueryTime() {
        return responseQueryTime;
    }

    public HttpEntity getResponseEntity() {
        return responseEntity;
    }

    public boolean getIsTimeOut() {
        return isTimeOut;
    }

    public String getResponsePageContent() {
        return responsePageContent;
    }
    //#endregion

    public void Execute() {
        long start = System.currentTimeMillis();
        try {
            response = httpClient.execute(request);
            if (response != null) {
                responseStatusCode = response.getStatusLine().getStatusCode();
                responseEntity = response.getEntity();
                StringWriter sw = new StringWriter();
                IOUtils.copy(responseEntity.getContent(), sw, "UTF-8");
                responsePageContent = sw.toString();
                responseQueryTime = System.currentTimeMillis() - start;
            } else {
                responseStatusCode = 0;
                responseEntity = null;
                responseQueryTime = 0;
                responsePageContent=null;
                isTimeOut = true;
            }
        } catch (IOException | IllegalStateException e) {
            responseStatusCode = 0;
            responseEntity = null;
            responseQueryTime = 0;
            responsePageContent = null;
            isTimeOut = true;

        } finally {
            if (request != null) {
                request.releaseConnection();
            }
        }
    }

    
}
