package Interfaces;

import DAL.ApiRequest;

public interface IFailedRequestsHandler {
void addRequest(ApiRequest request);
int getNumberOfFailedRequests();
void executeRequests();

}
