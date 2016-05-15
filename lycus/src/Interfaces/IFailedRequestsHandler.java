package Interfaces;

import DAL.ApiRequest;

public interface IFailedRequestsHandler {
public void addRequest(ApiRequest request);
public int getNumberOfFailedRequests();
public void executeRequests();

}
