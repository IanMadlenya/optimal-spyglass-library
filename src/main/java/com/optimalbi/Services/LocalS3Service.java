package com.optimalbi.Services;

import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.*;
import com.optimalbi.Controller.Containers.AmazonCredentials;
import com.optimalbi.SimpleLog.*;
import org.apache.commons.lang.Validate;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author Timothy Gray
 */
public class LocalS3Service extends AmazonService implements Comparable<Service> {
    private Bucket thisService;
    private String size;
    private int numberOfObjects;
    private String tagString;

    public LocalS3Service(String id, AmazonCredentials credentials, Region region, Bucket bucket, Logger logger) {
        super(id, credentials, logger);
        thisService = bucket;
        this.size = "";
        this.setNumberOfObjects(0);
        tagString = Service.getTagsString(this);
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
        return size;
    }

    public void setSize(String size){
        Validate.notNull(size);
        this.size = size;
    }

    @Override
    public double servicePrice() {
        return 0;
    }

    public List<S3ObjectSummary> getObjectSummaries(){
        AmazonS3Client client = new AmazonS3Client(getCredentials().getCredentials());
        ObjectListing objects = client.listObjects(thisService.getName());
        return objects.getObjectSummaries();
    }

    @Override
    public Region serviceRegion() {
        return Region.getRegion(Regions.DEFAULT_REGION);
    }

    /**
     * The method for attaching pricing for s3, currently not supported
     * @param pricing The map of service size to cost
     */
    @Override
    public void attachPricing(Map<String, Double> pricing) {
        throw new UnsupportedOperationException("Pricing not implemented for S3");
    }

    /**
     * Pricing currently not supported for S3
     * @return a empty map
     */
    @Override
    public Map<String, Double> getPricing() {
        return Collections.emptyMap();
    }

    @Override
    public Map<String, String> getTags() {
        AmazonS3Client client = new AmazonS3Client(getCredentials().getCredentials());
        BucketTaggingConfiguration taggingConfiguration = client.getBucketTaggingConfiguration(thisService.getName());
        if(taggingConfiguration != null) {
            TagSet tagSet = taggingConfiguration.getTagSet();
            if (tagSet != null) {
                return tagSet.getAllTags();
            }
        }
        //If all else fails
        return Collections.emptyMap();
    }

    @Override
    public String getTagsString() {
        return tagString;
    }

    public int getNumberOfObjects() {
        return numberOfObjects;
    }

    public void setNumberOfObjects(int numberOfObjects) {
        this.numberOfObjects = numberOfObjects;
    }
}
