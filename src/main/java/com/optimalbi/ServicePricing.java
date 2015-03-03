package com.optimalbi;

import org.timothygray.SimpleLog.Logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.Double;import java.lang.String;import java.util.HashMap;
import java.util.Map;

/**
 * A class for taking provided pricing CSV and constructing a map of those prices
 * CSV should be in the format: Service.Name,Service.Size,Cost.Per.Hour
 * i.e. "EC2,t1.micro,0.027"
 */
public class ServicePricing {
    private final File pricing;
    private final Logger logger;
    private Map<String, Map<String,Double>> pricingMap = new HashMap<>();
    public ServicePricing(File pricing, Logger logger){
        this.pricing = pricing;
        this.logger = logger;
        readPricing();
    }

    private void readPricing() {
        pricingMap = new HashMap<>();
        pricingMap.put("EC2",new HashMap<>());
        pricingMap.put("RDS",new HashMap<>());
        pricingMap.put("Redshift",new HashMap<>());

        BufferedReader fileReader = null;

        if (!pricing.exists()) {
            logger.warn("Pricing file does not exist");
            return;
        }

        try {
            fileReader = new BufferedReader(new FileReader(pricing));

            String headerLine = fileReader.readLine();
            String[] headers = headerLine.split(",");

            String line = fileReader.readLine();

            while (line != null) {
                String[] split = line.split(",");
                switch (split[0]){
                    case "EC2" :
                        pricingMap.get("EC2").put(split[1],Double.valueOf(split[2]));
                        break;
                    case "RDS" :
                        pricingMap.get("RDS").put(split[1],Double.valueOf(split[2]));
                        break;
                    case "Redshift" :
                        pricingMap.get("Redshift").put(split[1],Double.valueOf(split[2]));
                        break;
                    default :
                        logger.warn("Cannot find service for: " + split[0]);
                        break;
                }
                line = fileReader.readLine();
            }

        } catch (IOException e) {
            logger.error("Failed to read " + pricing.getName() +" properly");
            return;
        } finally {
            if (fileReader != null) {
                try {
                    fileReader.close();
                } catch (IOException e) {
                    logger.error("Failed to close fileReader");
                }
            }
        }
    }

    public Map<String,Double> getEc2Pricing(){
        return pricingMap.get("EC2");
    }

    public Map<String,Double> getRDSPricing(){
        return pricingMap.get("RDS");
    }

    public Map<String,Double> getRedshiftPricing(){
        return pricingMap.get("Redshift");
    }

    public Map<String,Map<String,Double>> getAllPricing(){
        return pricingMap;
    }
}