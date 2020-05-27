/*
 * Copyright (C) 2006-2020 Talend Inc. - www.talend.com
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
package org.talend.components.workday.datastore;

import java.io.Serializable;

import org.talend.components.workday.service.UIActionService;
import org.talend.sdk.component.api.component.Version;
import org.talend.sdk.component.api.configuration.Option;
import org.talend.sdk.component.api.configuration.action.Checkable;
import org.talend.sdk.component.api.configuration.condition.ActiveIf;
import org.talend.sdk.component.api.configuration.constraint.Required;
import org.talend.sdk.component.api.configuration.type.DataStore;
import org.talend.sdk.component.api.configuration.ui.DefaultValue;
import org.talend.sdk.component.api.configuration.ui.layout.GridLayout;
import org.talend.sdk.component.api.meta.Documentation;

import lombok.Data;

@Data
@Version(value = 2, migrationHandler = Migration.class)
@DataStore("WorkdayDataStore")
@GridLayout({ //
        @GridLayout.Row({ "authentication" }), //
        @GridLayout.Row({ "clientIdForm", "loginForm" }) //
})
@GridLayout(names = GridLayout.FormType.ADVANCED, //
        value = { @GridLayout.Row({ "clientIdForm", "loginForm" }) })
@Checkable(UIActionService.HEALTH_CHECK)
@Documentation("DataStore for workday connector")
public class WorkdayDataStore implements Serializable {

    private static final long serialVersionUID = -8628647674176772061L;

    public enum AuthenticationType {
        CLIENT_ID,
        LOGIN;
    }

    @Option
    @Documentation("Authentication mode (Login only for RAAS)")
    @DefaultValue("CLIENT_ID")
    @Required
    private AuthenticationType authentication = AuthenticationType.CLIENT_ID;

    @Option
    @Documentation("Workday Client Id")
    @ActiveIf(target = "authentication", value = "CLIENT_ID")
    private ClientIdForm clientIdForm = new ClientIdForm();

    @Option
    @Documentation("Workday Login connection")
    @ActiveIf(target = "authentication", value = "LOGIN")
    private UserFormForReport loginForm = new UserFormForReport();

    public String getEndPoint() {
        if (this.authentication == AuthenticationType.CLIENT_ID) {
            return this.getClientIdForm().getEndpoint();
        } else {
            return this.getLoginForm().getRealEndpoint();
        }
    }
}
