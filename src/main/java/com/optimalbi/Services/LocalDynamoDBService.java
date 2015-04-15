package com.optimalbi.Services;

import com.amazonaws.regions.Region;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.model.ListTablesResult;
import com.amazonaws.services.dynamodbv2.model.TableDescription;
import com.amazonaws.services.ec2.AmazonEC2;
import com.optimalbi.Controller.Containers.AmazonCredentials;
import org.timothygray.SimpleLog.Logger;

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


    public LocalDynamoDBService(String id, AmazonCredentials credentials, Region region, TableDescription thisInstance, Logger logger) {
        super(id, credentials, logger);
        this.region = region;
        this.thisInstance = thisInstance;
        refreshInstance();
        addShutdownHook();
    }

    @Override
    public void refreshInstance() {
        //No refresh for DynamoDB
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
        return String.format("Read: %d Write: %d",thisInstance.getProvisionedThroughput().getReadCapacityUnits(),thisInstance.getProvisionedThroughput().getWriteCapacityUnits());
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

    private void addShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            timer.cancel();
            timer.purge();
            timer = null;
        }));
    }
}
