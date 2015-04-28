package com.optimalbi.Services;


import com.amazonaws.AmazonClientException;
import com.amazonaws.regions.Region;
import com.amazonaws.services.glacier.AmazonGlacierClient;
import com.amazonaws.services.glacier.model.DescribeVaultOutput;
import com.amazonaws.services.glacier.model.DescribeVaultRequest;
import com.amazonaws.services.glacier.model.DescribeVaultResult;
import com.amazonaws.services.glacier.model.ListVaultsRequest;
import com.optimalbi.Controller.Containers.AmazonCredentials;
import com.optimalbi.SimpleLog.Logger;

import java.util.Collections;
import java.util.Map;

/**
 * Created by Timothy Gray on 28/04/2015.
 */
public class LocalGlacierService extends AmazonService {

    private DescribeVaultResult thisVault;
    private Region region;

    public LocalGlacierService(String id, AmazonCredentials credentials, Region region, DescribeVaultResult thisVault,Logger logger){
        super(id,credentials,logger);
        this.thisVault = thisVault;
        this.region = region;
    }

    @Override
    public void refreshInstance() {
        AmazonGlacierClient temp = new AmazonGlacierClient(getCredentials().getCredentials());
        temp.setRegion(region);
        DescribeVaultResult result = temp.describeVault(new DescribeVaultRequest(thisVault.getVaultName()));
        if(result != null){
            thisVault = result;
        } else {
            throw new AmazonClientException("Vault does not exist: " + thisVault.getVaultName());
        }
    }

    @Override
    public String serviceState() {
        return "active";
    }

    @Override
    public String serviceType() {
        return "Glacier";
    }

    @Override
    public String serviceName() {
        return thisVault.getVaultName();
    }

    @Override
    public String serviceSize() {
        double gbSize = thisVault.getSizeInBytes();
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

    public long getNumberOfObjects(){
        return thisVault.getNumberOfArchives();
    }

    @Override
    public String getTagsString() {
        return "";
    }
}
