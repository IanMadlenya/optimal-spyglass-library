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

package com.optimalbi.Services;

import com.amazonaws.regions.Region;
import com.amazonaws.services.rds.AmazonRDSClient;
import com.amazonaws.services.rds.model.DBInstance;
import com.amazonaws.services.rds.model.DescribeDBInstancesResult;
import com.optimalbi.Controller.Containers.AmazonCredentials;
import org.timothygray.SimpleLog.*;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

import java.util.List;
import java.util.Map;

/**
 * Created by Timothy Gray(timg) on 3/12/2014.
 * Version: 0.0.2
 */
public class LocalRDSService extends AmazonService {
    private final Region region;
    private DBInstance thisService;
    private VBox drawing = null;
    private Map<String, Double> pricing = null;
    private Label instanceState;

    public LocalRDSService(String id, AmazonCredentials credentials, Region region, DBInstance instance, Logger logger) {
        super(id, credentials, logger);
        this.thisService = instance;
        this.region = region;
    }

    public void refreshInstance() {
        AmazonRDSClient rds = new AmazonRDSClient(getCredentials().getCredentials());
        rds.setRegion(region);

        DescribeDBInstancesResult result = rds.describeDBInstances();
        List<DBInstance> instances = result.getDBInstances();
        DBInstance tempInstance = null;
        for(DBInstance d : instances){
            if(d.getDBInstanceIdentifier().equalsIgnoreCase(thisService.getDBInstanceIdentifier())){
                tempInstance = d;
            }
        }
        if(tempInstance != null){
            thisService = tempInstance;
        } else {
            logger.error("Failed to refresh " + this.serviceName());
        }
    }

    public String serviceState() {
        return thisService.getDBInstanceStatus();
    }

    public String serviceName() {
        return thisService.getDBInstanceIdentifier();
    }

    public String serviceSize() {
        return thisService.getDBInstanceClass();
    }

    public double servicePrice() {
        if(pricing != null){
            if (pricing.containsKey(this.serviceSize())) {
                return pricing.get(this.serviceSize());
            }
        }
        return 0;
    }

    public Region serviceRegion() {
        return region;
    }

    public void attachPricing(Map<String, Double> pricing) {
        this.pricing = pricing;
    }

    public Map<String, Double> getPricing(){
        return pricing;
    }

    public String serviceType() {
        return "RDS";
    }

}
