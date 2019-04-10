package org.talend.components.couchbase.output;

import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.CouchbaseCluster;
import com.couchbase.client.java.document.JsonDocument;
import com.couchbase.client.java.document.json.JsonArray;
import com.couchbase.client.java.document.json.JsonObject;
import com.couchbase.client.java.env.CouchbaseEnvironment;
import com.couchbase.client.java.env.DefaultCouchbaseEnvironment;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.components.couchbase.service.CouchbaseService;
import org.talend.sdk.component.api.component.Icon;
import org.talend.sdk.component.api.component.Version;
import org.talend.sdk.component.api.configuration.Option;
import org.talend.sdk.component.api.meta.Documentation;
import org.talend.sdk.component.api.processor.*;
import org.talend.sdk.component.api.record.Record;
import org.talend.sdk.component.api.record.Schema;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.Base64;
import java.util.List;

@Version(1)
@Slf4j
@Icon(Icon.IconType.STAR)
@Processor(name = "Output")
@Documentation("This component writes data to Couchbase")
public class CouchbaseOutput implements Serializable {

    private transient static final Logger LOG = LoggerFactory.getLogger(CouchbaseOutput.class);

    private final CouchbaseOutputConfiguration configuration;

    private final CouchbaseService service;

    private Cluster cluster;

    private Bucket bucket;

    private String idFieldName;

    public CouchbaseOutput(@Option("configuration") final CouchbaseOutputConfiguration configuration,
            final CouchbaseService service) {
        this.configuration = configuration;
        this.service = service;
    }

    @PostConstruct
    public void init() {
        String bootstrapNodes = configuration.getDataSet().getDatastore().getBootstrapNodes();
        String bucketName = configuration.getDataSet().getDatastore().getBucket();
        String password = configuration.getDataSet().getDatastore().getPassword();
        idFieldName = configuration.getIdFieldName();

        CouchbaseEnvironment environment = new DefaultCouchbaseEnvironment.Builder().connectTimeout(20000L).build();
        this.cluster = CouchbaseCluster.create(environment, bootstrapNodes);
        bucket = cluster.openBucket(bucketName, password);
    }

    @ElementListener
    public void onNext(@Input final Record defaultInput) {
        bucket.upsert(toJsonDocument(idFieldName, defaultInput));
    }

    @PreDestroy
    public void release() {
        bucket.close();
        cluster.disconnect();
    }

    private JsonDocument toJsonDocument(String idFieldName, Record record) {
        List<Schema.Entry> entries = record.getSchema().getEntries();
        JsonObject jsonObject = JsonObject.create();
        for (Schema.Entry entry : entries) {
            String entryName = entry.getName();

            Object value = null;

            switch (entry.getType()) {
            case INT:
                value = record.getInt(entryName);
                break;
            case LONG:
                value = record.getLong(entryName);
                break;
            case BYTES:
                value = record.getBytes(entryName);
                break;
            case FLOAT:
                value = record.getFloat(entryName);
                break;
            case DOUBLE:
                value = record.getDouble(entryName);
                break;
            case STRING:
                value = record.getString(entryName);
                break;
            case BOOLEAN:
                value = record.getBoolean(entryName);
                break;
            case ARRAY:
                value = record.getArray(List.class, entryName);
                break;
            case DATETIME:
                value = record.getDateTime(entryName);
                break;
            case RECORD:
                value = record.getRecord(entryName);
                break;
            default:
                value = record.get(Object.class, entryName);
                throw new IllegalArgumentException("Unknown Type " + entry.getType());
            }

            if (entryName.equals(idFieldName)) {
                value = String.valueOf(value);
            }
            if (value instanceof byte[]) {
                jsonObject.put(entryName, new String((byte[]) value));
                // TODO: decide what to do with byte array
            } else if (value instanceof Float) {
                jsonObject.put(entryName, Double.parseDouble(value.toString()));
            } else if (value instanceof ZonedDateTime) {
                jsonObject.put(entryName, value.toString());
            } else if (value instanceof List) {
                JsonArray jsonArray = JsonArray.from((List<?>) value);
                jsonObject.put(entryName, jsonArray);
            } else {
                jsonObject.put(entryName, value);
            }
        }
        return JsonDocument.create(String.valueOf(jsonObject.get(idFieldName)), jsonObject);
    }
}