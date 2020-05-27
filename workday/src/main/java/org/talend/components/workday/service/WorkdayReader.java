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
package org.talend.components.workday.service;

import java.util.Map;

import javax.json.JsonObject;

import org.talend.sdk.component.api.service.http.Header;
import org.talend.sdk.component.api.service.http.HttpClient;
import org.talend.sdk.component.api.service.http.Path;
import org.talend.sdk.component.api.service.http.QueryParams;
import org.talend.sdk.component.api.service.http.Request;
import org.talend.sdk.component.api.service.http.Response;

/**
 * Service data getter from workday.
 */
public interface WorkdayReader extends HttpClient {

    /**
     * search for workday data.
     *
     * @param token : authorize token.
     * @param servicePath: final path for workday REST service (common/v1/workers ...).
     * @param othersParam: other query REST parameters
     * @return response of workday call
     */
    @Request(path = "/{service}", method = "GET")
    Response<JsonObject> search(@Header("Authorization") String token, @Path("service") String servicePath,
            @QueryParams(encode = false) Map<String, String> othersParam);
}
