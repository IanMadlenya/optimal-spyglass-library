package com.optimalbi.Services;

import com.amazonaws.regions.Region;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.document.TableCollection;
import com.amazonaws.services.dynamodbv2.model.ListTablesResult;
import com.amazonaws.services.dynamodbv2.model.TableDescription;
import com.optimalbi.Controller.Containers.AmazonCredentials;
import com.optimalbi.SimpleLog.Logger;

import java.util.Collections;
import java.util.Map;
import java.util.Timer;

/**
 * Creates a local representation of a DynamoDB instance
 * @author Timothy Gray
 */
public class LocalDynamoDBService extends AmazonService implements Comparable<Service>{
    private TableDescription thisInstance;
    private Region region;
    //Global Main Components
    private Timer timer = new Timer();
    private Map<String, Double> pricing = null;
    private String tagString;


    public LocalDynamoDBService(String id, AmazonCredentials credentials, Region region, TableDescription thisInstance, Logger logger) {
        super(id, credentials,logger);
        this.region = region;
        this.thisInstance = thisInstance;
        refreshInstance();
        addShutdownHook();
        tagString = Service.getTagsString(this);
    }

    @Override
    public void refreshInstance() {
        AmazonDynamoDBClient DDB = new AmazonDynamoDBClient(getCredentials().getCredentials());
        DDB.setRegion(region);
        DynamoDB dynamoDB = new DynamoDB(DDB);
        TableCollection<ListTablesResult> tables = dynamoDB.listTables(thisInstance.getTableName());
        for (Table table : tables) {
            if(table.describe().getTableName().equals(thisInstance.getTableName())){
                thisInstance = table.describe();
            }
        }
    }

    @Override
    public String serviceState() {
        return thisInstance.getTableStatus();
    }

    @Override
    public String serviceType() {
        return "DynamoDB";
    }

    @Override
    public String serviceName() {
        return thisInstance.getTableName();
    }

    @Override
    public String serviceSize() {
        double gbSize = thisInstance.getTableSizeBytes();
        String retString = " B";
        if(gbSize > 1024){
            gbSize = gbSize / 1024;
            retString = " KB";
        }
        if(gbSize > 1024){
            gbSize = gbSize / 1024;
            retString = " MB";
        }
        if(gbSize > 1024){
            gbSize = gbSize / 1024;
            retString = " GB";
        }
        return gbSize + retString;
    }

    public String serviceRate(){
        return String.format("Read: %d \nWrite: %d",thisInstance.getProvisionedThroughput().getReadCapacityUnits(),thisInstance.getProvisionedThroughput().getWriteCapacityUnits());
    }

    @Override
    public double servicePrice() {
        return 0;
    }

    @Override
    public Region serviceRegion() {
        return region;
    }

    @Override
    public void attachPricing(Map<String, Double> pricing) {

    }

    @Override
    public Map<String, Double> getPricing() {
        return Collections.emptyMap();
    }

    @Override
    public Map<String, String> getTags() {
        return Collections.emptyMap();
    }

    @Override
    public String getTagsString() {
        return tagString;
    }

    private void addShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            timer.cancel();
            timer.purge();
            timer = null;
        }));
    }
}
