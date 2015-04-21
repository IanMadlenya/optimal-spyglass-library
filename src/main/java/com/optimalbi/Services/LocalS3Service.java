package com.optimalbi.Services;

import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.model.TableDescription;
import com.amazonaws.services.s3.model.Bucket;
import com.optimalbi.Controller.Containers.AmazonCredentials;
import com.optimalbi.SimpleLog.*;

import java.util.Collections;
import java.util.Map;

/**
 * Created by Timothy Gray on 21/04/2015.
 */
public class LocalS3Service extends AmazonService implements Comparable<Service> {
    private Bucket thisService;

    public LocalS3Service(String id, AmazonCredentials credentials, Region region, Bucket bucket, Logger logger) {
        super(id, credentials, logger);
        thisService = bucket;
    }

    @Override
    public void refreshInstance() {
        //No refresh yet
    }

    @Override
    public String serviceState() {
        return "active";
    }

    @Override
    public String serviceType() {
        return "S3";
    }

    @Override
    public String serviceName() {
        return thisService.getName();
    }

    @Override
    public String serviceSize() {
        return "";
    }

    @Override
    public double servicePrice() {
        return 0;
    }

    @Override
    public Region serviceRegion() {
        return Region.getRegion(Regions.DEFAULT_REGION);
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
}
