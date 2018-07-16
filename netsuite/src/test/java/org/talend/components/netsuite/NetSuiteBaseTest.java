package org.talend.components.netsuite;

import org.junit.ClassRule;
import org.junit.jupiter.api.BeforeAll;
import org.talend.components.netsuite.dataset.NetSuiteDataSet;
import org.talend.components.netsuite.datastore.NetSuiteDataStore;
import org.talend.components.netsuite.datastore.NetSuiteDataStore.LoginType;
import org.talend.components.netsuite.service.Messages;
import org.talend.components.netsuite.service.NetSuiteService;
import org.talend.sdk.component.api.service.record.RecordBuilderFactory;
import org.talend.sdk.component.junit.SimpleComponentRule;
import org.talend.sdk.component.maven.MavenDecrypter;
import org.talend.sdk.component.maven.Server;

public abstract class NetSuiteBaseTest {

    protected static NetSuiteDataStore dataStore;

    protected NetSuiteDataSet dataSet;

    protected static NetSuiteService service;

    protected static Messages messages;

    protected static RecordBuilderFactory factory;

    @ClassRule
    public static final SimpleComponentRule COMPONENT = new SimpleComponentRule("org.talend.components.netsuite");

    @BeforeAll
    public static void setupOnce() {
        final MavenDecrypter decrypter = new MavenDecrypter();
        Server consumer = decrypter.find("netsuite.consumer");
        Server token = decrypter.find("netsuite.token");
        dataStore = new NetSuiteDataStore();
        dataStore.setEnableCustomization(false);
        dataStore.setAccount(System.getProperty("netsuite.account"));
        dataStore.setEndpoint(System.getProperty("netsuite.endpoint.url"));
        dataStore.setLoginType(LoginType.TBA);
        dataStore.setConsumerKey(consumer.getUsername());
        dataStore.setConsumerSecret(consumer.getPassword());
        dataStore.setTokenId(token.getUsername());
        dataStore.setTokenSecret(token.getPassword());
        service = COMPONENT.findService(NetSuiteService.class);
        messages = COMPONENT.findService(Messages.class);
        factory = COMPONENT.findService(RecordBuilderFactory.class);
    }

}