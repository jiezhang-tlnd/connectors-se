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
package org.talend.components.workday.dataset;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.talend.components.workday.datastore.WorkdayDataStore;
import org.talend.components.workday.datastore.WorkdayDataStore.AuthenticationType;
import org.talend.sdk.component.api.configuration.Option;
import org.talend.sdk.component.api.configuration.ui.layout.GridLayout;
import org.talend.sdk.component.api.meta.Documentation;

import lombok.Data;

@Data
@GridLayout(@GridLayout.Row({ "user", "report" }))
public class RAASLayout implements Serializable, QueryHelper {

    private static final long serialVersionUID = 4748412283124079266L;

    private static final Map<String, String> jsonParam = new HashMap<>(1);

    static {
        RAASLayout.jsonParam.put("format", "json");
    }

    @Option
    @Documentation("The user who made the report")
    private String user;

    @Option
    @Documentation("report name")
    private String report;

    @Override
    public String getServiceToCall(WorkdayDataStore ds) {
        final String workdayReportName = this.report.replace(' ', '_'); // workday translate space to underscore.
        final String sp = this.getServicePattern(ds.getAuthentication());
        return String.format(sp, this.encodeString(this.user), this.encodeString(workdayReportName));
    }

    @Override
    public Map<String, String> extractQueryParam(WorkdayDataStore ds) {
        if (ds.getAuthentication() == AuthenticationType.LOGIN) {
            return RAASLayout.jsonParam;
        }
        return Collections.emptyMap();
    }

    private String getServicePattern(WorkdayDataStore.AuthenticationType authType) {
        if (authType == WorkdayDataStore.AuthenticationType.LOGIN) {
            return "%s/%s";
        }
        return "raas/%s/%s";
    }
}
