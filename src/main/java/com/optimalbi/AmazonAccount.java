/*
   Copyright 2015 OptimalBI

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */

package com.optimalbi;

import com.amazonaws.AmazonClientException;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.regions.ServiceAbbreviations;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.TableCollection;
import com.amazonaws.services.dynamodbv2.model.ListTablesResult;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.DescribeInstancesResult;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.Reservation;
import com.amazonaws.services.glacier.AmazonGlacierClient;
import com.amazonaws.services.glacier.model.*;
import com.amazonaws.services.rds.AmazonRDSClient;
import com.amazonaws.services.rds.model.DBInstance;
import com.amazonaws.services.rds.model.DescribeDBInstancesResult;
import com.amazonaws.services.redshift.AmazonRedshiftClient;
import com.amazonaws.services.redshift.model.Cluster;
import com.amazonaws.services.redshift.model.DescribeClustersResult;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.Bucket;
import com.optimalbi.Controller.Containers.AmazonCredentials;
import com.optimalbi.Services.*;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import org.apache.commons.lang.Validate;
import com.optimalbi.SimpleLog.*;

import java.util.*;
import java.util.function.Consumer;

/**
 * This creates a local representation of a AWS Account and its service
 *
 * @author Timothy Gray
 */
public class AmazonAccount {
    private final AmazonCredentials credentials;
    private final List<Region> regions;
    private final Logger logger;

    private final BooleanProperty ready = new SimpleBooleanProperty(false);
    private final IntegerProperty completed = new SimpleIntegerProperty(0);
    //File names
    private Map<Region, ServicePricing> servicePricings;
    private boolean readyValue = false;
    private Set<Service> services;
    //Statistics
    private int totalServices = 0;
    private int runningServices = 0;
    private Map<String, Integer> runningCount;

    /**
     * Amazon Account hold all the information needed to describe one account from many regions
     *
     * @param credentials     The AWS credentials this account will use to authenticate with the AWS Cloud
     * @param regions         The regions this account is interested in
     * @param logger          The SimpleLogger this will use for reporting
     * @param servicePricings A optional object that describes the costs of various services
     */
    public AmazonAccount(AmazonCredentials credentials, List<Region> regions, Logger logger, Map<Region, ServicePricing> servicePricings) {
        this.credentials = credentials;
        this.regions = regions;
        this.logger = logger;
        if (servicePricings != null) {
            this.servicePricings = servicePricings;
        }
        runningCount = new HashMap<>();
    }

    /**
     * Starts the process of polling the account for AWS services
     */
    public void startConfigure() {
        try {
            configure();
            if (getRegions().size() > 0) {
                populateEc2();
                populateRedshift();
                populateRDS();
                populateDynamoDB();
                populateS3();
                populateGlacier();
            }
        } catch (AmazonClientException e) {
            getLogger().error("Error in starting service: " + e.getMessage());
        } finally {
            populateStatistics();
            readyValue = true;
            ready.setValue(true);
        }
    }

    private void configure() throws AmazonClientException {
        services = new HashSet<>();
        if (getCredentials() == null) {
            throw new AmazonClientException("No credentials provided");
        }
    }

    private void populateEc2() throws AmazonClientException {
        for (Region region : getRegions()) {
            try {
//                services.addAll(Ec2Service.populateServices(region, getCredentials(), getLogger(), pricing));
                AmazonEC2Client ec2 = new AmazonEC2Client(getCredentials().getCredentials());
                ec2.setRegion(region);
                DescribeInstancesResult describeInstancesRequest = ec2.describeInstances();
                List<Reservation> reservations = describeInstancesRequest.getReservations();
                Set<Instance> inst = new HashSet<>();
                for (Reservation reservation : reservations) {
                    inst.addAll(reservation.getInstances());
                }

                getLogger().info("EC2, Adding " + inst.size() + " instances from " + region.getName());

                for (Instance i : inst) {
                    Service temp = new LocalEc2Service(i.getInstanceId(), getCredentials(), region, ec2, getLogger());
                    if (servicePricings != null && servicePricings.size() > 0) {
                        temp.attachPricing(servicePricings.get(region).getEc2Pricing());
                    }
                    services.add(temp);
                }

            } catch (AmazonClientException e) {
                throw new AmazonClientException(region.getName() + " " + e.getMessage());
            }
            completed.set(completed.get() + 1);
        }

    }

    private void populateRedshift() throws AmazonClientException {
        for (Region region : getRegions()) {
            try {
                if (region.isServiceSupported(ServiceAbbreviations.RedShift)) {
//                    services.addAll(RedshiftService.populateServices(region, getCredentials(), getLogger()));
                    AmazonRedshiftClient redshift = new AmazonRedshiftClient(getCredentials().getCredentials());
                    redshift.setRegion(region);

                    DescribeClustersResult clusterResult;
                    List<Cluster> clusters;
                    try {
                        clusterResult = redshift.describeClusters();
                        clusters = clusterResult.getClusters();
                    } catch (Exception e) {
                        throw new AmazonClientException("Failed to get clusters " + e.getMessage());
                    }

                    getLogger().info("Redshift, Adding " + clusters.size() + " clusters from " + region.getName());
                    for (Cluster cluster : clusters) {
                        getLogger().info("Cluster: " + cluster.getClusterIdentifier());
                        LocalRedshiftService temp = new LocalRedshiftService(cluster.getDBName(), getCredentials(), region, cluster, getLogger());
                        if (servicePricings != null && servicePricings.size() > 0) {
                            temp.attachPricing(servicePricings.get(region).getRedshiftPricing());
                        }
                        services.add(temp);
                    }
                } else {
                    getLogger().info("Redshift, NOPE from " + region.getName());
                }
            } catch (AmazonClientException e) {
                throw new AmazonClientException(region.getName() + " " + e.getMessage());
            }
            completed.set(completed.get() + 1);
        }
    }

