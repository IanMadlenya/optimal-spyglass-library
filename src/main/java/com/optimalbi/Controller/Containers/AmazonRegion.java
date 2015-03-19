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

package com.optimalbi.Controller.Containers;

import com.amazonaws.regions.Region;

/**
 * Created by Timothy Gray(timg) on 2/12/2014..
 * Version: 1.0.0
 */
public class AmazonRegion {
    private final Region region;
    private boolean active;

    /**
     * A amazon region that is or is not flagged as one we are interested at this moment
     * @param region The non-null AWS region that is described by this object
     * @param active The non-null flag that describes if we are interested in this region at this point in time
     */
    public AmazonRegion(Region region, boolean active){
        this.region = region;
        this.active = active;
    }

    /**
     * The AWS region
     * @return The AWS region from this object
     */
    public Region getRegion(){return region;}

    /**
     * The active flag
     * @return A boolean that is true if and only if we are interested in this region.
     */
    public boolean getActive(){return active;}

    /**
     * Changes the active flag to the opposite
     */
    public void toggleActive(){
        active = !active;
    }
}
