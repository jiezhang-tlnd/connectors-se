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
package org.talend.components.activemq.service;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.talend.components.activemq.ActiveMQTestExtention;
import org.talend.components.activemq.datastore.ActiveMQDataStore;
import org.talend.sdk.component.api.service.Service;
import org.talend.sdk.component.api.service.healthcheck.HealthCheckStatus;
import org.talend.sdk.component.junit5.WithComponents;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.talend.components.activemq.testutils.ActiveMQTestConstants.*;

@WithComponents("org.talend.components.activemq")
@ExtendWith(ActiveMQTestExtention.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ActionServiceTestIT {

    @Service
    private ActionService actionService;

    private ActiveMQTestExtention.TestContext testContext;

    @BeforeAll
    private void init(ActiveMQTestExtention.TestContext testContext) {
        this.testContext = testContext;
    }

    @Test
    public void testJMSSuccessfulConnection() {
        HealthCheckStatus status = actionService.validateBasicDatastore(testContext.getDataStore());

        assertEquals(HealthCheckStatus.Status.OK, status.getStatus());
    }

    @Test
    public void testJMSNotSuccessfulConnection() {
        ActiveMQDataStore dataStore = new ActiveMQDataStore();
        dataStore.setHost(WRONG_HOST);
        dataStore.setPort(WRONG_PORT);
        dataStore.setSSL(true);
        HealthCheckStatus status = actionService.validateBasicDatastore(dataStore);

        assertEquals(HealthCheckStatus.Status.KO, status.getStatus());
    }
}
