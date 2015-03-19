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
import org.apache.commons.lang.Validate;

import java.util.*;

/**
 * Created by Timothy Gray(timg) on 25/11/2014.
 * Version: 0.0.2
 */
public interface Service extends Comparable<Service>{

    /**
     * Causes this instance to update is status from the AWS cloud
     */
    void refreshInstance();

    /**
     * The id of this service
     * @return The id
     */
    String serviceID();

    /**
     * The services current state
     * @return The state
     */
    String serviceState();

    /**
     * The services type, i.e EC2
     * @return The type
     */
    String serviceType();

    /**
     * The name as found under the tag Name in the AWS cloud
     * @return The name of this service
     */
    String serviceName();

    /**
     * The size as related to cost of this service
     * @return The size of this service
     */
    String serviceSize();

    /**
     * If the pricing information has been attached this will return the costs of this service, otherwise it returns 0
     * @return The cost of this service
     */
    double servicePrice();

    /**
     * This region this service is in
     * @return The AWS region of this service
     */
    Region serviceRegion();

    /**
     * Attaches a pricing information map to calculate costs of this service
     * @param pricing The map of service size to cost
     */
    void attachPricing(Map<String, Double> pricing);

    public Map<String, Double> getPricing();

    /**
     * Returns a map of region names to their friendly titles
     */
    public static Map<String,String> regionNames() {
        Map<String,String> regionNames = new HashMap<>();
        regionNames.put("ap-northeast-1","Asia Pacific (Tokyo)");
        regionNames.put("ap-southeast-1","Asia Pacific (Singapore)");
        regionNames.put("ap-southeast-2","Asia Pacific (Sydney)");
        regionNames.put("eu-central-1","EU (Frankfurt)");
        regionNames.put("eu-west-1","EU (Ireland)");
        regionNames.put("sa-east-1","South America (Sao Paulo)");
        regionNames.put("us-east-1","US East (N. Virginia)");
        regionNames.put("us-west-1","US West (N. California)");
        regionNames.put("us-west-2","US West (Oregon)");
        return regionNames;
    }

    /**
     * Returns a friendly title for a given region name
     * @param regionId The AWS region id
     */
    public static String friendlyRegionName(String regionId){
        Validate.notNull(regionId);
        if(regionNames().containsKey(regionId)){
            return regionId;
        } else {
            return null;
        }
    }

    /**
     * A set of titles that mean the service is currently in a state that incurs cost
     */
    public static Set<String> runningTitles(){
        Set<String> runningTitles = new HashSet<>();
        runningTitles.add("running");
        runningTitles.add("available");
        runningTitles.add("creating");
        return runningTitles;
    }
}
