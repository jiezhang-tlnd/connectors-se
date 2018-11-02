package org.talend.components.azure.service;

import java.io.IOException;
import java.net.URISyntaxException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.components.azure.common.AzureConnection;
import org.talend.components.azure.common.Protocol;
import org.talend.sdk.component.api.service.Service;

import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.StorageCredentials;
import com.microsoft.azure.storage.StorageCredentialsAccountAndKey;
import com.microsoft.azure.storage.StorageCredentialsSharedAccessSignature;
import com.microsoft.azure.storage.StorageErrorCodeStrings;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.table.CloudTable;
import com.microsoft.azure.storage.table.DynamicTableEntity;
import com.microsoft.azure.storage.table.TableQuery;
import com.microsoft.azure.storage.table.TableServiceException;

@Service
public class AzureConnectionService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AzureConnectionService.class);

    public Iterable<DynamicTableEntity> executeQuery(CloudStorageAccount storageAccount, String tableName,
            TableQuery<DynamicTableEntity> partitionQuery) throws URISyntaxException, StorageException {
        LOGGER.debug("Executing query for table {} with filter: {}", tableName, partitionQuery.getFilterString());
        CloudTable cloudTable = createTableClient(storageAccount, tableName);
        return cloudTable.execute(partitionQuery, null, AzureTableUtils.getTalendOperationContext());
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

    public void createTable(CloudStorageAccount connection, String tableName) throws StorageException, URISyntaxException {
        CloudTable cloudTable = createTableClient(connection, tableName);
        cloudTable.create(null, AzureTableUtils.getTalendOperationContext());
    }

    public void createTableIfNotExists(CloudStorageAccount connection, String tableName)
            throws StorageException, URISyntaxException {
        CloudTable cloudTable = createTableClient(connection, tableName);
        cloudTable.createIfNotExists(null, AzureTableUtils.getTalendOperationContext());
    }

    public void deleteTableAndCreate(CloudStorageAccount connection, String tableName)
            throws URISyntaxException, StorageException, IOException {
        CloudTable cloudTable = createTableClient(connection, tableName);
        cloudTable.delete(null, AzureTableUtils.getTalendOperationContext());
        createTableAfterDeletion(cloudTable);
    }

    /**
     * This method create a table after it's deletion.<br/>
     * the table deletion take about 40 seconds to be effective on azure CF.
     * https://docs.microsoft.com/en-us/rest/api/storageservices/fileservices/Delete-Table#Remarks <br/>
     * So we try to wait 50 seconds if the first table creation return an
     * {@link StorageErrorCodeStrings.TABLE_BEING_DELETED } exception code
     *
     * @param cloudTable
     * @throws StorageException
     * @throws IOException
     */
    private void createTableAfterDeletion(CloudTable cloudTable) throws StorageException, IOException {
        try {
            cloudTable.create(null, AzureTableUtils.getTalendOperationContext());
        } catch (TableServiceException e) {
            if (!e.getErrorCode().equals(StorageErrorCodeStrings.TABLE_BEING_DELETED)) {
                throw e;
            }
            LOGGER.warn("Table '{}' is currently being deleted. We'll retry in a few moments...", cloudTable.getName());
            // wait 50 seconds (min is 40s) before retrying.
            // See https://docs.microsoft.com/en-us/rest/api/storageservices/fileservices/Delete-Table#Remarks
            try {
                Thread.sleep(50000);
            } catch (InterruptedException eint) {
                throw new IOException("Wait process for recreating table interrupted.");
            }
            cloudTable.create(null, AzureTableUtils.getTalendOperationContext());
            LOGGER.debug("Table {} created.", cloudTable.getName());
        }
    }

    public void deleteTableIfExists(CloudStorageAccount connection, String tableName)
            throws URISyntaxException, StorageException, IOException {
        CloudTable cloudTable = createTableClient(connection, tableName);
        cloudTable.deleteIfExists(null, AzureTableUtils.getTalendOperationContext());
        createTableAfterDeletion(cloudTable);
    }

    private CloudTable createTableClient(CloudStorageAccount connection, String tableName)
            throws URISyntaxException, StorageException {
        return connection.createCloudTableClient().getTableReference(tableName);
    }
}