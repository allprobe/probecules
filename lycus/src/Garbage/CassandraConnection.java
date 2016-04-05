/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Garbage;

import java.util.HashMap;
import java.util.Map;

import me.prettyprint.cassandra.model.QuorumAllConsistencyLevelPolicy;
import me.prettyprint.cassandra.service.CassandraHostConfigurator;
import me.prettyprint.hector.api.Cluster;
import me.prettyprint.hector.api.Keyspace;
import me.prettyprint.hector.api.factory.HFactory;

/**
 *
 * @author Roi
 */
public class CassandraConnection {
    private String userName;
    private String password;
    private final Cluster cluster;
    private final Keyspace keyspace;
    
    public CassandraConnection(String userName,String password,String cluster,String keyspace) throws Exception
    {
        this.userName=userName;
        this.password=password;
        Map<String, String> AccessMap = new HashMap<>();
        AccessMap.put("username", userName);
        AccessMap.put("password", password);
       
        try
        {
        this.cluster = HFactory.createCluster("Main", new CassandraHostConfigurator(cluster), AccessMap);
        this.keyspace = HFactory.createKeyspace(keyspace, this.cluster, new QuorumAllConsistencyLevelPolicy());
        }
        catch(Exception ex)
        {
        	throw new Exception("Error Setting Cassandra Connection!");
        }
        
    }
    
    //#region Getters/Setters
    /**
     * @return the userName
     */
    public String getUserName() {
        return userName;
    }

    /**
     * @param userName the userName to set
     */
    public void setUserName(String userName) {
        this.userName = userName;
    }

    /**
     * @return the password
     */
    public String getPassword() {
        return password;
    }

    /**
     * @param password the password to set
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * @return the cluster
     */
    public Cluster getCluster() {
        return cluster;
    }

    /**
     * @return the keyspace
     */
    public Keyspace getKeyspace() {
        return keyspace;
    }
    //#endregion
}