    private void populateRDS() throws AmazonClientException {
        for (Region region : getRegions()) {
            try {
                if (region.isServiceSupported(ServiceAbbreviations.RDS)) {
                    AmazonRDSClient rds = new AmazonRDSClient(getCredentials().getCredentials());
                    rds.setRegion(region);

                    DescribeDBInstancesResult result = rds.describeDBInstances();
                    List<DBInstance> instances = result.getDBInstances();

                    getLogger().info("RDS, Adding " + instances.size() + " instances from " + region.getName());

                    for (DBInstance i : instances) {
                        LocalRDSService temp;
                        if (i.getDBName() != null) {
                            temp = new LocalRDSService(i.getDBName(), getCredentials(), region, i, getLogger());
                        } else {
                            temp = new LocalRDSService(i.getDBInstanceIdentifier(), getCredentials(), region, i, getLogger());
                        }
                        if (servicePricings != null && servicePricings.size() > 0) {
                            if (servicePricings.get(region).getRDSPricing() != null) {
                                temp.attachPricing(servicePricings.get(region).getRDSPricing());
                            }
                        }
                        services.add(temp);
                    }
                } else {
                    getLogger().info("RDS, NOPE from " + region.getName());
                }
            } catch (AmazonClientException e) {
                throw new AmazonClientException(region.getName() + " " + e.getMessage());
            }
            completed.set(completed.get() + 1);
        }
    }

    private void populateDynamoDB() throws AmazonClientException {
        getRegions().stream().filter(region -> region.isServiceSupported(ServiceAbbreviations.Dynamodb)).forEach(region -> {
            AmazonDynamoDBClient DDB = new AmazonDynamoDBClient(credentials.getCredentials());
            DDB.setRegion(region);
            DynamoDB dynamoDB = new DynamoDB(DDB);
            TableCollection<ListTablesResult> tables = dynamoDB.listTables();
            tables.forEach(table -> services.add(new LocalDynamoDBService(table.getTableName(), credentials, region, table.describe(), logger)));
        });
    }

    private void populateS3() throws AmazonClientException {
        AmazonS3Client client = new AmazonS3Client(credentials.getCredentials());
        client.setRegion(regions.get(0));
        List<Bucket> buckets = client.listBuckets();
        for (Bucket bucket : buckets) {
            Service temp = new LocalS3Service(bucket.getName(), credentials, regions.get(0), bucket, logger);
            services.add(temp);
        }
    }

    private void populateGlacier() throws AmazonClientException{
        for(Region r : regions){
            if(r.isServiceSupported(ServiceAbbreviations.Glacier)){
                AmazonGlacierClient glacierClient = new AmazonGlacierClient(credentials.getCredentials());
                glacierClient.setRegion(r);
                ListVaultsResult result = glacierClient.listVaults(new ListVaultsRequest());
                List<DescribeVaultOutput> vaults = result.getVaultList();
                for(DescribeVaultOutput d : vaults){
                    DescribeVaultResult res = glacierClient.describeVault(new DescribeVaultRequest(d.getVaultName()));
                    Service temp = new LocalGlacierService(d.getVaultName(),credentials,r,res,logger);
                    services.add(temp);
                }
            }
        }
    }

    private void populateStatistics() {
        runningCount = new HashMap<>();
        totalServices = services.size();
        runningCount.put("costs", 0);
        runningServices = 0;
        runningCount.put("EC2", 0);
        runningCount.put("RDS", 0);
        runningCount.put("Redshift", 0);
        runningCount.put("DynamoDB", 0);
        runningCount.put("S3", 0);
        runningCount.put("Glacier",0);

        for (Service s : services) {
            if (Service.runningTitles().contains(s.serviceState().toLowerCase())) {
                runningServices++;
                if (runningCount.containsKey(s.serviceType())) {
                    runningCount.put(s.serviceType(), runningCount.get(s.serviceType()) +1);
                } else {
                    System.err.println("Service type not found: " + s.serviceType());
                }
            }
        }

    }

    public ServicePricing getPricing(Region region) {
        Validate.notNull(region);
        return servicePricings.get(region);
    }

    /**
     * This value will be set to ready when the account is fully populated
     */
    public BooleanProperty getReady() {
        return ready;
    }

    /**
     * This value will be set to ready when the account is fully populated
     */
    public boolean getReadyValue() {
        return readyValue;
    }

    /**
     * Returns a list of services attached to this account
     */
    public Set<Service> getServices() {
        return services;
    }

    /**
     * Returns the total number of services this account has
     */
    public int getTotalServices() {
        return totalServices;
    }

    /**
     * Returns the total number of services in a running state
     */
    public int getRunningServices() {
        return runningServices;
    }

    /**
     * Returns a map of running service count to service type
     */
    public Map<String, Integer> getRunningCount() {
        return runningCount;
    }

    /**
     * Returns the number of polled regions at the current point in time.
     * This is for use in progress bars and the like.
     */
    public IntegerProperty getCompleted() {
        return completed;
    }

    /**
     * Returns a list of regions that this account is polling.
     */
    List<Region> getRegions() {
        return regions;
    }

    /**
     * Returns the SimpleLogger that this is using
     */
    Logger getLogger() {
        return this.logger;
    }

    /**
     * Returns the AWS credentials used for this Account
     */
    public AmazonCredentials getCredentials() {
        return credentials;
    }
}
