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
package org.talend.components.magentocms.input;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.talend.sdk.component.api.configuration.Option;
import org.talend.sdk.component.api.meta.Documentation;

import java.io.Serializable;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Documentation("Data filter element")
public class SelectionFilter implements Serializable {

    @Option
    @Documentation("The name of field to filter, e.g. 'sku'")
    private String fieldName = "";

    @Option
    @Documentation("The condition to filter, e.g. 'eq' or 'like'")
    private String fieldNameCondition = "";

    @Option
    @Documentation("The value to filter, e.g. 'my_preferable_sku'")
    private String value = "";
}
