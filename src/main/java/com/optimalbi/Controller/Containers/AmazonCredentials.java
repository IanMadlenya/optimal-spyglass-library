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

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;

/**
 * Created by Timothy Gray(timg) on 28/11/2014.
 * Version: 1.0.0
 */
public class AmazonCredentials {
    private final String accountName;
    private final String accessKey;
    private final String secretKey;
    private final AWSCredentials credentials;

    /**
     * This it the credentials that is used to authenticate with the AWS cloud
     * @param accountName The name that describes this account
     * @param accessKey The access key that is provided by the AWS IAM console
     * @param secretKey The secret key that is provided by the AWS IAM console
     */
    public AmazonCredentials(String accountName, String accessKey, String secretKey){
        this.accountName = accountName;
        this.accessKey = accessKey;
        this.secretKey = secretKey;
        this.credentials = new BasicAWSCredentials(accessKey,secretKey);
    }

    /**
     * The name that is used to describe this account to the user
     * @return The name of this account
     */
    public String getAccountName(){return accountName;}

    /**
     * This access key that is used for authentication
     * @return The unencrypted access key
     */
    public String getAccessKey(){return accessKey;}

    /**
     * The secret key that is used for authentication
     * @return The unencrypted access key
     */
    public String getSecretKey(){return secretKey;}

    /**
     * The AWS credentials object as provided by the AWS Java API.
     * @return The AWS credentials object as provided by the AWS Java API.
     */
    public AWSCredentials getCredentials(){return credentials;}

}
