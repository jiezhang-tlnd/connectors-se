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
package org.talend.components.recordtester.service;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.talend.components.recordtester.conf.CodingConfig;
import org.talend.sdk.component.api.service.Service;
import org.talend.sdk.component.api.service.completion.SuggestionValues;
import org.talend.sdk.component.junit5.WithComponents;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@WithComponents(value = "org.talend.components.recordtester")
class GenericServiceTest {

    @Service
    GenericService service;

    @Test
    void getListNames() {
        final CodingConfig codingConfig = new CodingConfig();
        final SuggestionValues listFiles = service.getListFiles();
        assertEquals(1, listFiles.getItems().size());
    }
}