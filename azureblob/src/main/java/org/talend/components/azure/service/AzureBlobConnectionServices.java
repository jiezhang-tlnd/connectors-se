/*
 * Copyright (C) 2006-2019 Talend Inc. - www.talend.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

package org.talend.components.azure.service;

import java.net.URISyntaxException;
import java.util.HashMap;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.components.azure.common.Protocol;
import org.talend.components.azure.datastore.AzureConnection;
import org.talend.sdk.component.api.service.Service;

import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.OperationContext;
import com.microsoft.azure.storage.RetryExponentialRetry;
import com.microsoft.azure.storage.RetryPolicy;
import com.microsoft.azure.storage.StorageCredentials;
import com.microsoft.azure.storage.StorageCredentialsAccountAndKey;
import com.microsoft.azure.storage.StorageCredentialsSharedAccessSignature;
import com.microsoft.azure.storage.blob.CloudBlobClient;

@Service
public class AzureBlobConnectionServices {

    private static final Logger LOGGER = LoggerFactory.getLogger(AzureBlobConnectionServices.class);

    public static final RetryPolicy DEFAULT_RETRY_POLICY = new RetryExponentialRetry(10, 3);

    static final String USER_AGENT_KEY = "User-Agent";

    /**
     * Would be set as User-agent when real user-agent creation would fail
     */
    private static final String USER_AGENT_FORMAT = "APN/1.0 Talend/%s TaCoKit/%s";

    private static final String UNKNOWN_VERSION = "UNKNOWN";

    private static String applicationVersion = UNKNOWN_VERSION;

    private static String componentVersion = UNKNOWN_VERSION;

    private static OperationContext talendOperationContext;

    public static OperationContext getTalendOperationContext() {
        if (talendOperationContext == null) {
            talendOperationContext = new OperationContext();
            HashMap<String, String> talendUserHeaders = new HashMap<>();
            talendUserHeaders.put(USER_AGENT_KEY, getUserAgentString());
            talendOperationContext.setUserHeaders(talendUserHeaders);
        }

        return talendOperationContext;
    }

    public static void setApplicationVersion(String applicationVersion) {
        if (StringUtils.isNotEmpty(applicationVersion)) {
            AzureBlobConnectionServices.applicationVersion = applicationVersion;
        }
    }

    public static void setComponentVersion(String componentVersion) {
        if (StringUtils.isNotEmpty(componentVersion)) {
            AzureBlobConnectionServices.componentVersion = componentVersion;
        }
    }

    private static String getUserAgentString() {
        return String.format(USER_AGENT_FORMAT, applicationVersion, componentVersion);
    }

    public CloudStorageAccount createStorageAccount(AzureConnection azureConnection) throws URISyntaxException {
        StorageCredentials credentials = null;
        if (!azureConnection.isUseAzureSharedSignature()) {
            credentials = new StorageCredentialsAccountAndKey(azureConnection.getAccountName(), azureConnection.getAccountKey());
        } else {
            credentials = new StorageCredentialsSharedAccessSignature(azureConnection.getAzureSharedAccessSignature());
        }
        return new CloudStorageAccount(credentials, azureConnection.getProtocol() == Protocol.HTTPS);
    }

    public CloudBlobClient createCloudBlobClient(CloudStorageAccount connection, RetryPolicy retryPolicy) {
        CloudBlobClient tableClient = connection.createCloudBlobClient();
        tableClient.getDefaultRequestOptions().setRetryPolicyFactory(retryPolicy);

        return tableClient;
    }
}