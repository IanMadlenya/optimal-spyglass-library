package com.optimalbi;

import org.timothygray.SimpleLog.Logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.Double;
import java.lang.String;
import java.util.HashMap;
import java.util.Map;
import com.amazonaws.regions.Region;


/**
 * A class for taking provided pricing CSV and constructing a map of those prices
 * CSV have the columns: Service.Name,Service.Size,Cost.Per.Hour
 */
public class ServicePricing {
    private final File pricing;
    private final Logger logger;
    private final Region pricingRegion;
    private Map<String, Map<String,Double>> pricingMap = new HashMap<>();

    /**
     * ServicePricing takes a .cvs file in the format "Service.Name,Service.Size,Cost.Per.Hour" i.e. "EC2,t1.micro,0.022"
     * and constructs a map of the costs to that services
     * @param pricing The csv file that contains the pricing information
     * @param logger The SimpleLogger that will be used for reporting
     * @param pricingRegion The region that this pricing information pertains to
     */
    public ServicePricing(File pricing, Logger logger, Region pricingRegion){
        this.pricing = pricing;
        this.logger = logger;
        this.pricingRegion = pricingRegion;
        readPricing();
    }

    @SuppressWarnings("ConstantConditions")
    private void readPricing() {
        pricingMap = new HashMap<>();
        pricingMap.put("EC2",new HashMap<>());
        pricingMap.put("RDS",new HashMap<>());
        pricingMap.put("Redshift",new HashMap<>());

        int serviceNameIndex = -1;
        int serviceSizeIndex = -1;
        int costPerHourIndex = -1;

        BufferedReader fileReader = null;

        if (!pricing.exists()) {
            logger.warn("Pricing file does not exist");
            return;
        }

        try {
            fileReader = new BufferedReader(new FileReader(pricing));

            String headerLine = fileReader.readLine();
            String[] headers = headerLine.split(",");

            //Service.Name,Service.Size,Cost.Per.Hour
            for(int i = 0;i<headers.length;i++){
                switch (headers[i]){
                    case "Service.Name" : serviceNameIndex = i; break;
                    case "Service.Size" : serviceSizeIndex = i; break;
                    case "Cost.Per.Hour" : costPerHourIndex = i; break;
                }
            }

            String line = fileReader.readLine();

            while (line != null) {
                String[] split = line.split(",");
                switch (split[serviceNameIndex]){
                    case "EC2" :
                        pricingMap.get("EC2").put(split[serviceSizeIndex],Double.valueOf(split[costPerHourIndex]));
                        break;
                    case "RDS" :
                        pricingMap.get("RDS").put(split[serviceSizeIndex],Double.valueOf(split[costPerHourIndex]));
                        break;
                    case "Redshift" :
                        pricingMap.get("Redshift").put(split[serviceSizeIndex],Double.valueOf(split[costPerHourIndex]));
                        break;
                    default :
                        logger.warn("Cannot find service for: " + split[serviceNameIndex]);
                        break;
                }
                line = fileReader.readLine();
            }

        } catch (IOException e) {
            logger.error("Failed to read " + pricing.getName() +" properly");
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

    /**
     * Returns a map of EC2 size to cost
     * @return A Map that is of EC2 size to cost
     */
    public Map<String,Double> getEc2Pricing(){
        return pricingMap.get("EC2");
    }

    /**
     * Returns a map of RDS size to cost
     * @return A Map that is of RDS size to cost
     */
    public Map<String,Double> getRDSPricing(){
        return pricingMap.get("RDS");
    }

    /**
     * Returns a map of Redshift size to cost
     * @return A Map that is of Redshift size to cost
     */
    public Map<String,Double> getRedshiftPricing(){
        return pricingMap.get("Redshift");
    }

    /**
     * Returns a map of Service to Service Cost map.
     * @return A map is of the map's to the costs of that service
     */
    public Map<String,Map<String,Double>> getAllPricing(){
        return pricingMap;
    }

    /**
     * The region this pricing is described for
     * @return The AWS region that this pricing is for
     */
    public Region getPricingRegion(){
        return pricingRegion;
    }
}
