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
import com.amazonaws.services.redshift.model.Cluster;
import com.amazonaws.services.redshift.model.Tag;
import com.optimalbi.Controller.Containers.AmazonCredentials;
import org.timothygray.SimpleLog.*;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

import java.util.*;


/**
 * Created by Timothy Gray(timg) on 27/11/2014.
 * Version: 0.0.2
 */
public class LocalRedshiftService extends AmazonService {
    private final Region region;
    private final Cluster thisCluster;
    private VBox drawing;
    private Label instanceState;
    private Map<String, Double> pricing = null;

    public LocalRedshiftService(String id, AmazonCredentials credentials, Region region, Cluster cluster, Logger logger) {
        super(id, credentials, logger);
        this.region = region;
        this.thisCluster = cluster;
    }

    public String serviceState() {
        return thisCluster.getClusterStatus();
    }

    public String serviceType() {
        return "Redshift";
    }

    public String serviceName() {
        return stringCap(thisCluster.getClusterIdentifier());
    }

    public String serviceSize() {
        return thisCluster.getNodeType();
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

    public Map<String,String> getTags(){
        List<Tag> tags = thisCluster.getTags();
        Map<String,String> tagMap = new HashMap<>();
        for(Tag t : tags){
            tagMap.put(t.getKey(),t.getValue());
        }
        return tagMap;
    }

    public void attachPricing(Map<String, Double> pricing) {
        this.pricing = pricing;
    }

    public Map<String, Double> getPricing(){
        return pricing;
    }

    public void refreshInstance() {

    }
}
