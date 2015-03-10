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

import com.amazonaws.AmazonClientException;
import com.amazonaws.regions.Region;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.model.*;
import com.optimalbi.Controller.Containers.AmazonCredentials;
import org.timothygray.SimpleLog.*;
import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

import java.util.*;

/**
 * Created by Timothy Gray on 13/11/2014.
 * Version: 0.0.2
 */
public class LocalEc2Service extends AmazonService implements Comparable<Service> {
    private Instance thisInstance;
    private final AmazonEC2 amazonEC2;
    private VBox drawing = null;
    private final Region region;
    private Map<String, Double> pricing = null;

    //Global Main Components
    private Label instanceState;
    private Timer timer = new Timer();

    public LocalEc2Service(String id, AmazonCredentials credentials, Region region, AmazonEC2 amazonEC2, Logger logger) {
        //TODO: Figure out if part of auto-scaling group

        super(id, credentials, logger);
        this.amazonEC2 = amazonEC2;
        this.region = region;
        refreshInstance();
        addShutdownHook();
    }

    public void refreshInstance() {
        DescribeInstancesResult describeInstancesResult = amazonEC2.describeInstances(new DescribeInstancesRequest().withInstanceIds(this.serviceID()));
        List<Reservation> reservations = describeInstancesResult.getReservations();
        List<Instance> inst = new ArrayList<>();

        for (Reservation reservation : reservations) {
            inst.addAll(reservation.getInstances());
        }
        if (inst.size() > 1) {
            logger.error("Error in drawing instance " + this.serviceID());
            throw new AmazonClientException(this.serviceID() + " failed to draw");
        }
        thisInstance = inst.get(0);
    }

    public String serviceState() {
        return thisInstance.getState().getName();
    }

    public String serviceType() {
        return "ec2";
    }

    public String serviceName() {
        List<Tag> tags = thisInstance.getTags();

        String testString = "";
        for (Tag t : tags) {
            if (t.getKey().equals("Name")) {
                testString = t.getValue();
            }
        }
        return stringCap(testString);
    }

    public String serviceSize() {
        return thisInstance.getInstanceType();
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

    public void startService() {
        logger.info("Instance \"Started\"");
        List<String> instanceId = new ArrayList<>();
        instanceId.add(this.serviceID());
        amazonEC2.startInstances(new StartInstancesRequest(instanceId));
        waitForStateChange("running");
    }

    public void stopService() {
        logger.info("Instance \"Stopped\"");
        List<String> instanceId = new ArrayList<>();
        instanceId.add(this.serviceID());
        amazonEC2.stopInstances(new StopInstancesRequest(instanceId));
        waitForStateChange("stopped");
    }

    private void waitForStateChange(String oldState) {
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                logger.debug("Running state change waiter for " + serviceID());
                int timeout = 0;
                String intermediateState = "";
                while (!(thisInstance.getState().getName().equals(oldState))) {
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        logger.error("Failed to sleep thread in " + thisInstance.getInstanceId());
                    }
                    refreshInstance();
                    if ((!intermediateState.equals(thisInstance.getState().getName()))) {
                        intermediateState = thisInstance.getState().getName();
                        Platform.runLater(() -> {
                            instanceState.setText(serviceState());
                            if (instanceState.getText().equalsIgnoreCase("stopped")) {
                                instanceState.setTextFill(Color.RED);
                            } else if (instanceState.getText().equalsIgnoreCase("running")) {
                                instanceState.setTextFill(Color.GREEN);
                            } else {
                                instanceState.setTextFill(Color.ORANGERED);
                            }
                        });
                    }
                    timeout++;
                    logger.debug("Waiting for: " + thisInstance.getState().getName() + "(" + intermediateState + ")" + " to become: " + oldState);
                    if (timeout > 100) break; //Timeout condition
                }
            }
        };
        timer.schedule(task, 2);
    }

    public void attachPricing(Map<String, Double> pricing) {
        this.pricing = pricing;
    }

    public Map<String, Double> getPricing(){
        return pricing;
    }

    private void addShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            timer.cancel();
            timer.purge();
            timer = null;
        }));
    }
}
